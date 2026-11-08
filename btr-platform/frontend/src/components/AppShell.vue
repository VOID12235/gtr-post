<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BrandLogo from './BrandLogo.vue'
import { auth } from '../auth'

const route = useRoute()
const router = useRouter()

const userLabel = computed(() => auth.user?.nickname || auth.user?.account || '未登录')
const isAdmin = computed(() =>
  auth.user?.platformRole === 'PLATFORM_ADMIN' ||
  auth.user?.role === 'TENANT_OWNER' ||
  auth.user?.role === 'TENANT_ADMIN'
)
const isJudge = computed(() => auth.user?.role === 'JUDGE')

const pageTitle = computed(() => {
  if (route.path.startsWith('/tenants')) return '租户管理'
  if (route.path.startsWith('/sponsors')) return '赞助商'
  if (route.path.startsWith('/contests-admin')) return '赛事管理'
  if (route.path.startsWith('/rubrics')) return '评分表'
  if (route.path.startsWith('/certificates')) return '证书'
  if (route.path.startsWith('/judge')) return '评审工作台'
  if (route.path.startsWith('/settings')) return '系统设置'
  if (route.path.startsWith('/members')) return '成员管理'
  if (route.path.startsWith('/shop-admin')) return '商品管理'
  if (route.path.startsWith('/submit')) return '提交作品'
  if (route.path.startsWith('/me')) return '个人中心'
  return '仪表盘'
})

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="brand">
        <RouterLink to="/"><BrandLogo variant="light" /></RouterLink>
      </div>

      <nav class="nav">
        <RouterLink to="/" class="nav-item">公开站</RouterLink>
        <RouterLink v-if="isAdmin" to="/dashboard" class="nav-item">仪表盘</RouterLink>
        <RouterLink v-if="isJudge" to="/judge" class="nav-item">评审工作台</RouterLink>
        <template v-if="isAdmin">
          <RouterLink to="/contests-admin" class="nav-item">赛事管理</RouterLink>
          <RouterLink to="/tenants" class="nav-item">租户管理</RouterLink>
          <RouterLink to="/members" class="nav-item">成员管理</RouterLink>
          <RouterLink to="/sponsors" class="nav-item">赞助商</RouterLink>
          <RouterLink to="/rubrics" class="nav-item">评分表</RouterLink>
          <RouterLink to="/certificates" class="nav-item">证书</RouterLink>
          <RouterLink to="/settings" class="nav-item">系统设置</RouterLink>
          <RouterLink to="/shop-admin" class="nav-item">商品管理</RouterLink>
        </template>
        <RouterLink to="/me" class="nav-item">个人中心</RouterLink>
      </nav>

      <div class="sidebar-footer">
        <div class="tenant-name">{{ auth.tenant?.name || '—' }}</div>
        <div class="user-name">{{ userLabel }}</div>
        <button class="ghost-button" type="button" @click="logout">退出登录</button>
      </div>
    </aside>

    <main class="main">
      <header class="topbar">
        <div class="crumbs">BTR <span>/</span> {{ pageTitle }}</div>
        <div class="health"><span class="dot" /> 系统正常</div>
      </header>
      <section class="content">
        <slot />
      </section>
    </main>
  </div>
</template>
