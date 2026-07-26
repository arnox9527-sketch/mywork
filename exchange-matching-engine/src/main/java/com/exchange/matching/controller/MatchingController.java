package com.exchange.matching.controller;

import com.exchange.matching.dto.MatchResultDto;
import com.exchange.matching.dto.PlaceOrderRequest;
import com.exchange.matching.dto.PlaceOrderResponse;
import com.exchange.matching.model.MatchResult;
import com.exchange.matching.model.Order;
import com.exchange.matching.service.MqProducerService;
import com.exchange.matching.service.OrderBookManager;
import com.exchange.matching.util.OrderConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/matching")
public class MatchingController {

    @Autowired
    private OrderBookManager orderBookManager;

    @Autowired
    private MqProducerService mqProducerService;

    @PostMapping("/orders")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        log.info("📥 收到下单请求: symbol={}, side={}, type={}, price={}, qty={}",
                request.getSymbol(), request.getSide(), request.getOrderType(),
                request.getPrice(), request.getQuantity());

        try {
            // 1. 参数校验
            if (request.getSymbol() == null || request.getSymbol().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getOrderType() == null) {
                return ResponseEntity.badRequest().build();
            }

            // 2. 转换订单
            Order order = OrderConverter.toOrder(request);

            // 3. 获取或创建订单簿
            var orderBook = orderBookManager.getOrCreateOrderBook(request.getSymbol());

            // 4. 执行撮合
            List<MatchResult> results = orderBook.processOrder(order);

            // 5. 发送成交结果到 RocketMQ
            if (results != null && !results.isEmpty()) {
                for (MatchResult result : results) {
                    MatchResultDto dto = OrderConverter.toDto(result);
                    mqProducerService.sendMatchResult(dto);
                }
            }

            // 6. 构建响应
            PlaceOrderResponse response = PlaceOrderResponse.builder()
                    .orderId(order.getOrderId())
                    .symbol(order.getSymbol())
                    .status(order.getStatus())
                    .filledQuantity(order.getFilledQuantity())
                    .remainingQuantity(order.getRemainingQuantity())
                    .matchResults(OrderConverter.toDtoList(results))
                    .build();

            log.info("📤 订单处理完成: orderId={}, status={}, filled={}, remaining={}",
                    order.getOrderId(), order.getStatus(),
                    order.getFilledQuantity(), order.getRemainingQuantity());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("下单处理异常: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
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