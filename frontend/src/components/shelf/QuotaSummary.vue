<script setup>
import { formatDate } from '../../utils/format'

defineProps({
  quota: { type: Object, required: true },
  remaining: { type: Number, required: true },
})
</script>

<template>
  <p class="quota">
    <span :class="['tag', { member: quota.member }]">{{ quota.member ? '會員' : '非會員' }}</span>
    <span v-if="quota.member">有效至 {{ formatDate(quota.memberUntil) }}</span>
    <span>今日下載剩餘 {{ remaining }} / {{ quota.downloadLimit }} 本</span>
    <span class="note">自己上傳的書與當日已下載的書不扣次數</span>
  </p>
</template>

<style scoped>
.quota {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin: -8px 0 20px;
  font-size: 14px;
}

.tag {
  padding: 2px 10px;
  border: 1px solid var(--border);
  border-radius: 999px;
}

.tag.member {
  color: #fff;
  background: #a8792a;
  border-color: #a8792a;
}

.note {
  color: var(--muted);
}
</style>
