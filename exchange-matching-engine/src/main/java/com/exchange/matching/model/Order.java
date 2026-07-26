package com.exchange.matching.model;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderStatus;
import com.exchange.matching.enums.OrderType;
import com.exchange.matching.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String symbol;
    private ProductType productType;
    private OrderSide side;
    private OrderType orderType;
    private Long price;
    @Builder.Default
    private Long quantity = 0L;
    @Builder.Default
    private Long filledQuantity = 0L;
    private Long userId;
    private Long timestamp;
    private OrderStatus status;
    private Long sequence;

    public Long getRemainingQuantity() {
        long q = quantity != null ? quantity : 0L;
        long f = filledQuantity != null ? filledQuantity : 0L;
        return q - f;
    }

    public boolean isFilled() {
        long q = quantity != null ? quantity : 0L;
        long f = filledQuantity != null ? filledQuantity : 0L;
        return f >= q;
    }

    public boolean isActive() {
        return status == OrderStatus.OPEN || status == OrderStatus.PARTIALLY_FILLED;
    }

    public boolean isLimitOrder() {
        return orderType == OrderType.LIMIT;
    }

    public boolean isMarketOrder() {
        return orderType == OrderType.MARKET;
    }
}