<script setup>
import { onMounted, ref } from 'vue'
import { uploadBook } from '../../api/books'

const emit = defineEmits(['close', 'uploaded'])

const ACCEPT = '.epub,.txt,.pdf,.mobi'
const MAX_SIZE = 100 * 1024 * 1024

const dialog = ref(null)
const file = ref(null)
const title = ref('')
const isPrivate = ref(false)
const error = ref('')
const busy = ref(false)

onMounted(() => dialog.value.showModal())

function pickFile(event) {
  file.value = event.target.files[0] ?? null
  error.value = file.value?.size > MAX_SIZE ? '檔案超過 100 MB' : ''
}

async function submit() {
  if (!file.value || error.value === '檔案超過 100 MB') return
  busy.value = true
  error.value = ''
  const formData = new FormData()
  formData.append('file', file.value)
  formData.append('title', title.value)
  formData.append('isPrivate', isPrivate.value)
  try {
    emit('uploaded', await uploadBook(formData))
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}

function cancel(event) {
  event.preventDefault()
  if (!busy.value) emit('close')
}
</script>

<template>
  <dialog ref="dialog" class="dialog" @cancel="cancel">
    <form class="form" @submit.prevent="submit">
      <h2>上傳書籍</h2>
      <label class="field">
        選擇檔案（EPUB、TXT、PDF、MOBI）
        <input type="file" :accept="ACCEPT" required @change="pickFile" />
      </label>
      <label class="field">
        書名（選填，留空自動讀取）
        <input v-model.trim="title" maxlength="255" />
      </label>
      <label class="check">
        <input v-model="isPrivate" type="checkbox" />
        設為私密（僅自己可見）
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <div class="buttons">
        <button type="button" class="button" :disabled="busy" @click="emit('close')">取消</button>
        <button class="button primary" :disabled="busy || !file">
          {{ busy ? '上傳中…' : '上傳' }}
        </button>
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
  gap: 16px;
}

h2 {
  margin: 0;
}

.check {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.buttons {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
