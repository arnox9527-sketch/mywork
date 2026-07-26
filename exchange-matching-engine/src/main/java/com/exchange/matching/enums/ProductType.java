package com.exchange.matching.enums;

/**
 * 产品类型
 * 现货和合约共用同一套撮合逻辑，但业务处理不同
 */
public enum ProductType {
    /**
     * 现货交易
     */
    SPOT("现货"),
    
    /**
     * 永续合约
     */
    PERP("永续合约"),
    
    /**
     * 交割合约（可扩展）
     */
    FUTURE("交割合约");
    
    private final String description;
    
    ProductType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}