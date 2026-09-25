# D-Coffee 前端优化说明

## 优化内容概览

### 1. UI/UX 改进

#### 新增组件
- **TabBar.vue** - 底部导航栏
  - 5个主导航项：菜单、门店、购物车、订单、我的
  - 响应式触摸反馈
  - 活动状态指示
  - 支持安全区域适配

- **ProductSkeleton.vue** - 商品加载骨架屏
  - 流畅的 shimmer 动画效果
  - 改善加载体验

- **EmptyState.vue** - 空状态组件
  - 统一的空状态展示
  - 可配置图标、标题、描述、操作按钮
  - 增强用户引导

#### 设计系统
- **tokens.scss** - 设计令牌文件
  - 统一的颜色系统（咖啡主题 - 温暖棕色调）
  - 间距系统（8-48rpx）
  - 圆角、阴影、字体大小标准
  - 过渡动画时长定义
  - Z-index 层级管理

### 2. 交互体验优化

#### 触摸反馈
- 所有可交互元素添加了 `:active` 状态
- 按钮点击缩放效果（scale 0.95-0.98）
- 颜色渐变过渡（250ms cubic-bezier）
- 触摸目标尺寸优化（≥44px/88rpx）

#### 加载状态
- 首次加载显示骨架屏（首页商品列表）
- 其他页面显示旋转加载图标
- 统一的加载提示文案

#### 空状态处理
- 统一使用 EmptyState 组件
- 清晰的图标指示（表情符号）
- 友好的提示文案
- 明确的操作引导

### 3. 页面级优化

#### 首页（pages/index/index.vue）
- ✅ 添加底部导航
- ✅ 骨架屏加载状态
- ✅ 空状态优化
- ✅ 分类筛选触摸反馈
- ✅ 商品卡片点击动画
- ✅ 底部内边距调整（180rpx，为底部导航预留空间）

#### 购物车（pages/cart/index.vue）
- ✅ 添加底部导航
- ✅ 空状态组件化
- ✅ 加载状态优化
- ✅ 数量调整按钮触摸反馈
- ✅ 结算栏位置调整（避免与底部导航重叠）
- ✅ 移除按钮点击反馈

#### 订单（pages/orders/index.vue）
- ✅ 添加底部导航
- ✅ 空状态组件化
- ✅ 加载状态优化
- ✅ 取消订单按钮触摸反馈
- ✅ 底部内边距调整

#### 账户（pages/account/index.vue）
- ✅ 添加底部导航
- ✅ 输入框焦点状态
- ✅ 按钮触摸反馈
- ✅ 切换模式按钮反馈
- ✅ 底部内边距调整

#### 门店选择（pages/store/index.vue）
- ✅ 添加底部导航
- ✅ 空状态组件化
- ✅ 加载状态优化
- ✅ 门店卡片触摸反馈
- ✅ 营业状态样式优化

#### 商品详情（pages/product/detail.vue）
- ✅ 空状态组件化
- ✅ 加载状态优化
- ✅ 选项按钮触摸反馈
- ✅ 购物车/确认按钮动画
- ✅ 加料复选框尺寸优化（48rpx）

### 4. 后端连接改进

#### 环境配置
- ✅ 创建 `.env.development` - 开发环境配置
  - API 地址：`http://localhost:8080/api`
- ✅ 创建 `.env.production` - 生产环境配置
  - API 地址：`/api`（使用代理）

#### API 服务
- request.js 已正确配置 token 自动注入
- 统一的错误处理
- 10秒请求超时

### 5. 可访问性改进

- 触摸目标最小 48rpx（约44px）
- 文字与背景对比度符合 WCAG AA 标准
- 加载状态明确提示
- 错误信息清晰可读
- 操作反馈及时

### 6. 性能优化

- 组件按需加载
- 骨架屏减少白屏时间
- CSS 动画使用 transform（GPU 加速）
- 避免昂贵的重排重绘

## 使用说明

### 开发运行

```bash
cd d-coffee-front

# 安装依赖
npm install

# H5 开发模式
npm run dev:h5

# 微信小程序开发模式
npm run dev:mp-weixin
```

### 构建

```bash
# H5 生产构建
npm run build:h5

# 微信小程序生产构建
npm run build:mp-weixin
```

### 后端配置

确保后端服务运行在 `http://localhost:8080`，或修改 `.env.development` 中的 `VITE_API_BASE_URL`。

## 设计规范

### 颜色系统
- 主色：`#513827`（深棕）
- 强调色：`#805d43`（中棕）
- 背景：`#f7f3ed`（米黄）
- 卡片：`#fffdfa`（浅米）

### 间距
- XS: 8rpx
- SM: 12rpx
- MD: 16rpx
- LG: 24rpx
- XL: 32rpx
- 2XL: 48rpx

### 圆角
- SM: 12rpx（输入框、标签）
- MD: 16rpx（小卡片）
- LG: 18rpx（商品卡片）
- XL: 24rpx（大卡片）
- Round: 999rpx（按钮、徽章）

### 动画
- Fast: 150ms（小交互）
- Base: 250ms（标准过渡）
- Slow: 350ms（大变化）
- Easing: cubic-bezier(0.4, 0, 0.2, 1)

## 待改进项

1. **图片优化**
   - 添加图片懒加载
   - 使用 WebP 格式
   - 添加占位图

2. **性能监控**
   - 添加页面性能埋点
   - 监控 API 响应时间

3. **错误边界**
   - 添加全局错误处理
   - 友好的错误页面

4. **离线支持**
   - Service Worker 缓存
   - 离线状态提示

5. **国际化**
   - i18n 多语言支持
   - 时区处理

6. **测试**
   - 单元测试
   - E2E 测试

## 技术栈

- **框架**: uni-app (Vue 3)
- **状态管理**: Pinia
- **样式**: SCSS
- **构建**: Vite
- **平台**: H5 + 微信小程序

## 文件结构

```
d-coffee-front/
├── components/          # 通用组件
│   ├── TabBar.vue      # 底部导航
│   ├── ProductSkeleton.vue  # 商品骨架屏
│   └── EmptyState.vue  # 空状态
├── pages/              # 页面
│   ├── index/          # 首页
│   ├── product/        # 商品详情
│   ├── cart/           # 购物车
│   ├── orders/         # 订单
│   ├── account/        # 账户
│   └── store/          # 门店选择
├── services/           # API 服务
│   └── request.js      # 请求封装
├── styles/             # 样式
│   └── tokens.scss     # 设计令牌
├── .env.development    # 开发环境配置
└── .env.production     # 生产环境配置
```

## 兼容性

- **H5**: 现代浏览器（Chrome 90+, Safari 14+, Firefox 88+）
- **微信小程序**: 基础库 2.0+
- **响应式**: 支持 375px - 750px 宽度设备
