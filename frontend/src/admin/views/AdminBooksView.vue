<script setup>
import { ref } from 'vue'
import UploadDialog from '../../components/shelf/UploadDialog.vue'
import { formatSize } from '../../utils/format'
import { createAdminBook, deleteAdminBook, listAdminBooks, restoreAdminBook } from '../api'
import ActionNotice from '../components/ActionNotice.vue'
import BookEditDialog from '../components/BookEditDialog.vue'
import ListPager from '../components/ListPager.vue'
import StatusTabs from '../components/StatusTabs.vue'
import { usePagedList } from '../composables/usePagedList'
import { useSoftDelete } from '../composables/useSoftDelete'

const list = usePagedList(listAdminBooks)
const { items, total, page, pageCount, keyword, deleted, loading, pending, error, load } = list
const { notice, removeItem, restoreItem } = useSoftDelete(list, {
  remove: deleteAdminBook,
  restore: restoreAdminBook,
})

const uploading = ref(false)
const editing = ref(null)

function remove(book) {
  if (window.confirm(`確定刪除《${book.title}》（${book.ownerName}）？可在「已刪除」中復原。`)) {
    removeItem(book, `《${book.title}》`)
  }
}

function finish() {
  uploading.value = false
  editing.value = null
  load()
}
</script>

<template>
  <div class="admin-toolbar">
    <StatusTabs v-model="deleted" />
    <input v-model="keyword" type="search" placeholder="搜尋書名或作者" />
    <button class="button primary" @click="uploading = true">新增書籍</button>
  </div>
  <p v-if="error" class="error">{{ error }}</p>
  <ActionNotice :notice="notice" :disabled="pending" />

  <div class="table-wrap">
    <table class="admin-table">
      <thead>
        <tr>
          <th>編號</th>
          <th>書名</th>
          <th>作者</th>
          <th>格式</th>
          <th>上傳者</th>
          <th>狀態</th>
          <th>大小</th>
          <th>{{ deleted ? '刪除時間' : '上傳時間' }}</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in items" :key="book.id">
          <td>{{ book.id }}</td>
          <td class="wide">{{ book.title }}</td>
          <td>{{ book.author || '—' }}</td>
          <td>{{ book.format }}</td>
          <td>{{ book.ownerName }}</td>
          <td>{{ book.isPrivate ? '私密' : '公開' }}</td>
          <td>{{ formatSize(book.fileSize) }}</td>
          <td>{{ new Date(deleted ? book.deletedAt : book.createdAt).toLocaleString('zh-TW') }}</td>
          <td>
            <button
              v-if="deleted"
              class="button"
              :disabled="pending || loading"
              @click="restoreItem(book, `《${book.title}》`)"
            >
              復原
            </button>
            <template v-else>
              <button class="button" @click="editing = book">修改</button>
              <button class="button danger" :disabled="pending || loading" @click="remove(book)">
                刪除
              </button>
            </template>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-if="!items.length" class="admin-empty">{{ loading ? '載入中…' : '沒有資料' }}</p>
  </div>
  <ListPager :page="page" :page-count="pageCount" :total="total" @change="page = $event" />

  <UploadDialog
    v-if="uploading"
    :upload="createAdminBook"
    @close="uploading = false"
    @uploaded="finish"
  />
  <BookEditDialog v-if="editing" :book="editing" @close="editing = null" @saved="finish" />
</template>
