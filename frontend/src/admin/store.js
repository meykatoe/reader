import { reactive } from 'vue'
import { adminLogin, adminLogout, fetchAdmin } from './api'

export const adminAuth = reactive({
  admin: null,
  loaded: false,
})

// 僅控制畫面
export const can = (authority) => Boolean(adminAuth.admin?.authorities.includes(authority))

export async function loadAdmin() {
  if (adminAuth.loaded) return
  try {
    adminAuth.admin = await fetchAdmin()
  } catch (error) {
    if (error.status !== 401) throw error
    adminAuth.admin = null
  }
  adminAuth.loaded = true
}

export async function signInAdmin(form) {
  adminAuth.admin = await adminLogin(form)
  adminAuth.loaded = true
}

export async function signOutAdmin() {
  await adminLogout()
  adminAuth.admin = null
}

export function clearAdmin() {
  adminAuth.admin = null
}
