# 数据库说明

目标数据库为 MySQL 8，使用 InnoDB 和 utf8mb4。业务表统一使用 `dc_` 前缀、BIGINT 主键，以及 `create_time`、`update_time`、`deleted` 字段。金额采用 SQL `DECIMAL(10,2)`，Java 中使用 `BigDecimal`。

仓库保留了旧初始化文件 `db/init.sql` 供历史查阅。该脚本会删除表、表名不符合现行规范且数据覆盖不完整，不要对包含数据的数据库执行。当前规范脚本为[建表脚本](../database/schema.sql)和[基础数据脚本](../database/seed.sql)。升级已有数据库时避免删除重建，以免丢失业务数据。

管理员密码不写入种子脚本。管理员记录必须保存 BCrypt 密码摘要，不要保存明文密码。
