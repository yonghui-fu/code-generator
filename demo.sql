-- 示例数据库和表结构
-- 创建测试数据库
CREATE DATABASE IF NOT EXISTS test_db DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

USE test_db;

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

-- 订单表
CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `status` TINYINT DEFAULT 1 COMMENT '订单状态：1-待支付，2-已支付，3-已发货，4-已完成，5-已取消',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单表';

-- 商品表
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `description` TEXT COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否上架：1-上架，0-下架',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品表';

-- 分类表
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父分类ID',
  `level` TINYINT DEFAULT 1 COMMENT '层级',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品分类表';

-- 插入一些测试数据
INSERT INTO `user` (`username`, `email`, `phone`) VALUES
('admin', 'admin@example.com', '13800138000'),
('user1', 'user1@example.com', '13800138001'),
('user2', 'user2@example.com', '13800138002');

INSERT INTO `category` (`name`, `parent_id`, `level`) VALUES
('电子产品', NULL, 1),
('手机', 1, 2),
('电脑', 1, 2),
('家电', NULL, 1),
('空调', 4, 2);

INSERT INTO `product` (`name`, `description`, `price`, `stock`, `category_id`, `brand`) VALUES
('iPhone 14', '苹果手机', 6999.00, 100, 2, 'Apple'),
('MacBook Pro', '苹果笔记本', 12999.00, 50, 3, 'Apple'),
('格力空调', '家用空调', 2999.00, 30, 5, '格力');

INSERT INTO `order` (`order_no`, `user_id`, `product_name`, `quantity`, `price`, `total_amount`) VALUES
('ORD20231101001', 2, 'iPhone 14', 1, 6999.00, 6999.00),
('ORD20231101002', 3, 'MacBook Pro', 1, 12999.00, 12999.00);

COMMIT;