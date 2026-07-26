package com.exchange;

import com.exchange.matching.core.OrderBook;
import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderStatus;
import com.exchange.matching.enums.OrderType;
import com.exchange.matching.enums.ProductType;
import com.exchange.matching.model.MatchResult;
import com.exchange.matching.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class OrderBookTest {

    private OrderBook orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook("BTC-USDT");
    }

    @Test
    void testLimitOrderMatch() {
        log.info("▶ 测试: 限价单完全成交");
        try {
            Order buyOrder = createLimitOrder(OrderSide.BUY, 50000L, 1L);
            List<MatchResult> results = orderBook.processOrder(buyOrder);
            if (!results.isEmpty()) {
                log.error("❌ 没有对手盘时不应该有成交结果");
                fail();
            }
            if (buyOrder.getStatus() != OrderStatus.OPEN) {
                log.error("❌ 挂单后状态应该为 OPEN，实际为: {}", buyOrder.getStatus());
                fail();
            }
            if (orderBook.getBestBidPrice() == null || orderBook.getBestBidPrice() != 50000L) {
                log.error("❌ 买盘最优价应该为 50000，实际为: {}", orderBook.getBestBidPrice());
                fail();
            }

            Order sellOrder = createLimitOrder(OrderSide.SELL, 50000L, 1L);
            results = orderBook.processOrder(sellOrder);
            if (results.size() != 1) {
                log.error("❌ 应该产生 1 笔成交，实际产生: {}", results.size());
                fail();
            }
            if (results.get(0).getPrice() != 50000L) {
                log.error("❌ 成交价格应该为 50000，实际为: {}", results.get(0).getPrice());
                fail();
            }
            if (results.get(0).getQuantity() != 1L) {
                log.error("❌ 成交数量应该为 1，实际为: {}", results.get(0).getQuantity());
                fail();
            }
            if (buyOrder.getStatus() != OrderStatus.FILLED) {
                log.error("❌ 买单状态应该为 FILLED，实际为: {}", buyOrder.getStatus());
                fail();
            }
            if (sellOrder.getStatus() != OrderStatus.FILLED) {
                log.error("❌ 卖单状态应该为 FILLED，实际为: {}", sellOrder.getStatus());
                fail();
            }
            if (orderBook.getBestBidPrice() != null || orderBook.getBestAskPrice() != null) {
                log.error("❌ 订单簿应该为空");
                fail();
            }
            log.info("  ✅ 限价单完全成交");
        } catch (Exception e) {
            log.error("❌ 测试异常: {}", e.getMessage(), e);
            fail(e);
        }
    }

    @Test
    void testPartialMatch() {
        log.info("▶ 测试: 部分成交");
        try {
            Order buyOrder = createLimitOrder(OrderSide.BUY, 50000L, 5L);
            orderBook.processOrder(buyOrder);
            if (buyOrder.getStatus() != OrderStatus.OPEN) {
                log.error("❌ 买单状态应该为 OPEN，实际为: {}", buyOrder.getStatus());
                fail();
            }

            Order sellOrder = createLimitOrder(OrderSide.SELL, 50000L, 3L);
            List<MatchResult> results = orderBook.processOrder(sellOrder);
            if (results.size() != 1) {
                log.error("❌ 应该产生 1 笔成交，实际产生: {}", results.size());
                fail();
            }
            if (results.get(0).getQuantity() != 3L) {
                log.error("❌ 成交数量应该为 3，实际为: {}", results.get(0).getQuantity());
                fail();
            }
            if (buyOrder.getStatus() != OrderStatus.PARTIALLY_FILLED) {
                log.error("❌ 买单状态应该为 PARTIALLY_FILLED，实际为: {}", buyOrder.getStatus());
                fail();
            }
            if (sellOrder.getStatus() != OrderStatus.FILLED) {
                log.error("❌ 卖单状态应该为 FILLED，实际为: {}", sellOrder.getStatus());
                fail();
            }
            if (buyOrder.getRemainingQuantity() != 2L) {
                log.error("❌ 买单剩余数量应该为 2，实际为: {}", buyOrder.getRemainingQuantity());
                fail();
            }
            log.info("  ✅ 部分成交");
        } catch (Exception e) {
            log.error("❌ 测试异常: {}", e.getMessage(), e);
            fail(e);
        }
    }

    @Test
    void testMarketOrder() {
        log.info("▶ 测试: 市价单");
        try {
            Order limitSell = createLimitOrder(OrderSide.SELL, 50000L, 1L);
            orderBook.processOrder(limitSell);
            if (limitSell.getStatus() != OrderStatus.OPEN) {
                log.error("❌ 限价卖单挂单后状态应该为 OPEN，实际为: {}", limitSell.getStatus());
                fail();
            }

            Order marketBuy = createMarketOrder(OrderSide.BUY, 1L);
            List<MatchResult> results = orderBook.processOrder(marketBuy);
            if (results.size() != 1) {
                log.error("❌ 应该产生 1 笔成交，实际产生: {}", results.size());
                fail();
            }
            if (limitSell.getStatus() != OrderStatus.FILLED) {
                log.error("❌ 限价卖单成交后状态应该为 FILLED，实际为: {}", limitSell.getStatus());
                fail();
            }
            if (marketBuy.getStatus() != OrderStatus.FILLED) {
                log.error("❌ 市价买单状态应该为 FILLED，实际为: {}", marketBuy.getStatus());
                fail();
            }
            log.info("  ✅ 市价单");
        } catch (Exception e) {
            log.error("❌ 测试异常: {}", e.getMessage(), e);
            fail(e);
        }
    }

    @Test
    void testIOCOrder() {
        log.info("▶ 测试: IOC订单");
        try {
            Order limitSell = createLimitOrder(OrderSide.SELL, 50000L, 1L);
            orderBook.processOrder(limitSell);

            Order iocBuy = createIOCOrder(OrderSide.BUY, 50000L, 3L);
            List<MatchResult> results = orderBook.processOrder(iocBuy);
            if (results.size() != 1) {
                log.error("❌ 应该产生 1 笔成交，实际产生: {}", results.size());
                fail();
            }
            if (results.get(0).getQuantity() != 1L) {
                log.error("❌ 成交数量应该为 1，实际为: {}", results.get(0).getQuantity());
                fail();
            }
            if (iocBuy.getStatus() != OrderStatus.CANCELLED) {
                log.error("❌ IOC订单状态应该为 CANCELLED，实际为: {}", iocBuy.getStatus());
                fail();
            }
            if (iocBuy.getFilledQuantity() != 1L) {
                log.error("❌ IOC订单已成交数量应该为 1，实际为: {}", iocBuy.getFilledQuantity());
                fail();
            }
            log.info("  ✅ IOC订单");
        } catch (Exception e) {
            log.error("❌ 测试异常: {}", e.getMessage(), e);
            fail(e);
        }
    }

    @Test
    void testCancelOrder() {
        log.info("▶ 测试: 取消订单");
        try {
            Order buyOrder = createLimitOrder(OrderSide.BUY, 50000L, 1L);
            orderBook.processOrder(buyOrder);
            if (buyOrder.getStatus() != OrderStatus.OPEN) {
                log.error("❌ 买单状态应该为 OPEN，实际为: {}", buyOrder.getStatus());
                fail();
            }

            boolean cancelled = orderBook.cancelOrder(buyOrder.getOrderId());
            if (!cancelled) {
                log.error("❌ 取消订单应该成功");
                fail();
            }
            if (buyOrder.getStatus() != OrderStatus.CANCELLED) {
                log.error("❌ 订单状态应该为 CANCELLED，实际为: {}", buyOrder.getStatus());
                fail();
            }
            if (orderBook.getBestBidPrice() != null) {
                log.error("❌ 取消后买盘应该为空");
                fail();
            }
            log.info("  ✅ 取消订单");
        } catch (Exception e) {
            log.error("❌ 测试异常: {}", e.getMessage(), e);
            fail(e);
        }
    }

    @Test
    void testPricePriority() {
        log.info("▶ 测试: 价格优先");
        try {
            Order buyHigh = createLimitOrder(OrderSide.BUY, 50100L, 1L);
            Order buyLow = createLimitOrder(OrderSide.BUY, 50000L, 1L);
            orderBook.processOrder(buyHigh);
            orderBook.processOrder(buyLow);
            if (orderBook.getBestBidPrice() != 50100L) {
                log.error("❌ 最优买价应该为 50100，实际为: {}", orderBook.getBestBidPrice());
                fail();
            }

            Order sell = createLimitOrder(OrderSide.SELL, 50000L, 1L);
            List<MatchResult> results = orderBook.processOrder(sell);
            if (results.size() != 1) {
                log.error("❌ 应该产生 1 笔成交，实际产生: {}", results.size());
                fail();
            }
            if (buyHigh.getStatus() != OrderStatus.FILLED) {
                log.error("❌ 高价买单应该先成交，状态为 FILLED，实际为: {}", buyHigh.getStatus());
                fail();
            }
            if (buyLow.getStatus() != OrderStatus.OPEN) {
                log.error("❌ 低价买单应该未成交，状态为 OPEN，实际为: {}", buyLow.getStatus());
                fail();
            }
            log.info("  ✅ 价格优先");
        } catch (Exception e) {
            log.error("❌ 测试异常: {}", e.getMessage(), e);
            fail(e);
        }
    }

    @Test
    void testTimePriority() {
        log.info("▶ 测试: 时间优先");
        try {
            Order first = createLimitOrder(OrderSide.BUY, 50000L, 1L);
            Order second = createLimitOrder(OrderSide.BUY, 50000L, 1L);
            orderBook.processOrder(first);
            orderBook.processOrder(second);

            Order sell = createLimitOrder(OrderSide.SELL, 50000L, 1L);
            List<MatchResult> results = orderBook.processOrder(sell);
            if (results.size() != 1) {
                log.error("❌ 应该产生 1 笔成交，实际产生: {}", results.size());
                fail();
            }
            if (first.getStatus() != OrderStatus.FILLED) {
                log.error("❌ 先到的买单应该先成交，状态为 FILLED，实际为: {}", first.getStatus());
                fail();
            }
            if (second.getStatus() != OrderStatus.OPEN) {
                log.error("❌ 后到的买单应该未成交，状态为 OPEN，实际为: {}", second.getStatus());
                fail();
            }
            log.info("  ✅ 时间优先");
        } catch (Exception e) {
            log.error("❌ 测试异常: {}", e.getMessage(), e);
            fail(e);
        }
    }

    @Test
    void testMultiplePriceLevels() {
        log.info("▶ 测试: 多价格层级");
        try {
            Order buy1 = createLimitOrder(OrderSide.BUY, 50100L, 1L);
            Order buy2 = createLimitOrder(OrderSide.BUY, 50000L, 1L);
            Order buy3 = createLimitOrder(OrderSide.BUY, 49900L, 1L);
            orderBook.processOrder(buy1);
            orderBook.processOrder(buy2);
            orderBook.processOrder(buy3);

            Order sell1 = createLimitOrder(OrderSide.SELL, 50000L, 1L);
            List<MatchResult> results = orderBook.processOrder(sell1);
            if (results.size() != 1) {
                log.error("❌ 应该产生 1 笔成交，实际产生: {}", results.size());
                fail();
            }
            if (buy1.getStatus() != OrderStatus.FILLED) {
                log.error("❌ 最高价买单应该先成交");
                fail();
            }
            if (buy2.getStatus() != OrderStatus.OPEN) {
                log.error("❌ 次高价买单应该还在订单簿中");
                fail();
            }

            Order sell2 = createLimitOrder(OrderSide.SELL, 50000L, 1L);
            results = orderBook.processOrder(sell2);
            if (results.size() != 1) {
                log.error("❌ 应该产生 1 笔成交，实际产生: {}", results.size());
                fail();
            }
            if (buy2.getStatus() != OrderStatus.FILLED) {
                log.error("❌ 次高价买单应该第二个成交");
                fail();
            }
            if (buy3.getStatus() != OrderStatus.OPEN) {
                log.error("❌ 最低价买单应该还在订单簿中");
                fail();
            }

            var depth = orderBook.getBidDepth();
            if (depth.size() != 1) {
                log.error("❌ 买盘深度应该只有 1 个价格层级，实际: {}", depth.size());
                fail();
            }
            if (!depth.containsKey(49900L)) {
                log.error("❌ 买盘剩余价格层级应该是 49900");
                fail();
            }
            log.info("  ✅ 多价格层级");
        } catch (Exception e) {
            log.error("❌ 测试异常: {}", e.getMessage(), e);
            fail(e);
        }
    }

    @Test
    void testPostOnlyOrder() {
        log.info("▶ 测试: POST_ONLY 订单");
        try {
            // 1. 先挂一个卖单 50000，作为对手盘
            Order existingSell = createLimitOrder(OrderSide.SELL, 50000L, 1L);
            orderBook.processOrder(existingSell);

            // 2. POST_ONLY 买单 50000，价格相同，会立即成交，应被拒绝
            Order postOnlyBuy = createPostOnlyOrder(OrderSide.BUY, 50000L, 1L);
            List<MatchResult> results = orderBook.processOrder(postOnlyBuy);

            // 3. 验证结果
            if (results.isEmpty()) {
                // 没有成交，说明 POST_ONLY 被拒绝（或没有成交，但这里期望被拒绝）
                if (postOnlyBuy.getStatus() != OrderStatus.REJECTED) {
                    log.error("❌ POST_ONLY 订单状态应该为 REJECTED，实际为: {}", postOnlyBuy.getStatus());
                    fail();
                }
                log.info("  ✅ POST_ONLY 订单 (正确拒绝会吃单的订单)");
            } else {
                log.error("❌ POST_ONLY 不应该吃单，但产生了 {} 笔成交", results.size());
                fail();
            }
        } catch (Exception e) {
            log.error("❌ 测试异常: {}", e.getMessage(), e);
            fail(e);
        }
    }

    // ============ 辅助方法 ============
    private Order createLimitOrder(OrderSide side, Long price, Long quantity) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .symbol("BTC-USDT")
                .productType(ProductType.SPOT)
                .side(side)
                .orderType(OrderType.LIMIT)
                .price(price)
                .quantity(quantity)
                .filledQuantity(0L)
                .userId(1L)
                .build();
    }

    private Order createMarketOrder(OrderSide side, Long quantity) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .symbol("BTC-USDT")
                .productType(ProductType.SPOT)
                .side(side)
                .orderType(OrderType.MARKET)
                .price(null)
                .quantity(quantity)
                .filledQuantity(0L)
                .userId(1L)
                .build();
    }

    private Order createIOCOrder(OrderSide side, Long price, Long quantity) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .symbol("BTC-USDT")
                .productType(ProductType.SPOT)
                .side(side)
                .orderType(OrderType.IOC)
                .price(price)
                .quantity(quantity)
                .filledQuantity(0L)
                .userId(1L)
                .build();
    }

    private Order createPostOnlyOrder(OrderSide side, Long price, Long quantity) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .symbol("BTC-USDT")
                .productType(ProductType.SPOT)
                .side(side)
                .orderType(OrderType.POST_ONLY)
                .price(price)
                .quantity(quantity)
                .filledQuantity(0L)
                .userId(1L)
                .build();
    }
}