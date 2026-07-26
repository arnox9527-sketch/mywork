package com.exchange.matching.service;

import com.exchange.matching.dto.MatchResultDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MqProducerService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mq.topic.trade-results:trade-results}")
    private String tradeResultsTopic;

    /**
     * 发送成交结果到 RocketMQ
     */
    public void sendMatchResult(MatchResultDto result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            SendResult sendResult = rocketMQTemplate.syncSend(tradeResultsTopic, json);
            log.info("📤 成交结果已发送: tradeId={}, msgId={}",
                    result.getTradeId(), sendResult.getMsgId());
        } catch (Exception e) {
            log.error("发送成交结果失败: tradeId={}, error={}",
                    result.getTradeId(), e.getMessage(), e);
        }
    }
}