package com.exchange.asset.converter;

import com.exchange.asset.dto.TransferResponse;
import com.exchange.asset.dto.UserAssetResponse;
import com.exchange.asset.entity.AssetTransfer;
import com.exchange.asset.entity.UserAsset;

public class AssetConverter {

    private AssetConverter() {
    }

    public static UserAssetResponse toAssetResponse(UserAsset asset, String currencyCode, String walletTypeCode) {
        UserAssetResponse response = new UserAssetResponse();
        response.setId(asset.getId());
        response.setUserId(asset.getUserId());
        response.setCurrencyCode(currencyCode);
        response.setWalletTypeCode(walletTypeCode);
        response.setAvailable(asset.getAvailable());
        response.setFrozen(asset.getFrozen());
        response.setCreatedTime(asset.getCreatedTime());
        return response;
    }

    public static TransferResponse toTransferResponse(AssetTransfer transfer, String currencyCode,
                                                       String fromWalletCode, String toWalletCode) {
        TransferResponse response = new TransferResponse();
        response.setTransferNo(transfer.getTransferNo());
        response.setUserId(transfer.getUserId());
        response.setCurrencyCode(currencyCode);
        response.setFromWalletType(fromWalletCode);
        response.setToWalletType(toWalletCode);
        response.setAmount(transfer.getAmount());
        response.setStatus(transfer.getStatus());
        response.setCreatedTime(transfer.getCreatedTime());
        return response;
    }
}
