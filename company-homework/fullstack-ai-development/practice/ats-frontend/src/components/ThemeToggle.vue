<script setup lang="ts">
import { NButton, NDropdown, NIcon } from 'naive-ui'
import { computed, h } from 'vue'
import type { ThemeMode } from '@/stores/theme'
import { useThemeStore } from '@/stores/theme'

const theme = useThemeStore()

const MODE_LABEL: Record<ThemeMode, string> = {
  light: '浅色',
  dark: '深色',
  system: '跟随系统',
}

const options = computed(() =>
  (['light', 'dark', 'system'] as ThemeMode[]).map(key => ({
    key,
    label: `${MODE_LABEL[key]}${theme.mode === key ? ' ✓' : ''}`,
  })),
)

function renderIcon(path: string) {
  return () => h(NIcon, null, {
    default: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', width: 18, height: 18 }, [
      h('path', {
        d: path,
        stroke: 'currentColor',
        'stroke-width': 1.6,
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round',
      }),
    ]),
  })
}

const icon = computed(() =>
  theme.isDark
    ? renderIcon('M12 3 A6 6 0 1 0 12 15 A6 6 0 0 0 12 3 M12 17 V21 M4.22 4.22 L6.34 6.34 M17.66 17.66 L19.78 19.78 M3 12 H7 M17 12 H21 M4.22 19.78 L6.34 17.66 M17.66 6.34 L19.78 4.22')
    : renderIcon('M21 12.79 A9 9 0 1 1 11.21 3 A7 7 0 0 0 21 12.79'),
)

function onSelect(key: string) {
  theme.setMode(key as ThemeMode)
}
</script>

<template>
  <NDropdown :options="options" trigger="click" placement="bottom-end" @select="onSelect">
    <NButton
      quaternary
      circle
      :aria-label="`主题：${MODE_LABEL[theme.mode]}`"
    >
      <component :is="icon" />
    </NButton>
  </NDropdown>
</template>
