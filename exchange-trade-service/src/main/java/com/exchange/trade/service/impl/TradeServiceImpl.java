package com.exchange.trade.service.impl;

import com.exchange.trade.converter.OrderConverter;
import com.exchange.trade.dto.FreezeRequest;
import com.exchange.trade.dto.PlaceOrderRequest;
import com.exchange.trade.dto.PlaceOrderResponse;
import com.exchange.trade.entity.TradeOrder;
import com.exchange.trade.feign.AssetFeignClient;
import com.exchange.trade.mapper.TradeOrderMapper;
import com.exchange.trade.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private TradeOrderMapper tradeOrderMapper;

    @Autowired
    private AssetFeignClient assetFeignClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mq.topic.orders:orders}")
    private String ordersTopic;

    @Override
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        // 1. 创建订单记录
        TradeOrder order = OrderConverter.toEntity(request);

        // 2. 冻结资产
        freezeAsset(order);

        // 3. 保存订单
        tradeOrderMapper.insert(order);

        // 4. 发送订单到撮合引擎
        sendOrderToMatchingEngine(order);

        log.info("订单已提交: orderId={}, symbol={}, side={}", order.getOrderId(), order.getSymbol(), order.getSide());

        return OrderConverter.toResponse(order);
    }

    private void freezeAsset(TradeOrder order) {
        FreezeRequest freezeRequest = new FreezeRequest();
        freezeRequest.setUserId(order.getUserId());
        freezeRequest.setWalletType("SPOT");

        if ("BUY".equals(order.getSide())) {
            // 买入: 冻结计价货币(如USDT)
            String quoteAsset = order.getSymbol().split("-")[1];
            freezeRequest.setCurrencyCode(quoteAsset);
            Long freezeAmount = order.getPrice() != null ? order.getPrice() * order.getQuantity() : order.getQuantity();
            freezeRequest.setAmount(freezeAmount);
            order.setFrozenAmount(freezeAmount);
        } else {
            // 卖出: 冻结基础货币(如BTC)
            String baseAsset = order.getSymbol().split("-")[0];
            freezeRequest.setCurrencyCode(baseAsset);
            freezeRequest.setAmount(order.getQuantity());
            order.setFrozenAmount(order.getQuantity());
        }

        assetFeignClient.freeze(freezeRequest);
        log.info("资产已冻结: orderId={}, amount={}", order.getOrderId(), freezeRequest.getAmount());
    }

    private void sendOrderToMatchingEngine(TradeOrder order) {
        try {
            String message = objectMapper.writeValueAsString(buildMqMessage(order));
            rocketMQTemplate.syncSend(ordersTopic, message);
            order.setStatus("OPEN");
            tradeOrderMapper.updateById(order);
        } catch (Exception e) {
            log.error("发送订单到撮合引擎失败: orderId={}, error={}", order.getOrderId(), e.getMessage());
            order.setStatus("REJECTED");
            order.setRemark("发送撮合引擎失败: " + e.getMessage());
            tradeOrderMapper.updateById(order);
            throw new RuntimeException("发送订单失败", e);
        }
    }

    private java.util.Map<String, Object> buildMqMessage(TradeOrder order) {
        java.util.Map<String, Object> msg = new java.util.HashMap<>();
        msg.put("symbol", order.getSymbol());
        msg.put("productType", order.getProductType());
        msg.put("side", order.getSide());
        msg.put("orderType", order.getOrderType());
        msg.put("price", order.getPrice());
        msg.put("quantity", order.getQuantity());
        msg.put("userId", order.getUserId());
        return msg;
    }
}
