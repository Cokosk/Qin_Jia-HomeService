-- =====================================================
-- 家政服务平台数据库建表脚本
-- home_serve 数据库
-- =====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS home_serve 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE home_serve;

-- =====================================================
-- 用户表
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` INT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户 1-服务者 2-管理员',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `credit_score` INT NOT NULL DEFAULT 100 COMMENT '信用分 (0-200)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`),
    KEY `idx_credit_score` (`credit_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 服务分类表
-- =====================================================
DROP TABLE IF EXISTS `service_category`;
CREATE TABLE `service_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务分类表';

-- =====================================================
-- 服务项目表
-- =====================================================
DROP TABLE IF EXISTS `service_item`;
CREATE TABLE `service_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '服务描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `duration` INT DEFAULT NULL COMMENT '预计服务时长（分钟）',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '服务图片',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';

-- =====================================================
-- 订单表
-- =====================================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `worker_id` BIGINT DEFAULT NULL COMMENT '服务者ID',
    `service_id` BIGINT NOT NULL COMMENT '服务ID',
    `service_name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `price` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `appointment_time` DATETIME DEFAULT NULL COMMENT '预约时间',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '服务地址',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-待抢单 1-已接单 2-服务中 3-已完成 4-已取消',
    `grab_time` DATETIME DEFAULT NULL COMMENT '抢单时间',
    `start_time` DATETIME DEFAULT NULL COMMENT '服务开始时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '服务完成时间',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `pay_status` INT NOT NULL DEFAULT 0 COMMENT '支付状态: 0-未支付 1-已支付',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_worker_id` (`worker_id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_status` (`status`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =====================================================
-- 支付记录表
-- =====================================================
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `payment_no` VARCHAR(50) NOT NULL COMMENT '支付流水号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `pay_method` INT NOT NULL COMMENT '支付方式: 1-微信 2-支付宝 3-余额',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-待支付 1-已支付 2-已退款',
    `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付流水号',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
    `refund_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '退款金额',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_pay_method` (`pay_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- =====================================================
-- 评价表
-- =====================================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `worker_id` BIGINT NOT NULL COMMENT '服务者ID',
    `service_id` BIGINT NOT NULL COMMENT '服务ID',
    `rating` INT NOT NULL COMMENT '评分: 1-5分',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `images` VARCHAR(1000) DEFAULT NULL COMMENT '评价图片（逗号分隔）',
    `anonymous` INT NOT NULL DEFAULT 0 COMMENT '是否匿名: 0-否 1-是',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0-隐藏 1-显示',
    `reply` VARCHAR(500) DEFAULT NULL COMMENT '商家回复',
    `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_worker_id` (`worker_id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_rating` (`rating`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化服务分类
INSERT INTO `service_category` (`name`, `icon`, `sort`, `status`) VALUES
('家政保洁', 'icon-baojie', 1, 1),
('家电维修', 'icon-weixiu', 2, 1),
('水电维修', 'icon-shuidian', 3, 1),
('搬家服务', 'icon-banjia', 4, 1),
('管道疏通', 'icon-guan', 5, 1),
('空调服务', 'icon-kongtiao', 6, 1);

-- 初始化管理员账号（密码: admin123，BCrypt加密）
-- BCrypt hash for 'admin123': $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `credit_score`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHsM8lE9lBOsl7i', '系统管理员', 2, 1, 200);

-- 初始化测试用户（密码: test123）
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `role`, `status`, `credit_score`) VALUES
('test_user', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWrgVnXMHM8lE9lBOsl7iAt6Z5EHsM8lE', '测试用户', '13800138001', 0, 1, 100),
('test_worker', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWrgVnXMHM8lE9lBOsl7iAt6Z5EHsM8lE', '测试服务者', '13800138002', 1, 1, 100);

-- 初始化服务项目
INSERT INTO `service_item` (`category_id`, `name`, `description`, `price`, `duration`, `status`) VALUES
(1, '日常保洁', '日常家庭清洁服务', 80.00, 120, 1),
(1, '深度保洁', '全屋深度清洁消毒', 200.00, 240, 1),
(2, '空调维修', '家用空调故障检修', 150.00, 60, 1),
(2, '冰箱维修', '冰箱故障检修维护', 120.00, 60, 1),
(3, '水管维修', '水管漏水检测维修', 100.00, 90, 1),
(3, '电路检修', '家庭电路安全检查', 80.00, 60, 1);

