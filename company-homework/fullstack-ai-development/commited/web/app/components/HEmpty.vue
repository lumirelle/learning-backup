<script lang="ts" setup>
defineProps<{ icon?: string, label?: string, hint?: string }>()
</script>

<template>
  <div class="py-10 text-center flex flex-col gap-2 items-center">
    <div class="rounded-full bg-truegray-100/80 flex h-10 w-10 items-center justify-center dark:bg-white/6">
      <span class="text-lg text-truegray-300 dark:text-truegray-500" :class="icon || 'i-carbon-document-blank'" />
    </div>
    <div class="text-sm text-truegray-400">
      {{ label || '暂无数据' }}
    </div>
    <div v-if="hint" class="text-xs text-truegray-300 dark:text-truegray-500">
      {{ hint }}
    </div>
    <slot />
  </div>
</template>
