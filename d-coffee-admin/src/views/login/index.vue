<template>
  <main class="login-page">
    <section class="login-card">
      <div class="login-card__brand"><span>D</span><div>D-coffee<small>管理后台</small></div></div>
      <p class="login-card__eyebrow">欢迎回来</p>
      <h1>管理员登录</h1>
      <p class="login-card__description">登录后管理菜单商品与库存</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username"><el-input v-model="form.username" autocomplete="username" placeholder="管理员账号" :prefix-icon="User" /></el-form-item>
        <el-form-item prop="password"><el-input v-model="form.password" autocomplete="current-password" placeholder="登录密码" type="password" show-password :prefix-icon="Lock" /></el-form-item>
        <el-button class="login-card__submit" type="primary" :loading="loading" @click="submit">登录管理后台</el-button>
      </el-form>
      <p class="login-card__footnote">请使用已开通的管理员账号。忘记密码请联系系统负责人。</p>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { adminLogin } from '../../api/admin.js'
import { ADMIN_KEY, TOKEN_KEY } from '../../api/http.js'

const formRef = ref()
const loading = ref(false)
const route = useRoute()
const router = useRouter()
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
}

async function submit() {
  if (loading.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    const result = await adminLogin(form)
    localStorage.setItem(TOKEN_KEY, result.token)
    localStorage.setItem(ADMIN_KEY, JSON.stringify({ id: result.id, username: result.username, displayName: result.displayName }))
    ElMessage.success('登录成功')
    router.replace(typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard')
  } catch (error) {
    if (error instanceof Error) ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page { display: grid; min-height: 100vh; place-items: center; padding: 24px; background: radial-gradient(circle at 18% 15%, #eee3d5 0, transparent 36%), #f7f3ed; }
.login-card { width: min(100%, 420px); padding: 42px; border: 1px solid #e8ded2; border-radius: 18px; background: #fffdfa; box-shadow: 0 24px 80px rgba(59, 42, 29, .09); }
.login-card__brand { display: flex; align-items: center; gap: 12px; color: #513827; font-family: Georgia, serif; font-size: 20px; font-weight: 600; }
.login-card__brand > span { display: grid; width: 40px; height: 40px; place-items: center; border: 1px solid #806652; border-radius: 50%; }
.login-card__brand small { display: block; margin-top: 2px; color: #9a806c; font-family: 'Microsoft YaHei', sans-serif; font-size: 11px; font-weight: 400; }
.login-card__eyebrow { margin: 42px 0 8px; color: #a18468; font-size: 13px; }
.login-card h1 { margin: 0; color: #39291f; font-size: 27px; }
.login-card__description { margin: 12px 0 28px; color: #958679; font-size: 14px; }
.login-card__submit { width: 100%; margin-top: 6px; }
.login-card__footnote { margin: 24px 0 0; color: #a4978a; font-size: 12px; line-height: 1.7; }
</style>
