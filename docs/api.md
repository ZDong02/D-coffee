# API 接口

所有接口统一返回 JSON 包装结构，API servlet 根路径为 `/api`：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 健康检查、管理员登录与商品目录

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/health` | 公开 | 服务存活检查 |
| POST | `/api/auth/admin/login` | 公开 | 管理员账号密码登录，返回 JWT |
| POST | `/api/user/register` | 公开 | 用户手机号与密码注册，成功后返回用户 JWT |
| POST | `/api/user/login` | 公开 | 用户手机号与密码登录，返回用户 JWT |
| GET | `/api/stores` | 公开 | 查询未删除且未停用的门店；营业中门店优先，未营业门店可预选 |
| GET | `/api/user/cart?storeId={id}` | 用户 | 查询本人指定门店的购物车及后端计算金额 |
| POST | `/api/user/cart/items` | 用户 | 校验商品规格/加料后加入购物车 |
| PATCH | `/api/user/cart/items/{itemId}?storeId={id}` | 用户 | 更新本人购物车商品数量（1–99） |
| DELETE | `/api/user/cart/items/{itemId}?storeId={id}` | 用户 | 逻辑移除本人购物车商品 |
| GET | `/api/categories` | 公开 | 按排序返回启用分类 |
| GET | `/api/products` | 公开 | 分页读取在售商品；可选 `categoryId`、`keyword`、`recommended`、`page`、`pageSize` |
| GET | `/api/products/{id}` | 公开 | 商品详情，包含有效规格、规格值和加料 |
| GET | `/api/admin/products` | 管理员 | 分页读取所有状态商品；可选 `categoryId`、`keyword`、`status`、`page`、`pageSize` |
| GET | `/api/admin/products/{id}` | 管理员 | 后台商品详情 |
| POST | `/api/admin/products` | 管理员 | 新增商品，初始状态为 `DRAFT` |
| PUT | `/api/admin/products/{id}` | 管理员 | 更新商品字段，不修改库存 |
| PUT | `/api/admin/products/{id}/configuration` | 管理员 | 替换商品启用的规格组、规格值和加料 |
| PATCH | `/api/admin/products/{id}/status` | 管理员 | 状态设为 `DRAFT`、`ON_SALE` 或 `OFF_SALE` |
| PATCH | `/api/admin/products/{id}/inventory` | 管理员 | 按正负 `delta` 调整库存并记录原因 |

登录请求示例：`{ "username": "管理员账号", "password": "登录密码" }`。成功数据含 `id`、`username`、`displayName`、`role` 和 `token`。需通过 `Authorization: Bearer <token>` 调用管理员接口。数据库中必须预先存在启用的管理员及其 BCrypt 密码摘要；初始化脚本不会创建管理员账号。

用户注册字段为 `phone`、`password` 和可选 `nickname`，当前密码至少 8 个字符且 UTF-8 编码不能超过 72 字节。注册和登录成功数据含 `id`、`phone`、`nickname`、`role` 和 `token`。用户端在后续受保护请求中使用 `Authorization: Bearer <token>`。目前没有短信服务商，注册流程尚未验证手机号，只适用于本机开发测试；正式开放前必须接入短信验证码或其他手机号验证与防滥用措施。

购物车按已登录用户和门店分别保存。加入商品请求含 `storeId`、`productId`、`options: [{ optionId, valueIds }]` 和 `extraIds`；服务端重新读取商品规格、价格和加料库存，不信任客户端金额。相同商品及相同规格会合并数量。购物车只保存配置快照和选择，当前尚未实现下单/库存预占/支付；未营业门店只允许预选和存购物车，不能结算。

分页数据结构含 `records`、`total`、`page` 和 `pageSize`。金额使用 MySQL `DECIMAL(10,2)`，Java 中以 `BigDecimal` 处理。每页数量限制在 1–100。公开商品接口不会返回草稿或下架商品；详情只返回有效规格/规格值/加料。

新增商品字段：`categoryId`、`productCode`、`name`、`description`、`imageUrl`、`price`、`originalPrice`、`stock`、`recommended`、`isNew`、`sort`。编辑接口字段相同，但不包含 `stock`；库存只能通过审计接口调整。状态请求示例：`{ "status": "ON_SALE" }`。库存请求示例：`{ "delta": 12, "reason": "到货入库" }`；负数表示扣减，结果不能低于 0。

规格与加料保存请求示例：

```json
{
  "options": [
    {
      "code": "SIZE",
      "name": "杯型",
      "selectionType": "SINGLE",
      "required": true,
      "minSelect": 1,
      "maxSelect": 1,
      "sort": 0,
      "values": [
        { "code": "M", "name": "中杯", "priceDelta": 0, "sort": 0 },
        { "code": "L", "name": "大杯", "priceDelta": 3, "sort": 1 }
      ]
    }
  ],
  "extras": [
    { "code": "SHOT", "name": "浓缩咖啡液", "price": 4, "stock": 20, "sort": 0 }
  ]
}
```

保存操作会软停用本次请求中未出现的旧配置；商品详情接口只返回状态为启用且未删除的配置。

## 本地调用

用户端 H5 与管理端开发服务器默认将 `/api` 代理到 Tomcat 的 `/d_coffee_backend_war_exploded/api`，避免浏览器跨域。IntelliJ IDEA 改变应用上下文时，可在两个项目各自的 `.env.local` 中设置 `VITE_API_CONTEXT`。客户端可通过 `VITE_API_BASE_URL` 设置 API 根地址；微信小程序及生产构建应配置可访问的完整地址（例如 `https://域名/d_coffee_backend_war_exploded/api`）。若接口确实没有分类或商品，客户端显示真实空状态，不填充演示数据。
