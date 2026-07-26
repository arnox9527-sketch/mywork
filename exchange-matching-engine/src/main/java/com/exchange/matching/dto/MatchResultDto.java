package com.exchange.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultDto {
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