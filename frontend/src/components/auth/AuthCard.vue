<script setup>
defineProps({
  title: { type: String, required: true },
})
defineEmits(['submit'])
</script>

<template>
  <main class="auth-page">
    <form class="card" @submit.prevent="$emit('submit')">
      <h1>{{ title }}</h1>
      <slot />
    </form>
  </main>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  /* 防撐寬 */
  grid-template-columns: minmax(0, 1fr);
  place-items: center;
  padding: 16px;
}

.card {
  width: min(380px, 100%);
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 32px 28px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
}

h1 {
  margin: 0 0 8px;
  text-align: center;
}
</style>
