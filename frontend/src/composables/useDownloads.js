import { computed, onMounted, ref } from 'vue'
import { downloadUrl, getQuota, requestDownload } from '../api/downloads'

function saveFile(url) {
  const link = document.createElement('a')
  link.href = url
  link.download = ''
  document.body.append(link)
  link.click()
  link.remove()
}

// 下載與額度
export function useDownloads(onError) {
  const quota = ref(null)
  const pending = ref(false)
  const remaining = computed(() =>
    quota.value ? Math.max(0, quota.value.downloadLimit - quota.value.downloadUsed) : 0,
  )

  async function refresh() {
    try {
      quota.value = await getQuota()
    } catch (e) {
      onError(e.message)
    }
  }

  // 是否需扣次
  const consumes = (book) => !book.mine && !quota.value?.downloadedBookIds.includes(book.id)

  async function download(book) {
    if (pending.value) return
    onError('')
    pending.value = true
    try {
      await refresh()
      if (consumes(book)) {
        if (!remaining.value) {
          onError(`今日下載次數已用完（${quota.value.downloadLimit} 本），明天再試`)
          return
        }
        const message = `下載《${book.title}》會使用 1 次額度，今日剩餘 ${remaining.value} 次。確定下載嗎？`
        if (!window.confirm(message)) return
      }
      quota.value = await requestDownload(book.id)
      saveFile(downloadUrl(book.id))
    } catch (e) {
      onError(e.message)
    } finally {
      pending.value = false
    }
  }

  onMounted(refresh)

  return { quota, remaining, download }
}
