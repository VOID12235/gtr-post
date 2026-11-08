<script setup lang="ts">
withDefaults(defineProps<{ variant?: 'dark' | 'light' }>(), {
  variant: 'dark'
})
</script>

<template>
  <img
    class="brand-logo"
    :src="variant === 'light' ? '/brand/logo-horizontal-light.svg' : '/brand/logo-horizontal.svg'"
    alt="BTR 社会赛事平台"
  />
</template>
