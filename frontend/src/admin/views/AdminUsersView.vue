<script setup>
import { ref } from 'vue'
import { formatDate } from '../../utils/format'
import { deleteAdminUser, listAdminUsers, restoreAdminUser } from '../api'
import ActionNotice from '../components/ActionNotice.vue'
import ListPager from '../components/ListPager.vue'
import MembershipDialog from '../components/MembershipDialog.vue'
import StatusTabs from '../components/StatusTabs.vue'
import { usePagedList } from '../composables/usePagedList'
import { useSoftDelete } from '../composables/useSoftDelete'
import { can } from '../store'

const list = usePagedList(listAdminUsers)
const { items, total, page, pageCount, keyword, deleted, loading, pending, error, load } = list
const { notice, removeItem, restoreItem } = useSoftDelete(list, {
  remove: deleteAdminUser,
  restore: restoreAdminUser,
})

const editing = ref(null)

function finish() {
  editing.value = null
  load()
}
</script>

<template>
  <div class="admin-toolbar">
    <StatusTabs v-model="deleted" />
    <input v-model="keyword" type="search" placeholder="搜尋用戶名稱" />
  </div>
  <p v-if="deleted" class="hint">刪除的帳號無法登入，名稱與信箱仍保留。復原後私密書一併回復。</p>
  <p v-if="error" class="error">{{ error }}</p>
  <ActionNotice :notice="notice" :disabled="pending" />

  <div class="table-wrap">
    <table class="admin-table">
      <thead>
        <tr>
          <th>編號</th>
          <th>用戶名稱</th>
          <th>信箱</th>
          <th>書籍數</th>
          <th>會員</th>
          <th>{{ deleted ? '刪除時間' : '註冊時間' }}</th>
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
          <td>{{ new Date(deleted ? user.deletedAt : user.createdAt).toLocaleString('zh-TW') }}</td>
          <td v-if="can('USER_MEMBERSHIP') || can('USER_DELETE')">
            <template v-if="deleted">
              <button
                v-if="can('USER_DELETE')"
                class="button"
                :disabled="pending || loading"
                @click="restoreItem(user, user.username)"
              >
                復原
              </button>
            </template>
            <template v-else>
              <button v-if="can('USER_MEMBERSHIP')" class="button" @click="editing = user">
                會員設定
              </button>
              <button
                v-if="can('USER_DELETE')"
                class="button danger"
                :disabled="pending || loading"
                @click="removeItem(user, user.username)"
              >
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
  <MembershipDialog v-if="editing" :user="editing" @close="editing = null" @saved="finish" />
</template>
