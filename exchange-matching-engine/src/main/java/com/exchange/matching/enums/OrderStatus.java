package com.exchange.matching.enums;

/**
 * 订单状态
 */
public enum OrderStatus {
    /**
     * 已接收（等待处理）
     */
    RECEIVED,
    
    /**
     * 已挂单（进入订单簿等待成交）
     */
    OPEN,
    
    /**
     * 部分成交
     */
    PARTIALLY_FILLED,
    
    /**
     * 完全成交
     */
    FILLED,
    
    /**
     * 已取消
     */
    CANCELLED,
    
    /**
     * 已拒绝（无效订单）
     */
    REJECTED
}