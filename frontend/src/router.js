import { createRouter, createWebHistory } from 'vue-router'
import { onUnauthorized } from './api/http'
import { auth, clearUser, loadUser } from './stores/auth'
import ShelfView from './views/ShelfView.vue'
import LoginView from './views/LoginView.vue'
import ReaderView from './views/ReaderView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: ShelfView },
    { path: '/books/:id(\\d+)', component: ReaderView },
    { path: '/login', component: LoginView, meta: { public: true } },
  ],
})

router.beforeEach(async (to) => {
  await loadUser()
  if (!to.meta.public && !auth.user) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && auth.user) {
    return '/'
  }
})

// 登入逾時
onUnauthorized(() => {
  clearUser()
  router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
})

export default router
