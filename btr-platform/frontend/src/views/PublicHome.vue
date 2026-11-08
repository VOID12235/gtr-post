<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PublicShell from '../components/PublicShell.vue'
import { api } from '../api/http'
import type { Contest, PublicPlacement } from '../api/types'
import { statusLabel } from '../utils/format'

const contests = ref<Contest[]>([])
const sponsor = ref<PublicPlacement | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const [list, placements] = await Promise.all([
      api.get<Contest[]>('/api/v1/public/contests?tenantCode=demo'),
      api.get<PublicPlacement[]>('/api/v1/public/placements?slot=home.banner&tenantCode=demo')
    ])
    contests.value = list
    sponsor.value = placements[0] ?? null
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <PublicShell>
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">SOCIAL COMPETITION OS</p>
        <h1>把赛事，做成一件<span>可运营</span>的事。</h1>
        <p class="hero-sub">报名、作品、评审、赞助、证书，一套系统跑完。主办方免费，赞助商为结果付费。</p>
        <div class="hero-actions">
          <RouterLink class="primary-button" to="/contests">浏览赛事</RouterLink>
          <RouterLink class="secondary-button" to="/login">进入工作台</RouterLink>
        </div>
        <div class="hero-stats">
          <div><strong>{{ contests.length }}</strong><span>场公开赛事</span></div>
          <div><strong>3%</strong><span>报名费抽成</span></div>
          <div><strong>7 天</strong><span>公示周期</span></div>
        </div>
      </div>
      <div class="hero-card">
        <div class="hero-card-head">
          <span class="badge">LIVE</span>
          <span class="muted">平台概况</span>
        </div>
        <div class="hero-lines">
          <div><span>主办方</span><strong>免费</strong></div>
          <div><span>评审</span><strong>盲评 + 审计</strong></div>
          <div><span>证书</span><strong>扫码验真</strong></div>
          <div><span>赞助</span><strong>曝光/点击/报名</strong></div>
        </div>
        <RouterLink class="primary-button full" to="/contests">查看正在报名的赛事</RouterLink>
      </div>
    </section>

    <section v-if="sponsor" class="sponsor-strip">
      <span class="sponsor-label">赞助</span>
      <a :href="sponsor.url || '#'" target="_blank" rel="noreferrer">
        <strong>{{ sponsor.sponsorName }}</strong>
        <span>{{ sponsor.text }}</span>
      </a>
    </section>

    <section class="section">
      <div class="section-head">
        <div>
          <p class="eyebrow">FEATURED</p>
          <h2>正在进行的赛事</h2>
        </div>
        <RouterLink to="/contests">全部赛事 →</RouterLink>
      </div>

      <div v-if="loading" class="empty">加载中…</div>
      <div v-else-if="contests.length === 0" class="empty">暂无公开赛事。</div>
      <div v-else class="contest-grid">
        <RouterLink v-for="contest in contests.slice(0, 3)" :key="contest.id" class="contest-card" :to="`/contests/${contest.id}`">
          <div class="contest-cover">
            <span class="badge" :class="contest.status === 'SIGNUP' ? 'ok' : 'info'">{{ statusLabel(contest.status) }}</span>
          </div>
          <div class="contest-body">
            <h3>{{ contest.title }}</h3>
            <p>{{ contest.summary || '用作品说话，让评审看见。' }}</p>
            <span class="contest-meta">报名截止 {{ contest.signupEnd?.slice(0, 10) || '—' }}</span>
          </div>
        </RouterLink>
      </div>
    </section>

    <section class="section features">
      <div class="feature">
        <strong>01</strong>
        <h3>赞助可归因</h3>
        <p>曝光、点击、报名、作品全链路，赞助商拿得到报告。</p>
      </div>
      <div class="feature">
        <strong>02</strong>
        <h3>评审可审计</h3>
        <p>评分表、盲评、改分留痕，结果经得起复盘。</p>
      </div>
      <div class="feature">
        <strong>03</strong>
        <h3>证书可验真</h3>
        <p>每张证书一个编号，扫码即可验证。</p>
      </div>
    </section>
  </PublicShell>
</template>
