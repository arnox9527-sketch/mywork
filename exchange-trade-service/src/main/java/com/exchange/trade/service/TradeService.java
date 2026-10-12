package com.exchange.trade.service;

import com.exchange.trade.dto.PlaceOrderRequest;
import com.exchange.trade.dto.PlaceOrderResponse;

public interface TradeService {

    PlaceOrderResponse placeOrder(PlaceOrderRequest request);
}
