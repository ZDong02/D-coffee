<template>
  <router-view v-if="$route.meta.public" />
  <el-container v-else class="admin-shell">
    <el-aside class="admin-shell__aside" width="232px">
      <div class="brand"><span class="brand__mark">D</span><span>D-coffee</span></div>
      <el-menu :default-active="$route.path" router class="admin-menu">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><span>工作台</span></el-menu-item>
        <el-menu-item index="/products"><el-icon><CoffeeCup /></el-icon><span>商品管理</span></el-menu-item>
      </el-menu>
      <div class="aside-note">D-COFFEE 管理后台</div>
    </el-aside>
    <el-container>
      <el-header class="admin-shell__header">
        <div>
          <div class="header-eyebrow">D-COFFEE 管理后台</div>
          <h1>{{ $route.meta.title || '工作台' }}</h1>
        </div>
        <div class="header-actions">
          <span class="admin-name">{{ adminName }}</span>
          <el-button text @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="admin-shell__main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { CoffeeCup, Odometer } from '@element-plus/icons-vue'
import { ADMIN_KEY, TOKEN_KEY } from './api/http.js'

const router = useRouter()
const adminName = computed(() => {
  try { return JSON.parse(localStorage.getItem(ADMIN_KEY) || '{}').displayName || '管理员' }
  catch { return '管理员' }
})

function logout() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ADMIN_KEY)
  router.replace('/login')
}
</script>
