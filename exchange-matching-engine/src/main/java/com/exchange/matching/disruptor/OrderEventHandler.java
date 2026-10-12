package com.exchange.matching.disruptor;

import com.exchange.matching.core.OrderBook;
import com.exchange.matching.dto.MatchResultDto;
import com.exchange.matching.model.MatchResult;
import com.exchange.matching.model.Order;
import com.exchange.matching.service.MqProducerService;
import com.exchange.matching.service.OrderBookManager;
import com.exchange.matching.util.OrderConverter;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 事件消费者（EventHandler）—— 从 RingBuffer 中取出订单并执行撮合
 *
 * 【核心机制】
 * Disruptor 会启动一个独立的消费线程，不断轮询 RingBuffer，
 * 每当有新的事件被 publish，就调用 onEvent() 方法处理。
 *
 * 【为什么是单线程消费？】
 * 撮合引擎必须保证订单按顺序处理（价格优先 + 时间优先），
 * 如果多线程并发撮合，会导致订单簿状态不一致。
 * 所以这里只用一个消费者线程，保证串行处理。
 *
 * 【相比传统 BlockingQueue 的优势】
 * - 无锁：不需要 synchronized 或 Lock，通过 CAS + 内存屏障实现线程安全
 * - 无 GC：事件对象预创建，消费完清理后复用，不产生新对象
 * - 低延迟：消费线程用 CPU 自旋等待（BusySpin），不阻塞不唤醒，响应在微秒级
 */
@Slf4j
public class OrderEventHandler implements EventHandler<OrderEvent> {

    private final OrderBookManager orderBookManager;
    private final MqProducerService mqProducerService;

    public OrderEventHandler(OrderBookManager orderBookManager, MqProducerService mqProducerService) {
        this.orderBookManager = orderBookManager;
        this.mqProducerService = mqProducerService;
    }

    /**
     * 每当 RingBuffer 中有新事件可消费时，Disruptor 框架会回调这个方法。
     *
     * @param event      当前槽位的 OrderEvent 对象（预创建的，不是 new 出来的）
     * @param sequence   当前事件在 RingBuffer 中的序号（单调递增，可用于追踪顺序）
     * @param endOfBatch 是否为本次批量消费的最后一个事件（可用于优化：最后一批才刷盘/发MQ等）
     */
    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws Exception {
        Order order = event.getOrder();
        if (order == null) {
            return;
        }

        try {
            log.debug("Disruptor处理订单: orderId={}, symbol={}, side={}",
                    order.getOrderId(), order.getSymbol(), order.getSide());

            // 1. 获取该交易对的订单簿（不存在则创建）
            OrderBook orderBook = orderBookManager.getOrCreateOrderBook(order.getSymbol());

            // 2. 执行撮合 —— 这是整个撮合引擎的核心，订单在这里匹配成交
            List<MatchResult> results = orderBook.processOrder(order);

            // 3. 如果有成交，把每笔成交结果发到 RocketMQ，下游（trade-service）会消费并结算
            if (results != null && !results.isEmpty()) {
                for (MatchResult result : results) {
                    MatchResultDto dto = OrderConverter.toDto(result);
                    mqProducerService.sendMatchResult(dto);
                }
            }

            log.debug("订单撮合完成: orderId={}, status={}, filled={}, remaining={}",
                    order.getOrderId(), order.getStatus(),
                    order.getFilledQuantity(), order.getRemainingQuantity());

        } catch (Exception e) {
            log.error("Disruptor处理订单异常: orderId={}, error={}", order.getOrderId(), e.getMessage(), e);
        } finally {
            // 4. 清理槽位，释放对 Order 的引用，让这个 Order 对象可以被 GC 回收
            event.clear();
        }
    }
}
