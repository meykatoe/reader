<script setup>
import { defineAsyncComponent, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getBook } from '../api/books'
import TextReader from '../components/reader/TextReader.vue'

// 延後載入
const PdfReader = defineAsyncComponent(() => import('../components/reader/PdfReader.vue'))

const route = useRoute()
const book = ref(null)
const error = ref('')

onMounted(async () => {
  try {
    book.value = await getBook(route.params.id)
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <main v-if="error || !book" class="page">
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else>載入中…</p>
    <RouterLink to="/">返回書架</RouterLink>
  </main>
  <PdfReader v-else-if="book.format === 'PDF'" :book="book" />
  <TextReader v-else :book="book" />
</template>
