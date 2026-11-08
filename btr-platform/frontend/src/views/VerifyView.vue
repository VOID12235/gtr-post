<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import PublicShell from '../components/PublicShell.vue'
import { api } from '../api/http'
import type { Certificate } from '../api/types'
import { formatDate } from '../utils/format'

const route = useRoute()
const code = ref(typeof route.params.code === 'string' ? route.params.code : '')
const certificate = ref<Certificate | null>(null)
const error = ref('')
const loading = ref(false)

async function verify() {
  if (!code.value.trim()) return
  loading.value = true
  certificate.value = null
  error.value = ''
  try {
    certificate.value = await api.get<Certificate>(`/api/v1/public/certificates/${encodeURIComponent(code.value.trim())}`)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '查询失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (code.value) verify()
})
</script>

<template>
  <PublicShell>
    <section class="verify-wrap">
      <p class="eyebrow">VERIFY</p>
      <h1>证书验真</h1>
      <p class="muted">输入证书编号，验证赛事、奖项与持有人。</p>

      <form class="verify-form" @submit.prevent="verify">
        <input v-model="code" placeholder="例如 BTR-1-AB12CD34" />
        <button class="primary-button" type="submit" :disabled="loading">{{ loading ? '查询中…' : '验证' }}</button>
      </form>

      <p v-if="error" class="alert">{{ error }}</p>

      <article v-if="certificate" class="certificate-card">
        <div class="certificate-top">
          <img src="/brand/logo-mark.svg" alt="BTR" />
          <span class="badge ok">{{ certificate.status }}</span>
        </div>
        <p class="certificate-label">CERTIFICATE OF PARTICIPATION</p>
        <h2>{{ certificate.holder || '持证人' }}</h2>
        <p class="certificate-text">在 <strong>{{ certificate.contestTitle }}</strong> 中获得</p>
        <p class="certificate-award">{{ certificate.award }}</p>
        <div class="certificate-meta">
          <span>编号 {{ certificate.code }}</span>
          <span>签发 {{ formatDate(certificate.issuedAt) }}</span>
        </div>
      </article>
    </section>
  </PublicShell>
</template>
