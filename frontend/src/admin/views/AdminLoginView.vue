<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { signInAdmin } from '../store'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const error = ref('')
const busy = ref(false)

async function submit() {
  busy.value = true
  error.value = ''
  try {
    await signInAdmin({ ...form })
    router.push('/admin')
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <main class="login">
    <form class="card" @submit.prevent="submit">
      <h1>後臺管理</h1>
      <label class="field">
        管理員帳號
        <input v-model.trim="form.username" autocomplete="username" required />
      </label>
      <label class="field">
        密碼
        <input v-model="form.password" type="password" autocomplete="current-password" required />
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <button class="button primary" :disabled="busy">登入</button>
    </form>
  </main>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 16px;
  background: #2a3442;
}

.card {
  width: min(360px, 100%);
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 32px 28px;
  background: var(--surface);
  border-radius: 12px;
}

h1 {
  margin: 0 0 8px;
  text-align: center;
}
</style>
