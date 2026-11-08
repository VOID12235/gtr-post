<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PublicShell from '../components/PublicShell.vue'
import { api } from '../api/http'
import type { Contest } from '../api/types'
import { statusLabel } from '../utils/format'

const contests = ref<Contest[]>([])
const loading = ref(true)
const status = ref('ALL')
const keyword = ref('')

const filters = [
  { value: 'ALL', label: '全部' },
  { value: 'SIGNUP', label: '报名中' },
  { value: 'SUBMISSION', label: '提交中' },
  { value: 'JUDGING', label: '评审中' },
  { value: 'PUBLISHED', label: '已公示' },
  { value: 'FINISHED', label: '已结束' }
]

const visible = computed(() => contests.value.filter((contest) => {
  const matchStatus = status.value === 'ALL' || contest.status === status.value
  const matchKeyword = !keyword.value || contest.title.toLowerCase().includes(keyword.value.toLowerCase())
  return matchStatus && matchKeyword
}))

onMounted(async () => {
  try {
    contests.value = await api.get<Contest[]>('/api/v1/public/contests?tenantCode=demo')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <PublicShell>
    <section class="page-head public-page-head">
      <div>
        <p class="eyebrow">CONTESTS</p>
        <h1>全部赛事</h1>
        <p class="muted">找到适合你的赛道，报名、提交作品、等待公示。</p>
      </div>
    </section>

    <section class="filter-bar">
      <div class="chips">
        <button
          v-for="item in filters"
          :key="item.value"
          type="button"
          class="chip"
          :class="{ active: status === item.value }"
          @click="status = item.value"
        >
          {{ item.label }}
        </button>
      </div>
      <input v-model="keyword" class="search-input" placeholder="搜索赛事" />
    </section>

    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="visible.length === 0" class="empty">没有符合条件的赛事。</div>
    <div v-else class="contest-grid">
      <RouterLink v-for="contest in visible" :key="contest.id" class="contest-card" :to="`/contests/${contest.id}`">
        <div class="contest-cover">
          <span class="badge" :class="contest.status === 'SIGNUP' ? 'ok' : 'info'">{{ statusLabel(contest.status) }}</span>
        </div>
        <div class="contest-body">
          <h3>{{ contest.title }}</h3>
          <p>{{ contest.summary || '用作品说话，让评审看见。' }}</p>
          <div class="contest-meta-row">
            <span>{{ contest.mode === 'OFFLINE' ? '线下' : '线上' }}</span>
            <span>报名截止 {{ contest.signupEnd?.slice(0, 10) || '—' }}</span>
          </div>
        </div>
      </RouterLink>
    </div>
  </PublicShell>
</template>
