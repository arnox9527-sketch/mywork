package com.exchange.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_asset")
public class UserAsset {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long currencyId;

    private Long walletTypeId;

    private Long available;

    private Long frozen;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
