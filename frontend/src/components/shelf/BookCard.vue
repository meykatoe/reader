<script setup>
import { formatSize } from '../../utils/format'

defineProps({
  book: { type: Object, required: true },
})
defineEmits(['toggle-privacy', 'remove'])
</script>

<template>
  <article class="card">
    <div class="cover">
      <span class="cover-title">{{ book.title }}</span>
      <span class="badge format">{{ book.format }}</span>
      <span v-if="book.isPrivate" class="badge lock">私密</span>
    </div>

    <h2 class="title" :title="book.title">{{ book.title }}</h2>
    <p class="meta">{{ book.author || '未知作者' }}</p>
    <p class="meta">{{ book.mine ? '我上傳' : book.ownerName }} · {{ formatSize(book.fileSize) }}</p>

    <div v-if="book.mine" class="actions">
      <button class="button" @click="$emit('toggle-privacy', book)">
        {{ book.isPrivate ? '設為公開' : '設為私密' }}
      </button>
      <button class="button remove" @click="$emit('remove', book)">刪除</button>
    </div>
  </article>
</template>

<style scoped>
.card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.cover {
  position: relative;
  aspect-ratio: 3 / 4;
  display: grid;
  place-items: center;
  overflow: hidden;
  padding: 16px;
  background: linear-gradient(160deg, #44688c, #2a3f55);
  border-radius: 6px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 18%);
}

.cover-title {
  color: #fff;
  font-family: serif;
  font-size: 18px;
  text-align: center;
  overflow-wrap: anywhere;
}

.badge {
  position: absolute;
  padding: 2px 8px;
  font-size: 12px;
  color: #fff;
  background: rgb(0 0 0 / 65%);
  border-radius: 999px;
}

.format {
  right: 8px;
  bottom: 8px;
}

.lock {
  left: 8px;
  top: 8px;
}

.title {
  margin: 6px 0 0;
  font-size: 15px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.meta {
  margin: 0;
  font-size: 13px;
  color: var(--muted);
}

.actions {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}

.actions .button {
  padding: 4px 10px;
  font-size: 13px;
}

.remove {
  color: var(--danger);
}
</style>
