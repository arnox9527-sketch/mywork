package com.exchange.matching.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exchange.matching.config.SymbolConfig;
import com.exchange.matching.core.OrderBook;
import com.exchange.matching.entity.SymbolConfigEntity;
import com.exchange.matching.mapper.SymbolConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SymbolManager {

    @Autowired
    private SymbolConfigMapper symbolConfigMapper;

    @Autowired
    private OrderBookManager orderBookManager;

    private final Map<String, SymbolConfig> symbolConfigMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        List<SymbolConfigEntity> entities = symbolConfigMapper.selectList(
                new LambdaQueryWrapper<SymbolConfigEntity>()
                        .eq(SymbolConfigEntity::getEnabled, 1));

        for (SymbolConfigEntity entity : entities) {
            SymbolConfig config = toConfig(entity);
            symbolConfigMap.put(config.getSymbol(), config);
            orderBookManager.getOrCreateOrderBook(config.getSymbol());
            log.info("加载交易对: {}, 产品类型: {}", config.getSymbol(), config.getProductType());
        }

        log.info("交易对配置加载完成，共 {} 个，订单簿已初始化", symbolConfigMap.size());
    }

    private SymbolConfig toConfig(SymbolConfigEntity entity) {
        SymbolConfig config = new SymbolConfig();
        config.setSymbol(entity.getSymbol());
        config.setBaseAsset(entity.getBaseAsset());
        config.setQuoteAsset(entity.getQuoteAsset());
        config.setProductType(entity.getProductType());
        config.setPriceScale(entity.getPriceScale());
        config.setQuantityScale(entity.getQuantityScale());
        config.setMinQuantity(entity.getMinQuantity());
        config.setMaxQuantity(entity.getMaxQuantity());
        config.setTickSize(entity.getTickSize());
        config.setEnabled(entity.getEnabled() != null && entity.getEnabled() == 1);
        return config;
    }

    public void registerSymbol(SymbolConfig config) {
        symbolConfigMap.put(config.getSymbol(), config);
        log.info("注册交易对: {}", config.getSymbol());
    }

    public SymbolConfig getSymbolConfig(String symbol) {
        SymbolConfig config = symbolConfigMap.get(symbol);
        if (config == null) {
            throw new IllegalArgumentException("不支持的交易对: " + symbol);
        }
        return config;
    }

    public boolean isSupported(String symbol) {
        SymbolConfig config = symbolConfigMap.get(symbol);
        return config != null && config.getEnabled();
    }

    public Map<String, SymbolConfig> getAllSymbols() {
        return symbolConfigMap;
    }
}
