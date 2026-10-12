-- =============================================================
-- 麦当劳套餐推荐助手 - 表结构
-- MySQL 8.0+   字符集 utf8mb4
-- =============================================================
CREATE DATABASE IF NOT EXISTS `Mdangdang`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `Mdangdang`;

-- -------------------------------------------------------------
-- 1. 单品表（汉堡 / 小食 / 饮料 / 甜品 / 早餐）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mcd_product`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(64)  NOT NULL COMMENT '单品名称',
    `category`    VARCHAR(32)  NOT NULL COMMENT '分类：HAMBURGER/SIDE/DRINK/DESSERT/BREAKFAST',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '单点价格（元）',
    `calories`    INT          DEFAULT 0 COMMENT '热量（kcal）',
    `spicy_level` TINYINT      DEFAULT 0 COMMENT '辣度 0-3',
    `taste_tags`  VARCHAR(128) DEFAULT NULL COMMENT '口味标签，逗号分隔：经典,香辣,清淡',
    `image_url`   VARCHAR(255) DEFAULT NULL COMMENT '图片地址',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态 1上架 0下架',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='单品表';

-- -------------------------------------------------------------
-- 2. 套餐表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mcd_combo`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code`           VARCHAR(32)  NOT NULL COMMENT '套餐编码，唯一',
    `name`           VARCHAR(64)  NOT NULL COMMENT '套餐名称',
    `category`       VARCHAR(32)  NOT NULL COMMENT '分类：CLASSIC/SPICY/VALUE/FAMILY/BREAKFAST/TEA/DESSERT',
    `price`          DECIMAL(10, 2) NOT NULL COMMENT '套餐价（元）',
    `original_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '单点合计原价（元）',
    `people_count`   TINYINT      DEFAULT 1 COMMENT '建议就餐人数',
    `calories`       INT          DEFAULT 0 COMMENT '套餐总热量（kcal）',
    `spicy_level`    TINYINT      DEFAULT 0 COMMENT '辣度 0-3',
    `taste_tags`     VARCHAR(128) DEFAULT NULL COMMENT '口味标签，逗号分隔',
    `time_slots`     VARCHAR(128) DEFAULT 'ALL_DAY' COMMENT '适用时段，逗号分隔：BREAKFAST,LUNCH,DINNER,TEA,LATE_NIGHT,ALL_DAY',
    `tags`           VARCHAR(128) DEFAULT NULL COMMENT '营销标签：超值,人气,新品',
    `sales_count`    INT          DEFAULT 0 COMMENT '累计销量（用于人气打分）',
    `rating`         DECIMAL(3, 1) DEFAULT 5.0 COMMENT '评分 0-5',
    `description`    VARCHAR(255) DEFAULT NULL COMMENT '套餐描述',
    `image_url`      VARCHAR(255) DEFAULT NULL COMMENT '图片地址',
    `status`         TINYINT      DEFAULT 1 COMMENT '状态 1上架 0下架',
    `deleted`        TINYINT      DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_category` (`category`),
    KEY `idx_price` (`price`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='套餐表';

-- -------------------------------------------------------------
-- 3. 套餐明细表（套餐包含哪些单品）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mcd_combo_item`
(
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `combo_id`    BIGINT   NOT NULL COMMENT '套餐ID',
    `product_id`  BIGINT   NOT NULL COMMENT '单品ID',
    `quantity`    INT      DEFAULT 1 COMMENT '数量',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_combo_id` (`combo_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='套餐明细表';

-- -------------------------------------------------------------
-- 4. 用户偏好表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mcd_preference`
(
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`            VARCHAR(64)  NOT NULL COMMENT '用户标识',
    `budget_min`         DECIMAL(10, 2) DEFAULT NULL COMMENT '预算下限',
    `budget_max`         DECIMAL(10, 2) DEFAULT NULL COMMENT '预算上限',
    `taste_tags`         VARCHAR(128) DEFAULT NULL COMMENT '偏好口味，逗号分隔',
    `avoid_tags`         VARCHAR(128) DEFAULT NULL COMMENT '忌口标签，逗号分隔',
    `people_count`       TINYINT      DEFAULT 1 COMMENT '就餐人数',
    `time_slot`          VARCHAR(32)  DEFAULT NULL COMMENT '常用时段',
    `preferred_category` VARCHAR(32)  DEFAULT NULL COMMENT '偏好的套餐分类',
    `remark`             VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `deleted`            TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    `create_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户偏好表';

-- -------------------------------------------------------------
-- 5. 用户表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mcd_user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(64)  NOT NULL COMMENT '登录用户名',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码（BCrypt 哈希，不可逆）',
    `nickname`    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`       VARCHAR(64)  DEFAULT NULL COMMENT '邮箱',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `role`        VARCHAR(32)  DEFAULT 'USER' COMMENT '角色：ADMIN 管理员 / USER 普通用户',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态 1正常 0禁用',
    `last_login`  DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- -------------------------------------------------------------
-- 6. 推荐记录表（留痕，便于复盘与比赛演示）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mcd_recommend_record`
(
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       VARCHAR(64)  DEFAULT NULL COMMENT '用户标识',
    `request_json`  TEXT COMMENT '请求参数快照',
    `result_json`   TEXT COMMENT '推荐结果快照',
    `top_combo_id`  BIGINT   DEFAULT NULL COMMENT 'Top1 套餐ID',
    `top_score`     DECIMAL(6, 2) DEFAULT NULL COMMENT 'Top1 得分',
    `cost_ms`       BIGINT   DEFAULT NULL COMMENT '耗时（毫秒）',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='推荐记录表';
