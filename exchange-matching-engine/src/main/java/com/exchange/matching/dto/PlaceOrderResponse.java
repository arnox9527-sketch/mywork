package com.exchange.matching.dto;

import com.exchange.matching.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {
    private String orderId;
    private String symbol;
    private OrderStatus status;
    private Long filledQuantity;
    private Long remainingQuantity;
    private List<MatchResultDto> matchResults;
}