<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { deleteBook, listBooks, setBookPrivacy } from '../api/books'
import AppHeader from '../components/AppHeader.vue'
import BookCard from '../components/shelf/BookCard.vue'
import UploadDialog from '../components/shelf/UploadDialog.vue'

const tabs = [
  { mine: false, label: '全部書籍' },
  { mine: true, label: '我的上傳' },
]

const mine = ref(false)
const keyword = ref('')
const books = ref([])
const loading = ref(true)
const error = ref('')
const uploading = ref(false)

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return books.value
  return books.value.filter((book) =>
    `${book.title} ${book.author ?? ''}`.toLowerCase().includes(text),
  )
})

async function load() {
  const requested = mine.value
  loading.value = true
  error.value = ''
  try {
    const result = await listBooks(requested)
    // 忽略舊回應
    if (requested === mine.value) books.value = result
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function run(action) {
  error.value = ''
  try {
    await action()
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const togglePrivacy = (book) => run(() => setBookPrivacy(book.id, !book.isPrivate))

function remove(book) {
  if (window.confirm(`確定刪除《${book.title}》？刪除後無法復原。`)) {
    run(() => deleteBook(book.id))
  }
}

function onUploaded() {
  uploading.value = false
  load()
}

watch(mine, load)
onMounted(load)
</script>

<template>
  <AppHeader />
  <main class="page">
    <div class="toolbar">
      <div class="tabs">
        <button
          v-for="tab in tabs"
          :key="tab.label"
          :class="['button', { primary: mine === tab.mine }]"
          @click="mine = tab.mine"
        >
          {{ tab.label }}
        </button>
      </div>
      <input v-model="keyword" class="search" type="search" placeholder="搜尋書名或作者" />
      <button class="button primary" @click="uploading = true">上傳書籍</button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="loading" class="hint">載入中…</p>
    <p v-else-if="!filtered.length" class="hint">目前沒有書籍</p>
    <section v-else class="grid">
      <BookCard
        v-for="book in filtered"
        :key="book.id"
        :book="book"
        @toggle-privacy="togglePrivacy"
        @remove="remove"
      />
    </section>

    <UploadDialog v-if="uploading" @close="uploading = false" @uploaded="onUploaded" />
  </main>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

.tabs {
  display: flex;
  gap: 6px;
}

.search {
  flex: 1;
  min-width: 180px;
  padding: 8px 12px;
  font: inherit;
  border: 1px solid var(--border);
  border-radius: 8px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 28px 20px;
}

.hint {
  color: var(--muted);
  text-align: center;
  padding: 48px 0;
}
</style>
