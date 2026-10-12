package com.exchange.matching.config;

import lombok.Data;

@Data
public class SymbolConfig {

    private String symbol;

    private String baseAsset;

    private String quoteAsset;

    private String productType;

    private Integer priceScale;

    private Integer quantityScale;

    private Long minQuantity;

    private Long maxQuantity;

    private Long tickSize;

    private Boolean enabled;

    public static String parseBaseAsset(String symbol) {
        return symbol.split("-")[0];
    }

    public static String parseQuoteAsset(String symbol) {
        return symbol.split("-")[1];
    }
}
