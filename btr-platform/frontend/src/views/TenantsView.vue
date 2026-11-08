<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import type { Tenant } from '../auth'

const tenants = ref<Tenant[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  code: '',
  name: '',
  type: 'ENTERPRISE',
  contactName: '',
  contactEmail: '',
  ownerAccount: '',
  ownerPassword: '',
  ownerNickname: ''
})

const typeOptions = [
  { value: 'ENTERPRISE', label: '企业' },
  { value: 'ASSOCIATION', label: '协会 / 学会' },
  { value: 'GOVERNMENT', label: '政府' },
  { value: 'PARK', label: '园区' },
  { value: 'AGENCY', label: '赛事执行 / 代理' },
  { value: 'BRAND', label: '品牌 / 商场 / 媒体' },
  { value: 'COMMUNITY', label: '社区 / 社群' },
  { value: 'TRAINING', label: '培训机构' }
]

function typeLabel(value: string) {
  return typeOptions.find((item) => item.value === value)?.label ?? value
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    tenants.value = await api.get<Tenant[]>('/api/v1/platform/tenants')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function createTenant() {
  if (saving.value) return
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    await api.post<Tenant>('/api/v1/platform/tenants', {
      code: form.code.trim(),
      name: form.name.trim(),
      type: form.type,
      contactName: form.contactName.trim() || null,
      contactEmail: form.contactEmail.trim() || null,
      ownerAccount: form.ownerAccount.trim(),
      ownerPassword: form.ownerPassword,
      ownerNickname: form.ownerNickname.trim() || null
    })
    success.value = `租户 ${form.name} 已开通，所有者账号：${form.ownerAccount}`
    form.code = ''
    form.name = ''
    form.contactName = ''
    form.contactEmail = ''
    form.ownerAccount = ''
    form.ownerPassword = ''
    form.ownerNickname = ''
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">TENANTS</p>
        <h1>租户管理</h1>
        <p class="muted">社会赛事主办方：企业、协会、政府/园区、品牌、执行公司。</p>
      </div>
      <button class="secondary-button" type="button" @click="load">刷新</button>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>
    <p v-if="success" class="alert success">{{ success }}</p>

    <div class="content-grid tenants-grid">
      <section class="panel">
        <div class="panel-head">
          <h2>租户列表</h2>
          <span class="muted">{{ tenants.length }} 个</span>
        </div>

        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="tenants.length === 0" class="empty">还没有租户。</div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>租户</th>
                <th>编码</th>
                <th>类型</th>
                <th>状态</th>
                <th>创建时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tenant in tenants" :key="tenant.id">
                <td>
                  <strong>{{ tenant.name }}</strong>
                  <span v-if="tenant.contactName" class="muted block">{{ tenant.contactName }}</span>
                </td>
                <td><code>{{ tenant.code }}</code></td>
                <td>{{ typeLabel(tenant.type) }}</td>
                <td><span class="badge">{{ tenant.status }}</span></td>
                <td class="muted">{{ tenant.createdAt?.replace('T', ' ').slice(0, 16) || '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h2>开通租户</h2>
        </div>
        <form class="form" @submit.prevent="createTenant">
          <label class="field">
            <span>租户编码</span>
            <input v-model="form.code" placeholder="acme-2027" required maxlength="64" />
          </label>
          <label class="field">
            <span>租户名称</span>
            <input v-model="form.name" placeholder="示例科技有限公司" required maxlength="128" />
          </label>
          <label class="field">
            <span>类型</span>
            <select v-model="form.type">
              <option v-for="option in typeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
          <div class="field-row">
            <label class="field">
              <span>联系人</span>
              <input v-model="form.contactName" placeholder="张三" />
            </label>
            <label class="field">
              <span>联系邮箱</span>
              <input v-model="form.contactEmail" type="email" placeholder="ops@acme.com" />
            </label>
          </div>
          <p class="hint" style="margin: 4px 0 0">所有者账号（用于登录该租户）</p>
          <label class="field">
            <span>所有者账号</span>
            <input v-model="form.ownerAccount" placeholder="owner@acme.com" required />
          </label>
          <div class="field-row">
            <label class="field">
              <span>所有者密码（至少 6 位）</span>
              <input v-model="form.ownerPassword" type="password" minlength="6" required />
            </label>
            <label class="field">
              <span>所有者昵称</span>
              <input v-model="form.ownerNickname" placeholder="租户管理员" />
            </label>
          </div>
          <button class="primary-button" type="submit" :disabled="saving">
            {{ saving ? '开通中…' : '开通租户' }}
          </button>
          <p class="hint">开通后平台管理员可在该租户下继续创建赛事。</p>
        </form>
      </section>
    </div>
  </AppShell>
</template>
