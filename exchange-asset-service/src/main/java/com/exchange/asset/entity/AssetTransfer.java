package com.exchange.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("asset_transfer")
public class AssetTransfer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transferNo;

    private Long userId;

    private Long currencyId;

    private Long fromWalletType;

    private Long toWalletType;

    private Long amount;

    private Integer status;

    private String remark;

    private LocalDateTime createdTime;
}
