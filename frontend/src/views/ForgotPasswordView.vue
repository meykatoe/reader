<script setup>
import { computed, ref } from 'vue'
import { forgotPassword } from '../api/auth'
import AuthCard from '../components/auth/AuthCard.vue'
import CodeField from '../components/auth/CodeField.vue'
import NewPasswordFields from '../components/auth/NewPasswordFields.vue'
import { usePasswordReset } from '../composables/usePasswordReset'

const email = ref('')
const code = ref('')
const notice = ref('')
const emailValid = computed(() => /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email.value))
const { passwords, error, busy, done, reset } = usePasswordReset()

function onSent() {
  error.value = ''
  notice.value = '若此信箱已註冊，驗證碼與重設連結已寄出。可在此輸入驗證碼，或點擊信中連結。'
}
</script>

<template>
  <AuthCard title="忘記密碼" @submit="reset({ email, code })">
    <template v-if="done">
      <p>密碼已重設，請使用新密碼登入。</p>
      <RouterLink to="/login" class="button primary center">前往登入</RouterLink>
    </template>
    <template v-else>
      <label class="field">
        註冊信箱
        <input v-model.trim="email" type="email" autocomplete="email" maxlength="254" required />
      </label>
      <CodeField
        v-model="code"
        :send="() => forgotPassword(email)"
        :disabled="!emailValid"
        @sent="onSent"
        @failed="error = $event"
      />
      <p v-if="notice" class="notice">{{ notice }}</p>
      <NewPasswordFields
        v-model:password="passwords.password"
        v-model:confirmation="passwords.confirmation"
      />
      <p v-if="error" class="error">{{ error }}</p>
      <button class="button primary" :disabled="busy">重設密碼</button>
      <RouterLink to="/login" class="button link center">返回登入</RouterLink>
    </template>
  </AuthCard>
</template>
