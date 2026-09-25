-- D-coffee MySQL 8 规范表结构。
-- 本脚本只增量建表，可重复执行，不会删除表或数据行。
-- 请确认当前连接的是目标数据库；本脚本不会迁移旧版表结构。

CREATE DATABASE IF NOT EXISTS `d_coffee_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `d_coffee_db`;

CREATE TABLE IF NOT EXISTS `dc_member_level` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `level_code` VARCHAR(32) NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    `min_points` INT UNSIGNED NOT NULL DEFAULT 0,
    `discount_rate` DECIMAL(5,4) NOT NULL DEFAULT 1.0000,
    `sort` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_member_level_code` (`level_code`),
    KEY `idx_dc_member_level_sort` (`status`, `sort`),
    CONSTRAINT `ck_dc_member_level_discount` CHECK (`discount_rate` > 0 AND `discount_rate` <= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员等级';

CREATE TABLE IF NOT EXISTS `dc_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `phone` VARCHAR(20) NOT NULL,
    `password_hash` VARCHAR(100) NOT NULL,
    `nickname` VARCHAR(64) NOT NULL DEFAULT '',
    `avatar_url` VARCHAR(500) DEFAULT NULL,
    `gender` TINYINT UNSIGNED NOT NULL DEFAULT 0,
    `member_level_id` BIGINT DEFAULT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_user_phone` (`phone`),
    KEY `idx_dc_user_status_created` (`status`, `create_time`),
    CONSTRAINT `fk_dc_user_member_level` FOREIGN KEY (`member_level_id`) REFERENCES `dc_member_level` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_user_gender` CHECK (`gender` IN (0, 1, 2)),
    CONSTRAINT `ck_dc_user_status` CHECK (`status` IN ('ACTIVE', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户账号';

CREATE TABLE IF NOT EXISTS `dc_user_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `recipient_name` VARCHAR(64) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `province` VARCHAR(64) NOT NULL DEFAULT '',
    `city` VARCHAR(64) NOT NULL DEFAULT '',
    `district` VARCHAR(64) NOT NULL DEFAULT '',
    `address_line` VARCHAR(255) NOT NULL,
    `is_default` TINYINT(1) NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dc_user_address_owner` (`user_id`, `deleted`, `is_default`),
    CONSTRAINT `fk_dc_user_address_user` FOREIGN KEY (`user_id`) REFERENCES `dc_user` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户地址';

CREATE TABLE IF NOT EXISTS `dc_user_points` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `balance` INT UNSIGNED NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_user_points_user` (`user_id`),
    CONSTRAINT `fk_dc_user_points_user` FOREIGN KEY (`user_id`) REFERENCES `dc_user` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户积分余额';

CREATE TABLE IF NOT EXISTS `dc_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_code` VARCHAR(48) NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    `icon_url` VARCHAR(500) DEFAULT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_category_code` (`category_code`),
    KEY `idx_dc_category_status_sort` (`status`, `sort`),
    CONSTRAINT `ck_dc_category_status` CHECK (`status` IN ('ACTIVE', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类';

CREATE TABLE IF NOT EXISTS `dc_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_id` BIGINT NOT NULL,
    `product_code` VARCHAR(48) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(1000) NOT NULL DEFAULT '',
    `image_url` VARCHAR(500) DEFAULT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `original_price` DECIMAL(10,2) DEFAULT NULL,
    `stock` INT UNSIGNED NOT NULL DEFAULT 0,
    `sales_count` INT UNSIGNED NOT NULL DEFAULT 0,
    `is_recommended` TINYINT(1) NOT NULL DEFAULT 0,
    `is_new` TINYINT(1) NOT NULL DEFAULT 0,
    `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    `sort` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_product_code` (`product_code`),
    KEY `idx_dc_product_category_status` (`category_id`, `status`, `sort`),
    KEY `idx_dc_product_status_created` (`status`, `create_time`),
    CONSTRAINT `fk_dc_product_category` FOREIGN KEY (`category_id`) REFERENCES `dc_category` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_product_price` CHECK (`price` >= 0 AND (`original_price` IS NULL OR `original_price` >= 0)),
    CONSTRAINT `ck_dc_product_status` CHECK (`status` IN ('DRAFT', 'ON_SALE', 'OFF_SALE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品';

CREATE TABLE IF NOT EXISTS `dc_product_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `option_code` VARCHAR(48) NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    `selection_type` VARCHAR(16) NOT NULL DEFAULT 'SINGLE',
    `is_required` TINYINT(1) NOT NULL DEFAULT 0,
    `min_select` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    `max_select` SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    `sort` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_product_option_code` (`product_id`, `option_code`),
    KEY `idx_dc_product_option_product_sort` (`product_id`, `status`, `sort`),
    CONSTRAINT `fk_dc_product_option_product` FOREIGN KEY (`product_id`) REFERENCES `dc_product` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_product_option_selection` CHECK (`selection_type` IN ('SINGLE', 'MULTIPLE')),
    CONSTRAINT `ck_dc_product_option_count` CHECK (`max_select` >= `min_select`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格组';

CREATE TABLE IF NOT EXISTS `dc_product_option_value` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `option_id` BIGINT NOT NULL,
    `value_code` VARCHAR(48) NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    `price_delta` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `sort` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_product_option_value_code` (`option_id`, `value_code`),
    KEY `idx_dc_product_option_value_sort` (`option_id`, `status`, `sort`),
    CONSTRAINT `fk_dc_product_option_value_option` FOREIGN KEY (`option_id`) REFERENCES `dc_product_option` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_product_option_value_delta` CHECK (`price_delta` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格选项值';

CREATE TABLE IF NOT EXISTS `dc_product_extra` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `extra_code` VARCHAR(48) NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `stock` INT UNSIGNED NOT NULL DEFAULT 0,
    `sort` INT NOT NULL DEFAULT 0,
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_product_extra_code` (`product_id`, `extra_code`),
    KEY `idx_dc_product_extra_product_status` (`product_id`, `status`, `sort`),
    CONSTRAINT `fk_dc_product_extra_product` FOREIGN KEY (`product_id`) REFERENCES `dc_product` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_product_extra_price` CHECK (`price` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品加料';

CREATE TABLE IF NOT EXISTS `dc_store` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `store_code` VARCHAR(48) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `address` VARCHAR(500) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `open_time` TIME DEFAULT NULL,
    `close_time` TIME DEFAULT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'CLOSED',
    `sort` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_store_code` (`store_code`),
    KEY `idx_dc_store_status_sort` (`status`, `sort`),
    CONSTRAINT `ck_dc_store_status` CHECK (`status` IN ('OPEN', 'CLOSED', 'SUSPENDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店';

CREATE TABLE IF NOT EXISTS `dc_store_business_hours` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `store_id` BIGINT NOT NULL,
    `weekday` TINYINT UNSIGNED NOT NULL,
    `open_time` TIME DEFAULT NULL,
    `close_time` TIME DEFAULT NULL,
    `is_closed` TINYINT(1) NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_store_business_hours_day` (`store_id`, `weekday`),
    CONSTRAINT `fk_dc_store_business_hours_store` FOREIGN KEY (`store_id`) REFERENCES `dc_store` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_store_business_hours_weekday` CHECK (`weekday` BETWEEN 1 AND 7)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店每周营业时间';

CREATE TABLE IF NOT EXISTS `dc_cart` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `store_id` BIGINT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dc_cart_user_store` (`user_id`, `store_id`, `deleted`),
    CONSTRAINT `fk_dc_cart_user` FOREIGN KEY (`user_id`) REFERENCES `dc_user` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_cart_store` FOREIGN KEY (`store_id`) REFERENCES `dc_store` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='按门店区分的用户购物车';

CREATE TABLE IF NOT EXISTS `dc_cart_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `cart_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `option_signature` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    `quantity` INT UNSIGNED NOT NULL DEFAULT 1,
    `remark` VARCHAR(255) NOT NULL DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_cart_item_variant` (`cart_id`, `product_id`, `option_signature`),
    KEY `idx_dc_cart_item_cart` (`cart_id`, `deleted`),
    CONSTRAINT `fk_dc_cart_item_cart` FOREIGN KEY (`cart_id`) REFERENCES `dc_cart` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_cart_item_product` FOREIGN KEY (`product_id`) REFERENCES `dc_product` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_cart_item_quantity` CHECK (`quantity` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='按商品规格区分的购物车明细';

CREATE TABLE IF NOT EXISTS `dc_cart_item_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `cart_item_id` BIGINT NOT NULL,
    `selection_type` VARCHAR(16) NOT NULL,
    `option_group_name` VARCHAR(64) NOT NULL DEFAULT '',
    `selected_name` VARCHAR(64) NOT NULL,
    `catalog_value_id` BIGINT DEFAULT NULL,
    `price_delta` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dc_cart_item_option_item` (`cart_item_id`, `deleted`),
    CONSTRAINT `fk_dc_cart_item_option_item` FOREIGN KEY (`cart_item_id`) REFERENCES `dc_cart_item` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_cart_item_option_type` CHECK (`selection_type` IN ('OPTION', 'EXTRA')),
    CONSTRAINT `ck_dc_cart_item_option_delta` CHECK (`price_delta` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车规格选择';

CREATE TABLE IF NOT EXISTS `dc_coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `coupon_code` VARCHAR(48) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `coupon_type` VARCHAR(16) NOT NULL,
    `threshold_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `discount_rate` DECIMAL(5,4) DEFAULT NULL,
    `total_count` INT UNSIGNED NOT NULL DEFAULT 0,
    `claimed_count` INT UNSIGNED NOT NULL DEFAULT 0,
    `per_user_limit` INT UNSIGNED NOT NULL DEFAULT 1,
    `valid_from` DATETIME DEFAULT NULL,
    `valid_until` DATETIME DEFAULT NULL,
    `valid_days_after_claim` SMALLINT UNSIGNED DEFAULT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'DISABLED',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_coupon_code` (`coupon_code`),
    KEY `idx_dc_coupon_status_validity` (`status`, `valid_from`, `valid_until`),
    CONSTRAINT `ck_dc_coupon_type` CHECK (`coupon_type` IN ('FIXED', 'PERCENT')),
    CONSTRAINT `ck_dc_coupon_amounts` CHECK (`threshold_amount` >= 0 AND `discount_amount` >= 0),
    CONSTRAINT `ck_dc_coupon_discount_rate` CHECK (`discount_rate` IS NULL OR (`discount_rate` > 0 AND `discount_rate` < 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券定义';

CREATE TABLE IF NOT EXISTS `dc_user_coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `coupon_id` BIGINT NOT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
    `claimed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `valid_from` DATETIME DEFAULT NULL,
    `valid_until` DATETIME DEFAULT NULL,
    `used_at` DATETIME DEFAULT NULL,
    `order_id` BIGINT DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dc_user_coupon_owner_status` (`user_id`, `status`, `valid_until`),
    KEY `idx_dc_user_coupon_coupon` (`coupon_id`, `status`),
    CONSTRAINT `fk_dc_user_coupon_user` FOREIGN KEY (`user_id`) REFERENCES `dc_user` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_user_coupon_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `dc_coupon` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_user_coupon_status` CHECK (`status` IN ('AVAILABLE', 'USED', 'EXPIRED', 'REVOKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户领取的优惠券';

CREATE TABLE IF NOT EXISTS `dc_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(40) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `store_id` BIGINT NOT NULL,
    `user_coupon_id` BIGINT DEFAULT NULL,
    `status` VARCHAR(24) NOT NULL DEFAULT 'PENDING_PAYMENT',
    `payment_status` VARCHAR(16) NOT NULL DEFAULT 'UNPAID',
    `total_amount` DECIMAL(10,2) NOT NULL,
    `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `pay_amount` DECIMAL(10,2) NOT NULL,
    `remark` VARCHAR(500) NOT NULL DEFAULT '',
    `pickup_code` VARCHAR(16) DEFAULT NULL,
    `paid_at` DATETIME DEFAULT NULL,
    `completed_at` DATETIME DEFAULT NULL,
    `canceled_at` DATETIME DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_order_no` (`order_no`),
    KEY `idx_dc_order_user_status_time` (`user_id`, `status`, `create_time`),
    KEY `idx_dc_order_store_status_time` (`store_id`, `status`, `create_time`),
    KEY `idx_dc_order_pickup_code` (`store_id`, `pickup_code`),
    CONSTRAINT `fk_dc_order_user` FOREIGN KEY (`user_id`) REFERENCES `dc_user` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_order_store` FOREIGN KEY (`store_id`) REFERENCES `dc_store` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_order_user_coupon` FOREIGN KEY (`user_coupon_id`) REFERENCES `dc_user_coupon` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_order_status` CHECK (`status` IN ('PENDING_PAYMENT', 'PAID', 'MAKING', 'READY', 'COMPLETED', 'CANCELED', 'REFUNDING', 'REFUNDED')),
    CONSTRAINT `ck_dc_order_payment_status` CHECK (`payment_status` IN ('UNPAID', 'PAID', 'REFUND_PENDING', 'REFUNDED')),
    CONSTRAINT `ck_dc_order_amounts` CHECK (`total_amount` >= 0 AND `discount_amount` >= 0 AND `pay_amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户订单与不可变金额快照';

CREATE TABLE IF NOT EXISTS `dc_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name_snapshot` VARCHAR(100) NOT NULL,
    `product_image_snapshot` VARCHAR(500) DEFAULT NULL,
    `base_price` DECIMAL(10,2) NOT NULL,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `quantity` INT UNSIGNED NOT NULL,
    `subtotal` DECIMAL(10,2) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dc_order_item_order` (`order_id`, `id`),
    KEY `idx_dc_order_item_product` (`product_id`),
    CONSTRAINT `fk_dc_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `dc_order` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `dc_product` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_order_item_amounts` CHECK (`base_price` >= 0 AND `unit_price` >= 0 AND `quantity` > 0 AND `subtotal` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单历史中的商品快照';

CREATE TABLE IF NOT EXISTS `dc_order_item_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_item_id` BIGINT NOT NULL,
    `selection_type` VARCHAR(16) NOT NULL,
    `option_group_snapshot` VARCHAR(64) NOT NULL DEFAULT '',
    `selected_name_snapshot` VARCHAR(64) NOT NULL,
    `price_delta` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dc_order_item_option_item` (`order_item_id`),
    CONSTRAINT `fk_dc_order_item_option_item` FOREIGN KEY (`order_item_id`) REFERENCES `dc_order_item` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_order_item_option_type` CHECK (`selection_type` IN ('OPTION', 'EXTRA')),
    CONSTRAINT `ck_dc_order_item_option_delta` CHECK (`price_delta` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单规格快照';

CREATE TABLE IF NOT EXISTS `dc_points_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `order_id` BIGINT DEFAULT NULL,
    `change_amount` INT NOT NULL,
    `balance_after` INT UNSIGNED NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dc_points_record_user_time` (`user_id`, `create_time`),
    KEY `idx_dc_points_record_order` (`order_id`),
    CONSTRAINT `fk_dc_points_record_user` FOREIGN KEY (`user_id`) REFERENCES `dc_user` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_points_record_order` FOREIGN KEY (`order_id`) REFERENCES `dc_order` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_points_record_change` CHECK (`change_amount` <> 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='可审计的积分变更记录';

CREATE TABLE IF NOT EXISTS `dc_admin_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_code` VARCHAR(32) NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    `description` VARCHAR(255) NOT NULL DEFAULT '',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_admin_role_code` (`role_code`),
    CONSTRAINT `ck_dc_admin_role_status` CHECK (`status` IN ('ACTIVE', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员角色';

CREATE TABLE IF NOT EXISTS `dc_admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(64) NOT NULL,
    `password_hash` VARCHAR(100) NOT NULL,
    `display_name` VARCHAR(64) NOT NULL,
    `role_id` BIGINT NOT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `last_login_at` DATETIME DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_admin_username` (`username`),
    KEY `idx_dc_admin_role_status` (`role_id`, `status`),
    CONSTRAINT `fk_dc_admin_role` FOREIGN KEY (`role_id`) REFERENCES `dc_admin_role` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_admin_status` CHECK (`status` IN ('ACTIVE', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员账号';

CREATE TABLE IF NOT EXISTS `dc_inventory_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `admin_id` BIGINT NOT NULL,
    `quantity_delta` INT NOT NULL,
    `stock_before` INT UNSIGNED NOT NULL,
    `stock_after` INT UNSIGNED NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dc_inventory_log_product_time` (`product_id`, `create_time`),
    KEY `idx_dc_inventory_log_admin_time` (`admin_id`, `create_time`),
    CONSTRAINT `fk_dc_inventory_log_product` FOREIGN KEY (`product_id`) REFERENCES `dc_product` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_inventory_log_admin` FOREIGN KEY (`admin_id`) REFERENCES `dc_admin` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品库存调整记录';

CREATE TABLE IF NOT EXISTS `dc_system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL,
    `config_value` TEXT DEFAULT NULL,
    `description` VARCHAR(255) NOT NULL DEFAULT '',
    `is_secret` TINYINT(1) NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dc_system_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置；密钥不得返回客户端';

CREATE TABLE IF NOT EXISTS `dc_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `admin_id` BIGINT DEFAULT NULL,
    `operation` VARCHAR(100) NOT NULL,
    `http_method` VARCHAR(10) NOT NULL DEFAULT '',
    `request_path` VARCHAR(500) NOT NULL DEFAULT '',
    `result_code` VARCHAR(32) NOT NULL DEFAULT '',
    `duration_ms` INT UNSIGNED NOT NULL DEFAULT 0,
    `request_summary` VARCHAR(1000) NOT NULL DEFAULT '',
    `client_ip` VARCHAR(45) NOT NULL DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_dc_operation_log_admin_time` (`admin_id`, `create_time`),
    KEY `idx_dc_operation_log_operation_time` (`operation`, `create_time`),
    CONSTRAINT `fk_dc_operation_log_admin` FOREIGN KEY (`admin_id`) REFERENCES `dc_admin` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员审计日志；禁止记录密码或令牌';
