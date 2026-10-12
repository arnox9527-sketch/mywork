package com.exchange.trade.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.exchange.trade.dto.MatchResultMessage;
import com.exchange.trade.dto.SettleRequest;
import com.exchange.trade.entity.TradeOrder;
import com.exchange.trade.feign.AssetFeignClient;
import com.exchange.trade.mapper.TradeOrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${mq.topic.trade-results:trade-results}",
        consumerGroup = "trade-service-consumer"
)
public class TradeResultConsumer implements RocketMQListener<String> {

    @Autowired
    private TradeOrderMapper tradeOrderMapper;

    @Autowired
    private AssetFeignClient assetFeignClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(String message) {
        try {
            MatchResultMessage result = objectMapper.readValue(message, MatchResultMessage.class);
            log.info("收到成交结果: tradeId={}, symbol={}, price={}, qty={}",
                    result.getTradeId(), result.getSymbol(), result.getPrice(), result.getQuantity());

            // 处理买方订单
            processBuyerSettle(result);

            // 处理卖方订单
            processSellerSettle(result);

        } catch (Exception e) {
            log.error("处理成交结果失败: {}", e.getMessage(), e);
        }
    }

    private void processBuyerSettle(MatchResultMessage result) {
        TradeOrder buyOrder = tradeOrderMapper.selectOne(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderId, result.getBuyerOrderId()));

        if (buyOrder == null) {
            log.warn("买方订单不存在: orderId={}", result.getBuyerOrderId());
            return;
        }

        Long tradeAmount = result.getPrice() * result.getQuantity();

        // 更新订单成交信息
        tradeOrderMapper.update(null, new LambdaUpdateWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderId, result.getBuyerOrderId())
                .setSql("filled_quantity = filled_quantity + " + result.getQuantity())
                .setSql("filled_amount = filled_amount + " + tradeAmount)
                .set(TradeOrder::getStatus, "FILLED"));

        // 结算: 解冻计价货币(USDT), 增加基础货币(BTC)
        String[] parts = result.getSymbol().split("-");
        String baseAsset = parts[0];
        String quoteAsset = parts[1];

        SettleRequest settleRequest = new SettleRequest();
        settleRequest.setUserId(result.getBuyerUserId());
        settleRequest.setFreezeCurrency(quoteAsset);
        settleRequest.setFreezeWalletType("SPOT");
        settleRequest.setCreditCurrency(baseAsset);
        settleRequest.setCreditWalletType("SPOT");
        settleRequest.setFreezeAmount(tradeAmount);
        settleRequest.setCreditAmount(result.getQuantity());

        assetFeignClient.settle(settleRequest);
        log.info("买方结算完成: orderId={}, 解冻{}={}, 增加{}={}",
                result.getBuyerOrderId(), quoteAsset, tradeAmount, baseAsset, result.getQuantity());
    }

    private void processSellerSettle(MatchResultMessage result) {
        TradeOrder sellOrder = tradeOrderMapper.selectOne(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderId, result.getSellerOrderId()));

        if (sellOrder == null) {
            log.warn("卖方订单不存在: orderId={}", result.getSellerOrderId());
            return;
        }

        Long tradeAmount = result.getPrice() * result.getQuantity();

        // 更新订单成交信息
        tradeOrderMapper.update(null, new LambdaUpdateWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderId, result.getSellerOrderId())
                .setSql("filled_quantity = filled_quantity + " + result.getQuantity())
                .setSql("filled_amount = filled_amount + " + tradeAmount)
                .set(TradeOrder::getStatus, "FILLED"));

        // 结算: 解冻基础货币(BTC), 增加计价货币(USDT)
        String[] parts = result.getSymbol().split("-");
        String baseAsset = parts[0];
        String quoteAsset = parts[1];

        SettleRequest settleRequest = new SettleRequest();
        settleRequest.setUserId(result.getSellerUserId());
        settleRequest.setFreezeCurrency(baseAsset);
        settleRequest.setFreezeWalletType("SPOT");
        settleRequest.setCreditCurrency(quoteAsset);
        settleRequest.setCreditWalletType("SPOT");
        settleRequest.setFreezeAmount(result.getQuantity());
        settleRequest.setCreditAmount(tradeAmount);

        assetFeignClient.settle(settleRequest);
        log.info("卖方结算完成: orderId={}, 解冻{}={}, 增加{}={}",
                result.getSellerOrderId(), baseAsset, result.getQuantity(), quoteAsset, tradeAmount);
    }
}
