package com.exchange.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderId;

    private Long userId;

    private String symbol;

    private String productType;

    private String side;

    private String orderType;

    private Long price;

    private Long quantity;

    private Long filledQuantity;

    private Long filledAmount;

    private String status;

    private Long frozenAmount;

    private String remark;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
