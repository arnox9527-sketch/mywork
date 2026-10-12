package com.exchange.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.exchange.asset.converter.AssetConverter;
import com.exchange.asset.dto.FreezeRequest;
import com.exchange.asset.dto.SettleRequest;
import com.exchange.asset.dto.TransferRequest;
import com.exchange.asset.dto.TransferResponse;
import com.exchange.asset.dto.UserAssetResponse;
import com.exchange.asset.entity.AssetTransfer;
import com.exchange.asset.entity.Currency;
import com.exchange.asset.entity.UserAsset;
import com.exchange.asset.entity.WalletType;
import com.exchange.asset.mapper.AssetTransferMapper;
import com.exchange.asset.mapper.CurrencyMapper;
import com.exchange.asset.mapper.UserAssetMapper;
import com.exchange.asset.mapper.WalletTypeMapper;
import com.exchange.asset.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private UserAssetMapper userAssetMapper;

    @Autowired
    private CurrencyMapper currencyMapper;

    @Autowired
    private WalletTypeMapper walletTypeMapper;

    @Autowired
    private AssetTransferMapper assetTransferMapper;

    @Override
    public List<UserAssetResponse> getUserAssets(Long userId) {
        List<UserAsset> assets = userAssetMapper.selectList(new LambdaQueryWrapper<UserAsset>()
                .eq(UserAsset::getUserId, userId));

        List<UserAssetResponse> responses = new ArrayList<>();
        for (UserAsset asset : assets) {
            Currency currency = currencyMapper.selectById(asset.getCurrencyId());
            WalletType walletType = walletTypeMapper.selectById(asset.getWalletTypeId());
            responses.add(AssetConverter.toAssetResponse(asset, currency.getCode(), walletType.getCode()));
        }
        return responses;
    }

    @Override
    public UserAssetResponse getUserAsset(Long userId, String currencyCode, String walletTypeCode) {
        Currency currency = getCurrencyByCode(currencyCode);
        WalletType walletType = getWalletTypeByCode(walletTypeCode);

        UserAsset asset = userAssetMapper.selectOne(new LambdaQueryWrapper<UserAsset>()
                .eq(UserAsset::getUserId, userId)
                .eq(UserAsset::getCurrencyId, currency.getId())
                .eq(UserAsset::getWalletTypeId, walletType.getId()));

        if (asset == null) {
            return null;
        }
        return AssetConverter.toAssetResponse(asset, currency.getCode(), walletType.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferResponse transfer(TransferRequest request) {
        Currency currency = getCurrencyByCode(request.getCurrencyCode());
        WalletType fromWallet = getWalletTypeByCode(request.getFromWalletType());
        WalletType toWallet = getWalletTypeByCode(request.getToWalletType());

        if (fromWallet.getId().equals(toWallet.getId())) {
            throw new IllegalArgumentException("源钱包和目标钱包不能相同");
        }

        UserAsset fromAsset = getOrCreateUserAsset(request.getUserId(), currency.getId(), fromWallet.getId());

        if (fromAsset.getAvailable() < request.getAmount()) {
            throw new IllegalArgumentException("可用余额不足");
        }

        // 扣减源钱包
        int deducted = userAssetMapper.update(null, new LambdaUpdateWrapper<UserAsset>()
                .eq(UserAsset::getId, fromAsset.getId())
                .ge(UserAsset::getAvailable, request.getAmount())
                .setSql("available = available - " + request.getAmount()));

        if (deducted == 0) {
            throw new IllegalArgumentException("扣减余额失败，请重试");
        }

        // 增加目标钱包
        UserAsset toAsset = getOrCreateUserAsset(request.getUserId(), currency.getId(), toWallet.getId());
        userAssetMapper.update(null, new LambdaUpdateWrapper<UserAsset>()
                .eq(UserAsset::getId, toAsset.getId())
                .setSql("available = available + " + request.getAmount()));

        // 记录划转流水
        AssetTransfer transfer = new AssetTransfer();
        transfer.setTransferNo(generateTransferNo());
        transfer.setUserId(request.getUserId());
        transfer.setCurrencyId(currency.getId());
        transfer.setFromWalletType(fromWallet.getId());
        transfer.setToWalletType(toWallet.getId());
        transfer.setAmount(request.getAmount());
        transfer.setStatus(1);
        transfer.setCreatedTime(LocalDateTime.now());
        assetTransferMapper.insert(transfer);

        return AssetConverter.toTransferResponse(transfer, currency.getCode(),
                fromWallet.getCode(), toWallet.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freeze(FreezeRequest request) {
        Currency currency = getCurrencyByCode(request.getCurrencyCode());
        WalletType walletType = getWalletTypeByCode(request.getWalletType());
        UserAsset asset = getOrCreateUserAsset(request.getUserId(), currency.getId(), walletType.getId());

        if (asset.getAvailable() < request.getAmount()) {
            throw new IllegalArgumentException("可用余额不足, 可用: " + asset.getAvailable() + ", 需要: " + request.getAmount());
        }

        int updated = userAssetMapper.update(null, new LambdaUpdateWrapper<UserAsset>()
                .eq(UserAsset::getId, asset.getId())
                .ge(UserAsset::getAvailable, request.getAmount())
                .setSql("available = available - " + request.getAmount())
                .setSql("frozen = frozen + " + request.getAmount()));

        if (updated == 0) {
            throw new IllegalArgumentException("冻结失败，请重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settle(SettleRequest request) {
        Currency freezeCurrency = getCurrencyByCode(request.getFreezeCurrency());
        WalletType freezeWallet = getWalletTypeByCode(request.getFreezeWalletType());
        Currency creditCurrency = getCurrencyByCode(request.getCreditCurrency());
        WalletType creditWallet = getWalletTypeByCode(request.getCreditWalletType());

        UserAsset freezeAsset = getOrCreateUserAsset(request.getUserId(), freezeCurrency.getId(), freezeWallet.getId());
        int unfrozen = userAssetMapper.update(null, new LambdaUpdateWrapper<UserAsset>()
                .eq(UserAsset::getId, freezeAsset.getId())
                .ge(UserAsset::getFrozen, request.getFreezeAmount())
                .setSql("frozen = frozen - " + request.getFreezeAmount()));

        if (unfrozen == 0) {
            throw new IllegalArgumentException("解冻失败");
        }

        UserAsset creditAsset = getOrCreateUserAsset(request.getUserId(), creditCurrency.getId(), creditWallet.getId());
        userAssetMapper.update(null, new LambdaUpdateWrapper<UserAsset>()
                .eq(UserAsset::getId, creditAsset.getId())
                .setSql("available = available + " + request.getCreditAmount()));
    }

    private Currency getCurrencyByCode(String code) {
        Currency currency = currencyMapper.selectOne(new LambdaQueryWrapper<Currency>()
                .eq(Currency::getCode, code));
        if (currency == null) {
            throw new IllegalArgumentException("币种不存在: " + code);
        }
        return currency;
    }

    private WalletType getWalletTypeByCode(String code) {
        WalletType walletType = walletTypeMapper.selectOne(new LambdaQueryWrapper<WalletType>()
                .eq(WalletType::getCode, code));
        if (walletType == null) {
            throw new IllegalArgumentException("钱包类型不存在: " + code);
        }
        return walletType;
    }

    private UserAsset getOrCreateUserAsset(Long userId, Long currencyId, Long walletTypeId) {
        UserAsset asset = userAssetMapper.selectOne(new LambdaQueryWrapper<UserAsset>()
                .eq(UserAsset::getUserId, userId)
                .eq(UserAsset::getCurrencyId, currencyId)
                .eq(UserAsset::getWalletTypeId, walletTypeId));

        if (asset == null) {
            asset = new UserAsset();
            asset.setUserId(userId);
            asset.setCurrencyId(currencyId);
            asset.setWalletTypeId(walletTypeId);
            asset.setAvailable(0L);
            asset.setFrozen(0L);
            asset.setDeleted(0);
            userAssetMapper.insert(asset);
        }
        return asset;
    }

    private String generateTransferNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TF" + timestamp + uuid;
    }
}
