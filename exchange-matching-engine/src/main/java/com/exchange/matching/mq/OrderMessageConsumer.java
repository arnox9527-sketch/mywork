package com.exchange.matching.mq;

import com.exchange.matching.disruptor.OrderEventProducer;
import com.exchange.matching.dto.PlaceOrderRequest;
import com.exchange.matching.model.Order;
import com.exchange.matching.util.OrderConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${mq.topic.orders:orders}",
        consumerGroup = "matching-engine-consumer"
)
public class OrderMessageConsumer implements RocketMQListener<String> {

    @Autowired
    private OrderEventProducer orderEventProducer;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(String message) {
        try {
            PlaceOrderRequest request = objectMapper.readValue(message, PlaceOrderRequest.class);
            log.info("收到订单消息: symbol={}, side={}, type={}, price={}, qty={}",
                    request.getSymbol(), request.getSide(), request.getOrderType(),
                    request.getPrice(), request.getQuantity());

            Order order = OrderConverter.toOrder(request);
            orderEventProducer.publish(order);

        } catch (Exception e) {
            log.error("处理订单消息失败: {}", e.getMessage(), e);
        }
    }
}
