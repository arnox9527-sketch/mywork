package com.exchange.matching.util;

import com.exchange.matching.dto.MatchResultDto;
import com.exchange.matching.dto.PlaceOrderRequest;
import com.exchange.matching.model.MatchResult;
import com.exchange.matching.model.Order;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class OrderConverter {

    /**
     * 将请求转换为 Order 实体
     */
    public static Order toOrder(PlaceOrderRequest request) {
        return Order.builder()
                .orderId(generateOrderId())
                .symbol(request.getSymbol())
                .productType(request.getProductType())
                .side(request.getSide())
                .orderType(request.getOrderType())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .filledQuantity(0L)
                .userId(request.getUserId())
                .build();
    }

    /**
     * 将 MatchResult 转换为 DTO
     */
    public static MatchResultDto toDto(MatchResult result) {
        return MatchResultDto.builder()
                .tradeId(result.getTradeId())
                .symbol(result.getSymbol())
                .price(result.getPrice())
                .quantity(result.getQuantity())
                .buyerOrderId(result.getBuyerOrderId())
                .buyerUserId(result.getBuyerUserId())
                .sellerOrderId(result.getSellerOrderId())
                .sellerUserId(result.getSellerUserId())
                .timestamp(result.getTimestamp())
                .build();
    }

    /**
     * 批量转换 MatchResult 列表
     */
    public static List<MatchResultDto> toDtoList(List<MatchResult> results) {
        return results.stream()
                .map(OrderConverter::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 生成订单ID
     */
    private static String generateOrderId() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }
}