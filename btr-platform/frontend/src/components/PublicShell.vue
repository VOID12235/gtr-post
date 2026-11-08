<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import BrandLogo from './BrandLogo.vue'
import { auth } from '../auth'
import { api } from '../api/http'

const loggedIn = computed(() => Boolean(auth.token))
const site = reactive({
  name: 'BTR 社会赛事平台',
  footer: '主办方免费 · 赞助商付费 · 证书可验真'
})

onMounted(async () => {
  try {
    const data = await api.get<Record<string, string>>('/api/v1/public/site?tenantCode=demo')
    Object.assign(site, data)
  } catch {
    // keep defaults
  }
})
</script>

<template>
  <div class="public">
    <header class="public-header">
      <RouterLink to="/" class="public-brand"><BrandLogo /></RouterLink>
      <nav class="public-nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/contests">赛事</RouterLink>
        <RouterLink to="/shop">商城</RouterLink>
        <RouterLink to="/verify">证书验真</RouterLink>
      </nav>
      <div class="public-actions">
        <RouterLink v-if="loggedIn" class="secondary-button" to="/dashboard">工作台</RouterLink>
        <RouterLink v-else class="secondary-button" to="/login">登录</RouterLink>
        <RouterLink class="primary-button" to="/contests">立即报名</RouterLink>
      </div>
    </header>

    <main class="public-main">
      <slot />
    </main>

    <footer class="public-footer">
      <div>
        <strong>{{ site.name }}</strong>
        <span>{{ site.footer }}</span>
      </div>
      <div class="muted">主办方免费 · 赞助商付费 · 证书可验真</div>
    </footer>
  </div>
</template>
