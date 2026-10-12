package com.exchange.trade.dto;

import java.time.LocalDateTime;

public class PlaceOrderResponse {

    private String orderId;

    private String symbol;

    private String side;

    private String status;

    private Long price;

    private Long quantity;

    private Long filledQuantity;

    private LocalDateTime createdTime;

    public PlaceOrderResponse() {
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public Long getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(Long filledQuantity) { this.filledQuantity = filledQuantity; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
