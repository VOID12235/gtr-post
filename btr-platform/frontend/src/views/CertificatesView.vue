<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import type { Certificate, Contest } from '../api/types'
import { formatDate, statusLabel } from '../utils/format'

const contests = ref<Contest[]>([])
const contestId = ref<number | null>(null)
const certificates = ref<Certificate[]>([])
const award = ref('参赛证书')
const loading = ref(true)
const generating = ref(false)
const error = ref('')
const success = ref('')

async function loadContests() {
  contests.value = await api.get<Contest[]>('/api/v1/admin/contests')
  if (contests.value.length > 0 && !contestId.value) {
    contestId.value = contests.value[0].id
  }
}

async function loadCertificates() {
  if (!contestId.value) return
  certificates.value = await api.get<Certificate[]>(`/api/v1/admin/certificates?contestId=${contestId.value}`)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    await loadContests()
    await loadCertificates()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function generate() {
  if (!contestId.value || generating.value) return
  generating.value = true
  error.value = ''
  success.value = ''
  try {
    certificates.value = await api.post<Certificate[]>('/api/v1/admin/certificates/generate', {
      contestId: contestId.value,
      award: award.value
    })
    success.value = '证书已生成'
  } catch (e) {
    error.value = e instanceof Error ? e.message : '生成失败'
  } finally {
    generating.value = false
  }
}

async function copy(code: string) {
  try {
    await navigator.clipboard.writeText(code)
    success.value = '证书编号已复制'
  } catch {
    success.value = code
  }
}

watch(contestId, loadCertificates)
onMounted(load)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">CERTIFICATES</p>
        <h1>证书</h1>
        <p class="muted">按赛事批量生成，编号可验真。</p>
      </div>
      <select v-model="contestId" class="inline-select">
        <option v-for="contest in contests" :key="contest.id" :value="contest.id">{{ contest.title }}</option>
      </select>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>
    <p v-if="success" class="alert success">{{ success }}</p>

    <div class="content-grid tenants-grid">
      <section class="panel">
        <div class="panel-head">
          <h2>已签发</h2>
          <span class="muted">{{ certificates.length }} 张</span>
        </div>
        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="certificates.length === 0" class="empty">还没有证书。</div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>持证人</th><th>奖项</th><th>编号</th><th>状态</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="certificate in certificates" :key="certificate.id">
                <td>{{ certificate.holder || '—' }}</td>
                <td>{{ certificate.award }}</td>
                <td><code>{{ certificate.code }}</code></td>
                <td><span class="badge">{{ statusLabel(certificate.status) }}</span></td>
                <td>
                  <div class="row-actions">
                    <button class="text-link" type="button" @click="copy(certificate.code)">复制</button>
                    <RouterLink class="text-link" :to="`/verify/${certificate.code}`">验真</RouterLink>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>生成证书</h2></div>
        <form class="form" @submit.prevent="generate">
          <label class="field">
            <span>奖项名称</span>
            <input v-model="award" placeholder="参赛证书" />
          </label>
          <p class="hint">为已有评分的作品持有人生成证书；重复生成不会重复签发。</p>
          <button class="primary-button" type="submit" :disabled="generating">
            {{ generating ? '生成中…' : '生成证书' }}
          </button>
        </form>
      </section>
    </div>
  </AppShell>
</template>
