<script setup>
const password = defineModel('password', { type: String, required: true })
const confirmation = defineModel('confirmation', { type: String, required: true })
</script>

<template>
  <label class="field">
    新密碼（8 至 72 個字元）
    <input
      v-model="password"
      type="password"
      autocomplete="new-password"
      minlength="8"
      maxlength="72"
      required
    />
  </label>
  <label class="field">
    再次輸入新密碼
    <input v-model="confirmation" type="password" autocomplete="new-password" required />
  </label>
</template>
