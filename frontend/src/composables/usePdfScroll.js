import { nextTick, onBeforeUnmount, ref } from 'vue'

const PRELOAD_MARGIN = '800px 0px'

// 捲動與可見頁
export function usePdfScroll(container, onMove) {
  const current = ref(0)
  const nearby = ref(new Set())
  let observer = null
  let frame = 0
  let ratio = 0

  const pages = () => container.value?.querySelectorAll('[data-page]') ?? []

  // 二分搜尋目前頁
  function locate() {
    const list = pages()
    const probe = container.value.scrollTop + 1
    let low = 0
    let high = list.length - 1
    while (low < high) {
      const middle = Math.ceil((low + high) / 2)
      if (list[middle].offsetTop <= probe) low = middle
      else high = middle - 1
    }
    return { index: low, element: list[low] }
  }

  function onScroll() {
    cancelAnimationFrame(frame)
    frame = requestAnimationFrame(() => {
      if (!container.value) return
      const { index, element } = locate()
      if (!element) return
      current.value = index
      const offset = container.value.scrollTop - element.offsetTop
      ratio = Math.min(1, Math.max(0, offset / element.offsetHeight))
      onMove(index, ratio)
    })
  }

  function scrollTo(index, offset = 0) {
    const element = pages()[index]
    if (!element) return
    container.value.scrollTop = element.offsetTop + element.offsetHeight * offset
    current.value = index
    ratio = offset
  }

  // 縮放後復位
  async function keepPosition(change) {
    const index = current.value
    const offset = ratio
    change()
    await nextTick()
    scrollTo(index, offset)
  }

  function observe() {
    observer = new IntersectionObserver(
      (entries) => {
        const next = new Set(nearby.value)
        for (const entry of entries) {
          const index = Number(entry.target.dataset.page)
          entry.isIntersecting ? next.add(index) : next.delete(index)
        }
        nearby.value = next
      },
      { root: container.value, rootMargin: PRELOAD_MARGIN },
    )
    pages().forEach((element) => observer.observe(element))
  }

  onBeforeUnmount(() => {
    observer?.disconnect()
    cancelAnimationFrame(frame)
  })

  return { current, nearby, observe, onScroll, scrollTo, keepPosition }
}
