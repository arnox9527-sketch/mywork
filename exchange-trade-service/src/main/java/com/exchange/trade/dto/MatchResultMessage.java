package com.exchange.trade.dto;

import lombok.Data;

@Data
public class MatchResultMessage {

    private String tradeId;
    private String symbol;
    private Long price;
    private Long quantity;
    private String buyerOrderId;
    private Long buyerUserId;
    private String sellerOrderId;
    private Long sellerUserId;
    private Long timestamp;
}
