<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { signIn, signUp } from '../stores/auth'

const route = useRoute()
const router = useRouter()

const mode = ref('login')
const form = reactive({ username: '', password: '' })
const error = ref('')
const busy = ref(false)

const isRegister = computed(() => mode.value === 'register')

function toggleMode() {
  mode.value = isRegister.value ? 'login' : 'register'
  error.value = ''
}

function redirectTarget() {
  const target = route.query.redirect
  // 限站內路徑
  return typeof target === 'string' && target.startsWith('/') && !target.startsWith('//')
    ? target
    : '/'
}

async function submit() {
  busy.value = true
  error.value = ''
  try {
    await (isRegister.value ? signUp : signIn)({ ...form })
    router.push(redirectTarget())
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
      <h1>Reader</h1>
      <label class="field">
        使用者名稱
        <input v-model.trim="form.username" autocomplete="username" required />
      </label>
      <label class="field">
        密碼
        <input
          v-model="form.password"
          type="password"
          :autocomplete="isRegister ? 'new-password' : 'current-password'"
          required
        />
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <button class="button primary" :disabled="busy">{{ isRegister ? '註冊' : '登入' }}</button>
      <button type="button" class="button link" @click="toggleMode">
        {{ isRegister ? '已有帳號？前往登入' : '沒有帳號？立即註冊' }}
      </button>
    </form>
  </main>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 16px;
}

.card {
  width: min(360px, 100%);
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 32px 28px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
}

h1 {
  margin: 0 0 8px;
  text-align: center;
}
</style>
