<script setup>
defineProps({
  notice: { type: Object, default: null },
  disabled: { type: Boolean, default: false },
})
</script>

<template>
  <p v-if="notice" class="action-notice" role="status">
    {{ notice.text }}
    <button v-if="notice.undo" class="button link" :disabled="disabled" @click="notice.undo()">
      復原
    </button>
  </p>
</template>

<style scoped>
.action-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 14px;
}

.button {
  padding: 2px 8px;
  font-weight: 700;
}
</style>
