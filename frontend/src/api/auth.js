import { request } from './http'

const post = (path, body) => request(path, { method: 'POST', body })

export const register = (form) => post('/auth/register', form)
export const sendRegistrationCode = (email) => post('/auth/register/code', { email })
export const forgotPassword = (email) => post('/auth/password/forgot', { email })
export const resetPassword = (body) => post('/auth/password/reset', body)
export const login = (form) => post('/auth/login', form)
export const logout = () => post('/auth/logout')
export const fetchMe = () => request('/auth/me')
