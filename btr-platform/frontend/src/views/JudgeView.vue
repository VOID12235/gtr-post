<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import type { Contest, Work } from '../api/types'
import { formatDate } from '../utils/format'

const contests = ref<Contest[]>([])
const contestId = ref<number | null>(null)
const tasks = ref<Work[]>([])
const scores = ref<Record<number, number>>({})
const comments = ref<Record<number, string>>({})
const loading = ref(true)
const message = ref('')

async function loadContests() {
  contests.value = await api.get<Contest[]>('/api/v1/public/contests?tenantCode=demo')
  if (contests.value.length > 0 && !contestId.value) {
    contestId.value = contests.value[0].id
  }
}

async function loadTasks() {
  if (!contestId.value) return
  tasks.value = await api.get<Work[]>(`/api/v1/judge/tasks?contestId=${contestId.value}`)
}

async function load() {
  loading.value = true
  try {
    await loadContests()
    await loadTasks()
  } catch (e) {
    message.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function submit(work: Work) {
  const score = Number(scores.value[work.id])
  if (Number.isNaN(score) || score < 0 || score > 100) {
    message.value = '请输入 0–100 的分数'
    return
  }
  try {
    await api.post('/api/v1/judge/scores', {
      workId: work.id,
      totalScore: score,
      comment: comments.value[work.id] || ''
    })
    message.value = `《${work.title}》已打分`
    await loadTasks()
  } catch (e) {
    message.value = e instanceof Error ? e.message : '提交失败'
  }
}

watch(contestId, loadTasks)
onMounted(load)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">JUDGING</p>
        <h1>评审工作台</h1>
        <p class="muted">只显示分配给自己的作品；评审期内可修改分数。</p>
      </div>
      <select v-model="contestId" class="inline-select">
        <option v-for="contest in contests" :key="contest.id" :value="contest.id">{{ contest.title }}</option>
      </select>
    </div>

    <p v-if="message" class="alert">{{ message }}</p>

    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="tasks.length === 0" class="empty">没有待评作品，或当前阶段不开放打分。</div>
    <div v-else class="judge-grid">
      <article v-for="work in tasks" :key="work.id" class="panel judge-card">
        <div class="panel-head">
          <h2>{{ work.title }}</h2>
          <span class="muted">{{ work.authorNickname || '匿名' }}</span>
        </div>
        <p class="muted">{{ work.description }}</p>
        <a v-if="work.videoUrl" class="text-link" :href="work.videoUrl" target="_blank" rel="noreferrer">查看作品 →</a>
        <div class="judge-form">
          <label class="field">
            <span>分数（0–100）</span>
            <input v-model.number="scores[work.id]" type="number" min="0" max="100" step="0.5" />
          </label>
          <label class="field">
            <span>评语</span>
            <textarea v-model="comments[work.id]" rows="3" placeholder="写点评语"></textarea>
          </label>
          <button class="primary-button" type="button" @click="submit(work)">提交评分</button>
        </div>
      </article>
    </div>
  </AppShell>
</template>
