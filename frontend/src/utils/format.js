export function formatSize(bytes) {
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

// 臺北時區
export function formatDate(value) {
  return new Date(value).toLocaleDateString('zh-TW', { timeZone: 'Asia/Taipei' })
}
