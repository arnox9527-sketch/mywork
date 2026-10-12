package com.exchange.matching.disruptor;

import com.exchange.matching.model.Order;
import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;

/**
 * 事件生产者 —— 负责将订单发布到 Disruptor 的 RingBuffer 中
 *
 * 【发布流程（三步走）】
 * 1. ringBuffer.next()   → 申请一个可用的槽位序号（如果 RingBuffer 满了，这里会阻塞等待，这就是背压机制）
 * 2. ringBuffer.get(seq) → 拿到该槽位上的 OrderEvent 对象，往里面填入订单数据
 * 3. ringBuffer.publish() → 通知消费者"这个槽位已经填好了，可以消费了"
 *
 * 【为什么 next() 和 publish() 必须成对出现？】
 * next() 只是"占位"，此时消费者还看不到这个槽位的数据。
 * 只有调用 publish() 之后，消费者才能消费到这个事件。
 * 如果占位后不 publish（比如中间抛异常了），消费者会永远卡在这个位置等待。
 * 所以 publish() 必须放在 finally 块中，确保一定会执行。
 *
 * 【线程安全】
 * 多个线程可以同时调用 publish()，Disruptor 内部通过 CAS 保证序号分配不会冲突。
 * 这也是为什么 DisruptorConfig 中设置 ProducerType.MULTI 的原因。
 */
@Slf4j
public class OrderEventProducer {

    private final RingBuffer<OrderEvent> ringBuffer;

    public OrderEventProducer(RingBuffer<OrderEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * 将订单发布到 RingBuffer
     *
     * @param order 待撮合的订单
     */
    public void publish(Order order) {
        // 第1步：申请槽位序号。如果 RingBuffer 已满（消费跟不上），这里会阻塞等待
        long sequence = ringBuffer.next();
        try {
            // 第2步：拿到预创建的 OrderEvent 对象，填入订单数据
            OrderEvent event = ringBuffer.get(sequence);
            event.setOrder(order);
        } finally {
            // 第3步：发布事件，通知消费者可以消费了
            // 放在 finally 中保证即使 setOrder 抛异常，槽位也会被释放
            ringBuffer.publish(sequence);
        }
        log.debug("订单已发布到Disruptor: orderId={}, sequence={}", order.getOrderId(), sequence);
    }
}
