import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

const SPREAD_MIN_WIDTH = 960

function waitForImages(element) {
  const pending = [...element.querySelectorAll('img')]
    .filter((image) => !image.complete)
    .map(
      (image) =>
        new Promise((resolve) => {
          image.addEventListener('load', resolve, { once: true })
          image.addEventListener('error', resolve, { once: true })
        }),
    )
  return Promise.all(pending)
}

// 分欄分頁
export function usePagination(viewport, content, { gap = 48, allowSpread = () => true } = {}) {
  const page = ref(0)
  const pageCount = ref(1)
  const columnWidth = ref(0)
  const pageWidth = ref(0)
  const pageHeight = ref(0)
  const animate = ref(false)

  // 重排定位用
  let anchor = 0
  let version = 0
  let observer = null

  const contentStyle = computed(() => ({
    columnWidth: `${columnWidth.value}px`,
    columnGap: `${gap}px`,
    transform: `translateX(${-page.value * (pageWidth.value + gap)}px)`,
    transition: animate.value ? 'transform 0.22s ease' : 'none',
    '--page-height': `${pageHeight.value}px`,
  }))

  async function layout() {
    const current = ++version
    if (!viewport.value || !content.value) return false

    const width = viewport.value.clientWidth
    const columns = allowSpread() && width >= SPREAD_MIN_WIDTH ? 2 : 1
    animate.value = false
    pageWidth.value = width
    pageHeight.value = viewport.value.clientHeight
    columnWidth.value = Math.floor((width - gap * (columns - 1)) / columns)

    await nextTick()
    if (!content.value) return false
    await waitForImages(content.value)
    await document.fonts.ready
    if (current !== version || !content.value) return false

    const total = Math.round((content.value.scrollWidth + gap) / (columnWidth.value + gap))
    pageCount.value = Math.max(1, Math.ceil(total / columns))
    page.value = Math.min(pageCount.value - 1, Math.floor(anchor * pageCount.value + 0.001))
    return true
  }

  async function openAt(ratio) {
    anchor = ratio
    if (!(await layout())) return false
    anchor = page.value / pageCount.value
    return true
  }

  function turn(step) {
    const target = page.value + step
    if (target < 0 || target >= pageCount.value) return false
    animate.value = true
    page.value = target
    anchor = target / pageCount.value
    return true
  }

  onMounted(() => {
    observer = new ResizeObserver(() => layout())
    observer.observe(viewport.value)
  })
  onBeforeUnmount(() => observer?.disconnect())

  return { page, pageCount, contentStyle, layout, openAt, turn }
}
