import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const PAGE_SIZE = 20
const SEARCH_DELAY = 300

// 分頁與搜尋
export function usePagedList(fetcher) {
  const items = ref([])
  const total = ref(0)
  const page = ref(0)
  const keyword = ref('')
  const deleted = ref(false)
  const loading = ref(true)
  // 防連點誤刪
  const pending = ref(false)
  const error = ref('')
  const pageCount = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))
  let latest = 0
  let timer = 0

  async function load() {
    const ticket = ++latest
    loading.value = true
    try {
      const result = await fetcher({
        keyword: keyword.value,
        deleted: deleted.value,
        page: page.value,
        size: PAGE_SIZE,
      })
      if (ticket !== latest) return
      items.value = result.items
      total.value = result.total
      // 末頁空則退
      if (!result.items.length && page.value > 0) page.value--
    } catch (e) {
      if (ticket === latest) error.value = e.message
    } finally {
      if (ticket === latest) loading.value = false
    }
  }

  async function run(action) {
    if (pending.value) return false
    pending.value = true
    error.value = ''
    let succeeded = true
    try {
      await action()
    } catch (e) {
      error.value = e.message
      succeeded = false
    }
    await load()
    pending.value = false
    return succeeded
  }

  const reload = () => (page.value === 0 ? load() : (page.value = 0))

  watch(page, load)
  watch(deleted, () => {
    items.value = []
    reload()
  })
  watch(keyword, () => {
    clearTimeout(timer)
    timer = setTimeout(reload, SEARCH_DELAY)
  })
  onMounted(load)
  onBeforeUnmount(() => clearTimeout(timer))

  return { items, total, page, pageCount, keyword, deleted, loading, pending, error, load, run }
}
