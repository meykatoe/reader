import { request } from './http'

export const getQuota = () => request('/quota')
export const requestDownload = (bookId) =>
  request(`/books/${bookId}/downloads`, { method: 'POST' })
export const downloadUrl = (bookId) => `/api/books/${bookId}/download`
