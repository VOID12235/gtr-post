<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import type { Contest } from '../api/types'
import { formatDate, statusLabel } from '../utils/format'

const contests = ref<Contest[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  title: '',
  summary: '',
  mode: 'ONLINE',
  feeEnabled: false,
  feeAmount: 0,
  signupStart: '',
  signupEnd: '',
  submitStart: '',
  submitEnd: '',
  reviewStart: '',
  reviewEnd: ''
})

const statuses = ['DRAFT', 'SIGNUP', 'SUBMISSION', 'JUDGING', 'PUBLISHED', 'FINISHED']

async function load() {
  loading.value = true
  error.value = ''
  try {
    contests.value = await api.get<Contest[]>('/api/v1/admin/contests')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function createContest() {
  if (saving.value) return
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    await api.post<Contest>('/api/v1/admin/contests', {
      title: form.title.trim(),
      summary: form.summary.trim(),
      mode: form.mode,
      feeEnabled: form.feeEnabled,
      feeAmount: Number(form.feeAmount),
      signupStart: form.signupStart || null,
      signupEnd: form.signupEnd || null,
      submitStart: form.submitStart || null,
      submitEnd: form.submitEnd || null,
      reviewStart: form.reviewStart || null,
      reviewEnd: form.reviewEnd || null
    })
    success.value = '赛事已创建（DRAFT）'
    form.title = ''
    form.summary = ''
    form.signupStart = ''
    form.signupEnd = ''
    form.submitStart = ''
    form.submitEnd = ''
    form.reviewStart = ''
    form.reviewEnd = ''
    form.feeEnabled = false
    form.feeAmount = 0
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  } finally {
    saving.value = false
  }
}

async function changeStatus(contest: Contest, status: string) {
  try {
    await api.put(`/api/v1/admin/contests/${contest.id}/status`, { status })
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '状态更新失败'
  }
}

onMounted(load)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">CONTESTS</p>
        <h1>赛事管理</h1>
        <p class="muted">创建赛事、推进状态、查看公开页。</p>
      </div>
      <button class="secondary-button" type="button" @click="load">刷新</button>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>
    <p v-if="success" class="alert success">{{ success }}</p>

    <div class="content-grid tenants-grid">
      <section class="panel">
        <div class="panel-head">
          <h2>赛事列表</h2>
          <span class="muted">{{ contests.length }} 场</span>
        </div>
        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="contests.length === 0" class="empty">还没有赛事。</div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>赛事</th>
                <th>状态</th>
                <th>报名截止</th>
                <th>提交截止</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="contest in contests" :key="contest.id">
                <td>
                  <strong>{{ contest.title }}</strong>
                  <span class="muted block">{{ contest.mode }}</span>
                </td>
                <td><span class="badge">{{ statusLabel(contest.status) }}</span></td>
                <td class="muted">{{ formatDate(contest.signupEnd) }}</td>
                <td class="muted">{{ formatDate(contest.submitEnd) }}</td>
                <td>
                  <div class="row-actions">
                    <select :value="contest.status" @change="changeStatus(contest, ($event.target as HTMLSelectElement).value)">
                      <option v-for="status in statuses" :key="status" :value="status">{{ statusLabel(status) }}</option>
                    </select>
                    <RouterLink class="text-link" :to="`/contests/${contest.id}`">公开页</RouterLink>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>新建赛事</h2></div>
        <form class="form" @submit.prevent="createContest">
          <label class="field">
            <span>赛事名称</span>
            <input v-model="form.title" placeholder="2027 企业创新赛" required />
          </label>
          <label class="field">
            <span>简介</span>
            <textarea v-model="form.summary" rows="3" placeholder="一句话说明赛事"></textarea>
          </label>
          <label class="field">
            <span>形式</span>
            <select v-model="form.mode">
              <option value="ONLINE">线上</option>
              <option value="OFFLINE">线下</option>
            </select>
          </label>
          <div class="field-row">
            <label class="field">
              <span>报名费</span>
              <select v-model="form.feeEnabled">
                <option :value="false">免费</option>
                <option :value="true">收费</option>
              </select>
            </label>
            <label v-if="form.feeEnabled" class="field">
              <span>金额（元）</span>
              <input v-model.number="form.feeAmount" type="number" min="0" step="0.01" />
            </label>
          </div>
          <div class="field-row">
            <label class="field"><span>报名开始</span><input v-model="form.signupStart" type="datetime-local" /></label>
            <label class="field"><span>报名截止</span><input v-model="form.signupEnd" type="datetime-local" /></label>
          </div>
          <div class="field-row">
            <label class="field"><span>提交开始</span><input v-model="form.submitStart" type="datetime-local" /></label>
            <label class="field"><span>提交截止</span><input v-model="form.submitEnd" type="datetime-local" /></label>
          </div>
          <div class="field-row">
            <label class="field"><span>评审开始</span><input v-model="form.reviewStart" type="datetime-local" /></label>
            <label class="field"><span>评审截止</span><input v-model="form.reviewEnd" type="datetime-local" /></label>
          </div>
          <button class="primary-button" type="submit" :disabled="saving">
            {{ saving ? '创建中…' : '创建赛事' }}
          </button>
          <p class="hint">创建后为 DRAFT，可在列表中切换状态。</p>
        </form>
      </section>
    </div>
  </AppShell>
</template>
