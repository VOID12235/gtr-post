<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppShell from '../components/AppShell.vue'
import { api, uploadFile } from '../api/http'
import type { Contest } from '../api/types'

const route = useRoute()
const router = useRouter()
const contestId = Number(route.params.contestId)
const contest = ref<Contest | null>(null)
const title = ref('')
const description = ref('')
const videoUrl = ref('')
const fileUrl = ref('')
const fileName = ref('')
const uploading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')

onMounted(async () => {
  try {
    contest.value = await api.get<Contest>(`/api/v1/public/contests/${contestId}?tenantCode=demo`)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '赛事加载失败'
  }
})

async function chooseFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  error.value = ''
  try {
    const uploaded = await uploadFile(file)
    fileUrl.value = uploaded.url
    fileName.value = uploaded.originalName
    message.value = '文件已上传，提交后可在个人中心查看'
  } catch (e) {
    error.value = e instanceof Error ? e.message : '上传失败'
  } finally {
    uploading.value = false
  }
}

async function submit() {
  if (saving.value) return
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    await api.post('/api/v1/works', {
      contestId,
      title: title.value.trim(),
      description: description.value.trim(),
      videoUrl: videoUrl.value.trim() || null,
      fileUrl: fileUrl.value || null
    })
    await router.push('/me')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '提交失败'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">SUBMIT</p>
        <h1>提交作品</h1>
        <p class="muted">{{ contest?.title || '赛事' }} · 仅报名成功的选手可提交</p>
      </div>
      <RouterLink class="secondary-button" to="/me">返回个人中心</RouterLink>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>
    <p v-if="message" class="alert success">{{ message }}</p>

    <section class="panel">
      <form class="form" @submit.prevent="submit">
        <label class="field">
          <span>作品标题</span>
          <input v-model="title" required placeholder="给作品起个名字" />
        </label>
        <label class="field">
          <span>作品说明</span>
          <textarea v-model="description" rows="5" placeholder="灵感、过程、亮点"></textarea>
        </label>
        <label class="field">
          <span>视频 / 外部链接</span>
          <input v-model="videoUrl" placeholder="https://..." />
        </label>
        <label class="field">
          <span>作品文件</span>
          <input type="file" @change="chooseFile" />
          <span v-if="uploading" class="hint">上传中…</span>
          <span v-else-if="fileName" class="hint">已上传：{{ fileName }}</span>
        </label>
        <button class="primary-button" type="submit" :disabled="saving || uploading">
          {{ saving ? '提交中…' : '提交作品' }}
        </button>
      </form>
    </section>
  </AppShell>
</template>
