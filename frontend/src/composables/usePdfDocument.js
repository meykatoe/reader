import { onBeforeUnmount, ref, shallowRef } from 'vue'
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/legacy/build/pdf.mjs'
import workerUrl from 'pdfjs-dist/legacy/build/pdf.worker.min.mjs?url'

GlobalWorkerOptions.workerSrc = workerUrl

const ASSETS = `${import.meta.env.BASE_URL}pdfjs/`

async function resolvePage(pdf, destination) {
  const target = typeof destination === 'string' ? await pdf.getDestination(destination) : destination
  if (!target) return null
  const [reference] = target
  return typeof reference === 'object' ? pdf.getPageIndex(reference) : reference
}

// 攤平大綱
async function flattenOutline(pdf, nodes, depth = 0, result = []) {
  for (const node of nodes ?? []) {
    const target = node.dest ? await resolvePage(pdf, node.dest).catch(() => null) : null
    if (target !== null) {
      result.push({ key: result.length, title: node.title, target, depth })
    }
    await flattenOutline(pdf, node.items, depth + 1, result)
  }
  return result
}

export function usePdfDocument(url) {
  const pdf = shallowRef(null)
  const outline = ref([])
  const baseSize = ref({ width: 612, height: 792 })
  let task = null

  async function open() {
    task = getDocument({
      url,
      cMapUrl: `${ASSETS}cmaps/`,
      cMapPacked: true,
      standardFontDataUrl: `${ASSETS}standard_fonts/`,
      wasmUrl: `${ASSETS}wasm/`,
      iccUrl: `${ASSETS}iccs/`,
    })
    const document = await task.promise
    const first = await document.getPage(1)
    const { width, height } = first.getViewport({ scale: 1 })
    baseSize.value = { width, height }
    pdf.value = document
    flattenOutline(document, await document.getOutline())
      .then((items) => (outline.value = items))
      .catch(() => {})
  }

  onBeforeUnmount(() => task?.destroy())

  return { pdf, outline, baseSize, open }
}
