-- 家政服务平台数据库脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS home_serve DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE home_serve;

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) COMMENT '手机号',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-用户 1-服务者 2-管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `credit_score` INT DEFAULT 100 COMMENT '信用分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 服务分类表
-- =============================================
CREATE TABLE IF NOT EXISTS `service_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务分类表';

-- =============================================
-- 服务项目表
-- =============================================
CREATE TABLE IF NOT EXISTS `service_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `description` TEXT COMMENT '服务描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `duration` INT COMMENT '服务时长(分钟)',
    `image` VARCHAR(255) COMMENT '图片',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';

-- =============================================
-- 订单表
-- =============================================
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `worker_id` BIGINT COMMENT '服务者ID',
    `service_id` BIGINT NOT NULL COMMENT '服务ID',
    `service_name` VARCHAR(100) COMMENT '服务名称',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `appointment_time` DATETIME NOT NULL COMMENT '预约时间',
    `address` VARCHAR(255) NOT NULL COMMENT '地址',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `remark` VARCHAR(500) COMMENT '备注',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待抢单 1-已接单 2-服务中 3-已完成 4-已取消',
    `grab_time` DATETIME COMMENT '抢单时间',
    `start_time` DATETIME COMMENT '开始服务时间',
    `finish_time` DATETIME COMMENT '完成时间',
    `pay_time` DATETIME COMMENT '支付时间',
    `pay_status` TINYINT DEFAULT 0 COMMENT '支付状态: 0-未支付 1-已支付',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_worker_id` (`worker_id`),
    KEY `idx_status` (`status`),
    KEY `idx_appointment_time` (`appointment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =============================================
-- 评价表
-- =============================================
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `worker_id` BIGINT NOT NULL COMMENT '服务者ID',
    `rating` TINYINT NOT NULL COMMENT '评分1-5',
    `comment` TEXT COMMENT '评价内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_worker_id` (`worker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- =============================================
-- 初始化测试数据
-- =============================================
-- 插入用户 (密码: 123456)
INSERT INTO `user` (`username`, `password`, `phone`, `nickname`, `role`) VALUES
('test_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138000', '测试用户', 0),
('test_worker', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13900139000', '测试服务者', 1);

-- 插入服务分类
INSERT INTO `service_category` (`name`, `icon`, `sort`) VALUES
('保洁服务', 'cleaning', 1),
('家电清洗', 'appliance', 2),
('月嫂保姆', 'nanny', 3),
('维修服务', 'repair', 4);

-- 插入服务项目
INSERT INTO `service_item` (`category_id`, `name`, `description`, `price`, `duration`, `status`) VALUES
(1, '日常保洁', '日常打扫除尘', 99.00, 120, 1),
(1, '深度清洁', '全屋深度清洁', 299.00, 240, 1),
(2, '空调清洗', '挂机空调深度清洗', 80.00, 60, 1),
(2, '油烟机清洗', '油烟机拆洗', 150.00, 90, 1),
(3, '月嫂', '专业月嫂服务', 12000.00, 7200, 1),
(3, '保姆', '住家保姆服务', 5000.00, 7200, 1);