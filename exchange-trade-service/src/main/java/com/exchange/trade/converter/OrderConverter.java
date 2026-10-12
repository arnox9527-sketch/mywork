package com.exchange.trade.converter;

import com.exchange.trade.dto.PlaceOrderRequest;
import com.exchange.trade.dto.PlaceOrderResponse;
import com.exchange.trade.entity.TradeOrder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OrderConverter {

    private OrderConverter() {
    }

    public static TradeOrder toEntity(PlaceOrderRequest request) {
        TradeOrder order = new TradeOrder();
        order.setOrderId(generateOrderId());
        order.setUserId(request.getUserId());
        order.setSymbol(request.getSymbol());
        order.setProductType(request.getProductType());
        order.setSide(request.getSide());
        order.setOrderType(request.getOrderType());
        order.setPrice(request.getPrice());
        order.setQuantity(request.getQuantity());
        order.setFilledQuantity(0L);
        order.setFilledAmount(0L);
        order.setStatus("RECEIVED");
        order.setFrozenAmount(0L);
        order.setCreatedTime(LocalDateTime.now());
        order.setUpdatedTime(LocalDateTime.now());
        return order;
    }

    public static PlaceOrderResponse toResponse(TradeOrder order) {
        PlaceOrderResponse response = new PlaceOrderResponse();
        response.setOrderId(order.getOrderId());
        response.setSymbol(order.getSymbol());
        response.setSide(order.getSide());
        response.setStatus(order.getStatus());
        response.setPrice(order.getPrice());
        response.setQuantity(order.getQuantity());
        response.setFilledQuantity(order.getFilledQuantity());
        response.setCreatedTime(order.getCreatedTime());
        return response;
    }

    private static String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return "ORD" + timestamp + uuid;
    }
}
