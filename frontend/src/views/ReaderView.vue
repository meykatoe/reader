<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getBook } from '../api/books'
import TextReader from '../components/reader/TextReader.vue'

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
  <p v-else-if="book.format === 'PDF'" class="page">PDF 閱讀器開發中</p>
  <TextReader v-else :book="book" />
</template>
