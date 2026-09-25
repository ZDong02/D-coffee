# D-coffee

D-coffee 是一个分阶段开发的咖啡点单系统。当前包含 uni-app 用户端、Vue 管理后台和传统 Spring MVC + MyBatis 后端，业务能力按开发计划逐步接入。

## 项目目录

| 路径 | 用途 | 技术栈 |
| --- | --- | --- |
| `d-coffee-front/` | 用户端 | uni-app、Vue 3、JavaScript、Vite、Pinia、Sass |
| `d-coffee-backend/` | REST API | Java 17、Spring MVC、MyBatis、Maven WAR |
| `d-coffee-admin/` | 管理后台 | Vue 3、JavaScript、Vite、Pinia、Element Plus |
| `database/` | 数据库脚本 | MySQL 8 |
| `docs/` | 项目文档 | Markdown |

为避免移动现有文件，用户端和后端继续使用仓库中已有的 `d-coffee-front/` 与 `d-coffee-backend/` 目录名。旧脚本 `db/init.sql` 仅保留查阅，不应对已有数据的数据库执行。

## 环境要求

- Node.js 20.19+ 或 22.12+，以及 npm。
- JDK 17；后端提供 Maven Wrapper。
- Apache Tomcat 9，用于部署 WAR。
- MySQL 8；正式建库脚本为 `database/schema.sql` 和 `database/seed.sql`。

## 启动用户端

```powershell
cd d-coffee-front
npm install
npm run dev:h5
```

如需开发微信小程序，请先在 `manifest.json` 配置小程序 `appid`，再运行 `npm run dev:mp-weixin` 并用微信开发者工具打开生成目录。

## 启动管理后台

```powershell
cd d-coffee-admin
npm install
npm run dev
```

开发服务器默认通过 `/api` 代理到当前 IntelliJ Tomcat 的 `d_coffee_backend_war_exploded` 应用上下文。可复制 `.env.example` 为 `.env.local`，上下文不同则调整 `VITE_API_CONTEXT`。登录需要数据库中已配置有效管理员及 BCrypt 密码摘要。

## 构建后端

```powershell
cd d-coffee-backend
.\mvnw.cmd clean package
```

将 `target/d-coffee-server.war` 部署到 Tomcat 9。数据库连接可通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量覆盖，敏感凭据不要提交到版本库。API servlet 挂载路径为 `/api/*`。

## 当前功能

后端已包含管理员登录、商品与分类读取、商品管理及库存调整 API；管理端包含登录和商品管理页面。用户端已接入门店、购物车、下单、订单查看和未支付取消；支付服务尚未接入。已有数据库执行 `database/migrations/2026-09-25-order-stock-reservation.sql` 后才可使用订单库存预占与回补。阶段记录见[开发进度](docs/development.md)，技术边界见[系统架构](docs/architecture.md)。
