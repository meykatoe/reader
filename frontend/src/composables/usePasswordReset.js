import { reactive, ref } from 'vue'
import { resetPassword } from '../api/auth'
import { clearUser } from '../stores/auth'

// 送出新密碼
export function usePasswordReset() {
  const passwords = reactive({ password: '', confirmation: '' })
  const error = ref('')
  const busy = ref(false)
  const done = ref(false)

  async function reset(proof) {
    if (passwords.password !== passwords.confirmation) {
      error.value = '兩次輸入的密碼不一致'
      return
    }
    busy.value = true
    error.value = ''
    try {
      await resetPassword({ ...proof, password: passwords.password })
      // 舊登入已失效
      clearUser()
      done.value = true
    } catch (e) {
      error.value = e.message
    } finally {
      busy.value = false
    }
  }

  return { passwords, error, busy, done, reset }
}
