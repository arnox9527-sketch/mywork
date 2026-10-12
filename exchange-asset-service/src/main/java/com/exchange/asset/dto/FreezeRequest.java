package com.exchange.asset.dto;

import lombok.Data;

@Data
public class FreezeRequest {

    private Long userId;

    private String currencyCode;

    private String walletType;

    private Long amount;
}
