import { onBeforeUnmount, onMounted } from 'vue'
import { saveProgress } from '../api/reading'

const DELAY = 800

export function useProgressSaver(bookId) {
  let pending = null
  let timer = 0

  function flush() {
    clearTimeout(timer)
    if (!pending) return
    const progress = pending
    pending = null
    // 失敗不擋閱讀
    saveProgress(bookId, progress).catch(() => {})
  }

  function save(location, ratio) {
    pending = { location, ratio: Math.min(1, Math.max(0, ratio)) }
    clearTimeout(timer)
    timer = setTimeout(flush, DELAY)
  }

  onMounted(() => window.addEventListener('pagehide', flush))
  onBeforeUnmount(() => {
    window.removeEventListener('pagehide', flush)
    flush()
  })

  return { save }
}
