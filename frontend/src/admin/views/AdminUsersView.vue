<script setup>
import { ref } from 'vue'
import { formatDate } from '../../utils/format'
import { deleteAdminUser, listAdminUsers } from '../api'
import ListPager from '../components/ListPager.vue'
import MembershipDialog from '../components/MembershipDialog.vue'
import { usePagedList } from '../composables/usePagedList'
import { can } from '../store'

const { items, total, page, pageCount, keyword, loading, error, load, run } =
  usePagedList(listAdminUsers)

const editing = ref(null)

function finish() {
  editing.value = null
  load()
}

function remove(user) {
  const publicCount = user.bookCount - user.privateBookCount
  const answer = window.prompt(
    `刪除用戶 ${user.username} 會移除帳號與閱讀進度，無法復原。\n` +
      `${user.privateBookCount} 本私密書將連同檔案永久刪除。\n` +
      `${publicCount} 本公開書會保留。\n` +
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
          <th>信箱</th>
          <th>書籍數</th>
          <th>會員</th>
          <th>註冊時間</th>
          <th v-if="can('USER_MEMBERSHIP') || can('USER_DELETE')">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in items" :key="user.id">
          <td>{{ user.id }}</td>
          <td class="wide">{{ user.username }}</td>
          <td>{{ user.email || '—' }}</td>
          <td>{{ user.bookCount }}</td>
          <td>{{ user.member ? `至 ${formatDate(user.memberUntil)}` : '—' }}</td>
          <td>{{ new Date(user.createdAt).toLocaleString('zh-TW') }}</td>
          <td v-if="can('USER_MEMBERSHIP') || can('USER_DELETE')">
            <button v-if="can('USER_MEMBERSHIP')" class="button" @click="editing = user">
              會員設定
            </button>
            <button v-if="can('USER_DELETE')" class="button danger" @click="remove(user)">
              刪除
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-if="!items.length" class="admin-empty">{{ loading ? '載入中…' : '沒有資料' }}</p>
  </div>
  <ListPager :page="page" :page-count="pageCount" :total="total" @change="page = $event" />
  <MembershipDialog v-if="editing" :user="editing" @close="editing = null" @saved="finish" />
</template>
