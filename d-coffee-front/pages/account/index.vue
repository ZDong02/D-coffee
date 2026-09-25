<script setup>
import { computed, ref } from 'vue'
import { userLogin, userRegister } from '../../services/request.js'

const user = ref(uni.getStorageSync('dcoffee-user') || null)
const isRegister = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const form = ref({ phone: '', password: '', confirmPassword: '', nickname: '' })
const title = computed(() => isRegister.value ? '创建 D-coffee 账户' : '欢迎回来')

function validPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

async function submit() {
  errorMessage.value = ''
  const phone = form.value.phone.trim()
  if (!validPhone(phone)) {
    errorMessage.value = '请输入有效的中国大陆手机号'
    return
  }
  if (form.value.password.length < 8) {
    errorMessage.value = '密码至少需要 8 个字符'
    return
  }
  if (isRegister.value && form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }
  submitting.value = true
  try {
    const result = isRegister.value
      ? await userRegister({ phone, password: form.value.password, nickname: form.value.nickname.trim() })
      : await userLogin({ phone, password: form.value.password })
    const safeUser = { id: result.id, phone: result.phone, nickname: result.nickname, role: result.role }
    uni.setStorageSync('dcoffee-user-token', result.token)
    uni.setStorageSync('dcoffee-user', safeUser)
    user.value = safeUser
    uni.showToast({ title: isRegister.value ? '注册成功' : '登录成功', icon: 'success' })
    setTimeout(() => {
      if (getCurrentPages().length > 1) uni.navigateBack()
    }, 450)
  } catch (error) {
    errorMessage.value = error.message || (isRegister.value ? '注册失败，请稍后重试' : '登录失败，请检查账号密码')
  } finally {
    submitting.value = false
  }
}

function logout() {
  uni.removeStorageSync('dcoffee-user-token')
  uni.removeStorageSync('dcoffee-user')
  user.value = null
  form.value.password = ''
  uni.showToast({ title: '已退出登录', icon: 'none' })
}
</script>

<template>
  <view class="account-page">
    <view class="account-brand"><text class="brand-mark">D</text><text class="brand-name">D-COFFEE</text></view>
    <view v-if="user" class="account-card">
      <text class="card-eyebrow">我的账户</text>
      <text class="welcome-title">{{ user.nickname || '咖啡用户' }}</text>
      <text class="account-phone">{{ user.phone }}</text>
      <text class="account-note">账号已连接，可以继续浏览菜单。购物车功能正在开发中。</text>
      <text class="primary-button logout-button" @tap="logout">退出登录</text>
    </view>
    <view v-else class="account-card">
      <text class="card-eyebrow">MEMBER ACCOUNT</text>
      <text class="welcome-title">{{ title }}</text>
      <text class="welcome-copy">登录后继续探索每日咖啡菜单。</text>
      <label v-if="isRegister" class="form-label">昵称（选填）</label>
      <input v-if="isRegister" v-model="form.nickname" class="form-input" maxlength="64" placeholder="怎么称呼你" />
      <label class="form-label">手机号</label>
      <input v-model="form.phone" class="form-input" type="number" maxlength="11" placeholder="请输入手机号" />
      <label class="form-label">密码</label>
      <input v-model="form.password" class="form-input" password maxlength="72" placeholder="至少 8 个字符" />
      <template v-if="isRegister">
        <label class="form-label">确认密码</label>
        <input v-model="form.confirmPassword" class="form-input" password maxlength="72" placeholder="请再次输入密码" />
      </template>
      <text v-if="errorMessage" class="error-message">{{ errorMessage }}</text>
      <text class="primary-button" :class="{ 'primary-button--disabled': submitting }" @tap="!submitting && submit()">
        {{ submitting ? '请稍候…' : isRegister ? '注册并登录' : '登录' }}
      </text>
      <text class="switch-mode" @tap="isRegister = !isRegister; errorMessage = ''">
        {{ isRegister ? '已有账户？返回登录' : '还没有账户？立即注册' }}
      </text>
      <text class="security-note">当前为开发阶段注册流程，尚未接入短信验证码；正式开放注册前应启用手机号验证。</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.account-page { min-height: 100vh; box-sizing: border-box; padding: 60rpx 38rpx; background: #f7f3ed; }
.account-brand { display: flex; align-items: center; gap: 14rpx; margin: 20rpx 0 50rpx; }
.brand-mark { display: grid; width: 58rpx; height: 58rpx; place-items: center; border-radius: 50%; background: #513827; color: #fffaf4; font-family: Georgia, serif; font-size: 36rpx; }
.brand-name { color: #725940; font-size: 20rpx; letter-spacing: 5rpx; }
.account-card { padding: 42rpx 34rpx; border: 1rpx solid #eee5da; border-radius: 22rpx; background: #fffdfa; box-shadow: 0 12rpx 34rpx rgba(63, 45, 31, .05); }
.card-eyebrow { display: block; color: #a1876d; font-size: 19rpx; letter-spacing: 4rpx; }
.welcome-title { display: block; margin-top: 18rpx; color: #39291f; font-size: 38rpx; font-weight: 600; }
.welcome-copy, .account-phone { display: block; margin-top: 12rpx; color: #918477; font-size: 22rpx; }
.form-label { display: block; margin: 27rpx 0 10rpx; color: #604c3c; font-size: 21rpx; }
.form-input { box-sizing: border-box; width: 100%; height: 82rpx; padding: 0 22rpx; border: 1rpx solid #e8dfd5; border-radius: 12rpx; background: #fff; color: #39291f; font-size: 23rpx; }
.primary-button { display: block; margin-top: 34rpx; padding: 23rpx; border-radius: 40rpx; background: #513827; color: #fffaf4; font-size: 23rpx; text-align: center; }
.primary-button--disabled { opacity: .6; }
.logout-button { margin-top: 38rpx; }
.switch-mode { display: block; margin-top: 25rpx; color: #805d43; font-size: 21rpx; text-align: center; }
.error-message { display: block; margin-top: 20rpx; color: #bc5b4c; font-size: 20rpx; }
.security-note, .account-note { display: block; margin-top: 28rpx; color: #a09284; font-size: 18rpx; line-height: 1.6; }
</style>
