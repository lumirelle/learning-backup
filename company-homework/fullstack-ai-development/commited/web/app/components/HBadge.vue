<script lang="ts" setup>
const props = defineProps<{ tone?: string, label?: string, dot?: boolean }>()
const cls = computed(() => toneClass(props.tone || 'gray'))
</script>

<template>
  <span class="text-xs font-medium px-2 py-0.5 rounded-full inline-flex gap-1.5 ring-1 ring-inset items-center" :class="cls">
    <span v-if="dot" class="rounded-full bg-current opacity-65 h-1.5 w-1.5" />
    <slot>{{ label }}</slot>
  </span>
</template>
