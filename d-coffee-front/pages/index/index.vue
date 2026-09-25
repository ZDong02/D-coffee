<script setup>
import { computed, onMounted, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCategories, getProducts } from '../../services/request.js'

const categories = ref([])
const products = ref([])
const selectedCategoryId = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const hasLoaded = ref(false)
const accountLabel = ref('登录 / 注册')
const selectedCategoryName = computed(() =>
  categories.value.find((category) => category.id === selectedCategoryId.value)?.name || '全部商品',
)

async function loadProducts() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getProducts({ categoryId: selectedCategoryId.value, page: 1, pageSize: 50 })
    products.value = result.records || []
    hasLoaded.value = true
  } catch (error) {
    errorMessage.value = error.message || '商品加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function loadCatalog() {
  loading.value = true
  errorMessage.value = ''
  try {
    categories.value = await getCategories()
    await loadProducts()
  } catch (error) {
    errorMessage.value = error.message || '暂时无法连接商品服务'
    loading.value = false
  }
}

function selectCategory(categoryId) {
  if (selectedCategoryId.value === categoryId) return
  selectedCategoryId.value = categoryId
  loadProducts()
}

function formatPrice(price) {
  return Number(price || 0).toFixed(2)
}

function openAccount() {
  uni.navigateTo({ url: '/pages/account/index' })
}

function openStores() { uni.navigateTo({ url: '/pages/store/index' }) }
function openCart() { uni.navigateTo({ url: '/pages/cart/index' }) }

function refreshAccountLabel() {
  const user = uni.getStorageSync('dcoffee-user')
  accountLabel.value = user?.nickname || user?.phone || '登录 / 注册'
}

function openProduct(product) {
  if (product.stock <= 0) return
  uni.navigateTo({ url: `/pages/product/detail?id=${product.id}` })
}

onMounted(loadCatalog)
onShow(refreshAccountLabel)
</script>

<template>
  <view class="menu-page">
    <view class="hero">
      <text class="hero__eyebrow">D-COFFEE · FRESHLY BREWED</text>
      <text class="hero__title">一杯好咖啡，<text>从这里开始</text></text>
      <text class="hero__description">为日常留一点香气与松弛。</text>
      <text class="account-link" @tap="openAccount">{{ accountLabel }}</text>
      <text class="account-link account-link--secondary" @tap="openStores">选择门店</text>
      <text class="account-link account-link--secondary" @tap="openCart">购物车</text>
    </view>

    <view v-if="categories.length" class="category-strip">
      <text
        class="category-chip"
        :class="{ 'category-chip--active': selectedCategoryId === null }"
        @tap="selectCategory(null)"
      >全部</text>
      <text
        v-for="category in categories"
        :key="category.id"
        class="category-chip"
        :class="{ 'category-chip--active': selectedCategoryId === category.id }"
        @tap="selectCategory(category.id)"
      >{{ category.name }}</text>
    </view>

    <view class="section-heading">
      <view>
        <text class="section-heading__eyebrow">OUR MENU</text>
        <text class="section-heading__title">{{ selectedCategoryName }}</text>
      </view>
      <text v-if="hasLoaded && !loading" class="section-heading__count">{{ products.length }} 款</text>
    </view>

    <view v-if="loading && !hasLoaded" class="state-card">
      <text class="state-card__title">正在准备菜单</text>
      <text class="state-card__description">请稍等片刻…</text>
    </view>
    <view v-else-if="errorMessage" class="state-card">
      <text class="state-card__title">菜单暂时不可用</text>
      <text class="state-card__description">{{ errorMessage }}</text>
      <text class="retry-button" @tap="loadCatalog">重新加载</text>
    </view>
    <view v-else-if="!loading && hasLoaded && products.length === 0" class="state-card">
      <text class="state-card__title">这里还没有商品</text>
      <text class="state-card__description">换个分类看看，或稍后再来。</text>
    </view>
    <view v-else class="product-grid">
      <view v-for="product in products" :key="product.id" class="product-card" @tap="openProduct(product)">
        <view class="product-card__image-wrap">
          <image v-if="product.imageUrl" class="product-card__image" :src="product.imageUrl" mode="aspectFill" />
          <view v-else class="product-card__image-placeholder"><text> D </text></view>
          <text v-if="product.isNew" class="product-badge">NEW</text>
        </view>
        <view class="product-card__body">
          <text class="product-card__category">{{ product.categoryName }}</text>
          <text class="product-card__name">{{ product.name }}</text>
          <text class="product-card__description">{{ product.description || '一份简单而认真制作的好味道。' }}</text>
          <view class="product-card__footer">
            <text class="product-card__price">¥{{ formatPrice(product.price) }}</text>
            <text v-if="product.stock <= 0" class="product-card__soldout">暂时售罄</text>
            <text v-else-if="product.recommended" class="product-card__recommend">人气推荐</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.menu-page { min-height: 100vh; padding: 52rpx 36rpx 80rpx; box-sizing: border-box; }
.hero { padding: 42rpx 36rpx 48rpx; border-radius: 24rpx; background: #eee5d9; }
.hero__eyebrow, .section-heading__eyebrow { display: block; color: #896d57; font-size: 18rpx; letter-spacing: 4rpx; }
.hero__title { display: block; margin-top: 25rpx; color: #39291f; font-size: 42rpx; font-weight: 600; }
.hero__title text { color: #805d43; }
.hero__description { display: block; margin-top: 16rpx; color: #817366; font-size: 24rpx; }
.account-link { display: inline-block; margin-top: 26rpx; padding: 12rpx 22rpx; border: 1rpx solid #cdbca9; border-radius: 24rpx; color: #624a37; font-size: 20rpx; }
.account-link--secondary { margin-left: 12rpx; }
.category-strip { display: flex; gap: 16rpx; overflow-x: auto; padding: 34rpx 0 12rpx; white-space: nowrap; }
.category-chip { flex: none; padding: 15rpx 25rpx; border: 1rpx solid #e5dbd0; border-radius: 30rpx; color: #705d4e; font-size: 23rpx; }
.category-chip--active { border-color: #513827; background: #513827; color: #fffaf4; }
.section-heading { display: flex; align-items: flex-end; justify-content: space-between; margin: 34rpx 0 22rpx; }
.section-heading__title { display: block; margin-top: 8rpx; color: #39291f; font-size: 34rpx; font-weight: 600; }
.section-heading__count { color: #978a7d; font-size: 22rpx; }
.product-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 22rpx; }
.product-card { overflow: hidden; border-radius: 18rpx; background: #fffdfa; box-shadow: 0 5rpx 22rpx rgba(63, 45, 31, .06); }
.product-card__image-wrap { position: relative; height: 250rpx; background: #eee7df; }
.product-card__image { width: 100%; height: 100%; }
.product-card__image-placeholder { display: flex; width: 100%; height: 100%; align-items: center; justify-content: center; color: #9b7e64; font-family: Georgia, serif; font-size: 72rpx; }
.product-badge { position: absolute; top: 14rpx; left: 14rpx; padding: 6rpx 12rpx; border-radius: 12rpx; background: #f8eee2; color: #845d3d; font-size: 16rpx; letter-spacing: 2rpx; }
.product-card__body { padding: 19rpx 20rpx 22rpx; }
.product-card__category { display: block; color: #a1876d; font-size: 18rpx; }
.product-card__name { display: block; overflow: hidden; margin-top: 8rpx; color: #38271d; font-size: 26rpx; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
.product-card__description { display: -webkit-box; overflow: hidden; height: 54rpx; margin-top: 8rpx; color: #918477; font-size: 19rpx; line-height: 1.45; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.product-card__footer { display: flex; align-items: center; justify-content: space-between; margin-top: 17rpx; }
.product-card__price { color: #543b29; font-size: 27rpx; font-weight: 600; }
.product-card__soldout, .product-card__recommend { color: #9d8a79; font-size: 17rpx; }
.state-card { display: flex; min-height: 270rpx; flex-direction: column; align-items: center; justify-content: center; padding: 28rpx; border-radius: 18rpx; background: #fffdfa; text-align: center; }
.state-card__title { color: #48362a; font-size: 28rpx; font-weight: 600; }
.state-card__description { margin-top: 14rpx; color: #998b7d; font-size: 21rpx; }
.retry-button { margin-top: 25rpx; padding: 12rpx 30rpx; border-radius: 24rpx; background: #513827; color: white; font-size: 21rpx; }
</style>
