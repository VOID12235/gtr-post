<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SliderCaptcha from '../components/SliderCaptcha.vue'
import { auth } from '../auth'

const route = useRoute()
const router = useRouter()

type Mode = 'login' | 'register' | 'forgot'
const mode = ref<Mode>('login')
const account = ref('')
const email = ref('')
const password = ref('')
const nickname = ref('')
const code = ref('')
const newPassword = ref('')
const sliderTicket = ref('')
const captchaKey = ref(0)
const loading = ref(false)
const sending = ref(false)
const error = ref('')
const message = ref('')

function switchMode(next: Mode) {
  mode.value = next
  error.value = ''
  message.value = ''
  sliderTicket.value = ''
  captchaKey.value += 1
}

function onVerified(ticket: string) {
  sliderTicket.value = ticket
  error.value = ''
}

function onCaptchaReset() {
  sliderTicket.value = ''
}

function useDemo(kind: 'player' | 'judge') {
  switchMode('login')
  account.value = kind
  password.value = `${kind}123`
}

function useAdmin() {
  switchMode('login')
  account.value = 'admin'
  password.value = ''
  message.value = '请输入部署时设置的管理员密码'
}

async function sendCode() {
  if (!email.value.trim()) {
    error.value = '请先填写邮箱'
    return
  }
  if (!sliderTicket.value) {
    error.value = '请先完成滑动验证'
    return
  }
  sending.value = true
  error.value = ''
  message.value = ''
  try {
    if (mode.value === 'register') {
      await auth.sendRegisterCode(email.value.trim(), sliderTicket.value)
      message.value = '验证码已发送到邮箱，请查收（含垃圾箱）'
    } else {
      await auth.forgotPassword(email.value.trim(), sliderTicket.value)
      message.value = '如果邮箱存在，重置验证码已发送'
    }
    sliderTicket.value = ''
    captchaKey.value += 1
  } catch (e) {
    error.value = e instanceof Error ? e.message : '发送失败'
  } finally {
    sending.value = false
  }
}

async function submit() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    if (mode.value === 'login') {
      if (!sliderTicket.value) {
        error.value = '请先完成滑动验证'
        return
      }
      await auth.login(account.value.trim(), password.value, sliderTicket.value)
      const fallback = auth.user?.role === 'PLAYER' ? '/me' : auth.user?.role === 'JUDGE' ? '/judge' : '/dashboard'
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : fallback
      await router.replace(redirect)
    } else if (mode.value === 'register') {
      await auth.register(email.value.trim(), password.value, nickname.value.trim(), code.value.trim())
      await router.replace('/me')
    } else {
      await auth.resetPassword(email.value.trim(), code.value.trim(), newPassword.value)
      mode.value = 'login'
      message.value = '密码已重置，请用新密码登录'
      sliderTicket.value = ''
      captchaKey.value += 1
      password.value = ''
      newPassword.value = ''
      code.value = ''
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <section class="login-brand">
      <RouterLink to="/"><img class="login-mark" src="/brand/logo-mark.svg" alt="BTR" /></RouterLink>
      <h1>把赛事，做成基础设施。</h1>
      <p>多租户、赞助投放、评审公信力、证书与交易，一套系统跑完。</p>
      <div class="login-tags">
        <span>社会赛事</span>
        <span>赞助变现</span>
        <span>评审可审计</span>
      </div>
      <RouterLink to="/" class="login-back">← 返回公开站</RouterLink>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="login-heading">
          <h2>
            {{ mode === 'login' ? '登录 BTR' : mode === 'register' ? '邮箱注册' : '找回密码' }}
          </h2>
          <p>
            {{ mode === 'login' ? '使用邮箱（或账号）与密码登录' : mode === 'register' ? '账号即邮箱，注册后可直接报名' : '通过邮箱验证码重置密码' }}
          </p>
        </div>

        <form class="form" @submit.prevent="submit">
          <template v-if="mode === 'login'">
            <label class="field">
              <span>邮箱 / 账号</span>
              <input v-model="account" autocomplete="username" placeholder="you@example.com" required />
            </label>
            <label class="field">
              <span>密码</span>
              <input v-model="password" type="password" autocomplete="current-password" placeholder="••••••••" required />
            </label>
          </template>

          <template v-if="mode === 'register'">
            <label class="field">
              <span>邮箱（账号）</span>
              <input v-model="email" type="email" autocomplete="email" placeholder="you@example.com" required />
            </label>
            <label class="field">
              <span>昵称</span>
              <input v-model="nickname" placeholder="你的昵称" required />
            </label>
            <label class="field">
              <span>密码（至少 6 位）</span>
              <input v-model="password" type="password" autocomplete="new-password" minlength="6" required />
            </label>
            <label class="field">
              <span>邮箱验证码</span>
              <div class="field-row">
                <input v-model="code" inputmode="numeric" maxlength="6" placeholder="6 位验证码" required />
                <button class="secondary-button" type="button" :disabled="sending || !sliderTicket" @click="sendCode">
                  {{ sending ? '发送中…' : '发送验证码' }}
                </button>
              </div>
            </label>
          </template>

          <template v-if="mode === 'forgot'">
            <label class="field">
              <span>注册邮箱</span>
              <input v-model="email" type="email" autocomplete="email" placeholder="you@example.com" required />
            </label>
            <label class="field">
              <span>邮箱验证码</span>
              <div class="field-row">
                <input v-model="code" inputmode="numeric" maxlength="6" placeholder="6 位验证码" required />
                <button class="secondary-button" type="button" :disabled="sending || !sliderTicket" @click="sendCode">
                  {{ sending ? '发送中…' : '发送验证码' }}
                </button>
              </div>
            </label>
            <label class="field">
              <span>新密码（至少 6 位）</span>
              <input v-model="newPassword" type="password" autocomplete="new-password" minlength="6" required />
            </label>
          </template>

          <div class="captcha-block">
            <span class="captcha-label">{{ mode === 'login' ? '安全验证' : '发送验证码前完成滑动验证' }}</span>
            <SliderCaptcha :key="captchaKey" @verified="onVerified" @reset="onCaptchaReset" />
          </div>

          <p v-if="error" class="form-error">{{ error }}</p>
          <p v-if="message" class="alert success">{{ message }}</p>

          <button class="primary-button" type="submit" :disabled="loading">
            {{ loading ? '处理中…' : mode === 'login' ? '进入工作台' : mode === 'register' ? '注册并登录' : '重置密码' }}
          </button>
        </form>

        <div class="login-switch">
          <template v-if="mode === 'login'">
            <button type="button" class="link-button" @click="switchMode('forgot')">忘记密码？</button>
            <span class="muted"> · </span>
            <button type="button" class="link-button" @click="switchMode('register')">没有账号？注册</button>
          </template>
          <template v-else>
            <button type="button" class="link-button" @click="switchMode('login')">返回登录</button>
          </template>
        </div>

        <div v-if="mode === 'login'" class="demo-box">
          <span>演示账号</span>
          <div class="demo-actions">
            <button type="button" @click="useAdmin">管理员</button>
            <button type="button" @click="useDemo('player')">选手</button>
            <button type="button" @click="useDemo('judge')">评委</button>
          </div>
          <p>选手 player/player123 · 评委 judge/judge123 · 管理员密码见部署清单</p>
        </div>
      </div>
    </section>
  </div>
</template>
