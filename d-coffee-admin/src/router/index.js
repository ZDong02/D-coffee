import { createRouter, createWebHistory } from 'vue-router'
import { TOKEN_KEY } from '../api/http.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: () => import('../views/login/index.vue'), meta: { public: true } },
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', name: 'dashboard', component: () => import('../views/dashboard/index.vue'), meta: { title: '工作台' } },
    { path: '/products', name: 'products', component: () => import('../views/products/index.vue'), meta: { title: '商品管理' } },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  const hasToken = Boolean(localStorage.getItem(TOKEN_KEY))
  if (!to.meta.public && !hasToken) return { name: 'login', query: { redirect: to.fullPath } }
  if (to.name === 'login' && hasToken) return { name: 'dashboard' }
  return true
})

export default router
