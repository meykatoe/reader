<script setup>
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { TextLayer } from 'pdfjs-dist/legacy/build/pdf.mjs'

const MAX_CANVAS_PIXELS = 16_000_000

const props = defineProps({
  pdf: { type: Object, required: true },
  number: { type: Number, required: true },
  scale: { type: Number, required: true },
  baseSize: { type: Object, required: true },
  // 是否靠近視窗
  active: { type: Boolean, default: false },
})

const canvas = ref(null)
const textContainer = ref(null)
const size = shallowRef(null)
const failed = ref(false)

let renderTask = null
let textLayer = null
let version = 0

function cancel() {
  version++
  renderTask?.cancel()
  textLayer?.cancel()
  renderTask = null
  textLayer = null
}

// 釋放記憶體
function release() {
  cancel()
  if (canvas.value) {
    canvas.value.width = 0
    canvas.value.height = 0
  }
  textContainer.value?.replaceChildren()
}

async function render() {
  if (!props.active) {
    release()
    return
  }
  cancel()
  const current = version
  try {
    const page = await props.pdf.getPage(props.number)
    if (current !== version) return
    const original = page.getViewport({ scale: 1 })
    size.value = { width: original.width, height: original.height }

    const viewport = page.getViewport({ scale: props.scale })
    const maxRatio = Math.sqrt(MAX_CANVAS_PIXELS / (viewport.width * viewport.height))
    const ratio = Math.min(window.devicePixelRatio || 1, maxRatio)
    canvas.value.width = Math.floor(viewport.width * ratio)
    canvas.value.height = Math.floor(viewport.height * ratio)

    renderTask = page.render({
      canvas: canvas.value,
      viewport,
      transform: ratio === 1 ? null : [ratio, 0, 0, ratio, 0, 0],
    })
    await renderTask.promise
    if (current !== version) return

    textContainer.value.replaceChildren()
    textLayer = new TextLayer({
      textContentSource: page.streamTextContent(),
      container: textContainer.value,
      viewport,
    })
    await textLayer.render()
    failed.value = false
  } catch (error) {
    // 取消不算錯誤
    if (current === version) failed.value = true
  }
}

watch(() => [props.active, props.scale], render)
onMounted(render)
onBeforeUnmount(cancel)
</script>

<template>
  <div
    class="pdf-page"
    :style="{
      width: `${(size ?? baseSize).width * scale}px`,
      height: `${(size ?? baseSize).height * scale}px`,
      '--total-scale-factor': scale,
      '--scale-round-x': '1px',
      '--scale-round-y': '1px',
    }"
  >
    <canvas ref="canvas" />
    <div ref="textContainer" class="textLayer" />
    <p v-if="failed" class="failed">第 {{ number }} 頁顯示失敗</p>
  </div>
</template>

<style scoped>
.pdf-page {
  position: relative;
  flex-shrink: 0;
  margin: 0 auto;
  background: #fff;
  box-shadow: 0 2px 10px rgb(0 0 0 / 30%);
}

canvas {
  display: block;
  width: 100%;
  height: 100%;
}

.failed {
  position: absolute;
  inset: 45% 0 auto;
  text-align: center;
  color: var(--danger);
}
</style>
