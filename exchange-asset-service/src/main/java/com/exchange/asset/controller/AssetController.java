package com.exchange.asset.controller;

import com.exchange.asset.dto.FreezeRequest;
import com.exchange.asset.dto.Result;
import com.exchange.asset.dto.SettleRequest;
import com.exchange.asset.dto.TransferRequest;
import com.exchange.asset.dto.TransferResponse;
import com.exchange.asset.dto.UserAssetResponse;
import com.exchange.asset.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @GetMapping("/users/{userId}")
    public Result<List<UserAssetResponse>> getUserAssets(@PathVariable Long userId) {
        return Result.success(assetService.getUserAssets(userId));
    }

    @GetMapping("/users/{userId}/detail")
    public Result<UserAssetResponse> getUserAsset(
            @PathVariable Long userId,
            @RequestParam String currency,
            @RequestParam String walletType) {
        UserAssetResponse response = assetService.getUserAsset(userId, currency, walletType);
        if (response == null) {
            return Result.fail(404, "资产记录不存在");
        }
        return Result.success(response);
    }

    @PostMapping("/transfer")
    public Result<TransferResponse> transfer(@RequestBody TransferRequest request) {
        try {
            TransferResponse response = assetService.transfer(request);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/freeze")
    public Result<Void> freeze(@RequestBody FreezeRequest request) {
        try {
            assetService.freeze(request);
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/settle")
    public Result<Void> settle(@RequestBody SettleRequest request) {
        try {
            assetService.settle(request);
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }
}
