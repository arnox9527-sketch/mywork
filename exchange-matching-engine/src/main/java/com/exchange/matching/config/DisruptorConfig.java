package com.exchange.matching.config;

import com.exchange.matching.disruptor.OrderEvent;
import com.exchange.matching.disruptor.OrderEventFactory;
import com.exchange.matching.disruptor.OrderEventHandler;
import com.exchange.matching.disruptor.OrderEventProducer;
import com.exchange.matching.service.MqProducerService;
import com.exchange.matching.service.OrderBookManager;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ThreadFactory;

/**
 * Disruptor 的 Spring 配置类
 *
 * 【整体流程图】
 *
 *   生产者（MQ消费者 / HTTP接口）
 *        │
 *        │ orderEventProducer.publish(order)
 *        ▼
 *   ┌─────────────────────────────────┐
 *   │         RingBuffer              │  ← 固定大小的环形数组（100万+槽位）
 *   │  [Event0][Event1][Event2]...    │     预创建，反复复用
 *   │     ↑                    │       │
 *   │   生产者写入           消费者读取  │
 *   └─────────────────────────────────┘
 *        │                    │
 *        │                    ▼
 *        │            OrderEventHandler.onEvent()
 *        │                    │
 *        │                    ├── OrderBook.processOrder()  ← 撮合
 *        │                    └── MqProducerService.send()  ← 发成交结果
 *
 * 【关键参数解释】
 *
 * 1. RingBuffer 大小 = 1024 * 1024（约100万）
 *    - 必须是 2 的幂次（Disruptor 用位运算代替取模，提升性能）
 *    - 越大能缓冲越多订单，但占用更多内存
 *    - 如果消费速度够快，实际用到的槽位远小于这个值
 *
 * 2. WaitStrategy = BusySpinWaitStrategy
 *    - 消费线程在没有新事件时，用 CPU 自旋（while 循环空转）等待
 *    - 优点：延迟最低（微秒级），因为没有线程切换的开销
 *    - 缺点：会持续占用一个 CPU 核心
 *    - 适用场景：对延迟极度敏感的撮合引擎
 *    - 其他可选策略：
 *      · YieldingWaitStrategy  —— 自旋一段时间后再让出 CPU，延迟稍高但省 CPU
 *      · SleepingWaitStrategy  —— 没事件就 sleep，最省 CPU 但延迟最高（毫秒级）
 *      · BlockingWaitStrategy  —— 用 Lock + Condition 等待，和传统阻塞队列类似
 *
 * 3. ProducerType = MULTI
 *    - 允许多个线程同时发布事件（比如 MQ 消费线程 + HTTP 请求线程）
 *    - 如果确定只有一个生产者线程，可以改成 SINGLE，性能会更高（少了 CAS 竞争）
 */
@Configuration
public class DisruptorConfig {

    private static final int RING_BUFFER_SIZE = 1024 * 1024;

    @Autowired
    private OrderBookManager orderBookManager;

    @Autowired
    private MqProducerService mqProducerService;

    private Disruptor<OrderEvent> disruptor;

    @Bean
    public OrderEventProducer orderEventProducer() {
        // 创建消费线程：设为守护线程，JVM 退出时自动结束
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("disruptor-matching-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        };

        // 创建 Disruptor 实例
        disruptor = new Disruptor<>(
                new OrderEventFactory(),       // 事件工厂：启动时预创建所有 OrderEvent
                RING_BUFFER_SIZE,              // RingBuffer 大小
                threadFactory,                 // 消费线程工厂
                ProducerType.MULTI,            // 支持多生产者
                new BusySpinWaitStrategy()     // 自旋等待策略（最低延迟）
        );

        // 注册消费者：当有新事件时，由 OrderEventHandler 处理
        disruptor.handleEventsWith(new OrderEventHandler(orderBookManager, mqProducerService));

        // 启动 Disruptor：此时消费线程开始运行，轮询 RingBuffer 等待事件
        disruptor.start();

        // 拿到 RingBuffer 的引用，包装成 Producer 暴露给外部使用
        RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();
        return new OrderEventProducer(ringBuffer);
    }

    /**
     * Spring 容器关闭时，优雅关停 Disruptor。
     * shutdown() 会等待所有已发布的事件处理完毕后再停止消费线程。
     */
    @PreDestroy
    public void shutdown() {
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }
}
