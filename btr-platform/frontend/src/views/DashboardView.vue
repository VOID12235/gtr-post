<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import { auth, type Tenant } from '../auth'
import type { Dashboard } from '../api/types'

const tenants = ref<Tenant[]>([])
const dashboard = ref<Dashboard | null>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const [tenantList, stats] = await Promise.all([
      api.get<Tenant[]>('/api/v1/platform/tenants'),
      api.get<Dashboard>('/api/v1/admin/dashboard')
    ])
    tenants.value = tenantList
    dashboard.value = stats
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">OVERVIEW</p>
        <h1>你好，{{ auth.user?.nickname || auth.user?.account }}</h1>
        <p class="muted">当前租户：{{ auth.tenant?.name }}（{{ auth.tenant?.code }}）</p>
      </div>
      <div class="page-actions">
        <RouterLink class="primary-button" to="/contests-admin">新建赛事</RouterLink>
        <RouterLink class="secondary-button" to="/sponsors">赞助投放</RouterLink>
      </div>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>

    <div class="stat-grid">
      <article class="stat-card"><span class="stat-label">赛事</span><strong class="stat-value">{{ dashboard?.contests ?? '—' }}</strong><span class="stat-hint">本租户全部赛事</span></article>
      <article class="stat-card"><span class="stat-label">报名</span><strong class="stat-value">{{ dashboard?.signups ?? '—' }}</strong><span class="stat-hint">累计报名</span></article>
      <article class="stat-card"><span class="stat-label">作品</span><strong class="stat-value">{{ dashboard?.works ?? '—' }}</strong><span class="stat-hint">已提交作品</span></article>
      <article class="stat-card"><span class="stat-label">证书</span><strong class="stat-value">{{ dashboard?.certificates ?? '—' }}</strong><span class="stat-hint">已签发</span></article>
    </div>

    <div class="content-grid">
      <section class="panel">
        <div class="panel-head">
          <h2>最近租户</h2>
          <RouterLink to="/tenants">全部</RouterLink>
        </div>
        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="tenants.length === 0" class="empty">还没有租户。</div>
        <ul v-else class="tenant-list">
          <li v-for="tenant in tenants.slice(0, 5)" :key="tenant.id">
            <div>
              <strong>{{ tenant.name }}</strong>
              <span class="muted">{{ tenant.code }} · {{ tenant.type }}</span>
            </div>
            <span class="badge">{{ tenant.status }}</span>
          </li>
        </ul>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>P1 进度</h2><span class="muted">可部署版</span></div>
        <ul class="roadmap">
          <li class="done"><span /> 多租户 / 登录 / 租户管理</li>
          <li class="done"><span /> 赛事 / 报名 / 作品</li>
          <li class="done"><span /> 赞助商 / 投放 / 库存位</li>
          <li class="done"><span /> 评分表 / 证书 / 通知</li>
          <li class="active"><span /> 公开站验收</li>
          <li><span /> 支付 / 商品（P1.5）</li>
        </ul>
      </section>
    </div>
  </AppShell>
</template>
