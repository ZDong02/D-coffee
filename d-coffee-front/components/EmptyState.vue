<script setup>
defineProps({
  icon: { type: String, default: '📦' },
  title: { type: String, default: '暂无内容' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' },
})

defineEmits(['action'])
</script>

<template>
  <view class="empty-state">
    <text class="empty-icon">{{ icon }}</text>
    <text class="empty-title">{{ title }}</text>
    <text v-if="description" class="empty-description">{{ description }}</text>
    <text v-if="actionText" class="empty-action" @tap="$emit('action')">{{ actionText }}</text>
  </view>
</template>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  min-height: 50vh;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18rpx;
  padding: 40rpx;
  text-align: center;
}

.empty-icon {
  font-size: 100rpx;
  line-height: 1;
  opacity: 0.5;
}

.empty-title {
  color: #48362a;
  font-size: 27rpx;
  font-weight: 600;
}

.empty-description {
  color: #a09284;
  font-size: 20rpx;
  line-height: 1.6;
}

.empty-action {
  margin-top: 12rpx;
  padding: 13rpx 28rpx;
  border-radius: 28rpx;
  background: #513827;
  color: #fffaf4;
  font-size: 20rpx;
  transition: all 250ms cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    opacity: 0.8;
    transform: scale(0.96);
  }
}
</style>
