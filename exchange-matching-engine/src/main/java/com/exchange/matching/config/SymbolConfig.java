package com.exchange.matching.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易对配置
 * 定义每个交易对的基本属性和精度
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymbolConfig {
    
    /**
     * 交易对名称（如 BTC-USDT）
     */
    private String symbol;
    
    /**
     * 基础货币（如 BTC）
     */
    private String baseAsset;
    
    /**
     * 计价货币（如 USDT）
     */
    private String quoteAsset;
    
    /**
     * 产品类型：SPOT / PERP / FUTURE
     */
    private String productType;
    
    /**
     * 价格精度（小数位数）
     * 如 USDT 精确到分：2
     */
    private Integer priceScale;
    
    /**
     * 数量精度（小数位数）
     * 如 BTC 精确到聪：8
     */
    private Integer quantityScale;
    
    /**
     * 最小下单数量（以最小单位表示）
     */
    private Long minQuantity;
    
    /**
     * 最大下单数量（以最小单位表示）
     */
    private Long maxQuantity;
    
    /**
     * 最小价格变动（tick size，以最小单位表示）
     */
    private Long tickSize;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    // ========== 工具方法 ==========
    
    /**
     * 从 symbol 解析基础货币
     */
    public static String parseBaseAsset(String symbol) {
        return symbol.split("-")[0];
    }
    
    /**
     * 从 symbol 解析计价货币
     */
    public static String parseQuoteAsset(String symbol) {
        return symbol.split("-")[1];
    }
}