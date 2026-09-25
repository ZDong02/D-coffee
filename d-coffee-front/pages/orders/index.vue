<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { cancelOrder, getOrders } from '../../services/request.js'
import TabBar from '../../components/TabBar.vue'
import EmptyState from '../../components/EmptyState.vue'

const orders = ref([])
const loading = ref(false)
const errorMessage = ref('')
const cancelingId = ref(null)

const statusLabels = {
  PENDING_PAYMENT: '待支付', PAID: '已支付', MAKING: '制作中', READY: '等待取餐',
  COMPLETED: '已完成', CANCELED: '已取消', REFUNDING: '退款处理中', REFUNDED: '已退款',
}

async function loadOrders() {
  if (!uni.getStorageSync('dcoffee-user-token')) {
    orders.value = []
    errorMessage.value = '请先登录后查看订单。'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    orders.value = await getOrders()
  } catch (error) {
    errorMessage.value = error.message || '订单暂时无法加载'
  } finally {
    loading.value = false
  }
}

async function cancel(order) {
  const confirmed = await new Promise((resolve) => uni.showModal({
    title: '取消订单', content: '取消后将释放本单预占的商品库存，确定继续吗？',
    confirmText: '确认取消', success: (result) => resolve(result.confirm), fail: () => resolve(false),
  }))
  if (!confirmed) return
  cancelingId.value = order.id
  try {
    await cancelOrder(order.id)
    uni.showToast({ title: '订单已取消', icon: 'success' })
    await loadOrders()
  } catch (error) {
    uni.showToast({ title: error.message || '取消失败', icon: 'none' })
  } finally {
    cancelingId.value = null
  }
}

function formatAmount(amount) { return Number(amount || 0).toFixed(2) }
onShow(loadOrders)
</script>

<template>
  <view class="orders-page">
    <view class="page-heading"><text class="heading-title">我的订单</text><text class="heading-copy">订单记录与当前状态</text></view>
    <view v-if="loading" class="loading-state">
      <text class="loading-spinner">⏳</text>
      <text>正在加载订单…</text>
    </view>
    <EmptyState
      v-else-if="errorMessage && !uni.getStorageSync('dcoffee-user-token')"
      icon="🔐"
      title="请先登录"
      description="登录后查看订单。"
      action-text="去登录"
      @action="uni.navigateTo({ url: '/pages/account/index' })"
    />
    <EmptyState
      v-else-if="errorMessage"
      icon="⚠️"
      title="订单暂时无法加载"
      :description="errorMessage"
      action-text="重新加载"
      @action="loadOrders"
    />
    <EmptyState
      v-else-if="!orders.length"
      icon="📋"
      title="还没有订单"
      description="选好咖啡后，在购物车提交即可创建订单。"
    />
    <view v-else class="order-list">
      <view v-for="order in orders" :key="order.id" class="order-card">
        <view class="order-top"><view><text class="store-name">{{ order.storeName }}</text><text class="order-no">{{ order.orderNo }}</text></view><text class="order-status">{{ statusLabels[order.status] || order.status }}</text></view>
        <view v-for="item in order.items" :key="item.id" class="order-item">
          <view class="item-copy"><text class="item-name">{{ item.productName }} × {{ item.quantity }}</text><text class="item-options">{{ item.options.map((option) => `${option.groupName}：${option.selectedName}`).join(' · ') || '标准配置' }}</text></view>
          <text class="item-amount">¥{{ formatAmount(item.subtotal) }}</text>
        </view>
        <view class="order-bottom"><text class="payment-note">{{ order.paymentStatus === 'UNPAID' ? '支付服务暂未接入' : `支付状态：${order.paymentStatus}` }}</text><text class="order-total">合计 ¥{{ formatAmount(order.payAmount) }}</text></view>
        <text v-if="order.status === 'PENDING_PAYMENT' && order.paymentStatus === 'UNPAID'" class="cancel-button" :class="{ 'cancel-button--busy': cancelingId === order.id }" @tap="cancelingId !== order.id && cancel(order)">{{ cancelingId === order.id ? '处理中…' : '取消订单' }}</text>
      </view>
    </view>
    <TabBar />
  </view>
</template>

<style lang="scss" scoped>
.orders-page { min-height: 100vh; box-sizing: border-box; padding: 28rpx 30rpx 160rpx; background: #f7f3ed; }
.page-heading { margin: 12rpx 2rpx 24rpx; }
.heading-title, .heading-copy { display: block; }
.heading-title { color: #39291f; font-size: 36rpx; font-weight: 600; }
.heading-copy { margin-top: 8rpx; color: #a09284; font-size: 19rpx; }
.loading-state {
  display: flex;
  min-height: 55vh;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18rpx;
  color: #918477;
  font-size: 21rpx;
}
.loading-spinner {
  font-size: 60rpx;
  animation: rotate 1s linear infinite;
}
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.order-list { display: flex; flex-direction: column; gap: 17rpx; }
.order-card {
  padding: 24rpx;
  border-radius: 17rpx;
  background: #fffdfa;
  box-shadow: 0 5rpx 22rpx rgba(63, 45, 31, 0.06);
}
.order-top, .order-bottom, .order-item { display: flex; align-items: center; justify-content: space-between; gap: 18rpx; }
.store-name, .order-no { display: block; }
.store-name { color: #48362a; font-size: 23rpx; font-weight: 600; }
.order-no { margin-top: 7rpx; color: #a09284; font-size: 16rpx; }
.order-status { color: #946d48; font-size: 20rpx; }
.order-item { align-items: flex-start; margin-top: 21rpx; padding-top: 18rpx; border-top: 1rpx solid #f0e9e1; }
.item-copy { min-width: 0; }
.item-name, .item-options { display: block; }
.item-name { color: #594536; font-size: 20rpx; }
.item-options { margin-top: 7rpx; color: #a09284; font-size: 16rpx; }
.item-amount { flex: 0 0 auto; color: #604c3c; font-size: 19rpx; }
.order-bottom { margin-top: 20rpx; }
.payment-note { color: #a09284; font-size: 16rpx; }
.order-total { color: #543b29; font-size: 22rpx; font-weight: 600; }
.cancel-button {
  display: block;
  width: fit-content;
  margin: 18rpx 0 0 auto;
  padding: 11rpx 25rpx;
  border: 1rpx solid #e5dbd0;
  border-radius: 30rpx;
  color: #806449;
  font-size: 18rpx;
  transition: all 250ms cubic-bezier(0.4, 0, 0.2, 1);

  &:active:not(.cancel-button--busy) {
    background: #f5f0ea;
    transform: scale(0.96);
  }
}
.cancel-button--busy { opacity: .55; }
</style>
