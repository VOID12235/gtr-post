<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { api } from '../api/http'

const emit = defineEmits<{
  verified: [ticket: string]
  reset: []
}>()

const track = ref<HTMLElement | null>(null)
const trackWidth = ref(0)
const position = ref(0)
const dragging = ref(false)
const verified = ref(false)
const loading = ref(false)
const challengeId = ref('')
const startX = ref(0)
const startTime = ref(0)
const error = ref('')

function measure() {
  trackWidth.value = track.value?.clientWidth ?? 0
}

async function loadChallenge() {
  error.value = ''
  verified.value = false
  position.value = 0
  try {
    const data = await api.get<{ challengeId: string }>('/api/v1/auth/captcha/slider')
    challengeId.value = data.challengeId
    measure()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '验证加载失败'
  }
}

function onDown(event: PointerEvent) {
  if (verified.value || loading.value) return
  dragging.value = true
  startX.value = event.clientX
  startTime.value = Date.now()
  try {
    ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
  } catch {
    // synthetic events or older browsers may not support pointer capture
  }
}

function onMove(event: PointerEvent) {
  if (!dragging.value) return
  const max = Math.max(trackWidth.value - 44, 1)
  const delta = event.clientX - startX.value
  position.value = Math.min(1, Math.max(0, delta / max))
}

async function onUp() {
  if (!dragging.value) return
  dragging.value = false
  if (position.value < 0.9) {
    reset()
    return
  }
  loading.value = true
  error.value = ''
  try {
    const result = await api.post<{ ticket: string }>('/api/v1/auth/captcha/slider/verify', {
      challengeId: challengeId.value,
      position: position.value,
      duration: Date.now() - startTime.value
    })
    verified.value = true
    position.value = 1
    emit('verified', result.ticket)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '验证失败'
    setTimeout(loadChallenge, 600)
  } finally {
    loading.value = false
  }
}

function reset() {
  position.value = 0
  verified.value = false
  error.value = ''
  emit('reset')
  loadChallenge()
}

defineExpose({ reset })

onMounted(() => {
  loadChallenge()
  window.addEventListener('resize', measure)
})

onUnmounted(() => {
  window.removeEventListener('resize', measure)
})
</script>

<template>
  <div class="slider-captcha">
    <div
      ref="track"
      class="slider-track"
      :class="{ verified }"
      @pointerdown="onDown"
      @pointermove="onMove"
      @pointerup="onUp"
      @pointercancel="onUp"
    >
      <div class="slider-fill" :style="{ width: `${position * 100}%` }" />
      <div class="slider-handle" :style="{ transform: `translateX(${Math.max(0, trackWidth - 44) * position}px)` }">
        {{ verified ? '✓' : '→' }}
      </div>
      <span class="slider-label">
        {{ verified ? '验证通过' : loading ? '验证中…' : '按住滑块，向右拖动' }}
      </span>
    </div>
    <p v-if="error" class="form-error">{{ error }}</p>
  </div>
</template>
