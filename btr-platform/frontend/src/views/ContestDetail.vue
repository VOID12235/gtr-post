<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import PublicShell from '../components/PublicShell.vue'
import { api } from '../api/http'
import { auth } from '../auth'
import type { Contest, PublicPlacement, Rubric, Work } from '../api/types'
import { formatDate, statusLabel } from '../utils/format'

const route = useRoute()
const contest = ref<Contest | null>(null)
const works = ref<Work[]>([])
const rubric = ref<Rubric | null>(null)
const sponsor = ref<PublicPlacement | null>(null)
const loading = ref(true)
const message = ref('')
const signingUp = ref(false)
const signedUp = ref(false)

const contestId = computed(() => Number(route.params.id))

async function load() {
  loading.value = true
  try {
    const [detail, workList, placementList, rubricList] = await Promise.all([
      api.get<Contest>(`/api/v1/public/contests/${contestId.value}?tenantCode=demo`),
      api.get<Work[]>(`/api/v1/public/works?contestId=${contestId.value}&tenantCode=demo`),
      api.get<PublicPlacement[]>(`/api/v1/public/placements?slot=contest.title&contestId=${contestId.value}&tenantCode=demo`),
      api.get<Rubric[]>(`/api/v1/public/rubrics?contestId=${contestId.value}&tenantCode=demo`)
    ])
    contest.value = detail
    works.value = workList
    sponsor.value = placementList[0] ?? null
    rubric.value = rubricList[0] ?? null
  } catch (e) {
    message.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function signup() {
  if (!contest.value || signingUp.value) return
  signingUp.value = true
  message.value = ''
  try {
    await api.post('/api/v1/signups', {
      contestId: contest.value.id,
      teamName: auth.user?.nickname + '的队伍',
      contact: auth.user?.account
    })
    signedUp.value = true
    message.value = contest.value.feeEnabled
      ? '报名已创建，请到个人中心查看待支付订单；管理员确认支付后报名生效。'
      : '报名成功，下一步提交作品。'
  } catch (e) {
    message.value = e instanceof Error ? e.message : '报名失败'
  } finally {
    signingUp.value = false
  }
}

onMounted(load)
</script>

<template>
  <PublicShell>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!contest" class="empty">{{ message || '赛事不存在' }}</div>
    <template v-else>
      <section class="detail-head">
        <div>
          <span class="badge" :class="contest.status === 'SIGNUP' ? 'ok' : 'info'">{{ statusLabel(contest.status) }}</span>
          <h1>{{ contest.title }}</h1>
          <p class="muted">{{ contest.summary }}</p>
          <div class="detail-meta">
            <span>{{ contest.mode === 'OFFLINE' ? '线下赛事' : '线上赛事' }}</span>
            <span>报名截止 {{ formatDate(contest.signupEnd) }}</span>
            <span>提交截止 {{ formatDate(contest.submitEnd) }}</span>
            <span v-if="contest.feeEnabled">报名费 ¥{{ contest.feeAmount }}</span>
          </div>
        </div>
        <div class="detail-action">
          <RouterLink v-if="!auth.token" class="primary-button" :to="{ path: '/login', query: { redirect: route.fullPath } }">
            登录后报名
          </RouterLink>
          <button v-else class="primary-button" type="button" :disabled="signingUp" @click="signup">
            {{ signingUp ? '报名中…' : contest.feeEnabled ? '报名并支付' : '立即报名' }}
          </button>
          <RouterLink class="secondary-button" to="/contests">返回列表</RouterLink>
        </div>
      </section>

      <p v-if="message" class="alert">{{ message }}</p>
      <div v-if="signedUp" class="page-actions">
        <RouterLink class="primary-button" to="/me">查看报名 / 支付状态</RouterLink>
        <RouterLink v-if="!contest.feeEnabled" class="secondary-button" :to="`/submit/${contest.id}`">
          提交作品
        </RouterLink>
      </div>

      <section v-if="sponsor" class="sponsor-strip">
        <span class="sponsor-label">赞助</span>
        <a :href="sponsor.url || '#'" target="_blank" rel="noreferrer">
          <strong>{{ sponsor.sponsorName }}</strong>
          <span>{{ sponsor.text }}</span>
        </a>
      </section>

      <div class="detail-grid">
        <section class="panel">
          <div class="panel-head"><h2>赛事时间线</h2></div>
          <ul class="timeline">
            <li><span>报名</span><strong>{{ formatDate(contest.signupStart) }} → {{ formatDate(contest.signupEnd) }}</strong></li>
            <li><span>提交</span><strong>{{ formatDate(contest.submitStart) }} → {{ formatDate(contest.submitEnd) }}</strong></li>
            <li><span>评审</span><strong>{{ formatDate(contest.reviewStart) }} → {{ formatDate(contest.reviewEnd) }}</strong></li>
            <li><span>公示</span><strong>{{ formatDate(contest.publishedAt) }}</strong></li>
          </ul>
        </section>

        <section class="panel">
          <div class="panel-head"><h2>评审标准</h2></div>
          <div v-if="!rubric" class="empty">评审标准待公布。</div>
          <ul v-else class="rubric-list">
            <li v-for="item in rubric.items" :key="item.id">
              <div><strong>{{ item.label }}</strong><span class="muted">{{ item.min }}–{{ item.max }}</span></div>
              <span class="price">{{ Math.round(Number(item.weight) * 100) }}%</span>
            </li>
          </ul>
        </section>
      </div>

      <section class="section">
        <div class="section-head">
          <div>
            <p class="eyebrow">WORKS</p>
            <h2>参赛作品</h2>
          </div>
          <span class="muted">{{ works.length }} 件</span>
        </div>
        <div v-if="works.length === 0" class="empty">作品提交中，敬请期待。</div>
        <div v-else class="work-grid">
          <article v-for="work in works" :key="work.id" class="work-card">
            <h3>{{ work.title }}</h3>
            <p>{{ work.description }}</p>
            <div class="work-meta">
              <span>{{ work.authorNickname || '匿名选手' }}</span>
              <span v-if="work.totalScore != null">得分 {{ work.totalScore }}</span>
              <a v-if="work.videoUrl" :href="work.videoUrl" target="_blank" rel="noreferrer">查看作品 →</a>
            </div>
          </article>
        </div>
      </section>
    </template>
  </PublicShell>
</template>
