import { reactive } from 'vue'
import * as authApi from '../api/auth'

export const auth = reactive({
  user: null,
  loaded: false,
})

export async function loadUser() {
  if (auth.loaded) return
  try {
    auth.user = await authApi.fetchMe()
  } catch (error) {
    if (error.status !== 401) throw error
    auth.user = null
  }
  auth.loaded = true
}

export async function signIn(form) {
  auth.user = await authApi.login(form)
  auth.loaded = true
}

export async function signUp(form) {
  await authApi.register(form)
  await signIn({ username: form.username, password: form.password })
}

export async function signOut() {
  await authApi.logout()
  auth.user = null
}

export function clearUser() {
  auth.user = null
}
