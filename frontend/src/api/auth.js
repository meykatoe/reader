import { request } from './http'

export const register = (form) => request('/auth/register', { method: 'POST', body: form })
export const login = (form) => request('/auth/login', { method: 'POST', body: form })
export const logout = () => request('/auth/logout', { method: 'POST' })
export const fetchMe = () => request('/auth/me')
