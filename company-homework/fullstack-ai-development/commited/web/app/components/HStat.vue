<script lang="ts" setup>
const props = defineProps<{
  label: string
  value: number | string
  icon: string
  /** 图标容器色调 */
  tone?: 'primary' | 'emerald' | 'amber' | 'violet'
  hint?: string
}>()

const toneCls = computed(() => ({
  primary: 'bg-primary-50 text-primary dark:bg-primary-500/12 dark:text-primary-300',
  emerald: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/12 dark:text-emerald-300',
  amber: 'bg-amber-50 text-amber-600 dark:bg-amber-500/12 dark:text-amber-300',
  violet: 'bg-violet-50 text-violet-600 dark:bg-violet-500/12 dark:text-violet-300',
}[props.tone || 'primary']))
</script>

<template>
  <div class="p-5 app-card flex gap-3 items-start justify-between">
    <div class="min-w-0">
      <div class="text-xs text-truegray-400 tracking-wide">
        {{ label }}
      </div>
      <div class="tnum text-3xl text-truegray-900 leading-none font-serif mt-1.5 dark:text-white">
        {{ value }}
      </div>
      <div v-if="hint" class="text-xs text-truegray-300 mt-2 dark:text-truegray-500">
        {{ hint }}
      </div>
    </div>
    <div class="rounded-lg flex shrink-0 h-9 w-9 items-center justify-center" :class="toneCls">
      <span class="text-lg" :class="icon" />
    </div>
  </div>
</template>
