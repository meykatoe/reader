import { request } from '../api/http'

const query = (params) => new URLSearchParams(params).toString()

export const adminLogin = (form) => request('/admin/auth/login', { method: 'POST', body: form })
export const adminLogout = () => request('/admin/auth/logout', { method: 'POST' })
export const fetchAdmin = () => request('/admin/auth/me')

export const listAdminBooks = (params) => request(`/admin/books?${query(params)}`)
export const createAdminBook = (formData) =>
  request('/admin/books', { method: 'POST', body: formData })
export const updateAdminBook = (id, body) => request(`/admin/books/${id}`, { method: 'PUT', body })
export const listAdminUsers = (params) => request(`/admin/users?${query(params)}`)
export const deleteAdminUser = (id) => request(`/admin/users/${id}`, { method: 'DELETE' })

export const deleteAdminBook = (id) => request(`/admin/books/${id}`, { method: 'DELETE' })
