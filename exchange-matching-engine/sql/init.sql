-- ============================================
-- 交易对配置表
-- Database: exchange
-- ============================================

USE `exchange`;

CREATE TABLE IF NOT EXISTS `symbol_config` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `symbol`          VARCHAR(32)  NOT NULL COMMENT '交易对名称(如BTC-USDT)',
    `base_asset`      VARCHAR(16)  NOT NULL COMMENT '基础货币(如BTC)',
    `quote_asset`     VARCHAR(16)  NOT NULL COMMENT '计价货币(如USDT)',
    `product_type`    VARCHAR(16)  NOT NULL COMMENT '产品类型: SPOT/PERP/FUTURE',
    `price_scale`     INT          NOT NULL DEFAULT 2 COMMENT '价格精度(小数位数)',
    `quantity_scale`  INT          NOT NULL DEFAULT 8 COMMENT '数量精度(小数位数)',
    `min_quantity`    BIGINT       NOT NULL DEFAULT 0 COMMENT '最小下单数量(最小单位)',
    `max_quantity`    BIGINT       NOT NULL DEFAULT 0 COMMENT '最大下单数量(最小单位)',
    `tick_size`       BIGINT       NOT NULL DEFAULT 1 COMMENT '最小价格变动(最小单位)',
    `enabled`         TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `created_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_symbol` (`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='交易对配置表';

-- 初始化交易对数据
INSERT INTO `symbol_config` (`symbol`, `base_asset`, `quote_asset`, `product_type`, `price_scale`, `quantity_scale`, `min_quantity`, `max_quantity`, `tick_size`, `enabled`) VALUES
('BTC-USDT', 'BTC', 'USDT', 'SPOT', 2, 8, 10000, 1000000000, 1, 1),
('ETH-USDT', 'ETH', 'USDT', 'SPOT', 2, 6, 100000, 100000000, 1, 1),
('BTC-USD-PERP', 'BTC', 'USD', 'PERP', 1, 0, 1, 1000, 0, 1);
