<script setup>
import { computed } from 'vue'

const pages = getCurrentPages()
const currentPage = computed(() => {
  const page = pages[pages.length - 1]
  return page?.route || ''
})

const tabs = [
  { path: '/pages/index/index', icon: '☕', label: '菜单', name: 'index' },
  { path: '/pages/store/index', icon: '📍', label: '门店', name: 'store' },
  { path: '/pages/cart/index', icon: '🛒', label: '购物车', name: 'cart' },
  { path: '/pages/orders/index', icon: '📋', label: '订单', name: 'orders' },
  { path: '/pages/account/index', icon: '👤', label: '我的', name: 'account' },
]

function isActive(tab) {
  return currentPage.value.includes(tab.name)
}

function navigate(tab) {
  if (isActive(tab)) return

  // 首页使用 switchTab，其他使用 navigateTo
  if (tab.name === 'index') {
    uni.switchTab({ url: tab.path })
  } else {
    uni.navigateTo({ url: tab.path })
  }
}
</script>

<template>
  <view class="tab-bar" :style="{ paddingBottom: 'calc(12rpx + env(safe-area-inset-bottom))' }">
    <view
      v-for="tab in tabs"
      :key="tab.name"
      class="tab-item"
      :class="{ 'tab-item--active': isActive(tab) }"
      @tap="navigate(tab)"
    >
      <text class="tab-icon">{{ tab.icon }}</text>
      <text class="tab-label">{{ tab.label }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.tab-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 300;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 12rpx 0;
  background: rgba(255, 253, 250, 0.98);
  backdrop-filter: blur(20rpx);
  box-shadow: 0 -5rpx 20rpx rgba(63, 45, 31, 0.06);
}

.tab-item {
  display: flex;
  min-width: 88rpx;
  min-height: 88rpx;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  transition: all 250ms cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    opacity: 0.6;
    transform: scale(0.95);
  }
}

.tab-icon {
  font-size: 40rpx;
  line-height: 1;
  transition: transform 250ms cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-label {
  color: #a09284;
  font-size: 20rpx;
  transition: color 250ms cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item--active {
  .tab-icon {
    transform: scale(1.1);
  }

  .tab-label {
    color: #513827;
    font-weight: 600;
  }
}
</style>
