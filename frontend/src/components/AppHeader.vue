<script setup>
import { useRouter } from 'vue-router'
import { auth, signOut } from '../stores/auth'

const router = useRouter()

async function handleSignOut() {
  await signOut()
  router.push('/login')
}
</script>

<template>
  <header class="header">
    <RouterLink to="/" class="brand">Reader</RouterLink>
    <div class="account">
      <span>{{ auth.user?.username }}</span>
      <button class="button" @click="handleSignOut">登出</button>
    </div>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
}

.brand {
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
  text-decoration: none;
}

.account {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
