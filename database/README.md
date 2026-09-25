# 数据库脚本说明

- `schema.sql`：创建规范的 `dc_` 前缀表结构，不会删除现有数据。
- `seed.sql`：写入非敏感基础参考数据，可重复执行。
- `../db/init.sql`：保留的旧脚本，仅供历史查阅；会删除表且表名不符合现行规范，禁止对含有业务数据的数据库执行。

## 首次初始化

```sql
SOURCE database/schema.sql;
SOURCE database/seed.sql;
```

## 已有数据库升级

如果数据库此前已经按旧版本初始化，需要在 `dc_order`、`dc_order_item` 等订单表已存在的前提下，先执行订单库存预占迁移：

```sql
SOURCE database/migrations/2026-09-25-order-stock-reservation.sql;
```

该表用于记录下单时扣减的商品/加料库存，并保证取消未支付订单时只回补一次。迁移不会删除或重建业务数据；在执行前仍应按日常运维要求备份数据库。

脚本不会写入真实管理员密码。应在 BCrypt 配置完成后，通过安全的账号开通流程创建首个管理员并保存密码摘要。

项目后端提供本机交互式开通工具。确认数据库已初始化后，在后端目录运行：

```powershell
cd d-coffee-backend
.\mvnw.cmd -DskipTests compile exec:java "-Dexec.mainClass=com.dcoffee.tools.AdminProvisioningCli"
```

工具会从后端数据库配置读取连接信息，并在终端中隐藏密码输入。它只允许创建首位管理员；如果数据库已有管理员会拒绝执行。请使用可信的本机交互式终端，不要将密码写入命令参数或脚本。

## 金额与删除约定

金额列统一使用 `DECIMAL(10,2)`。应用代码使用 `BigDecimal`，从商品目录计算金额，不能信任客户端提交的最终金额。表中保留 `deleted` 逻辑删除标记，业务删除不直接物理删行。
