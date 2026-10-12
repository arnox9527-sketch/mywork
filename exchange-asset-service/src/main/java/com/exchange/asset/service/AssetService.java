package com.exchange.asset.service;

import com.exchange.asset.dto.FreezeRequest;
import com.exchange.asset.dto.SettleRequest;
import com.exchange.asset.dto.TransferRequest;
import com.exchange.asset.dto.TransferResponse;
import com.exchange.asset.dto.UserAssetResponse;

import java.util.List;

public interface AssetService {

    List<UserAssetResponse> getUserAssets(Long userId);

    UserAssetResponse getUserAsset(Long userId, String currencyCode, String walletTypeCode);

    TransferResponse transfer(TransferRequest request);

    void freeze(FreezeRequest request);

    void settle(SettleRequest request);
}
