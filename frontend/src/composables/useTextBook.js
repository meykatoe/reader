import { computed, ref } from 'vue'
import { getChapter, getProgress, listChapters } from '../api/reading'

// 章節載入
export function useTextBook(bookId) {
  const chapters = ref([])
  const chapter = ref(null)
  const loading = ref(true)
  const error = ref('')
  const cache = new Map()
  let latest = 0

  const totalChars = computed(() => chapters.value.reduce((sum, item) => sum + item.charCount, 0))

  function fetchChapter(index) {
    if (!cache.has(index)) {
      const task = getChapter(bookId, index)
      task.catch(() => cache.delete(index))
      cache.set(index, task)
    }
    return cache.get(index)
  }

  async function load(index) {
    const ticket = ++latest
    error.value = ''
    loading.value = true
    try {
      const result = await fetchChapter(index)
      if (ticket !== latest) return false
      chapter.value = result
      // 預載下一章
      if (index + 1 < chapters.value.length) fetchChapter(index + 1).catch(() => {})
      return true
    } catch (e) {
      if (ticket === latest) error.value = e.message
      return false
    } finally {
      if (ticket === latest) loading.value = false
    }
  }

  async function init() {
    const [list, progress] = await Promise.all([listChapters(bookId), getProgress(bookId)])
    chapters.value = list
    return progress
  }

  // 全書百分比
  function percentAt(index, ratio) {
    if (!totalChars.value) {
      return ((index + ratio) / Math.max(1, chapters.value.length)) * 100
    }
    const before = chapters.value.slice(0, index).reduce((sum, item) => sum + item.charCount, 0)
    return ((before + (chapters.value[index]?.charCount ?? 0) * ratio) / totalChars.value) * 100
  }

  return { chapters, chapter, loading, error, init, load, percentAt }
}
