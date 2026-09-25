# 部署说明

部署流程会随着 API、客户端和数据库联调逐步完善。后端打包为 WAR，目标运行环境为 Apache Tomcat 9；管理端使用 Vite 构建，用户端使用 uni-app 构建。

生产环境必须使用外部密钥管理、HTTPS、权限受限的数据库账号、定期备份和明确配置的文件上传目录。仓库中不保存生产凭据或支付服务配置。

后端通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量读取数据库配置；JWT 签名密钥通过 `JWT_SECRET` 配置，长度至少 32 字节。管理端生产构建前通过 `VITE_API_BASE_URL` 指定可访问的 API 根地址（包含 `/api`）。
