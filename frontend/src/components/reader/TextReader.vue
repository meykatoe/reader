<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { usePageGestures } from '../../composables/usePageGestures'
import { usePagination } from '../../composables/usePagination'
import { useProgressSaver } from '../../composables/useProgressSaver'
import { useTextBook } from '../../composables/useTextBook'
import './content.css'

const props = defineProps({
  book: { type: Object, required: true },
})

const viewport = ref(null)
const content = ref(null)
const chromeVisible = ref(true)
// 排版完成才顯示
const ready = ref(false)

const { chapters, chapter, loading, error, init, load, percentAt } = useTextBook(props.book.id)
const { page, pageCount, contentStyle, openAt, turn } = usePagination(viewport, content)
const { save } = useProgressSaver(props.book.id)

const percent = computed(() => {
  if (!chapter.value) return 0
  const ratio = (page.value + 1) / pageCount.value
  return Math.round(percentAt(chapter.value.index, ratio))
})

function remember() {
  save(chapter.value.index, page.value / pageCount.value)
}

async function goToChapter(index, ratio = 0) {
  ready.value = false
  if (!(await load(index))) return
  await nextTick()
  if (await openAt(ratio)) {
    ready.value = true
    remember()
  }
}

function next() {
  if (!ready.value) return
  if (turn(1)) remember()
  else if (chapter.value.index + 1 < chapters.value.length) goToChapter(chapter.value.index + 1)
}

function previous() {
  if (!ready.value) return
  if (turn(-1)) remember()
  else if (chapter.value.index > 0) goToChapter(chapter.value.index - 1, 1)
}

const { onPointerDown, onPointerUp } = usePageGestures({
  previous,
  next,
  toggle: () => (chromeVisible.value = !chromeVisible.value),
})

onMounted(async () => {
  try {
    const progress = await init()
    await goToChapter(Math.min(progress.location, chapters.value.length - 1), progress.ratio)
  } catch (e) {
    error.value = e.message
    loading.value = false
  }
})
</script>

<template>
  <div class="reader">
    <header :class="['bar', { hidden: !chromeVisible }]">
      <RouterLink to="/" class="button">返回書架</RouterLink>
      <h1 class="book-title">{{ book.title }}</h1>
    </header>

    <div class="stage" @pointerdown="onPointerDown" @pointerup="onPointerUp">
      <p v-if="error" class="status error">{{ error }}</p>
      <p v-else-if="loading" class="status">載入中…</p>
      <div ref="viewport" class="viewport">
        <!-- 後端已過濾 -->
        <div
          ref="content"
          :class="['book-content', { invisible: !ready }]"
          :style="contentStyle"
          v-html="chapter?.html"
        />
      </div>
    </div>

    <footer :class="['bar', 'footer', { hidden: !chromeVisible }]">
      <button class="button" @click="previous">上一頁</button>
      <div class="position">
        <span class="chapter-title">{{ chapter?.title }}</span>
        <span>本章 {{ page + 1 }} / {{ pageCount }} · 全書 {{ percent }}%</span>
        <progress :value="percent" max="100" />
      </div>
      <button class="button" @click="next">下一頁</button>
    </footer>
  </div>
</template>

<style scoped>
.reader {
  height: 100dvh;
  display: flex;
  flex-direction: column;
  color: var(--reader-text, #222);
  background: var(--reader-bg, #fdfdfb);
}

.bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
}

/* 保留版面高度 */
.hidden {
  visibility: hidden;
}

.book-title {
  flex: 1;
  margin: 0;
  font-size: 16px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.stage {
  position: relative;
  flex: 1;
  min-height: 0;
  padding: 16px var(--reader-margin, 7%);
  touch-action: pan-y;
}

.viewport {
  height: 100%;
  overflow: hidden;
}

.invisible {
  visibility: hidden;
}

.status {
  position: absolute;
  inset: 40% 0 auto;
  text-align: center;
}

.position {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  opacity: 0.75;
}

.chapter-title {
  max-width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

progress {
  width: min(420px, 100%);
  height: 4px;
}
</style>
