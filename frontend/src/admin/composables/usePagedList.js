import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const PAGE_SIZE = 20
const SEARCH_DELAY = 300

// 分頁與搜尋
export function usePagedList(fetcher) {
  const items = ref([])
  const total = ref(0)
  const page = ref(0)
  const keyword = ref('')
  const loading = ref(true)
  const error = ref('')
  const pageCount = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))
  let latest = 0
  let timer = 0

  async function load() {
    const ticket = ++latest
    loading.value = true
    try {
      const result = await fetcher({ keyword: keyword.value, page: page.value, size: PAGE_SIZE })
      if (ticket !== latest) return
      items.value = result.items
      total.value = result.total
      // 末頁刪空後退
      if (!result.items.length && page.value > 0) page.value--
    } catch (e) {
      if (ticket === latest) error.value = e.message
    } finally {
      if (ticket === latest) loading.value = false
    }
  }

  async function run(action) {
    error.value = ''
    try {
      await action()
    } catch (e) {
      error.value = e.message
    }
    await load()
  }

  watch(page, load)
  watch(keyword, () => {
    clearTimeout(timer)
    timer = setTimeout(() => (page.value === 0 ? load() : (page.value = 0)), SEARCH_DELAY)
  })
  onMounted(load)
  onBeforeUnmount(() => clearTimeout(timer))

  return { items, total, page, pageCount, keyword, loading, error, load, run }
}
