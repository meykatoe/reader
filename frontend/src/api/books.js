import { request } from './http'

export const listBooks = (mine) => request(`/books?mine=${mine}`)
export const getBook = (id) => request(`/books/${id}`)
export const uploadBook = (formData) => request('/books', { method: 'POST', body: formData })
export const setBookPrivacy = (id, isPrivate) =>
  request(`/books/${id}`, { method: 'PATCH', body: { isPrivate } })
export const deleteBook = (id) => request(`/books/${id}`, { method: 'DELETE' })
