<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import 'pdfjs-dist/legacy/web/pdf_viewer.css'
import { getProgress } from '../../api/reading'
import { usePdfDocument } from '../../composables/usePdfDocument'
import { usePdfScroll } from '../../composables/usePdfScroll'
import { useProgressSaver } from '../../composables/useProgressSaver'
import PdfPage from './pdf/PdfPage.vue'
import PdfToolbar from './pdf/PdfToolbar.vue'
import TocDrawer from './TocDrawer.vue'

const ZOOM_STEPS = [0.5, 0.67, 0.8, 1, 1.25, 1.5, 2, 2.5, 3]
const MAX_FIT_WIDTH = 1000
const PADDING = 32

const props = defineProps({
  book: { type: Object, required: true },
})

const container = ref(null)
const containerWidth = ref(0)
const zoom = ref(1)
const showOutline = ref(false)
const loading = ref(true)
const error = ref('')

const { pdf, outline, baseSize, open } = usePdfDocument(`/api/books/${props.book.id}/file`)
const { save } = useProgressSaver(props.book.id)
const { current, nearby, observe, onScroll, scrollTo, keepPosition } = usePdfScroll(container, save)

const pageCount = computed(() => pdf.value?.numPages ?? 0)

// 1 代表符合寬度
const scale = computed(() => {
  const fitWidth = Math.min(Math.max(containerWidth.value - PADDING, 100), MAX_FIT_WIDTH)
  return (fitWidth / baseSize.value.width) * zoom.value
})

function stepZoom(direction) {
  const index = ZOOM_STEPS.indexOf(zoom.value) + direction
  if (index >= 0 && index < ZOOM_STEPS.length) {
    keepPosition(() => (zoom.value = ZOOM_STEPS[index]))
  }
}

function goTo(index) {
  if (index < 0 || index >= pageCount.value) return
  scrollTo(index)
  save(index, 0)
}

function selectOutline(index) {
  showOutline.value = false
  goTo(index)
}

function onKey(event) {
  if (showOutline.value || event.target.closest?.('input')) return
  if (event.key === 'ArrowRight') goTo(current.value + 1)
  else if (event.key === 'ArrowLeft') goTo(current.value - 1)
}

let resizeObserver = null

onMounted(async () => {
  window.addEventListener('keydown', onKey)
  containerWidth.value = container.value.clientWidth
  resizeObserver = new ResizeObserver(([entry]) => {
    const width = entry.contentRect.width
    if (width !== containerWidth.value) keepPosition(() => (containerWidth.value = width))
  })
  resizeObserver.observe(container.value)

  try {
    const [progress] = await Promise.all([getProgress(props.book.id), open()])
    await nextTick()
    scrollTo(Math.min(progress.location, pageCount.value - 1), progress.ratio)
    observe()
    container.value.focus()
  } catch (e) {
    error.value = e.status ? e.message : '無法開啟 PDF'
  } finally {
    loading.value = false
  }
})

watch(current, () => (showOutline.value = false))

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKey)
  resizeObserver?.disconnect()
})
</script>

<template>
  <div class="reader">
    <PdfToolbar
      :title="book.title"
      :page="current"
      :page-count="pageCount"
      :zoom="zoom"
      :has-outline="outline.length > 0"
      @outline="showOutline = true"
      @jump="goTo"
      @zoom="stepZoom"
      @fit="keepPosition(() => (zoom = 1))"
    />

    <TocDrawer
      v-if="showOutline"
      :items="outline"
      :current="current"
      @select="selectOutline"
      @close="showOutline = false"
    />

    <div ref="container" class="pages" tabindex="0" @scroll.passive="onScroll">
      <p v-if="error" class="status">{{ error }}</p>
      <p v-else-if="loading" class="status">載入中…</p>
      <PdfPage
        v-for="number in pageCount"
        :key="number"
        :data-page="number - 1"
        :pdf="pdf"
        :number="number"
        :scale="scale"
        :base-size="baseSize"
        :active="nearby.has(number - 1)"
      />
    </div>
  </div>
</template>

<style scoped>
.reader {
  height: 100dvh;
  display: flex;
  flex-direction: column;
}

.pages {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: auto;
  padding: 12px 16px;
  background: #525659;
  outline: none;
}

.status {
  color: #fff;
  text-align: center;
}
</style>
