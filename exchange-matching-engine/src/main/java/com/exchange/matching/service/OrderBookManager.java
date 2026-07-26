package com.exchange.matching.service;

import com.exchange.matching.core.OrderBook;
import com.exchange.matching.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单簿管理器
 * 管理所有交易对的 OrderBook 实例
 */
@Slf4j
@Component
public class OrderBookManager {

    /**
     * 交易对 -> OrderBook 实例
     */
    private final Map<String, OrderBook> orderBookMap = new ConcurrentHashMap<>();

    /**
     * 获取或创建指定交易对的 OrderBook
     */
    public OrderBook getOrCreateOrderBook(String symbol) {
        return orderBookMap.computeIfAbsent(symbol, key -> {
            log.info("创建新的订单簿: {}", key);
            return new OrderBook(key);
        });
    }

    /**
     * 获取指定交易对的 OrderBook（如果不存在则返回 null）
     */
    public OrderBook getOrderBook(String symbol) {
        return orderBookMap.get(symbol);
    }

    /**
     * 获取所有交易对
     */
    public Map<String, OrderBook> getAllOrderBooks() {
        return orderBookMap;
    }

    /**
     * 移除指定交易对的 OrderBook
     */
    public void removeOrderBook(String symbol) {
        OrderBook removed = orderBookMap.remove(symbol);
        if (removed != null) {
            log.info("移除订单簿: {}", symbol);
        }
    }
}