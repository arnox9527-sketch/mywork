package com.exchange.matching.service;

import com.exchange.matching.config.SymbolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交易对管理器
 * 管理所有交易对的配置和订单簿实例
 */
@Slf4j
@Component
public class SymbolManager {
    
    /**
     * 交易对配置缓存
     */
    private final Map<String, SymbolConfig> symbolConfigMap = new ConcurrentHashMap<>();
    
    /**
     * 初始化交易对配置
     * 实际生产环境应从数据库或配置中心加载
     */
    @PostConstruct
    public void init() {
        // 这里硬编码几个交易对用于开发测试
        // 实际应从数据库或 Nacos 配置中心加载
        registerSymbol(SymbolConfig.builder()
            .symbol("BTC-USDT")
            .baseAsset("BTC")
            .quoteAsset("USDT")
            .productType("SPOT")
            .priceScale(2)
            .quantityScale(8)
            .minQuantity(10000L)    // 0.0001 BTC
            .maxQuantity(1000000000L) // 10 BTC
            .tickSize(1L)           // 1 USDT
            .enabled(true)
            .build());
        
        registerSymbol(SymbolConfig.builder()
            .symbol("ETH-USDT")
            .baseAsset("ETH")
            .quoteAsset("USDT")
            .productType("SPOT")
            .priceScale(2)
            .quantityScale(6)
            .minQuantity(100000L)    // 0.001 ETH
            .maxQuantity(100000000L) // 100 ETH
            .tickSize(1L)
            .enabled(true)
            .build());
        
        registerSymbol(SymbolConfig.builder()
            .symbol("BTC-USD-PERP")
            .baseAsset("BTC")
            .quoteAsset("USD")
            .productType("PERP")
            .priceScale(1)
            .quantityScale(0)        // 合约按张数，整数
            .minQuantity(1L)
            .maxQuantity(1000L)
            .tickSize(0L)
            .enabled(true)
            .build());
        
        log.info("初始化交易对配置完成，共 {} 个", symbolConfigMap.size());
    }
    
    /**
     * 注册交易对
     */
    public void registerSymbol(SymbolConfig config) {
        symbolConfigMap.put(config.getSymbol(), config);
        log.info("注册交易对: {}", config.getSymbol());
    }
    
    /**
     * 获取交易对配置
     */
    public SymbolConfig getSymbolConfig(String symbol) {
        SymbolConfig config = symbolConfigMap.get(symbol);
        if (config == null) {
            throw new IllegalArgumentException("不支持的交易对: " + symbol);
        }
        return config;
    }
    
    /**
     * 检查交易对是否支持
     */
    public boolean isSupported(String symbol) {
        SymbolConfig config = symbolConfigMap.get(symbol);
        return config != null && config.getEnabled();
    }
    
    /**
     * 获取所有交易对
     */
    public Map<String, SymbolConfig> getAllSymbols() {
        return symbolConfigMap;
    }
}