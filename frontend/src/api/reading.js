import { request } from './http'

export const listChapters = (bookId) => request(`/books/${bookId}/chapters`)
export const getChapter = (bookId, index) => request(`/books/${bookId}/chapters/${index}`)
export const getProgress = (bookId) => request(`/books/${bookId}/progress`)
export const saveProgress = (bookId, progress) =>
  request(`/books/${bookId}/progress`, { method: 'PUT', body: progress, keepalive: true })
