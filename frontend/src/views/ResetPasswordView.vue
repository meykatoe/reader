<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthCard from '../components/auth/AuthCard.vue'
import NewPasswordFields from '../components/auth/NewPasswordFields.vue'
import { usePasswordReset } from '../composables/usePasswordReset'

const route = useRoute()
const router = useRouter()
const token = new URLSearchParams(route.hash.slice(1)).get('token') ?? ''
const { passwords, error, busy, done, reset } = usePasswordReset()

// 清除網址憑證
onMounted(() => router.replace({ path: route.path, hash: '' }))
</script>

<template>
  <AuthCard title="重設密碼" @submit="reset({ token })">
    <template v-if="done">
      <p>密碼已重設，請使用新密碼登入。</p>
      <RouterLink to="/login" class="button primary center">前往登入</RouterLink>
    </template>
    <template v-else-if="!token">
      <p class="error">連結不完整，請重新點擊信中的連結。</p>
      <RouterLink to="/forgot-password" class="button primary center">重新申請</RouterLink>
    </template>
    <template v-else>
      <NewPasswordFields
        v-model:password="passwords.password"
        v-model:confirmation="passwords.confirmation"
      />
      <p v-if="error" class="error">{{ error }}</p>
      <button class="button primary" :disabled="busy">重設密碼</button>
      <RouterLink to="/forgot-password" class="button link center">連結失效？重新申請</RouterLink>
    </template>
  </AuthCard>
</template>
