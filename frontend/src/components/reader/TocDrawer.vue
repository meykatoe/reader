<script setup>
import { onMounted, ref } from 'vue'

defineProps({
  items: { type: Array, required: true },
  current: { type: Number, default: 0 },
})
defineEmits(['select', 'close'])

const list = ref(null)

// 捲到目前章節
onMounted(() => list.value.querySelector('.active')?.scrollIntoView({ block: 'center' }))
</script>

<template>
  <div class="overlay" @click.self="$emit('close')">
    <nav class="drawer">
      <header>
        <h2>目錄</h2>
        <button class="button" @click="$emit('close')">關閉</button>
      </header>
      <ul ref="list">
        <li v-for="item in items" :key="item.key">
          <button
            :class="{ active: item.target === current }"
            :style="{ paddingLeft: `${16 + (item.depth ?? 0) * 16}px` }"
            @click="$emit('select', item.target)"
          >
            {{ item.title }}
          </button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 30;
  background: rgb(0 0 0 / 35%);
}

.drawer {
  width: min(340px, 85vw);
  height: 100%;
  display: flex;
  flex-direction: column;
  color: #222;
  background: #fff;
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
}

h2 {
  margin: 0;
  font-size: 18px;
}

ul {
  flex: 1;
  overflow-y: auto;
  margin: 0;
  padding: 0;
  list-style: none;
}

li button {
  width: 100%;
  padding: 12px 16px;
  font: inherit;
  text-align: left;
  background: none;
  border: none;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

li button.active {
  color: var(--accent);
  font-weight: 700;
  background: #eef3f8;
}
</style>
