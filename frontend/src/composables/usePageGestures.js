import { onBeforeUnmount, onMounted } from 'vue'

const SWIPE_DISTANCE = 50
const EDGE_ZONE = 0.3

// 翻頁操作
export function usePageGestures({ previous, next, toggle }) {
  let start = null

  function onKey(event) {
    if (event.target.closest?.('input, select, textarea, button')) return
    if (['ArrowRight', 'PageDown', ' '].includes(event.key)) {
      event.preventDefault()
      next()
    } else if (['ArrowLeft', 'PageUp'].includes(event.key)) {
      event.preventDefault()
      previous()
    }
  }

  function onPointerDown(event) {
    start = { x: event.clientX, y: event.clientY }
  }

  function onPointerUp(event) {
    if (!start) return
    const dx = event.clientX - start.x
    const dy = event.clientY - start.y
    start = null

    if (event.pointerType !== 'mouse' && Math.abs(dx) > SWIPE_DISTANCE && Math.abs(dx) > Math.abs(dy)) {
      dx < 0 ? next() : previous()
      return
    }
    // 拖曳選字略過
    if (Math.abs(dx) > 8 || Math.abs(dy) > 8 || String(window.getSelection())) return

    const bounds = event.currentTarget.getBoundingClientRect()
    const position = (event.clientX - bounds.left) / bounds.width
    if (position < EDGE_ZONE) previous()
    else if (position > 1 - EDGE_ZONE) next()
    else toggle()
  }

  onMounted(() => window.addEventListener('keydown', onKey))
  onBeforeUnmount(() => window.removeEventListener('keydown', onKey))

  return { onPointerDown, onPointerUp }
}
