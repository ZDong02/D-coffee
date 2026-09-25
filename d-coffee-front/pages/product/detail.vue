<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addCartItem, getProduct } from '../../services/request.js'

const product = ref(null)
const productId = ref('')
const loading = ref(true)
const errorMessage = ref('')
const selectedOptions = ref({})
const selectedExtras = ref([])
const adding = ref(false)

const totalPrice = computed(() => {
  if (!product.value) return 0
  const optionPrice = (product.value.options || []).reduce((total, option) => {
    const selectedIds = selectedOptions.value[option.id] || []
    return total + (option.values || [])
      .filter((value) => selectedIds.includes(value.id))
      .reduce((sum, value) => sum + Number(value.priceDelta || 0), 0)
  }, 0)
  const extraPrice = (product.value.extras || [])
    .filter((extra) => selectedExtras.value.includes(extra.id))
    .reduce((total, extra) => total + Number(extra.price || 0), 0)
  return Number(product.value.price || 0) + optionPrice + extraPrice
})

onLoad((query) => {
  productId.value = query?.id || ''
  loadProduct(productId.value)
})

async function loadProduct(id) {
  if (!id) {
    errorMessage.value = '商品编号无效'
    loading.value = false
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    product.value = await getProduct(id)
    const defaults = {}
    for (const option of product.value.options || []) {
      const minimum = Number(option.minSelect || 0)
      if (minimum > 0 && option.values?.length) {
        defaults[option.id] = option.values.slice(0, minimum).map((value) => value.id)
      }
    }
    selectedOptions.value = defaults
  } catch (error) {
    errorMessage.value = error.message || '商品详情加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function isOptionSelected(option, value) {
  return (selectedOptions.value[option.id] || []).includes(value.id)
}

function toggleOption(option, value) {
  const current = selectedOptions.value[option.id] || []
  if (option.selectionType !== 'MULTIPLE') {
    selectedOptions.value = { ...selectedOptions.value, [option.id]: [value.id] }
    return
  }
  if (current.includes(value.id)) {
    selectedOptions.value = {
      ...selectedOptions.value,
      [option.id]: current.filter((id) => id !== value.id),
    }
    return
  }
  const maximum = Number(option.maxSelect || 1)
  if (current.length >= maximum) {
    uni.showToast({ title: `最多选择 ${maximum} 项`, icon: 'none' })
    return
  }
  selectedOptions.value = { ...selectedOptions.value, [option.id]: [...current, value.id] }
}

function toggleExtra(extra) {
  if (extra.stock <= 0 && !selectedExtras.value.includes(extra.id)) {
    uni.showToast({ title: '该加料暂时售罄', icon: 'none' })
    return
  }
  selectedExtras.value = selectedExtras.value.includes(extra.id)
    ? selectedExtras.value.filter((id) => id !== extra.id)
    : [...selectedExtras.value, extra.id]
}

function confirmConfiguration() {
  for (const option of product.value?.options || []) {
    const count = (selectedOptions.value[option.id] || []).length
    const minimum = Number(option.minSelect || 0)
    const requiredMinimum = option.required ? Math.max(1, minimum) : minimum
    if (count < requiredMinimum) {
      uni.showToast({ title: `请选择${option.name}`, icon: 'none' })
      return
    }
  }
  if (!uni.getStorageSync('dcoffee-user-token')) {
    uni.showModal({ title: '请先登录', content: '登录后才能将商品加入购物车。', success: (result) => {
      if (result.confirm) uni.navigateTo({ url: '/pages/account/index' })
    } })
    return
  }
  if (!uni.getStorageSync('dcoffee-store')) {
    uni.showModal({ title: '请先选择门店', content: '购物车按门店分别保存，请先选择取餐门店。', success: (result) => {
      if (result.confirm) uni.navigateTo({ url: '/pages/store/index' })
    } })
    return
  }
  addCurrentItem()
}

async function addCurrentItem() {
  if (adding.value) return
  adding.value = true
  try {
    const store = uni.getStorageSync('dcoffee-store')
    await addCartItem({
      storeId: store.id,
      productId: product.value.id,
      options: (product.value.options || []).map((option) => ({
        optionId: option.id,
        valueIds: selectedOptions.value[option.id] || [],
      })),
      extraIds: selectedExtras.value,
    })
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '加入购物车失败', icon: 'none' })
  } finally {
    adding.value = false
  }
}

function openCart() {
  uni.navigateTo({ url: '/pages/cart/index' })
}

function formatPrice(price) {
  return Number(price || 0).toFixed(2)
}
</script>

<template>
  <view class="detail-page">
    <view v-if="loading" class="state-card">正在加载商品详情…</view>
    <view v-else-if="errorMessage" class="state-card">
      <text>{{ errorMessage }}</text>
      <text class="retry-button" @tap="loadProduct(productId)">重新加载</text>
    </view>
    <template v-else-if="product">
      <view class="product-hero">
        <image v-if="product.imageUrl" class="product-image" :src="product.imageUrl" mode="aspectFill" />
        <view v-else class="product-image product-image--empty">D</view>
      </view>
      <view class="product-summary">
        <text class="product-category">{{ product.categoryName || 'D-COFFEE' }}</text>
        <text class="product-name">{{ product.name }}</text>
        <text class="product-description">{{ product.description || '为日常留一点香气与松弛。' }}</text>
        <text class="base-price">¥{{ formatPrice(product.price) }}</text>
      </view>

      <view v-for="option in product.options || []" :key="option.id" class="selection-section">
        <view class="section-title-row">
          <text class="section-title">{{ option.name }}</text>
          <text class="section-hint">{{ option.required ? '必选' : '可选' }} · {{ option.selectionType === 'MULTIPLE' ? `最多 ${option.maxSelect} 项` : '单选' }}</text>
        </view>
        <view class="choice-list">
          <view
            v-for="value in option.values || []"
            :key="value.id"
            class="choice-chip"
            :class="{ 'choice-chip--selected': isOptionSelected(option, value) }"
            @tap="toggleOption(option, value)"
          >
            <text>{{ value.name }}</text>
            <text v-if="Number(value.priceDelta) > 0" class="choice-price">+¥{{ formatPrice(value.priceDelta) }}</text>
          </view>
          <text v-if="!option.values?.length" class="empty-hint">暂无可选项</text>
        </view>
      </view>

      <view v-if="product.extras?.length" class="selection-section">
        <view class="section-title-row">
          <text class="section-title">加料</text>
          <text class="section-hint">按需添加</text>
        </view>
        <view class="extra-list">
          <view
            v-for="extra in product.extras"
            :key="extra.id"
            class="extra-row"
            :class="{ 'extra-row--disabled': extra.stock <= 0 }"
            @tap="toggleExtra(extra)"
          >
            <view>
              <text class="extra-name">{{ extra.name }}</text>
              <text class="extra-price">+¥{{ formatPrice(extra.price) }}<text v-if="extra.stock <= 0"> · 售罄</text></text>
            </view>
            <text class="check-mark" :class="{ 'check-mark--selected': selectedExtras.includes(extra.id) }">
              {{ selectedExtras.includes(extra.id) ? '✓' : '+' }}
            </text>
          </view>
        </view>
      </view>

      <view class="bottom-bar">
        <view>
          <text class="total-label">当前配置</text>
          <text class="total-price">¥{{ formatPrice(totalPrice) }}</text>
        </view>
        <view class="action-buttons">
          <text class="cart-button" @tap="openCart">购物车</text>
          <text class="confirm-button" :class="{ 'confirm-button--disabled': adding }" @tap="confirmConfiguration">{{ adding ? '加入中…' : '加入购物车' }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.detail-page { min-height: 100vh; padding-bottom: 130rpx; background: #f7f3ed; color: #39291f; }
.product-hero { height: 480rpx; background: #eee7df; }
.product-image { width: 100%; height: 100%; }
.product-image--empty { display: flex; align-items: center; justify-content: center; color: #9b7e64; font-family: Georgia, serif; font-size: 130rpx; }
.product-summary { padding: 32rpx 36rpx 36rpx; background: #fffdfa; }
.product-category { display: block; color: #a1876d; font-size: 20rpx; letter-spacing: 2rpx; }
.product-name { display: block; margin-top: 10rpx; font-size: 38rpx; font-weight: 600; }
.product-description { display: block; margin-top: 14rpx; color: #918477; font-size: 23rpx; line-height: 1.6; }
.base-price { display: block; margin-top: 22rpx; color: #805d43; font-size: 34rpx; font-weight: 600; }
.selection-section { margin-top: 18rpx; padding: 30rpx 36rpx; background: #fffdfa; }
.section-title-row { display: flex; align-items: center; justify-content: space-between; }
.section-title { font-size: 28rpx; font-weight: 600; }
.section-hint { color: #a09284; font-size: 20rpx; }
.choice-list { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 24rpx; }
.choice-chip { display: flex; min-width: 140rpx; justify-content: center; gap: 8rpx; padding: 17rpx 20rpx; border: 1rpx solid #e8dfd5; border-radius: 12rpx; color: #665447; font-size: 22rpx; }
.choice-chip--selected { border-color: #684a34; background: #f5eee7; color: #513827; }
.choice-price { color: #9a7656; }
.empty-hint { color: #a09284; font-size: 21rpx; }
.extra-list { margin-top: 10rpx; }
.extra-row { display: flex; align-items: center; justify-content: space-between; padding: 22rpx 0; border-bottom: 1rpx solid #f0e9e1; }
.extra-row:last-child { border-bottom: 0; }
.extra-row--disabled { opacity: .48; }
.extra-name, .extra-price { display: block; }
.extra-name { font-size: 23rpx; }
.extra-price { margin-top: 7rpx; color: #a09284; font-size: 19rpx; }
.check-mark { display: flex; width: 38rpx; height: 38rpx; align-items: center; justify-content: center; border: 1rpx solid #d8cabc; border-radius: 50%; color: #9b8b7c; }
.check-mark--selected { border-color: #513827; background: #513827; color: #fff; }
.bottom-bar { position: fixed; right: 0; bottom: 0; left: 0; display: flex; align-items: center; justify-content: space-between; padding: 20rpx 32rpx calc(20rpx + env(safe-area-inset-bottom)); background: rgba(255, 253, 250, .98); box-shadow: 0 -5rpx 20rpx rgba(63, 45, 31, .06); }
.total-label, .total-price { display: block; }
.total-label { color: #a09284; font-size: 18rpx; }
.total-price { margin-top: 5rpx; color: #543b29; font-size: 31rpx; font-weight: 600; }
.confirm-button { padding: 20rpx 32rpx; border-radius: 40rpx; background: #513827; color: #fffaf4; font-size: 23rpx; }
.action-buttons { display: flex; align-items: center; gap: 14rpx; }
.cart-button { padding: 18rpx 20rpx; border: 1rpx solid #d8cabc; border-radius: 40rpx; color: #604a37; font-size: 21rpx; }
.confirm-button--disabled { opacity: .6; }
.state-card { display: flex; min-height: 55vh; flex-direction: column; align-items: center; justify-content: center; gap: 24rpx; color: #79695b; font-size: 23rpx; }
.retry-button { padding: 12rpx 28rpx; border-radius: 28rpx; background: #513827; color: white; }
</style>
