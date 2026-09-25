<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { createOrder, getCart, removeCartItem, updateCartItem } from '../../services/request.js'
import TabBar from '../../components/TabBar.vue'
import EmptyState from '../../components/EmptyState.vue'

const cart = ref(null)
const store = ref(uni.getStorageSync('dcoffee-store') || null)
const loading = ref(false)
const errorMessage = ref('')
const updatingItemId = ref(null)
const submittingOrder = ref(false)
const amount = computed(() => Number(cart.value?.totalAmount || 0).toFixed(2))

async function loadCart() {
  store.value = uni.getStorageSync('dcoffee-store') || null
  if (!uni.getStorageSync('dcoffee-user-token')) {
    cart.value = null
    errorMessage.value = '请先登录，查看与管理你的购物车。'
    return
  }
  if (!store.value?.id) {
    cart.value = null
    errorMessage.value = '请先选择取餐门店。'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    cart.value = await getCart(store.value.id)
  } catch (error) {
    errorMessage.value = error.message || '购物车暂时无法加载'
  } finally {
    loading.value = false
  }
}

async function changeQuantity(item, amountDelta) {
  const nextQuantity = item.quantity + amountDelta
  if (nextQuantity < 1 || nextQuantity > 99) return
  updatingItemId.value = item.id
  try {
    cart.value = await updateCartItem(item.id, store.value.id, nextQuantity)
  } catch (error) {
    uni.showToast({ title: error.message || '数量更新失败', icon: 'none' })
  } finally {
    updatingItemId.value = null
  }
}

async function removeItem(item) {
  updatingItemId.value = item.id
  try {
    cart.value = await removeCartItem(item.id, store.value.id)
  } catch (error) {
    uni.showToast({ title: error.message || '移除失败', icon: 'none' })
  } finally {
    updatingItemId.value = null
  }
}

function openLogin() { uni.navigateTo({ url: '/pages/account/index' }) }
function openStorePicker() { uni.navigateTo({ url: '/pages/store/index' }) }

async function checkout() {
  if (submittingOrder.value || !cart.value?.items?.length || cart.value.storeStatus !== 'OPEN') return
  const confirmed = await new Promise((resolve) => uni.showModal({
    title: '确认提交订单',
    content: `共 ${cart.value.totalQuantity} 件商品，合计 ¥${amount.value}。当前仅创建待支付订单，支付服务尚未接入。`,
    confirmText: '提交订单',
    success: (result) => resolve(result.confirm),
    fail: () => resolve(false),
  }))
  if (!confirmed) return
  submittingOrder.value = true
  try {
    const order = await createOrder({ storeId: store.value.id, remark: '' })
    uni.showToast({ title: '订单已创建', icon: 'success' })
    setTimeout(() => uni.navigateTo({ url: `/pages/orders/index?orderId=${order.id}` }), 300)
  } catch (error) {
    uni.showToast({ title: error.message || '提交失败，请检查购物车后重试', icon: 'none', duration: 3000 })
    await loadCart()
  } finally {
    submittingOrder.value = false
  }
}

onShow(loadCart)
</script>

<template>
  <view class="cart-page">
    <view v-if="store" class="store-banner" @tap="openStorePicker">
      <view><text class="store-caption">取餐门店</text><text class="store-name">{{ cart?.storeName || store.name }}</text></view>
      <text class="switch-store">更换 ›</text>
    </view>
    <view v-if="store?.status === 'CLOSED'" class="closed-notice">该门店当前未营业，购物车可预先保存，暂不能提交订单。</view>
    <view v-if="loading" class="loading-state">
      <text class="loading-spinner">⏳</text>
      <text>正在加载购物车…</text>
    </view>
    <EmptyState
      v-else-if="errorMessage && !uni.getStorageSync('dcoffee-user-token')"
      icon="🔐"
      title="请先登录"
      description="登录后才能查看与管理你的购物车。"
      action-text="去登录"
      @action="openLogin"
    />
    <EmptyState
      v-else-if="errorMessage && !store"
      icon="📍"
      title="请先选择门店"
      description="购物车按门店分别保存，请先选择取餐门店。"
      action-text="选择门店"
      @action="openStorePicker"
    />
    <EmptyState
      v-else-if="errorMessage"
      icon="⚠️"
      title="购物车暂时无法加载"
      :description="errorMessage"
      action-text="重新加载"
      @action="loadCart"
    />
    <EmptyState
      v-else-if="!cart?.items?.length"
      icon="🛒"
      title="购物车还是空的"
      description="去菜单挑一杯喜欢的咖啡吧。"
      action-text="返回菜单"
      @action="uni.switchTab({ url: '/pages/index/index' })"
    />
    <template v-else>
      <view class="item-list">
        <view v-for="item in cart.items" :key="item.id" class="cart-item">
          <image v-if="item.imageUrl" class="item-image" :src="item.imageUrl" mode="aspectFill" />
          <view v-else class="item-image item-image--empty">D</view>
          <view class="item-main">
            <text class="item-name">{{ item.productName }}</text>
            <text class="item-options">{{ item.options.map((option) => `${option.groupName}：${option.selectedName}`).join(' · ') || '标准配置' }}</text>
            <view class="item-footer">
              <text class="item-price">¥{{ Number(item.unitPrice).toFixed(2) }}</text>
              <view class="quantity-control">
                <text class="quantity-button" @tap="changeQuantity(item, -1)">−</text>
                <text class="quantity-number">{{ item.quantity }}</text>
                <text class="quantity-button" @tap="changeQuantity(item, 1)">+</text>
              </view>
            </view>
            <text class="remove-link" @tap="removeItem(item)">移除</text>
          </view>
          <view v-if="updatingItemId === item.id" class="item-loading">更新中…</view>
        </view>
      </view>
      <view class="summary-bar">
        <view><text class="summary-caption">共 {{ cart.totalQuantity }} 件</text><text class="summary-price">¥{{ amount }}</text></view>
        <text class="checkout-button" :class="{ 'checkout-button--disabled': cart.storeStatus !== 'OPEN' || submittingOrder }" @tap="checkout">
          {{ submittingOrder ? '提交中…' : cart.storeStatus === 'OPEN' ? '提交订单' : '门店未营业' }}
        </text>
      </view>
      <text class="checkout-note">提交后会创建待支付订单并预占库存；支付功能暂未接入。</text>
    </template>
    <TabBar />
  </view>
</template>

<style lang="scss" scoped>
.cart-page { min-height: 100vh; box-sizing: border-box; padding: 24rpx 30rpx 150rpx; background: #f7f3ed; }
.store-banner { display: flex; align-items: center; justify-content: space-between; padding: 22rpx 25rpx; border-radius: 16rpx; background: #fffdfa; }
.store-caption, .store-name { display: block; }
.store-caption { color: #a09284; font-size: 17rpx; }
.store-name { margin-top: 6rpx; color: #48362a; font-size: 23rpx; font-weight: 600; }
.switch-store { color: #896d57; font-size: 20rpx; }
.closed-notice { margin-top: 14rpx; padding: 16rpx 20rpx; border-radius: 12rpx; background: #f6ead6; color: #8b6943; font-size: 19rpx; line-height: 1.5; }
.item-list { margin-top: 18rpx; }
.cart-item { position: relative; display: flex; gap: 20rpx; margin-bottom: 14rpx; padding: 22rpx; border-radius: 16rpx; background: #fffdfa; }
.item-image { width: 132rpx; height: 132rpx; flex: 0 0 auto; border-radius: 12rpx; background: #eee7df; }
.item-image--empty { display: grid; place-items: center; color: #9b7e64; font-family: Georgia, serif; font-size: 54rpx; }
.item-main { min-width: 0; flex: 1; }
.item-name { display: block; overflow: hidden; color: #39291f; font-size: 24rpx; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
.item-options { display: block; overflow: hidden; margin-top: 10rpx; color: #9a8b7e; font-size: 18rpx; text-overflow: ellipsis; white-space: nowrap; }
.item-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 14rpx; }
.item-price { color: #805d43; font-size: 23rpx; font-weight: 600; }
.quantity-control { display: flex; align-items: center; gap: 16rpx; }
.quantity-button { display: grid; width: 42rpx; height: 42rpx; place-items: center; border: 1rpx solid #e5dbd0; border-radius: 50%; color: #624a37; font-size: 28rpx; }
.quantity-number { min-width: 22rpx; color: #604c3c; font-size: 21rpx; text-align: center; }
.remove-link { display: inline-block; margin-top: 8rpx; color: #a09284; font-size: 17rpx; }
.item-loading { position: absolute; inset: 0; display: grid; place-items: center; border-radius: 16rpx; background: rgba(255, 253, 250, .65); color: #604c3c; font-size: 20rpx; }
.summary-bar { position: fixed; right: 0; bottom: 0; left: 0; display: flex; align-items: center; justify-content: space-between; padding: 19rpx 30rpx calc(19rpx + env(safe-area-inset-bottom)); background: #fffdfa; box-shadow: 0 -5rpx 20rpx rgba(63, 45, 31, .06); }
.summary-caption, .summary-price { display: block; }
.summary-caption { color: #a09284; font-size: 18rpx; }
.summary-price { margin-top: 4rpx; color: #543b29; font-size: 29rpx; font-weight: 600; }
.checkout-button { padding: 18rpx 36rpx; border-radius: 36rpx; background: #513827; color: #fff; font-size: 21rpx; }
.checkout-button--disabled { background: #c8bdb2; }
.checkout-note { display: block; margin-top: 16rpx; color: #a09284; font-size: 18rpx; text-align: center; }
.state-card { display: flex; min-height: 55vh; flex-direction: column; align-items: center; justify-content: center; gap: 18rpx; color: #918477; font-size: 22rpx; text-align: center; }
.empty-icon { color: #cbb7a3; font-family: Georgia, serif; font-size: 100rpx; }
.empty-title { color: #48362a; font-size: 27rpx; font-weight: 600; }
.empty-copy { color: #a09284; font-size: 20rpx; }
.action-link { margin-top: 12rpx; padding: 13rpx 28rpx; border-radius: 28rpx; background: #513827; color: #fffaf4; font-size: 20rpx; }
</style>
