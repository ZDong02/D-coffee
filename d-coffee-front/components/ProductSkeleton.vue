<template>
  <view class="product-skeleton">
    <view class="skeleton-image shimmer" />
    <view class="skeleton-body">
      <view class="skeleton-category shimmer" />
      <view class="skeleton-name shimmer" />
      <view class="skeleton-desc shimmer" />
      <view class="skeleton-price shimmer" />
    </view>
  </view>
</template>

<style lang="scss" scoped>
.product-skeleton {
  overflow: hidden;
  border-radius: 18rpx;
  background: #fffdfa;
  box-shadow: 0 5rpx 22rpx rgba(63, 45, 31, 0.06);
}

.skeleton-image {
  height: 250rpx;
  background: #eee7df;
}

.skeleton-body {
  padding: 19rpx 20rpx 22rpx;
}

.skeleton-category {
  width: 80rpx;
  height: 18rpx;
  border-radius: 4rpx;
  background: #eee7df;
}

.skeleton-name {
  width: 140rpx;
  height: 26rpx;
  margin-top: 8rpx;
  border-radius: 4rpx;
  background: #eee7df;
}

.skeleton-desc {
  width: 100%;
  height: 54rpx;
  margin-top: 8rpx;
  border-radius: 4rpx;
  background: #eee7df;
}

.skeleton-price {
  width: 100rpx;
  height: 27rpx;
  margin-top: 17rpx;
  border-radius: 4rpx;
  background: #eee7df;
}

.shimmer {
  position: relative;
  overflow: hidden;

  &::after {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
    animation: shimmer 1.5s infinite;
    content: '';
    transform: translateX(-100%);
  }
}

@keyframes shimmer {
  100% {
    transform: translateX(100%);
  }
}
</style>
