<script setup>
import { ref } from 'vue'
import UploadDialog from '../../components/shelf/UploadDialog.vue'
import { formatSize } from '../../utils/format'
import { createAdminBook, deleteAdminBook, listAdminBooks } from '../api'
import BookEditDialog from '../components/BookEditDialog.vue'
import ListPager from '../components/ListPager.vue'
import { usePagedList } from '../composables/usePagedList'

const { items, total, page, pageCount, keyword, loading, error, load, run } =
  usePagedList(listAdminBooks)

const uploading = ref(false)
const editing = ref(null)

function remove(book) {
  if (window.confirm(`確定刪除《${book.title}》（${book.ownerName}）？刪除後無法復原。`)) {
    run(() => deleteAdminBook(book.id))
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
    <input v-model="keyword" type="search" placeholder="搜尋書名或作者" />
    <button class="button primary" @click="uploading = true">新增書籍</button>
  </div>
  <p v-if="error" class="error">{{ error }}</p>

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
          <th>上傳時間</th>
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
          <td>{{ new Date(book.createdAt).toLocaleString('zh-TW') }}</td>
          <td>
            <button class="button" @click="editing = book">修改</button>
            <button class="button danger" @click="remove(book)">刪除</button>
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
