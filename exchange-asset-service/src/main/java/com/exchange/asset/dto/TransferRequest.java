package com.exchange.asset.dto;

import lombok.Data;

@Data
public class TransferRequest {

    private Long userId;

    private String currencyCode;

    private String fromWalletType;

    private String toWalletType;

    private Long amount;
}
