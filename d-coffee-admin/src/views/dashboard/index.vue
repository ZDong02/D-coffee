<template>
  <section class="dashboard-welcome">
    <div class="dashboard-welcome__mark"><CoffeeCup /></div>
    <div class="dashboard-welcome__content">
      <p class="dashboard-welcome__eyebrow">欢迎回到 D-COFFEE</p>
      <h2>让每一杯咖啡都恰到好处。</h2>
      <p>从商品管理开始维护实际菜单、价格与库存。</p>
      <el-button type="primary" @click="$router.push('/products')">进入商品管理</el-button>
    </div>
  </section>
</template>

<script setup>
import { CoffeeCup } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.dashboard-welcome { display: flex; max-width: 920px; min-height: 250px; align-items: center; gap: 28px; padding: 42px; border: 1px solid #e7ded4; border-radius: 14px; background: #fffdfa; }
.dashboard-welcome__mark { display: grid; width: 72px; height: 72px; flex: 0 0 auto; place-items: center; border-radius: 50%; background: #f1e8de; color: #72553f; font-size: 30px; }
.dashboard-welcome__eyebrow { color: #9a806c; font-size: 12px; letter-spacing: 1px; }
.dashboard-welcome h2 { margin: 12px 0; font-size: 24px; }
.dashboard-welcome__content > p:not(.dashboard-welcome__eyebrow) { margin-bottom: 22px; color: #88796e; }
@media (max-width: 640px) { .dashboard-welcome { align-items: flex-start; flex-direction: column; padding: 24px; } }
</style>
