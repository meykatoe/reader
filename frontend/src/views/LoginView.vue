<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { sendRegistrationCode } from '../api/auth'
import AuthCard from '../components/auth/AuthCard.vue'
import CodeField from '../components/auth/CodeField.vue'
import { signIn, signUp } from '../stores/auth'

const route = useRoute()
const router = useRouter()

const mode = ref('login')
const form = reactive({ username: '', password: '', email: '', code: '' })
const error = ref('')
const notice = ref('')
const busy = ref(false)

const isRegister = computed(() => mode.value === 'register')
const emailValid = computed(() => /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email))

function toggleMode() {
  mode.value = isRegister.value ? 'login' : 'register'
  error.value = ''
  notice.value = ''
}

function onSent() {
  error.value = ''
  notice.value = '驗證信已寄出，請查看信箱（含垃圾郵件）。'
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
    if (isRegister.value) await signUp({ ...form })
    else await signIn({ username: form.username, password: form.password })
    router.push(redirectTarget())
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <AuthCard title="Reader" @submit="submit">
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
    <template v-if="isRegister">
      <label class="field">
        信箱
        <input v-model.trim="form.email" type="email" autocomplete="email" maxlength="254" required />
      </label>
      <CodeField
        v-model="form.code"
        :send="() => sendRegistrationCode(form.email)"
        :disabled="!emailValid"
        @sent="onSent"
        @failed="error = $event"
      />
      <p v-if="notice" class="notice">{{ notice }}</p>
    </template>
    <p v-if="error" class="error">{{ error }}</p>
    <button class="button primary" :disabled="busy">{{ isRegister ? '註冊' : '登入' }}</button>
    <RouterLink v-if="!isRegister" to="/forgot-password" class="button link center">
      忘記密碼？
    </RouterLink>
    <button type="button" class="button link" @click="toggleMode">
      {{ isRegister ? '已有帳號？前往登入' : '沒有帳號？立即註冊' }}
    </button>
  </AuthCard>
</template>
