-- ============================================
-- 资产服务表
-- Database: exchange
-- ============================================

USE `exchange`;

-- 币种配置表
CREATE TABLE IF NOT EXISTS `currency` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `code`            VARCHAR(32)  NOT NULL COMMENT '币种代码(如BTC、ETH、USDT)',
    `name`            VARCHAR(64)  NOT NULL COMMENT '币种名称',
    `symbol`          VARCHAR(16)  NOT NULL COMMENT '币种符号(如₿、Ξ)',
    `decimals`        INT          NOT NULL DEFAULT 8 COMMENT '精度位数',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `created_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='币种配置表';

-- 钱包类型表
CREATE TABLE IF NOT EXISTS `wallet_type` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `code`            VARCHAR(32)  NOT NULL COMMENT '钱包类型代码(如SPOT、FUND、CONTRACT)',
    `name`            VARCHAR(64)  NOT NULL COMMENT '钱包类型名称',
    `description`     VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `created_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='钱包类型表';

-- 用户资产表
CREATE TABLE IF NOT EXISTS `user_asset` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `currency_id`     BIGINT       NOT NULL COMMENT '币种ID',
    `wallet_type_id`  BIGINT       NOT NULL COMMENT '钱包类型ID',
    `available`       BIGINT       NOT NULL DEFAULT 0 COMMENT '可用余额(最小单位)',
    `frozen`          BIGINT       NOT NULL DEFAULT 0 COMMENT '冻结余额(最小单位)',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `created_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_currency_wallet` (`user_id`, `currency_id`, `wallet_type_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户资产表';

-- 资产划转记录表
CREATE TABLE IF NOT EXISTS `asset_transfer` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `transfer_no`       VARCHAR(64)  NOT NULL COMMENT '划转流水号',
    `user_id`           BIGINT       NOT NULL COMMENT '用户ID',
    `currency_id`       BIGINT       NOT NULL COMMENT '币种ID',
    `from_wallet_type`  BIGINT       NOT NULL COMMENT '源钱包类型ID',
    `to_wallet_type`    BIGINT       NOT NULL COMMENT '目标钱包类型ID',
    `amount`            BIGINT       NOT NULL COMMENT '划转金额(最小单位)',
    `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
    `remark`            VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transfer_no` (`transfer_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='资产划转记录表';

-- 初始化钱包类型数据
INSERT INTO `wallet_type` (`code`, `name`, `description`) VALUES
('SPOT', '现货钱包', '用于现货交易'),
('FUND', '资金钱包', '用于提币和理财'),
('CONTRACT', '合约钱包', '用于合约交易');

-- 初始化币种数据
INSERT INTO `currency` (`code`, `name`, `symbol`, `decimals`) VALUES
('BTC', '比特币', '₿', 8),
('ETH', '以太坊', 'Ξ', 8),
('USDT', '泰达币', '₮', 6);
