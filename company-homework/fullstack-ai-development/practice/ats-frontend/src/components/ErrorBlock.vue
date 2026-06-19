<script setup lang="ts">
import { NButton } from 'naive-ui'

withDefaults(defineProps<{
  /** 主标题 */
  title?: string
  /** 错误详情 / 提示 */
  description?: string
  /** 是否展示重试按钮 */
  showRetry?: boolean
  /** 重试按钮 loading 态 */
  retrying?: boolean
}>(), {
  title: '加载失败',
  description: '请检查网络后重试，问题持续请联系管理员',
  showRetry: true,
  retrying: false,
})

const emit = defineEmits<{
  retry: []
}>()
</script>

<template>
  <div
    flex flex-col items-center justify-center text-center py-14 px-6
    role="alert"
  >
    <!-- 警示图标 · 红色渐变 -->
    <div
      relative mb-5 flex items-center justify-center w-72px h-72px rounded-full
      :style="{
        background: 'linear-gradient(135deg, var(--danger-50), rgba(239,68,68,.18))',
        boxShadow: '0 0 0 6px rgba(239,68,68,.08)',
      }"
    >
      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" :style="{ color: 'var(--danger-700)' }" aria-hidden="true">
        <path
          d="M12 8v5M12 16.5h.01M10.3 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.7 3.86a2 2 0 0 0-3.4 0Z"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </div>

    <h3 m-0 mb-1 text-lg font-semibold text-primary tracking-tight>
      {{ title }}
    </h3>
    <p m-0 mb-5 max-w-480px text-sm text-secondary leading-relaxed>
      {{ description }}
    </p>

    <div flex items-center gap-3>
      <NButton
        v-if="showRetry"
        type="primary"
        :loading="retrying"
        size="medium"
        @click="emit('retry')"
      >
        <template #icon>
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M2 7a5 5 0 0 1 8.5-3.5M12 7a5 5 0 0 1-8.5 3.5M10 1.5v3h-3M4 12.5v-3h3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </template>
        重新加载
      </NButton>
      <slot name="extra" />
    </div>
  </div>
</template>
