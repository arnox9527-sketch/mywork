package com.exchange.asset.dto;

import java.time.LocalDateTime;

public class UserAssetResponse {

    private Long id;

    private Long userId;

    private String currencyCode;

    private String walletTypeCode;

    private Long available;

    private Long frozen;

    private LocalDateTime createdTime;

    public UserAssetResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getWalletTypeCode() {
        return walletTypeCode;
    }

    public void setWalletTypeCode(String walletTypeCode) {
        this.walletTypeCode = walletTypeCode;
    }

    public Long getAvailable() {
        return available;
    }

    public void setAvailable(Long available) {
        this.available = available;
    }

    public Long getFrozen() {
        return frozen;
    }

    public void setFrozen(Long frozen) {
        this.frozen = frozen;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
