<script setup>
import { onMounted, ref } from 'vue'
import { getStores } from '../../services/request.js'

const stores = ref([])
const selectedStore = ref(uni.getStorageSync('dcoffee-store') || null)
const loading = ref(true)
const errorMessage = ref('')

async function loadStores() {
  loading.value = true
  errorMessage.value = ''
  try {
    stores.value = await getStores()
  } catch (error) {
    errorMessage.value = error.message || '门店信息加载失败'
  } finally {
    loading.value = false
  }
}

function chooseStore(store) {
  const safeStore = { id: store.id, name: store.name, status: store.status, address: store.address }
  uni.setStorageSync('dcoffee-store', safeStore)
  selectedStore.value = safeStore
  uni.showToast({ title: store.status === 'OPEN' ? '已选择营业门店' : '已保存预选门店', icon: 'success' })
  setTimeout(() => {
    const pages = getCurrentPages()
    if (pages.length > 1) uni.navigateBack()
  }, 450)
}

onMounted(loadStores)
</script>

<template>
  <view class="store-page">
    <view class="intro-card">
      <text class="intro-title">选择取餐门店</text>
      <text class="intro-copy">购物车会按门店分别保存，请选择计划取餐的门店。</text>
    </view>
    <view v-if="loading" class="state-card">正在加载门店…</view>
    <view v-else-if="errorMessage" class="state-card">
      <text>{{ errorMessage }}</text>
      <text class="retry-button" @tap="loadStores">重新加载</text>
    </view>
    <view v-else-if="!stores.length" class="state-card">目前没有可选门店，请稍后再试。</view>
    <view v-else class="store-list">
      <view v-for="store in stores" :key="store.id" class="store-card" @tap="chooseStore(store)">
        <view class="store-card__heading">
          <text class="store-name">{{ store.name }}</text>
          <text class="store-status" :class="{ 'store-status--open': store.status === 'OPEN' }">
            {{ store.status === 'OPEN' ? '营业中' : '未营业 · 可预选' }}
          </text>
        </view>
        <text class="store-address">{{ store.address }}</text>
        <text class="store-hours">营业时间：{{ store.openTime || '--:--' }}–{{ store.closeTime || '--:--' }}</text>
        <text v-if="selectedStore?.id === store.id" class="selected-label">当前已选</text>
      </view>
    </view>
    <text class="notice">未营业门店仅可预选和加入购物车，订单提交将在门店开放营业后支持。</text>
  </view>
</template>

<style lang="scss" scoped>
.store-page { min-height: 100vh; box-sizing: border-box; padding: 32rpx; background: #f7f3ed; }
.intro-card, .store-card { padding: 30rpx; border-radius: 18rpx; background: #fffdfa; }
.intro-title { display: block; color: #39291f; font-size: 30rpx; font-weight: 600; }
.intro-copy { display: block; margin-top: 12rpx; color: #918477; font-size: 21rpx; line-height: 1.5; }
.store-list { display: flex; flex-direction: column; gap: 16rpx; margin-top: 20rpx; }
.store-card { position: relative; border: 1rpx solid #eee5da; }
.store-card__heading { display: flex; align-items: center; justify-content: space-between; gap: 16rpx; }
.store-name { color: #48362a; font-size: 27rpx; font-weight: 600; }
.store-status { color: #a17c51; font-size: 19rpx; }
.store-status--open { color: #55825f; }
.store-address, .store-hours { display: block; margin-top: 13rpx; color: #817366; font-size: 21rpx; line-height: 1.5; }
.selected-label { display: inline-block; margin-top: 16rpx; padding: 7rpx 15rpx; border-radius: 20rpx; background: #eee5da; color: #654c36; font-size: 18rpx; }
.notice { display: block; margin: 24rpx 8rpx; color: #a09284; font-size: 19rpx; line-height: 1.6; }
.state-card { display: flex; min-height: 45vh; flex-direction: column; align-items: center; justify-content: center; gap: 20rpx; color: #887767; font-size: 22rpx; text-align: center; }
.retry-button { padding: 12rpx 28rpx; border-radius: 26rpx; background: #513827; color: #fff; }
</style>
