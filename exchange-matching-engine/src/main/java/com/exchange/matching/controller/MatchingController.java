package com.exchange.matching.controller;

import com.exchange.matching.disruptor.OrderEventProducer;
import com.exchange.matching.dto.PlaceOrderRequest;
import com.exchange.matching.model.Order;
import com.exchange.matching.service.OrderBookManager;
import com.exchange.matching.util.OrderConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/matching")
public class MatchingController {

    @Autowired
    private OrderBookManager orderBookManager;

    @Autowired
    private OrderEventProducer orderEventProducer;

    @PostMapping("/orders")
    public ResponseEntity<String> placeOrder(@RequestBody PlaceOrderRequest request) {
        log.info("收到下单请求: symbol={}, side={}, type={}, price={}, qty={}",
                request.getSymbol(), request.getSide(), request.getOrderType(),
                request.getPrice(), request.getQuantity());

        try {
            if (request.getSymbol() == null || request.getSymbol().isEmpty()) {
                return ResponseEntity.badRequest().body("symbol不能为空");
            }
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body("quantity必须大于0");
            }
            if (request.getOrderType() == null) {
                return ResponseEntity.badRequest().body("orderType不能为空");
            }

            Order order = OrderConverter.toOrder(request);
            orderEventProducer.publish(order);

            log.info("订单已提交到Disruptor: orderId={}", order.getOrderId());
            return ResponseEntity.ok(order.getOrderId());

        } catch (Exception e) {
            log.error("下单处理异常: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("下单失败: " + e.getMessage());
        }
    }

    @GetMapping("/depth/{symbol}")
    public ResponseEntity<?> getDepth(@PathVariable String symbol) {
        var orderBook = orderBookManager.getOrderBook(symbol);
        if (orderBook == null) {
            return ResponseEntity.notFound().build();
        }

        var response = new DepthResponse(
                orderBook.getBestBidPrice(),
                orderBook.getBestAskPrice(),
                orderBook.getBidDepth(),
                orderBook.getAskDepth()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/orders/{symbol}/{orderId}")
    public ResponseEntity<Boolean> cancelOrder(
            @PathVariable String symbol,
            @PathVariable String orderId) {
        var orderBook = orderBookManager.getOrderBook(symbol);
        if (orderBook == null) {
            return ResponseEntity.notFound().build();
        }

        boolean cancelled = orderBook.cancelOrder(orderId);
        return ResponseEntity.ok(cancelled);
    }
}

class DepthResponse {
    public final Long bestBid;
    public final Long bestAsk;
    public final java.util.Map<Long, Long> bids;
    public final java.util.Map<Long, Long> asks;

    public DepthResponse(Long bestBid, Long bestAsk,
                         java.util.Map<Long, Long> bids,
                         java.util.Map<Long, Long> asks) {
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
        this.bids = bids;
        this.asks = asks;
    }
}
