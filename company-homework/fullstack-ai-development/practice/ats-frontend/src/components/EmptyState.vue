<script setup lang="ts">
withDefaults(defineProps<{
  /** 主标题，例 "还没有任何投递" */
  title?: string
  /** 副文案，描述场景 */
  description?: string
  /** 图标类型 —— inbox / search / chart / lock / spark；默认 spark */
  icon?: 'inbox' | 'search' | 'chart' | 'lock' | 'spark'
  /** 紧凑模式（卡片内嵌时用），减少上下 padding */
  compact?: boolean
}>(), {
  title: '暂无数据',
  description: '',
  icon: 'spark',
  compact: false,
})
</script>

<template>
  <div
    flex flex-col items-center justify-center text-center
    :class="compact ? 'py-10 px-6' : 'py-16 px-6'"
  >
    <!-- 装饰性图标 · 走品牌渐变背景 + 单色描边 SVG -->
    <div
      relative mb-5 flex items-center justify-center w-72px h-72px rounded-full
      :style="{
        background: 'linear-gradient(135deg, var(--brand-50), var(--accent-mint) 200%)',
        boxShadow: '0 0 0 6px rgba(16,185,129,.08)',
      }"
    >
      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" :style="{ color: 'var(--brand-600)' }" aria-hidden="true">
        <!-- inbox -->
        <template v-if="icon === 'inbox'">
          <path d="M3 13l3-7h12l3 7M3 13v6a1 1 0 0 0 1 1h16a1 1 0 0 0 1-1v-6M3 13h5l1 2h6l1-2h5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </template>
        <!-- search -->
        <template v-else-if="icon === 'search'">
          <circle cx="11" cy="11" r="6" stroke="currentColor" stroke-width="1.6" />
          <path d="m20 20-3.5-3.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
        </template>
        <!-- chart -->
        <template v-else-if="icon === 'chart'">
          <path d="M4 19V5M4 19h16M8 15v-4M12 15V8M16 15v-2" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </template>
        <!-- lock -->
        <template v-else-if="icon === 'lock'">
          <rect x="5" y="11" width="14" height="9" rx="2" stroke="currentColor" stroke-width="1.6" />
          <path d="M8 11V7a4 4 0 0 1 8 0v4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
        </template>
        <!-- spark · 默认 -->
        <template v-else>
          <path d="M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M5.6 18.4l2.1-2.1M16.3 7.7l2.1-2.1" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
        </template>
      </svg>
    </div>

    <h3 m-0 mb-1 text-lg font-semibold text-primary tracking-tight>
      {{ title }}
    </h3>
    <p v-if="description" m-0 max-w-420px text-sm text-secondary leading-relaxed>
      {{ description }}
    </p>

    <!-- action slot · 放置 CTA 按钮 -->
    <div v-if="$slots.action" mt-5>
      <slot name="action" />
    </div>
  </div>
</template>
