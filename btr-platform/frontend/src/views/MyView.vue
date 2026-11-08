<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import { auth } from '../auth'
import type { Notification, PaymentOrder, ShopOrder, Signup, Work } from '../api/types'
import { formatDate, statusLabel } from '../utils/format'

const signups = ref<Signup[]>([])
const works = ref<Work[]>([])
const notifications = ref<Notification[]>([])
const payments = ref<PaymentOrder[]>([])
const shopOrders = ref<ShopOrder[]>([])
const loading = ref(true)
const error = ref('')
const currentPassword = ref('')
const newPassword = ref('')
const pwdMessage = ref('')
const pwdError = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [signupList, workList, noticeList, paymentList, shopList] = await Promise.all([
      api.get<Signup[]>('/api/v1/signups/mine'),
      api.get<Work[]>('/api/v1/works/mine'),
      api.get<Notification[]>('/api/v1/notifications'),
      api.get<PaymentOrder[]>('/api/v1/payments/mine'),
      api.get<ShopOrder[]>('/api/v1/shop/orders/mine')
    ])
    signups.value = signupList
    works.value = workList
    notifications.value = noticeList
    payments.value = paymentList
    shopOrders.value = shopList
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function read(notification: Notification) {
  if (notification.readAt) return
  try {
    await api.put(`/api/v1/notifications/${notification.id}/read`, {})
    notification.readAt = new Date().toISOString()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}

async function changePassword() {
  pwdError.value = ''
  pwdMessage.value = ''
  try {
    await api.put('/api/v1/auth/password', {
      currentPassword: currentPassword.value,
      newPassword: newPassword.value
    })
    currentPassword.value = ''
    newPassword.value = ''
    pwdMessage.value = '密码已修改，下次登录请使用新密码'
  } catch (e) {
    pwdError.value = e instanceof Error ? e.message : '修改失败'
  }
}

onMounted(load)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">MY SPACE</p>
        <h1>{{ auth.user?.nickname || '个人中心' }}</h1>
        <p class="muted">我的报名、作品与通知。</p>
      </div>
      <RouterLink class="primary-button" to="/contests">去报名</RouterLink>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>

    <div class="content-grid">
      <section class="panel">
        <div class="panel-head"><h2>我的报名</h2><span class="muted">{{ signups.length }}</span></div>
        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="signups.length === 0" class="empty">还没有报名，去赛事列表看看。</div>
        <ul v-else class="tenant-list">
          <li v-for="signup in signups" :key="signup.id">
            <div>
              <strong>赛事 #{{ signup.contestId }}</strong>
              <span class="muted">{{ signup.teamName || '个人参赛' }} · {{ formatDate(signup.createdAt) }}</span>
            </div>
            <div class="row-actions">
              <span class="badge">{{ statusLabel(signup.status) }}</span>
              <RouterLink v-if="signup.status === 'APPROVED'" class="text-link" :to="`/submit/${signup.contestId}`">
                提交作品
              </RouterLink>
            </div>
          </li>
        </ul>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>我的作品</h2><span class="muted">{{ works.length }}</span></div>
        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="works.length === 0" class="empty">还没有作品。</div>
        <ul v-else class="tenant-list">
          <li v-for="work in works" :key="work.id">
            <div>
              <strong>{{ work.title }}</strong>
              <span class="muted">{{ work.status }} · {{ work.totalScore ?? '待评分' }}</span>
            </div>
            <a v-if="work.videoUrl" class="text-link" :href="work.videoUrl" target="_blank" rel="noreferrer">查看</a>
          </li>
        </ul>
      </section>
    </div>

    <section class="panel" style="margin-top: 16px">
      <div class="panel-head"><h2>报名支付</h2><span class="muted">{{ payments.length }}</span></div>
      <div v-if="payments.length === 0" class="empty">暂无报名费订单。</div>
      <ul v-else class="tenant-list">
        <li v-for="order in payments" :key="order.id">
          <div>
            <strong>赛事 #{{ order.contestId }} · ¥{{ order.amount }}</strong>
            <span class="muted">{{ formatDate(order.createdAt) }}</span>
          </div>
          <span class="badge">{{ statusLabel(order.status) }}</span>
        </li>
      </ul>
      <p class="hint">线下支付后，由主办方在后台确认，报名即生效。</p>
    </section>

    <section class="panel" style="margin-top: 16px">
      <div class="panel-head"><h2>商品订单</h2><span class="muted">{{ shopOrders.length }}</span></div>
      <div v-if="shopOrders.length === 0" class="empty">暂无商品订单。</div>
      <ul v-else class="tenant-list">
        <li v-for="order in shopOrders" :key="order.id">
          <div>
            <strong>商品 #{{ order.productId }} × {{ order.quantity }} · ¥{{ order.amount }}</strong>
            <span class="muted">{{ formatDate(order.createdAt) }}<template v-if="order.trackingNo"> · 快递 {{ order.trackingNo }}</template></span>
          </div>
          <span class="badge">{{ statusLabel(order.status) }}</span>
        </li>
      </ul>
      <p class="hint">下单后联系主办方线下支付，管理员确认后发货。</p>
    </section>

    <section class="panel" style="margin-top: 16px">
      <div class="panel-head"><h2>账户安全</h2></div>
      <form class="form" @submit.prevent="changePassword">
        <div class="field-row">
          <label class="field"><span>当前密码</span><input v-model="currentPassword" type="password" required /></label>
          <label class="field"><span>新密码（至少 6 位）</span><input v-model="newPassword" type="password" minlength="6" required /></label>
        </div>
        <p v-if="pwdError" class="form-error">{{ pwdError }}</p>
        <p v-if="pwdMessage" class="hint">{{ pwdMessage }}</p>
        <button class="primary-button" type="submit">修改密码</button>
      </form>
    </section>

    <section class="panel" style="margin-top: 16px">
      <div class="panel-head"><h2>通知</h2><span class="muted">{{ notifications.length }}</span></div>
      <div v-if="notifications.length === 0" class="empty">暂无通知。</div>
      <ul v-else class="notice-list">
        <li v-for="notification in notifications" :key="notification.id" :class="{ unread: !notification.readAt }" @click="read(notification)">
          <div>
            <strong>{{ notification.title }}</strong>
            <p class="muted">{{ notification.body }}</p>
          </div>
          <span class="muted">{{ formatDate(notification.createdAt) }}</span>
        </li>
      </ul>
    </section>
  </AppShell>
</template>
