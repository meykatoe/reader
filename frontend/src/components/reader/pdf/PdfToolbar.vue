<script setup>
const props = defineProps({
  title: { type: String, required: true },
  page: { type: Number, required: true },
  pageCount: { type: Number, required: true },
  zoom: { type: Number, required: true },
  hasOutline: { type: Boolean, default: false },
})
const emit = defineEmits(['outline', 'jump', 'zoom', 'fit'])

function submitPage(event) {
  const value = Number.parseInt(event.target.value, 10)
  if (value >= 1 && value <= props.pageCount) emit('jump', value - 1)
  else event.target.value = props.page + 1
}
</script>

<template>
  <header class="toolbar">
    <RouterLink to="/" class="button">返回書架</RouterLink>
    <button v-if="hasOutline" class="button" @click="emit('outline')">目錄</button>
    <h1>{{ title }}</h1>

    <div class="group">
      <button class="button" aria-label="上一頁" @click="emit('jump', page - 1)">‹</button>
      <input
        :value="page + 1"
        inputmode="numeric"
        aria-label="頁碼"
        @change="submitPage"
      />
      <span>/ {{ pageCount }}</span>
      <button class="button" aria-label="下一頁" @click="emit('jump', page + 1)">›</button>
    </div>

    <div class="group">
      <button class="button" aria-label="縮小" @click="emit('zoom', -1)">−</button>
      <button class="button" title="符合寬度" @click="emit('fit')">
        {{ Math.round(zoom * 100) }}%
      </button>
      <button class="button" aria-label="放大" @click="emit('zoom', 1)">＋</button>
    </div>
  </header>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
}

.button {
  padding: 6px 12px;
  font-size: 14px;
  text-decoration: none;
}

h1 {
  flex: 1;
  min-width: 80px;
  margin: 0;
  font-size: 16px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.group {
  display: flex;
  align-items: center;
  gap: 6px;
}

input {
  width: 56px;
  padding: 6px;
  font: inherit;
  text-align: center;
  border: 1px solid var(--border);
  border-radius: 6px;
}
</style>
