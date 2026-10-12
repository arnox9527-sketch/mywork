package com.exchange.trade.dto;

import lombok.Data;

@Data
public class SettleRequest {

    private Long userId;

    private String freezeCurrency;

    private String freezeWalletType;

    private String creditCurrency;

    private String creditWalletType;

    private Long freezeAmount;

    private Long creditAmount;
}
