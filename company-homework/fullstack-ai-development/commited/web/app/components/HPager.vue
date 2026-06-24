<script lang="ts" setup>
defineProps<{ page: number, totalPages: number }>()
defineEmits<{ go: [delta: number] }>()
</script>

<template>
  <div class="mt-4 flex gap-3 items-center justify-end">
    <button class="btn-secondary px-2.5! py-1.5!" :disabled="page <= 1" @click="$emit('go', -1)">
      <span class="i-carbon-chevron-left" />上一页
    </button>
    <span class="tnum text-sm text-truegray-500">{{ page }} / {{ totalPages }}</span>
    <button class="btn-secondary px-2.5! py-1.5!" :disabled="page >= totalPages" @click="$emit('go', 1)">
      下一页<span class="i-carbon-chevron-right" />
    </button>
  </div>
</template>
