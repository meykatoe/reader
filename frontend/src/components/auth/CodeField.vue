<script setup>
import { ref } from 'vue'
import { useCountdown } from '../../composables/useCountdown'

const props = defineProps({
  // 寄碼函式
  send: { type: Function, required: true },
  disabled: { type: Boolean, default: false },
})
const code = defineModel({ type: String, required: true })
const emit = defineEmits(['sent', 'failed'])

const { left, start } = useCountdown()
const sending = ref(false)

async function request() {
  sending.value = true
  try {
    await props.send()
    start()
    emit('sent')
  } catch (error) {
    emit('failed', error.message)
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <label class="field">
    驗證碼（15 分鐘內有效）
    <span class="row">
      <input
        v-model.trim="code"
        inputmode="numeric"
        autocomplete="one-time-code"
        maxlength="6"
        pattern="\d{6}"
        title="6 位數字"
        required
      />
      <button
        type="button"
        class="button"
        :disabled="disabled || sending || left > 0"
        @click="request"
      >
        {{ sending ? '寄送中…' : left > 0 ? `${left} 秒後重寄` : '發送驗證碼' }}
      </button>
    </span>
  </label>
</template>

<style scoped>
.row {
  display: flex;
  gap: 8px;
}

.row input {
  flex: 1;
  width: 0;
  min-width: 0;
}

.row .button {
  white-space: nowrap;
}
</style>
