<script setup>
const deleted = defineModel({ type: Boolean, required: true })
</script>

<template>
  <div class="tabs">
    <button :class="['button', { primary: !deleted }]" @click="deleted = false">使用中</button>
    <button :class="['button', { primary: deleted }]" @click="deleted = true">已刪除</button>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: 6px;
}
</style>
