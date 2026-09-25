-- 为已建库增加订单库存预占记录；表已存在时可安全重复执行。
CREATE TABLE IF NOT EXISTS `dc_order_stock_reservation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `order_item_id` BIGINT NOT NULL,
    `reservation_type` VARCHAR(16) NOT NULL,
    `product_id` BIGINT DEFAULT NULL,
    `extra_id` BIGINT DEFAULT NULL,
    `quantity` INT UNSIGNED NOT NULL,
    `released` TINYINT(1) NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_dc_order_reservation_order` (`order_id`, `released`),
    CONSTRAINT `fk_dc_order_reservation_order` FOREIGN KEY (`order_id`) REFERENCES `dc_order` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_order_reservation_item` FOREIGN KEY (`order_item_id`) REFERENCES `dc_order_item` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_order_reservation_product` FOREIGN KEY (`product_id`) REFERENCES `dc_product` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `fk_dc_order_reservation_extra` FOREIGN KEY (`extra_id`) REFERENCES `dc_product_extra` (`id`)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT `ck_dc_order_reservation_type` CHECK (
        (`reservation_type` = 'PRODUCT' AND `product_id` IS NOT NULL AND `extra_id` IS NULL)
        OR (`reservation_type` = 'EXTRA' AND `product_id` IS NULL AND `extra_id` IS NOT NULL)
    ),
    CONSTRAINT `ck_dc_order_reservation_quantity` CHECK (`quantity` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单库存预占与取消回补记录';
