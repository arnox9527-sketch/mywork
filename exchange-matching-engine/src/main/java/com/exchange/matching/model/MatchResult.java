package com.exchange.matching.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 撮合成交结果
 * 注意：price 和 quantity 均以 "最小单位"（Long）存储
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {

    /**
     * 成交ID
     */
    private String tradeId;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 成交价格（最小价格单位）
     */
    private Long price;

    /**
     * 成交数量（最小数量单位）
     */
    private Long quantity;

    /**
     * 买方订单ID
     */
    private String buyerOrderId;

    /**
     * 买方用户ID
     */
    private Long buyerUserId;

    /**
     * 卖方订单ID
     */
    private String sellerOrderId;

    /**
     * 卖方用户ID
     */
    private Long sellerUserId;

    /**
     * 成交时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 成交金额 = price * quantity（最小单位）
     */
    public Long getAmount() {
        return price * quantity;
    }
}