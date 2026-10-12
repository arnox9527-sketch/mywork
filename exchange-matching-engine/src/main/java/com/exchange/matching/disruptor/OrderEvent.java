package com.exchange.matching.disruptor;

import com.exchange.matching.model.Order;
import lombok.Data;

/**
 * Disruptor 事件对象 —— 在 RingBuffer 中流转的数据载体
 *
 * 【什么是 RingBuffer？】
 * Disruptor 的核心是一个固定大小的环形数组（Ring Buffer），就像一个首尾相连的队列。
 * 每个槽位（slot）存放一个 OrderEvent 对象。
 *
 * 【为什么不直接传 Order？】
 * Disruptor 在启动时会预创建所有 OrderEvent 对象（比如 RingBuffer 大小是 100万，就预创建 100万个 OrderEvent）。
 * 生产者往槽位里填数据时，是往已有的对象上 set，而不是 new 一个新对象。
 * 这样做的好处：避免运行时频繁创建对象，减少 GC 压力，降低延迟抖动。
 */
@Data
public class OrderEvent {

    /**
     * 当前槽位承载的订单。
     * 生产者通过 setOrder() 填入订单，消费者通过 getOrder() 取出订单。
     */
    private Order order;

    /**
     * 消费完成后清理槽位，释放对 Order 对象的引用。
     * 防止已处理的订单一直被 RingBuffer 持有，导致内存泄漏。
     */
    public void clear() {
        this.order = null;
    }
}
