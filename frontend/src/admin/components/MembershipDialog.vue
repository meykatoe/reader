<script setup>
import { onMounted, ref } from 'vue'
import { formatDate } from '../../utils/format'
import { setMembership } from '../api'

const props = defineProps({
  user: { type: Object, required: true },
})
const emit = defineEmits(['close', 'saved'])

const DAY = 24 * 60 * 60 * 1000

// 格式為年月日
const toInput = (time) => new Date(time).toLocaleDateString('sv-SE', { timeZone: 'Asia/Taipei' })

const today = toInput(Date.now())
const date = ref(props.user.member ? toInput(props.user.memberUntil) : '')
const dialog = ref(null)
const error = ref('')
const busy = ref(false)

onMounted(() => dialog.value.showModal())

// 未到期則順延
function extend(days) {
  const base = date.value && date.value >= today ? Date.parse(`${date.value}T12:00:00+08:00`) : Date.now()
  date.value = toInput(base + days * DAY)
}

async function save(memberUntil) {
  busy.value = true
  error.value = ''
  try {
    await setMembership(props.user.id, memberUntil)
    emit('saved')
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}

// 當日結束到期
const submit = () => save(new Date(`${date.value}T23:59:59+08:00`).toISOString())

function revoke() {
  if (window.confirm(`確定取消 ${props.user.username} 的會員資格嗎？`)) save(null)
}

function cancel(event) {
  event.preventDefault()
  if (!busy.value) emit('close')
}
</script>

<template>
  <dialog ref="dialog" class="dialog" @cancel="cancel">
    <form class="form" @submit.prevent="submit">
      <h2>會員設定：{{ user.username }}</h2>
      <p>目前：{{ user.member ? `會員，至 ${formatDate(user.memberUntil)}` : '非會員' }}</p>
      <label class="field">
        到期日（當天結束時失效）
        <input v-model="date" type="date" :min="today" max="9999-12-31" required />
      </label>
      <div class="buttons start">
        <button type="button" class="button" @click="extend(30)">+30 天</button>
        <button type="button" class="button" @click="extend(365)">+1 年</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <div class="buttons">
        <button v-if="user.member" type="button" class="button revoke" :disabled="busy" @click="revoke">
          取消會員
        </button>
        <button type="button" class="button" :disabled="busy" @click="emit('close')">關閉</button>
        <button class="button primary" :disabled="busy || !date">儲存</button>
      </div>
    </form>
  </dialog>
</template>

<style scoped>
.dialog {
  width: min(420px, 92vw);
  padding: 24px;
  border: none;
  border-radius: 12px;
}

.dialog::backdrop {
  background: rgb(0 0 0 / 40%);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

h2,
p {
  margin: 0;
}

.buttons {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.start {
  justify-content: flex-start;
}

.revoke {
  margin-right: auto;
  color: var(--danger);
}
</style>
