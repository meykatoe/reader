<script setup>
defineProps({
  page: { type: Number, required: true },
  pageCount: { type: Number, required: true },
  total: { type: Number, required: true },
})
defineEmits(['change'])
</script>

<template>
  <div class="pager">
    <span>共 {{ total }} 筆</span>
    <button class="button" :disabled="page <= 0" @click="$emit('change', page - 1)">上一頁</button>
    <span>{{ page + 1 }} / {{ pageCount }}</span>
    <button class="button" :disabled="page + 1 >= pageCount" @click="$emit('change', page + 1)">
      下一頁
    </button>
  </div>
</template>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  font-size: 14px;
}
</style>
