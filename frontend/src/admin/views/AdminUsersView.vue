<script setup>
import { deleteAdminUser, listAdminUsers } from '../api'
import ListPager from '../components/ListPager.vue'
import { usePagedList } from '../composables/usePagedList'
import { can } from '../store'

const { items, total, page, pageCount, keyword, loading, error, run } =
  usePagedList(listAdminUsers)

function remove(user) {
  const answer = window.prompt(
    `刪除用戶 ${user.username} 會一併刪除其上傳的 ${user.bookCount} 本書與閱讀進度，無法復原。\n` +
      '請輸入用戶名稱確認：',
  )
  if (answer === null) return
  if (answer !== user.username) {
    error.value = '名稱不符，已取消刪除'
    return
  }
  run(() => deleteAdminUser(user.id))
}
</script>

<template>
  <div class="admin-toolbar">
    <input v-model="keyword" type="search" placeholder="搜尋用戶名稱" />
  </div>
  <p v-if="error" class="error">{{ error }}</p>

  <div class="table-wrap">
    <table class="admin-table">
      <thead>
        <tr>
          <th>編號</th>
          <th>用戶名稱</th>
          <th>書籍數</th>
          <th>註冊時間</th>
          <th v-if="can('USER_DELETE')">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in items" :key="user.id">
          <td>{{ user.id }}</td>
          <td class="wide">{{ user.username }}</td>
          <td>{{ user.bookCount }}</td>
          <td>{{ new Date(user.createdAt).toLocaleString('zh-TW') }}</td>
          <td v-if="can('USER_DELETE')">
            <button class="button danger" @click="remove(user)">刪除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-if="!items.length" class="admin-empty">{{ loading ? '載入中…' : '沒有資料' }}</p>
  </div>
  <ListPager :page="page" :page-count="pageCount" :total="total" @change="page = $event" />
</template>
