<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import type { Member } from '../api/types'
import { statusLabel } from '../utils/format'

const members = ref<Member[]>([])
const loading = ref(true)
const error = ref('')
const message = ref('')
const form = reactive({
  account: '',
  password: '',
  nickname: '',
  email: '',
  role: 'JUDGE'
})

const roles = [
  { value: 'TENANT_ADMIN', label: '租户管理员' },
  { value: 'ORGANIZER', label: '赛事运营' },
  { value: 'JUDGE', label: '评委' },
  { value: 'PLAYER', label: '选手' }
]

async function load() {
  loading.value = true
  error.value = ''
  try {
    members.value = await api.get<Member[]>('/api/v1/admin/members')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function create() {
  error.value = ''
  message.value = ''
  try {
    await api.post<Member>('/api/v1/admin/members', {
      account: form.account.trim(),
      password: form.password,
      nickname: form.nickname.trim() || form.account.trim(),
      email: form.email.trim() || null,
      role: form.role
    })
    message.value = '成员已创建'
    form.account = ''
    form.password = ''
    form.nickname = ''
    form.email = ''
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  }
}

async function changeRole(member: Member, role: string) {
  try {
    await api.put(`/api/v1/admin/members/${member.id}/role`, { role })
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '修改失败'
  }
}

async function changeStatus(member: Member, status: string) {
  try {
    await api.put(`/api/v1/admin/members/${member.id}/status`, { status })
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '修改失败'
  }
}

onMounted(load)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">MEMBERS</p>
        <h1>成员管理</h1>
        <p class="muted">创建评委/运营账号，调整角色与启停状态。</p>
      </div>
      <button class="secondary-button" type="button" @click="load">刷新</button>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>
    <p v-if="message" class="alert success">{{ message }}</p>

    <div class="content-grid tenants-grid">
      <section class="panel">
        <div class="panel-head"><h2>成员列表</h2><span class="muted">{{ members.length }}</span></div>
        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="members.length === 0" class="empty">还没有成员。</div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead><tr><th>账号</th><th>昵称</th><th>角色</th><th>状态</th></tr></thead>
            <tbody>
              <tr v-for="member in members" :key="member.id">
                <td><strong>{{ member.account }}</strong><span class="muted block">{{ member.email || '—' }}</span></td>
                <td>{{ member.nickname || '—' }}</td>
                <td>
                  <select :value="member.role" @change="changeRole(member, ($event.target as HTMLSelectElement).value)">
                    <option v-for="role in roles" :key="role.value" :value="role.value">{{ role.label }}</option>
                    <option value="TENANT_OWNER">租户所有者</option>
                  </select>
                </td>
                <td>
                  <select :value="member.status" @change="changeStatus(member, ($event.target as HTMLSelectElement).value)">
                    <option value="ACTIVE">正常</option>
                    <option value="DISABLED">停用</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>新建成员</h2></div>
        <form class="form" @submit.prevent="create">
          <label class="field"><span>账号</span><input v-model="form.account" required placeholder="judge@acme.com" /></label>
          <label class="field"><span>初始密码（至少 6 位）</span><input v-model="form.password" type="password" minlength="6" required /></label>
          <label class="field"><span>昵称</span><input v-model="form.nickname" placeholder="评委老师" /></label>
          <label class="field"><span>邮箱</span><input v-model="form.email" type="email" /></label>
          <label class="field">
            <span>角色</span>
            <select v-model="form.role">
              <option v-for="role in roles" :key="role.value" :value="role.value">{{ role.label }}</option>
            </select>
          </label>
          <button class="primary-button" type="submit">创建成员</button>
          <p class="hint">已有账号会直接加入当前租户。</p>
        </form>
      </section>
    </div>
  </AppShell>
</template>
