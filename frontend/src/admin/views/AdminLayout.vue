<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { adminAuth, can, signOutAdmin } from '../store'
import '../admin.css'

const router = useRouter()

const MENU = [{ to: '/admin/books', label: '圖書管理', authority: 'BOOK_MANAGE' }]

const menu = computed(() => MENU.filter((item) => can(item.authority)))
const roleName = computed(() => (adminAuth.admin?.permission === 1 ? '超級管理員' : '一般管理員'))

async function signOut() {
  await signOutAdmin()
  router.push('/admin/login')
}
</script>

<template>
  <header class="admin-header">
    <strong>後臺管理</strong>
    <nav>
      <RouterLink v-for="item in menu" :key="item.to" :to="item.to">{{ item.label }}</RouterLink>
    </nav>
    <span class="account">{{ adminAuth.admin?.username }}（{{ roleName }}）</span>
    <button class="button" @click="signOut">登出</button>
  </header>
  <main class="page">
    <RouterView />
  </main>
</template>

<style scoped>
.admin-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  padding: 10px 24px;
  color: #fff;
  background: #2a3442;
}

nav {
  flex: 1;
  display: flex;
  gap: 4px;
}

nav a {
  padding: 6px 12px;
  color: #cfd8e3;
  text-decoration: none;
  border-radius: 6px;
}

nav a.router-link-active {
  color: #fff;
  background: rgb(255 255 255 / 16%);
}

.account {
  font-size: 14px;
}
</style>
