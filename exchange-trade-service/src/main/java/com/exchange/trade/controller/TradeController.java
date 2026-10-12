package com.exchange.trade.controller;

import com.exchange.trade.dto.PlaceOrderRequest;
import com.exchange.trade.dto.PlaceOrderResponse;
import com.exchange.trade.dto.Result;
import com.exchange.trade.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trade")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @PostMapping("/orders")
    public Result<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        try {
            PlaceOrderResponse response = tradeService.placeOrder(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
