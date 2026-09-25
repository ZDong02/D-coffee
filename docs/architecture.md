# 系统架构

## 目标架构

系统由 uni-app 用户端、Vue 管理端、Java SSM REST API 与 MySQL 8 组成。两个客户端通过 API 访问业务数据；鉴权、订单状态、价格计算和库存事务均由后端负责。

```text
uni-app 用户端 ─┐
               ├── REST API（Spring MVC → Service → MyBatis）→ MySQL 8
Vue 管理端 ────┘
```

## 当前阶段边界

用户端首页已接入商品目录 API。管理端已接入管理员登录与商品管理 API。完整点单、购物车、订单、会员和支付流程尚未完成；不得将静态页面或构建成功误认为业务已实现。

## 工作区目录

仓库已有 `d-coffee-front/` 与 `d-coffee-backend/`，分别承担用户端和后端职责，保留目录名以避免移动用户文件。管理端位于 `d-coffee-admin/`。

## 后端分层约定

- Controller：HTTP 输入、参数校验、调用服务并封装响应。
- Service：业务规则、事务、数据归属校验、订单计价与库存逻辑。
- Mapper：仅放置带参数绑定的 MyBatis SQL。
- Entity、DTO、VO 分层维护，API 不直接返回数据库实体。

## 暂不采用

当前范围不包含 Redis、消息队列、微服务、第三方支付或第三方对象存储。
