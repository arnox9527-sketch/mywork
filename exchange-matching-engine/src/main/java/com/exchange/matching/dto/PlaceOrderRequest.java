package com.exchange.matching.dto;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderType;
import com.exchange.matching.enums.ProductType;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    private String symbol;
    private ProductType productType;
    private OrderSide side;
    private OrderType orderType;
    private Long price;      // 限价单必填，市价单可为 null
    private Long quantity;
    private Long userId;
}