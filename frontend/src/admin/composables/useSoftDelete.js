import { ref, watch } from 'vue'

// 刪除可復原
export function useSoftDelete({ run, deleted }, { remove, restore }) {
  const notice = ref(null)

  async function removeItem(item, name) {
    notice.value = null
    if (await run(() => remove(item.id))) {
      notice.value = { text: `已刪除 ${name}`, undo: () => restoreItem(item, name) }
    }
  }

  async function restoreItem(item, name) {
    notice.value = null
    if (await run(() => restore(item.id))) {
      notice.value = { text: `已復原 ${name}` }
    }
  }

  watch(deleted, () => (notice.value = null))

  return { notice, removeItem, restoreItem }
}
