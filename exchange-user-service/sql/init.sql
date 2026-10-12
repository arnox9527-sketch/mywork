-- ============================================
-- 交易所用户表
-- Database: exchange
-- ============================================

CREATE DATABASE IF NOT EXISTS `exchange`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `exchange`;

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `email`           VARCHAR(128) NOT NULL COMMENT '邮箱',
    `phone`           VARCHAR(32)  DEFAULT NULL COMMENT '手机号',
    `password`        VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    `nickname`        VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `created_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';
