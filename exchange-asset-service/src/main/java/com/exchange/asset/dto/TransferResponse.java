package com.exchange.asset.dto;

import java.time.LocalDateTime;

public class TransferResponse {

    private String transferNo;

    private Long userId;

    private String currencyCode;

    private String fromWalletType;

    private String toWalletType;

    private Long amount;

    private Integer status;

    private LocalDateTime createdTime;

    public TransferResponse() {
    }

    public String getTransferNo() {
        return transferNo;
    }

    public void setTransferNo(String transferNo) {
        this.transferNo = transferNo;
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

    public String getFromWalletType() {
        return fromWalletType;
    }

    public void setFromWalletType(String fromWalletType) {
        this.fromWalletType = fromWalletType;
    }

    public String getToWalletType() {
        return toWalletType;
    }

    public void setToWalletType(String toWalletType) {
        this.toWalletType = toWalletType;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
