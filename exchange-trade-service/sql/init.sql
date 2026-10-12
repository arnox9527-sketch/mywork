-- ============================================
-- 交易订单表
-- Database: exchange
-- ============================================

USE `exchange`;

CREATE TABLE IF NOT EXISTS `trade_order` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id`          VARCHAR(64)  NOT NULL COMMENT '订单ID',
    `user_id`           BIGINT       NOT NULL COMMENT '用户ID',
    `symbol`            VARCHAR(32)  NOT NULL COMMENT '交易对(如BTC-USDT)',
    `product_type`      VARCHAR(16)  NOT NULL COMMENT '产品类型: SPOT/PERP/FUTURE',
    `side`              VARCHAR(8)   NOT NULL COMMENT '方向: BUY/SELL',
    `order_type`        VARCHAR(16)  NOT NULL COMMENT '订单类型: LIMIT/MARKET/IOC/FOK/POST_ONLY',
    `price`             BIGINT       DEFAULT NULL COMMENT '价格(最小单位)',
    `quantity`          BIGINT       NOT NULL COMMENT '数量(最小单位)',
    `filled_quantity`   BIGINT       NOT NULL DEFAULT 0 COMMENT '已成交数量',
    `filled_amount`     BIGINT       NOT NULL DEFAULT 0 COMMENT '已成交金额',
    `status`            VARCHAR(16)  NOT NULL COMMENT '状态: RECEIVED/OPEN/PARTIALLY_FILLED/FILLED/CANCELLED/REJECTED',
    `frozen_amount`     BIGINT       NOT NULL DEFAULT 0 COMMENT '冻结金额',
    `remark`            VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `created_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_symbol` (`symbol`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='交易订单表';
