<script setup lang="ts">
import { NButton, NTooltip } from 'naive-ui'
import { ref } from 'vue'
import { useCopy } from '@/composables/use-copy'

const props = withDefaults(defineProps<{
  /** 要复制的文本 */
  text: string
  /** 复制成功后 toast 文案 */
  hint?: string
  /** 按钮 size */
  size?: 'tiny' | 'small' | 'medium'
  /** 按钮风格 —— ghost = 仅 icon、tertiary = 浅底带边、quaternary = 隐形按钮 */
  variant?: 'ghost' | 'tertiary' | 'quaternary'
  /** 显示在按钮内的辅助文案；不传则纯 icon */
  label?: string
  /** tooltip 文案，默认根据 hint 衍生 */
  tooltip?: string
}>(), {
  hint: '已复制到剪贴板',
  size: 'tiny',
  variant: 'quaternary',
  label: '',
  tooltip: '',
})

const { copy } = useCopy()
const justCopied = ref(false)

async function onClick(e: MouseEvent) {
  e.stopPropagation()
  const ok = await copy(props.text, props.hint)
  if (ok) {
    justCopied.value = true
    setTimeout(() => {
      justCopied.value = false
    }, 1200)
  }
}
</script>

<template>
  <NTooltip :disabled="!tooltip && !label">
    <template #trigger>
      <NButton
        :size="size"
        :quaternary="variant === 'quaternary'"
        :tertiary="variant === 'tertiary'"
        :ghost="variant === 'ghost'"
        transition-colors duration-fast ease-out
        :class="justCopied ? 'text-success-700' : 'text-tertiary hover:text-primary'"
        @click="onClick"
      >
        <template #icon>
          <svg v-if="!justCopied" width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path
              d="M4.5 1.5h6a1 1 0 0 1 1 1v8M3 3.5h6a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1v-8a1 1 0 0 1 1-1Z"
              stroke="currentColor"
              stroke-width="1.4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <svg v-else width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path
              d="M2.5 7.5L5.5 10.5L11.5 4"
              stroke="currentColor"
              stroke-width="1.8"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </template>
        <span v-if="label">{{ justCopied ? '已复制' : label }}</span>
      </NButton>
    </template>
    {{ tooltip || (justCopied ? '已复制' : `复制：${text}`) }}
  </NTooltip>
</template>
