<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import type { Campaign, Sponsor } from '../api/types'
import { formatDate, statusLabel } from '../utils/format'

const sponsors = ref<Sponsor[]>([])
const campaigns = ref<Campaign[]>([])
const loading = ref(true)
const error = ref('')
const success = ref('')

const sponsorForm = reactive({ name: '', industry: '', contactName: '', contactEmail: '' })
const campaignForm = reactive({ sponsorId: 0, name: '', type: 'TITLE', budget: 200000, creativeText: '', creativeUrl: '', slot: 'contest.title' })

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [sponsorList, campaignList] = await Promise.all([
      api.get<Sponsor[]>('/api/v1/platform/sponsors'),
      api.get<Campaign[]>('/api/v1/platform/campaigns')
    ])
    sponsors.value = sponsorList
    campaigns.value = campaignList
    if (!campaignForm.sponsorId && sponsorList.length > 0) {
      campaignForm.sponsorId = sponsorList[0].id
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function createSponsor() {
  error.value = ''
  success.value = ''
  try {
    await api.post('/api/v1/platform/sponsors', sponsorForm)
    success.value = '赞助商已创建'
    sponsorForm.name = ''
    sponsorForm.industry = ''
    sponsorForm.contactName = ''
    sponsorForm.contactEmail = ''
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  }
}

async function createCampaign() {
  error.value = ''
  success.value = ''
  try {
    await api.post('/api/v1/platform/campaigns', {
      sponsorId: campaignForm.sponsorId,
      name: campaignForm.name,
      type: campaignForm.type,
      budget: Number(campaignForm.budget),
      creativeText: campaignForm.creativeText,
      creativeUrl: campaignForm.creativeUrl,
      slot: campaignForm.slot,
      startAt: new Date().toISOString().slice(0, 19),
      endAt: new Date(Date.now() + 60 * 86400000).toISOString().slice(0, 19)
    })
    success.value = '赞助活动已创建（DRAFT），点击通过后上线'
    campaignForm.name = ''
    campaignForm.creativeText = ''
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  }
}

async function approve(campaign: Campaign) {
  try {
    await api.post(`/api/v1/platform/campaigns/${campaign.id}/approve`, {})
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '审核失败'
  }
}

onMounted(load)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">SPONSORSHIP</p>
        <h1>赞助商与投放</h1>
        <p class="muted">赞助商、活动、库存位、公开页曝光。</p>
      </div>
      <button class="secondary-button" type="button" @click="load">刷新</button>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>
    <p v-if="success" class="alert success">{{ success }}</p>

    <div class="stat-grid">
      <article class="stat-card"><span class="stat-label">赞助商</span><strong class="stat-value">{{ sponsors.length }}</strong><span class="stat-hint">平台级客户</span></article>
      <article class="stat-card"><span class="stat-label">投放活动</span><strong class="stat-value">{{ campaigns.length }}</strong><span class="stat-hint">含草稿与已通过</span></article>
      <article class="stat-card"><span class="stat-label">已上线</span><strong class="stat-value">{{ campaigns.filter(c => c.status === 'APPROVED').length }}</strong><span class="stat-hint">公开页可见</span></article>
      <article class="stat-card"><span class="stat-label">库存位</span><strong class="stat-value">{{ campaigns.reduce((n, c) => n + c.placements.length, 0) }}</strong><span class="stat-hint">冠名/榜单/证书</span></article>
    </div>

    <section class="panel">
      <div class="panel-head"><h2>投放活动</h2><span class="muted">{{ campaigns.length }}</span></div>
      <div v-if="loading" class="empty">加载中…</div>
      <div v-else-if="campaigns.length === 0" class="empty">还没有投放活动。</div>
      <div v-else class="campaign-list">
        <article v-for="campaign in campaigns" :key="campaign.id" class="campaign-card">
          <div>
            <div class="campaign-title">
              <strong>{{ campaign.name }}</strong>
              <span class="badge" :class="campaign.status === 'APPROVED' ? 'ok' : 'warn'">{{ statusLabel(campaign.status) }}</span>
            </div>
            <p class="muted">{{ campaign.sponsorName }} · {{ campaign.type }} · ¥{{ campaign.budget ?? 0 }}</p>
            <p class="muted">{{ formatDate(campaign.startAt) }} → {{ formatDate(campaign.endAt) }}</p>
            <div class="placement-tags">
              <span v-for="placement in campaign.placements" :key="placement.id">{{ placement.slot }}</span>
            </div>
          </div>
          <button v-if="campaign.status !== 'APPROVED'" class="primary-button" type="button" @click="approve(campaign)">通过审核</button>
        </article>
      </div>
    </section>

    <div class="content-grid" style="margin-top: 16px">
      <section class="panel">
        <div class="panel-head"><h2>新建赞助商</h2></div>
        <form class="form" @submit.prevent="createSponsor">
          <label class="field"><span>名称</span><input v-model="sponsorForm.name" required /></label>
          <label class="field"><span>行业</span><input v-model="sponsorForm.industry" placeholder="云计算" /></label>
          <div class="field-row">
            <label class="field"><span>联系人</span><input v-model="sponsorForm.contactName" /></label>
            <label class="field"><span>邮箱</span><input v-model="sponsorForm.contactEmail" type="email" /></label>
          </div>
          <button class="primary-button" type="submit">创建赞助商</button>
        </form>
      </section>

      <section class="panel">
        <div class="panel-head"><h2>新建投放</h2></div>
        <form class="form" @submit.prevent="createCampaign">
          <label class="field">
            <span>赞助商</span>
            <select v-model.number="campaignForm.sponsorId">
              <option v-for="sponsor in sponsors" :key="sponsor.id" :value="sponsor.id">{{ sponsor.name }}</option>
            </select>
          </label>
          <label class="field"><span>活动名称</span><input v-model="campaignForm.name" required /></label>
          <div class="field-row">
            <label class="field">
              <span>类型</span>
              <select v-model="campaignForm.type">
                <option value="TITLE">冠名</option>
                <option value="DISPLAY">广告位</option>
                <option value="TALENT">招聘</option>
              </select>
            </label>
            <label class="field"><span>预算</span><input v-model.number="campaignForm.budget" type="number" /></label>
          </div>
          <label class="field">
            <span>库存位</span>
            <select v-model="campaignForm.slot">
              <option value="contest.title">赛事冠名</option>
              <option value="rank.sponsor">榜单赞助条</option>
              <option value="certificate.logo">证书 Logo</option>
              <option value="home.banner">首页 Banner</option>
            </select>
          </label>
          <label class="field"><span>文案</span><input v-model="campaignForm.creativeText" placeholder="品牌主张" /></label>
          <label class="field"><span>链接</span><input v-model="campaignForm.creativeUrl" placeholder="https://" /></label>
          <button class="primary-button" type="submit">创建投放</button>
        </form>
      </section>
    </div>
  </AppShell>
</template>
