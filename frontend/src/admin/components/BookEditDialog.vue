<script setup>
import { onMounted, reactive, ref } from 'vue'
import { updateAdminBook } from '../api'

const props = defineProps({
  book: { type: Object, required: true },
})
const emit = defineEmits(['close', 'saved'])

const dialog = ref(null)
const form = reactive({
  title: props.book.title,
  author: props.book.author ?? '',
  isPrivate: props.book.isPrivate,
})
const error = ref('')
const busy = ref(false)

onMounted(() => dialog.value.showModal())

// 公開私人書籍
function exposesPrivateBook() {
  return props.book.ownerId !== null && props.book.isPrivate && !form.isPrivate
}

async function submit() {
  if (
    exposesPrivateBook() &&
    !window.confirm(`這是 ${props.book.ownerName} 的私密書籍，公開後所有人都能閱讀。確定嗎？`)
  ) {
    return
  }
  busy.value = true
  error.value = ''
  try {
    await updateAdminBook(props.book.id, { ...form })
    emit('saved')
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
      <h2>修改書籍</h2>
      <label class="field">
        書名
        <input v-model.trim="form.title" maxlength="255" required />
      </label>
      <label class="field">
        作者
        <input v-model.trim="form.author" maxlength="255" />
      </label>
      <label class="check">
        <input v-model="form.isPrivate" type="checkbox" />
        {{ book.ownerId === null ? '下架（僅後臺可見）' : '設為私密（僅上傳者可見）' }}
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <div class="buttons">
        <button type="button" class="button" :disabled="busy" @click="emit('close')">取消</button>
        <button class="button primary" :disabled="busy">儲存</button>
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
