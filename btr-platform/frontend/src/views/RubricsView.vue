<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import type { Contest, Rubric } from '../api/types'
import { formatDate, statusLabel } from '../utils/format'

const contests = ref<Contest[]>([])
const contestId = ref<number | null>(null)
const rubrics = ref<Rubric[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  name: '创新赛评分表',
  mode: 'WEIGHTED',
  items: [
    { key: 'innovation', label: '创新性', weight: 0.3 },
    { key: 'completion', label: '完成度', weight: 0.4 },
    { key: 'presentation', label: '展示效果', weight: 0.3 }
  ]
})

const weightSum = computed(() => form.items.reduce((sum, item) => sum + Number(item.weight || 0), 0))

async function loadContests() {
  contests.value = await api.get<Contest[]>('/api/v1/admin/contests')
  if (contests.value.length > 0 && !contestId.value) {
    contestId.value = contests.value[0].id
  }
}

async function loadRubrics() {
  if (!contestId.value) return
  rubrics.value = await api.get<Rubric[]>(`/api/v1/admin/rubrics?contestId=${contestId.value}`)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    await loadContests()
    await loadRubrics()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function createRubric() {
  if (!contestId.value || saving.value) return
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    await api.post<Rubric>('/api/v1/admin/rubrics', {
      contestId: contestId.value,
      name: form.name,
      mode: form.mode,
      items: form.items.map((item) => ({
        key: item.key,
        label: item.label,
        weight: Number(item.weight),
        min: 0,
        max: 100
      }))
    })
    success.value = '评分表已创建'
    await loadRubrics()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  } finally {
    saving.value = false
  }
}

watch(contestId, loadRubrics)
onMounted(load)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">RUBRICS</p>
        <h1>评分表</h1>
        <p class="muted">维度权重、分值区间，后端校验权重之和为 1。</p>
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
          <h2>已有评分表</h2>
          <span class="muted">{{ rubrics.length }} 个</span>
        </div>
        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="rubrics.length === 0" class="empty">该赛事还没有评分表。</div>
        <div v-else class="rubric-cards">
          <article v-for="rubric in rubrics" :key="rubric.id" class="rubric-card">
            <div class="panel-head">
              <h3>{{ rubric.name }}</h3>
              <span class="badge">{{ rubric.mode }}</span>
            </div>
            <ul class="rubric-list">
              <li v-for="item in rubric.items" :key="item.id">
                <div><strong>{{ item.label }}</strong><span class="muted">{{ item.min }}–{{ item.max }}</span></div>
                <span class="price">{{ Math.round(Number(item.weight) * 100) }}%</span>
              </li>
            </ul>
          </article>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>新建评分表</h2></div>
        <form class="form" @submit.prevent="createRubric">
          <label class="field">
            <span>名称</span>
            <input v-model="form.name" required />
          </label>
          <div v-for="(item, index) in form.items" :key="item.key" class="rubric-edit-row">
            <input v-model="item.label" :placeholder="`维度 ${index + 1}`" />
            <input v-model.number="item.weight" type="number" step="0.05" min="0" max="1" />
          </div>
          <p class="hint" :class="{ danger: Math.abs(weightSum - 1) > 0.001 }">
            权重合计：{{ (weightSum * 100).toFixed(0) }}%（必须为 100%）
          </p>
          <button class="primary-button" type="submit" :disabled="saving || Math.abs(weightSum - 1) > 0.001">
            {{ saving ? '保存中…' : '保存评分表' }}
          </button>
        </form>
      </section>
    </div>
  </AppShell>
</template>
