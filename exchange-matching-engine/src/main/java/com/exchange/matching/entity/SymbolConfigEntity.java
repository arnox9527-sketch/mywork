package com.exchange.matching.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("symbol_config")
public class SymbolConfigEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String symbol;

    private String baseAsset;

    private String quoteAsset;

    private String productType;

    private Integer priceScale;

    private Integer quantityScale;

    private Long minQuantity;

    private Long maxQuantity;

    private Long tickSize;

    private Integer enabled;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
