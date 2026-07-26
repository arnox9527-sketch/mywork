package com.exchange.matching.core;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderStatus;
import com.exchange.matching.enums.OrderType;
import com.exchange.matching.model.MatchResult;
import com.exchange.matching.model.Order;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class OrderBook {

    private final String symbol;
    private final TreeMap<Long, Deque<Order>> bids;
    private final TreeMap<Long, Deque<Order>> asks;
    private final Map<String, Order> orderMap;
    private final AtomicLong sequenceGenerator;

    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.bids = new TreeMap<>(Comparator.reverseOrder());
        this.asks = new TreeMap<>(Comparator.naturalOrder());
        this.orderMap = new ConcurrentHashMap<>();
        this.sequenceGenerator = new AtomicLong(0);
    }

    public List<MatchResult> processOrder(Order order) {
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            order.setStatus(OrderStatus.REJECTED);
            log.warn("订单数量无效: {}", order.getOrderId());
            return Collections.emptyList();
        }

        order.setSequence(sequenceGenerator.incrementAndGet());
        order.setTimestamp(System.currentTimeMillis());
        order.setFilledQuantity(0L);

        log.debug("📥 processOrder: id={}, side={}, type={}, price={}, quantity={}, filledQty={}",
                order.getOrderId(), order.getSide(), order.getOrderType(),
                order.getPrice(), order.getQuantity(), order.getFilledQuantity());

        List<MatchResult> results;
        switch (order.getOrderType()) {
            case MARKET:
                results = handleMarketOrder(order);
                break;
            case POST_ONLY:
                results = handlePostOnlyOrder(order);
                break;
            case FOK:
                results = handleFokOrder(order);
                break;
            case IOC:
                results = handleIocOrder(order);
                break;
            default:
                results = handleLimitOrder(order);
        }

        log.debug("📤 订单完成: id={}, status={}, filled={}, remaining={}",
                order.getOrderId(), order.getStatus(),
                order.getFilledQuantity(), order.getRemainingQuantity());
        return results;
    }

    // ==================== 订单处理器 ====================
    private List<MatchResult> handleLimitOrder(Order order) {
        List<MatchResult> results = new ArrayList<>();

        while (order.getRemainingQuantity() > 0 && canMatch(order)) {
            Order counter = getBestCounterOrder(order);
            if (counter == null) break;

            long matchQty = Math.min(order.getRemainingQuantity(), counter.getRemainingQuantity());
            log.debug("⚡ 撮合: {} 与 {}，数量 {}", order.getOrderId(), counter.getOrderId(), matchQty);

            results.add(createMatchResult(order, counter, matchQty));

            order.setFilledQuantity(order.getFilledQuantity() + matchQty);
            counter.setFilledQuantity(counter.getFilledQuantity() + matchQty);

            if (counter.isFilled()) {
                counter.setStatus(OrderStatus.FILLED);
                removeFromBook(counter);
            } else {
                counter.setStatus(OrderStatus.PARTIALLY_FILLED);
            }
        }

        if (order.getRemainingQuantity() > 0) {
            if (order.getFilledQuantity() > 0) {
                order.setStatus(OrderStatus.PARTIALLY_FILLED);
            } else {
                order.setStatus(OrderStatus.OPEN);
            }
            addToBook(order);
            log.debug("📌 挂单: id={}, price={}, remaining={}", order.getOrderId(), order.getPrice(), order.getRemainingQuantity());
        } else {
            order.setStatus(OrderStatus.FILLED);
            log.debug("✅ 完全成交: id={}", order.getOrderId());
        }

        return results;
    }

    private List<MatchResult> handleMarketOrder(Order order) {
        List<MatchResult> results = new ArrayList<>();

        while (order.getRemainingQuantity() > 0) {
            Order counter = getBestCounterOrder(order);
            if (counter == null) {
                order.setStatus(OrderStatus.CANCELLED);
                log.warn("市价单 {} 无法完全成交，剩余 {} 已取消", order.getOrderId(), order.getRemainingQuantity());
                break;
            }

            long matchQty = Math.min(order.getRemainingQuantity(), counter.getRemainingQuantity());
            results.add(createMatchResult(order, counter, matchQty));

            order.setFilledQuantity(order.getFilledQuantity() + matchQty);
            counter.setFilledQuantity(counter.getFilledQuantity() + matchQty);

            if (counter.isFilled()) {
                counter.setStatus(OrderStatus.FILLED);
                removeFromBook(counter);
            } else {
                counter.setStatus(OrderStatus.PARTIALLY_FILLED);
            }
        }

        if (order.isFilled()) {
            order.setStatus(OrderStatus.FILLED);
        }
        return results;
    }

    private List<MatchResult> handleIocOrder(Order order) {
        List<MatchResult> results = new ArrayList<>();

        while (order.getRemainingQuantity() > 0 && canMatch(order)) {
            Order counter = getBestCounterOrder(order);
            if (counter == null) break;

            long matchQty = Math.min(order.getRemainingQuantity(), counter.getRemainingQuantity());
            results.add(createMatchResult(order, counter, matchQty));

            order.setFilledQuantity(order.getFilledQuantity() + matchQty);
            counter.setFilledQuantity(counter.getFilledQuantity() + matchQty);

            if (counter.isFilled()) {
                counter.setStatus(OrderStatus.FILLED);
                removeFromBook(counter);
            } else {
                counter.setStatus(OrderStatus.PARTIALLY_FILLED);
            }
        }

        if (order.getRemainingQuantity() > 0) {
            order.setStatus(OrderStatus.CANCELLED);
        } else {
            order.setStatus(OrderStatus.FILLED);
        }
        return results;
    }

    private List<MatchResult> handleFokOrder(Order order) {
        long available = getAvailableQuantity(order);
        if (available < order.getQuantity()) {
            order.setStatus(OrderStatus.CANCELLED);
            return Collections.emptyList();
        }

        List<MatchResult> results = new ArrayList<>();
        while (order.getRemainingQuantity() > 0 && canMatch(order)) {
            Order counter = getBestCounterOrder(order);
            if (counter == null) break;

            long matchQty = Math.min(order.getRemainingQuantity(), counter.getRemainingQuantity());
            results.add(createMatchResult(order, counter, matchQty));

            order.setFilledQuantity(order.getFilledQuantity() + matchQty);
            counter.setFilledQuantity(counter.getFilledQuantity() + matchQty);

            if (counter.isFilled()) {
                counter.setStatus(OrderStatus.FILLED);
                removeFromBook(counter);
            } else {
                counter.setStatus(OrderStatus.PARTIALLY_FILLED);
            }
        }

        order.setStatus(order.isFilled() ? OrderStatus.FILLED : OrderStatus.CANCELLED);
        return results;
    }

    private List<MatchResult> handlePostOnlyOrder(Order order) {
        if (canMatch(order)) {
            order.setStatus(OrderStatus.REJECTED);
            return Collections.emptyList();
        }
        order.setStatus(OrderStatus.OPEN);
        addToBook(order);
        return Collections.emptyList();
    }

    // ==================== 核心辅助方法 ====================

    private boolean canMatch(Order order) {
        if (order.getSide() == OrderSide.BUY) {
            Long bestAsk = getBestAskPrice();
            return bestAsk != null && bestAsk <= order.getPrice();
        } else {
            Long bestBid = getBestBidPrice();
            return bestBid != null && bestBid >= order.getPrice();
        }
    }

    private Order getBestCounterOrder(Order order) {
        if (order.getSide() == OrderSide.BUY) {
            return getFirstActiveOrder(asks);
        } else {
            return getFirstActiveOrder(bids);
        }
    }

    /**
     * 获取指定订单簿中的第一个活跃订单，并清理沿途的无效订单
     */
    private Order getFirstActiveOrder(TreeMap<Long, Deque<Order>> book) {
        while (!book.isEmpty()) {
            Map.Entry<Long, Deque<Order>> entry = book.firstEntry();
            Deque<Order> queue = entry.getValue();
            cleanQueue(queue); // 清理无效订单
            if (queue.isEmpty()) {
                book.remove(entry.getKey());
                continue;
            }
            return queue.peekFirst();
        }
        return null;
    }

    private long getAvailableQuantity(Order order) {
        long total = 0L;
        if (order.getSide() == OrderSide.BUY) {
            for (Map.Entry<Long, Deque<Order>> entry : asks.entrySet()) {
                if (entry.getKey() <= order.getPrice()) {
                    for (Order o : entry.getValue()) {
                        if (o.isActive()) total += o.getRemainingQuantity();
                    }
                }
            }
        } else {
            for (Map.Entry<Long, Deque<Order>> entry : bids.entrySet()) {
                if (entry.getKey() >= order.getPrice()) {
                    for (Order o : entry.getValue()) {
                        if (o.isActive()) total += o.getRemainingQuantity();
                    }
                }
            }
        }
        return total;
    }

    // ==================== 订单簿操作 ====================

    private void addToBook(Order order) {
        TreeMap<Long, Deque<Order>> book = order.getSide() == OrderSide.BUY ? bids : asks;
        Deque<Order> queue = book.computeIfAbsent(order.getPrice(), k -> new ArrayDeque<>());
        queue.addLast(order);
        orderMap.put(order.getOrderId(), order);
    }

    private void removeFromBook(Order order) {
        TreeMap<Long, Deque<Order>> book = order.getSide() == OrderSide.BUY ? bids : asks;
        Deque<Order> queue = book.get(order.getPrice());
        if (queue != null) {
            queue.remove(order);
            if (queue.isEmpty()) {
                book.remove(order.getPrice());
            }
        }
        orderMap.remove(order.getOrderId());
    }

    /**
     * 清理队列中不活跃的订单，并从 orderMap 中移除它们
     *
     * @param queue 订单队列（必须是双端队列）
     * @return 清理后队列是否为空
     */
    private boolean cleanQueue(Deque<Order> queue) {
        while (!queue.isEmpty() && !queue.peekFirst().isActive()) {
            Order invalid = queue.pollFirst();
            orderMap.remove(invalid.getOrderId());
        }
        return queue.isEmpty();
    }

    private MatchResult createMatchResult(Order taker, Order maker, long qty) {
        return MatchResult.builder()
                .tradeId("T" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6))
                .symbol(symbol)
                .price(maker.getPrice() != null ? maker.getPrice() : taker.getPrice())
                .quantity(qty)
                .buyerOrderId(taker.getSide() == OrderSide.BUY ? taker.getOrderId() : maker.getOrderId())
                .buyerUserId(taker.getSide() == OrderSide.BUY ? taker.getUserId() : maker.getUserId())
                .sellerOrderId(taker.getSide() == OrderSide.SELL ? taker.getOrderId() : maker.getOrderId())
                .sellerUserId(taker.getSide() == OrderSide.SELL ? taker.getUserId() : maker.getUserId())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ==================== 公共查询方法 ====================

    public String getSymbol() {
        return symbol;
    }

    public Long getBestBidPrice() {
        while (!bids.isEmpty()) {
            Map.Entry<Long, Deque<Order>> entry = bids.firstEntry();
            Deque<Order> queue = entry.getValue();
            cleanQueue(queue);
            if (queue.isEmpty()) {
                bids.remove(entry.getKey());
                continue;
            }
            return entry.getKey();
        }
        return null;
    }

    public Long getBestAskPrice() {
        while (!asks.isEmpty()) {
            Map.Entry<Long, Deque<Order>> entry = asks.firstEntry();
            Deque<Order> queue = entry.getValue();
            cleanQueue(queue);
            if (queue.isEmpty()) {
                asks.remove(entry.getKey());
                continue;
            }
            return entry.getKey();
        }
        return null;
    }

    public Map<Long, Long> getBidDepth() {
        Map<Long, Long> depth = new LinkedHashMap<>();
        for (Map.Entry<Long, Deque<Order>> entry : bids.entrySet()) {
            Deque<Order> queue = entry.getValue();
            cleanQueue(queue); // 统一清理，保持一致性
            if (queue.isEmpty()) continue;
            long total = queue.stream()
                    .filter(Order::isActive)
                    .mapToLong(Order::getRemainingQuantity)
                    .sum();
            if (total > 0) depth.put(entry.getKey(), total);
        }
        return depth;
    }

    public Map<Long, Long> getAskDepth() {
        Map<Long, Long> depth = new LinkedHashMap<>();
        for (Map.Entry<Long, Deque<Order>> entry : asks.entrySet()) {
            Deque<Order> queue = entry.getValue();
            cleanQueue(queue); // 统一清理
            if (queue.isEmpty()) continue;
            long total = queue.stream()
                    .filter(Order::isActive)
                    .mapToLong(Order::getRemainingQuantity)
                    .sum();
            if (total > 0) depth.put(entry.getKey(), total);
        }
        return depth;
    }

    public boolean cancelOrder(String orderId) {
        Order order = orderMap.get(orderId);
        if (order == null || !order.isActive()) {
            return false;
        }
        order.setStatus(OrderStatus.CANCELLED);
        removeFromBook(order);
        return true;
    }

    public int getOrderCount() {
        return orderMap.size();
    }
}