package com.exchange.trade.dto;

import lombok.Data;

@Data
public class PlaceOrderRequest {

    private Long userId;

    private String symbol;

    private String productType;

    private String side;

    private String orderType;

    private Long price;

    private Long quantity;
}
