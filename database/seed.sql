-- D-coffee 基础参考数据；建表脚本执行后可重复运行。
USE `d_coffee_db`;

START TRANSACTION;

INSERT INTO `dc_member_level`
    (`level_code`, `name`, `min_points`, `discount_rate`, `sort`, `status`)
VALUES
    ('STANDARD', '普通会员', 0, 1.0000, 10, 'ACTIVE'),
    ('SILVER', '银卡会员', 1000, 0.9800, 20, 'ACTIVE'),
    ('GOLD', '金卡会员', 5000, 0.9500, 30, 'ACTIVE')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`), `min_points` = VALUES(`min_points`),
    `discount_rate` = VALUES(`discount_rate`), `sort` = VALUES(`sort`),
    `status` = VALUES(`status`), `deleted` = 0;

INSERT INTO `dc_admin_role` (`role_code`, `name`, `description`, `status`)
VALUES ('ADMIN', '系统管理员', 'D-coffee 管理后台管理员', 'ACTIVE')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `description` = VALUES(`description`), `status` = VALUES(`status`), `deleted` = 0;

INSERT INTO `dc_category` (`category_code`, `name`, `sort`, `status`)
VALUES
    ('COFFEE', '咖啡', 10, 'ACTIVE'),
    ('NON_COFFEE', '非咖啡', 20, 'ACTIVE'),
    ('TEA', '茶饮', 30, 'ACTIVE'),
    ('DESSERT', '甜品', 40, 'ACTIVE'),
    ('SEASONAL', '季节限定', 50, 'ACTIVE')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `sort` = VALUES(`sort`), `status` = VALUES(`status`), `deleted` = 0;

INSERT INTO `dc_store`
    (`store_code`, `name`, `address`, `phone`, `open_time`, `close_time`, `status`, `sort`)
VALUES
    ('DCOFFEE_CENTER', 'D-coffee 中心店', '请在门店管理中补充实际地址', '请在门店管理中补充电话', '08:00:00', '22:00:00', 'CLOSED', 10)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `address` = VALUES(`address`), `phone` = VALUES(`phone`), `open_time` = VALUES(`open_time`), `close_time` = VALUES(`close_time`), `sort` = VALUES(`sort`), `deleted` = 0;

INSERT INTO `dc_system_config` (`config_key`, `config_value`, `description`, `is_secret`)
VALUES
    ('order.pickup_code_length', '4', '取餐码长度', 0),
    ('upload.max_size_bytes', '5242880', '图片上传大小上限 5 MB', 0),
    ('upload.allowed_extensions', 'jpg,jpeg,png,gif,webp', '允许的图片扩展名', 0)
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `description` = VALUES(`description`), `is_secret` = VALUES(`is_secret`), `deleted` = 0;

COMMIT;

-- 种子数据不包含管理员密码。启用 BCrypt 后，应通过安全的账号开通流程
-- 创建首位管理员；不要将真实密码提交到版本库。
