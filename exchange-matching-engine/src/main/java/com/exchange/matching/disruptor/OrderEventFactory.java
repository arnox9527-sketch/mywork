package com.exchange.matching.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * 事件工厂 —— Disruptor 启动时用它来预创建所有 OrderEvent 对象
 *
 * 【什么时候调用？】
 * Disruptor.start() 时，会根据 RingBuffer 的大小（比如 100万），
 * 调用 100万次 newInstance()，预先填满整个环形数组。
 *
 * 【为什么需要工厂？】
 * 因为 Disruptor 要求所有事件对象在启动时就创建好，后续只是反复复用（填数据 → 消费 → 清理 → 再填数据），
 * 所以需要一个工厂来批量生产这些"空壳"事件。
 */
public class OrderEventFactory implements EventFactory<OrderEvent> {

    @Override
    public OrderEvent newInstance() {
        return new OrderEvent();
    }
}
