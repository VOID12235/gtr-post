<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import type { Contest, PaymentOrder, RiskView } from '../api/types'
import { formatDate, statusLabel } from '../utils/format'

const tabs = [
  { key: 'site', label: '站点' },
  { key: 'mail', label: '邮件' },
  { key: 'upload', label: '上传' },
  { key: 'payment', label: '报名费' },
  { key: 'security', label: '安全策略' },
  { key: 'risk', label: '奖金 / 保险' },
  { key: 'orders', label: '支付订单' }
]
const tab = ref('site')
const loading = ref(false)
const error = ref('')
const message = ref('')

const site = reactive({ name: '', footer: '', logo: '', about: '' })
const mail = reactive({ enabled: 'false', host: '', port: '465', username: '', password: '', from: '', ssl: 'true' })
const upload = reactive({ max_size_mb: '50', allowed_types: '' })
const payment = reactive({ manual_enabled: 'true', instructions: '' })
const security = reactive({ login_fail_limit: '5', login_lock_minutes: '10' })
const testTo = ref('')

const contests = ref<Contest[]>([])
const contestId = ref<number | null>(null)
const risk = ref<RiskView | null>(null)
const prize = reactive({ totalAmount: 0, fundedAmount: 0, status: 'UNFUNDED', note: '' })
const insurance = reactive({ type: '公众责任险', insurer: '', policyNo: '', coverage: '', status: 'ACTIVE' })
const safety = reactive({ level: 'L1', venue: '', capacity: 0, plan: '', status: 'DRAFT' })
const orders = ref<PaymentOrder[]>([])

async function loadGroup(group: string, target: Record<string, unknown>) {
  const data = await api.get<Record<string, string>>(`/api/v1/admin/config/${group}`)
  Object.assign(target, data)
}

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    await Promise.all([
      loadGroup('site', site),
      loadGroup('mail', mail),
      loadGroup('upload', upload),
      loadGroup('payment', payment),
      loadGroup('security', security)
    ])
    contests.value = await api.get<Contest[]>('/api/v1/admin/contests')
    if (contests.value.length > 0 && !contestId.value) {
      contestId.value = contests.value[0].id
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function saveGroup(group: string, target: Record<string, unknown>) {
  error.value = ''
  message.value = ''
  try {
    await api.put(`/api/v1/admin/config/${group}`, target)
    message.value = '配置已保存'
    await loadGroup(group, target)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  }
}

async function testMail() {
  error.value = ''
  message.value = ''
  try {
    const result = await api.post<{ success: boolean }>('/api/v1/admin/config/mail/test', { to: testTo.value })
    message.value = result.success ? '测试邮件已发送' : '发送失败，请检查 SMTP 配置或邮件服务是否开启'
  } catch (e) {
    error.value = e instanceof Error ? e.message : '发送失败'
  }
}

async function loadRisk() {
  if (!contestId.value) return
  try {
    const data = await api.get<RiskView>(`/api/v1/admin/risk/${contestId.value}`)
    risk.value = data
    if (data.prize) Object.assign(prize, data.prize)
    if (data.safety) Object.assign(safety, data.safety)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载奖金/保险失败'
  }
}

async function loadOrders() {
  if (!contestId.value) return
  try {
    orders.value = await api.get<PaymentOrder[]>(`/api/v1/admin/payments?contestId=${contestId.value}`)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载支付订单失败'
  }
}

async function savePrize() {
  if (!contestId.value) return
  try {
    await api.put(`/api/v1/admin/risk/${contestId.value}/prize`, {
      totalAmount: Number(prize.totalAmount),
      fundedAmount: Number(prize.fundedAmount),
      status: prize.status,
      note: prize.note
    })
    message.value = '奖金池已保存'
    await loadRisk()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  }
}

async function saveInsurance() {
  if (!contestId.value) return
  try {
    await api.put(`/api/v1/admin/risk/${contestId.value}/insurance`, insurance)
    message.value = '保险记录已保存'
    await loadRisk()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  }
}

async function saveSafety() {
  if (!contestId.value) return
  try {
    await api.put(`/api/v1/admin/risk/${contestId.value}/safety`, {
      level: safety.level,
      venue: safety.venue,
      capacity: Number(safety.capacity),
      plan: safety.plan,
      status: safety.status
    })
    message.value = '安全方案已保存'
    await loadRisk()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  }
}

async function markPaid(order: PaymentOrder) {
  try {
    await api.post(`/api/v1/admin/payments/${order.id}/paid`, { reference: 'MANUAL-' + Date.now() })
    message.value = '已确认支付'
    await loadOrders()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}

async function refund(order: PaymentOrder) {
  try {
    await api.post(`/api/v1/admin/payments/${order.id}/refund`, {})
    message.value = '已退款'
    await loadOrders()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}

watch(contestId, () => {
  loadRisk()
  loadOrders()
})

onMounted(loadAll)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">SETTINGS</p>
        <h1>系统设置</h1>
        <p class="muted">站点、邮件、上传、报名费、奖金/保险、支付订单，改完即时生效。</p>
      </div>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>
    <p v-if="message" class="alert success">{{ message }}</p>

    <div class="settings-tabs">
      <button
        v-for="item in tabs"
        :key="item.key"
        type="button"
        class="chip"
        :class="{ active: tab === item.key }"
        @click="tab = item.key"
      >
        {{ item.label }}
      </button>
    </div>

    <div v-if="loading" class="empty">加载中…</div>

    <template v-else>
      <section v-if="tab === 'site'" class="panel">
        <div class="panel-head"><h2>站点信息</h2></div>
        <form class="form" @submit.prevent="saveGroup('site', site)">
          <label class="field"><span>站点名称</span><input v-model="site.name" /></label>
          <label class="field"><span>页脚文案</span><input v-model="site.footer" /></label>
          <label class="field"><span>Logo URL</span><input v-model="site.logo" /></label>
          <label class="field"><span>关于文案</span><textarea v-model="site.about" rows="3"></textarea></label>
          <button class="primary-button" type="submit">保存站点信息</button>
        </form>
      </section>

      <section v-if="tab === 'mail'" class="panel">
        <div class="panel-head"><h2>邮件服务</h2></div>
        <form class="form" @submit.prevent="saveGroup('mail', mail)">
          <div class="field-row">
            <label class="field">
              <span>启用邮件</span>
              <select v-model="mail.enabled"><option value="true">开启</option><option value="false">关闭</option></select>
            </label>
            <label class="field">
              <span>SSL</span>
              <select v-model="mail.ssl"><option value="true">开启</option><option value="false">关闭</option></select>
            </label>
          </div>
          <div class="field-row">
            <label class="field"><span>SMTP 主机</span><input v-model="mail.host" placeholder="smtp.163.com" /></label>
            <label class="field"><span>端口</span><input v-model="mail.port" placeholder="465" /></label>
          </div>
          <div class="field-row">
            <label class="field"><span>账号</span><input v-model="mail.username" /></label>
            <label class="field"><span>授权码</span><input v-model="mail.password" type="password" placeholder="******" /></label>
          </div>
          <label class="field"><span>发件人显示名</span><input v-model="mail.from" /></label>
          <button class="primary-button" type="submit">保存邮件配置</button>
        </form>
        <div class="panel-head" style="margin-top: 24px"><h2>发送测试邮件</h2></div>
        <form class="form" @submit.prevent="testMail">
          <div class="field-row">
            <label class="field"><span>收件人</span><input v-model="testTo" type="email" placeholder="you@example.com" /></label>
            <button class="secondary-button" type="submit">发送测试</button>
          </div>
        </form>
      </section>

      <section v-if="tab === 'upload'" class="panel">
        <div class="panel-head"><h2>上传限制</h2></div>
        <form class="form" @submit.prevent="saveGroup('upload', upload)">
          <label class="field"><span>单文件上限（MB）</span><input v-model="upload.max_size_mb" type="number" /></label>
          <label class="field"><span>允许类型（逗号分隔）</span><textarea v-model="upload.allowed_types" rows="3"></textarea></label>
          <button class="primary-button" type="submit">保存上传配置</button>
        </form>
      </section>

      <section v-if="tab === 'payment'" class="panel">
        <div class="panel-head"><h2>报名费</h2></div>
        <form class="form" @submit.prevent="saveGroup('payment', payment)">
          <label class="field">
            <span>线下人工确认</span>
            <select v-model="payment.manual_enabled"><option value="true">开启</option><option value="false">关闭</option></select>
          </label>
          <label class="field"><span>支付说明</span><textarea v-model="payment.instructions" rows="4"></textarea></label>
          <button class="primary-button" type="submit">保存报名费配置</button>
        </form>
      </section>

      <section v-if="tab === 'security'" class="panel">
        <div class="panel-head"><h2>安全策略</h2></div>
        <form class="form" @submit.prevent="saveGroup('security', security)">
          <div class="field-row">
            <label class="field"><span>登录失败锁定阈值</span><input v-model="security.login_fail_limit" type="number" /></label>
            <label class="field"><span>锁定时长（分钟）</span><input v-model="security.login_lock_minutes" type="number" /></label>
          </div>
          <button class="primary-button" type="submit">保存安全策略</button>
        </form>
      </section>

      <template v-if="tab === 'risk'">
        <section class="panel">
          <div class="panel-head">
            <h2>奖金池</h2>
            <select v-model="contestId" class="inline-select">
              <option v-for="contest in contests" :key="contest.id" :value="contest.id">{{ contest.title }}</option>
            </select>
          </div>
          <form class="form" @submit.prevent="savePrize">
            <div class="field-row">
              <label class="field"><span>奖金总额</span><input v-model.number="prize.totalAmount" type="number" step="0.01" /></label>
              <label class="field"><span>已托管金额</span><input v-model.number="prize.fundedAmount" type="number" step="0.01" /></label>
            </div>
            <div class="field-row">
              <label class="field">
                <span>状态</span>
                <select v-model="prize.status">
                  <option value="UNFUNDED">未托管</option>
                  <option value="FUNDED">已托管</option>
                  <option value="RELEASED">已发放</option>
                </select>
              </label>
              <label class="field"><span>备注</span><input v-model="prize.note" /></label>
            </div>
            <button class="primary-button" type="submit">保存奖金池</button>
          </form>
        </section>

        <section class="panel" style="margin-top: 16px">
          <div class="panel-head"><h2>保险</h2><span class="muted">{{ risk?.insurances?.length || 0 }} 份</span></div>
          <form class="form" @submit.prevent="saveInsurance">
            <div class="field-row">
              <label class="field"><span>险种</span><input v-model="insurance.type" /></label>
              <label class="field"><span>保险公司</span><input v-model="insurance.insurer" /></label>
            </div>
            <div class="field-row">
              <label class="field"><span>保单号</span><input v-model="insurance.policyNo" /></label>
              <label class="field"><span>保障范围</span><input v-model="insurance.coverage" /></label>
            </div>
            <label class="field">
              <span>状态</span>
              <select v-model="insurance.status"><option value="ACTIVE">有效</option><option value="EXPIRED">过期</option><option value="PENDING">待生效</option></select>
            </label>
            <button class="primary-button" type="submit">新增保险记录</button>
          </form>
        </section>

        <section class="panel" style="margin-top: 16px">
          <div class="panel-head"><h2>线下安全方案</h2></div>
          <form class="form" @submit.prevent="saveSafety">
            <div class="field-row">
              <label class="field">
                <span>风险等级</span>
                <select v-model="safety.level"><option value="L1">L1 低风险</option><option value="L2">L2 中风险</option><option value="L3">L3 高风险</option></select>
              </label>
              <label class="field"><span>场地</span><input v-model="safety.venue" /></label>
            </div>
            <div class="field-row">
              <label class="field"><span>容量</span><input v-model.number="safety.capacity" type="number" /></label>
              <label class="field">
                <span>状态</span>
                <select v-model="safety.status"><option value="DRAFT">草稿</option><option value="APPROVED">已审核</option></select>
              </label>
            </div>
            <label class="field"><span>方案说明</span><textarea v-model="safety.plan" rows="4"></textarea></label>
            <button class="primary-button" type="submit">保存安全方案</button>
          </form>
        </section>
      </template>

      <section v-if="tab === 'orders'" class="panel">
        <div class="panel-head">
          <h2>支付订单</h2>
          <select v-model="contestId" class="inline-select">
            <option v-for="contest in contests" :key="contest.id" :value="contest.id">{{ contest.title }}</option>
          </select>
        </div>
        <div v-if="orders.length === 0" class="empty">暂无报名费订单。</div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead><tr><th>用户</th><th>金额</th><th>状态</th><th>时间</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="order in orders" :key="order.id">
                <td>#{{ order.userId }}</td>
                <td>¥{{ order.amount }}</td>
                <td><span class="badge">{{ statusLabel(order.status) }}</span></td>
                <td class="muted">{{ formatDate(order.createdAt) }}</td>
                <td>
                  <div class="row-actions">
                    <button v-if="order.status === 'PENDING'" class="text-link" type="button" @click="markPaid(order)">确认支付</button>
                    <button v-if="order.status === 'PAID'" class="text-link" type="button" @click="refund(order)">退款</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>
  </AppShell>
</template>
