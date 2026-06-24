<script lang="ts" setup>
const props = defineProps<{ data?: Hr.Bucket[] }>()
const max = computed(() => Math.max(1, ...(props.data || []).map(d => d.count)))
const total = computed(() => (props.data || []).reduce((s, d) => s + d.count, 0))
</script>

<template>
  <div class="flex flex-col gap-2.5">
    <div v-for="b in data" :key="b.label" class="text-sm flex gap-3 items-center">
      <span class="text-truegray-500 shrink-0 w-24 truncate" :title="b.label">{{ b.label }}</span>
      <div class="rounded-full bg-truegray-100 flex-1 h-2 dark:bg-white/8">
        <div
          class="rounded-full bg-primary-500/90 h-full transition-all duration-500"
          :style="{ width: `${Math.max(2, (b.count / max) * 100)}%` }"
        />
      </div>
      <span class="tnum font-medium text-right shrink-0 w-9">{{ b.count }}</span>
      <span class="tnum text-xs text-truegray-300 text-right shrink-0 w-10 dark:text-truegray-500">
        {{ total ? `${Math.round((b.count / total) * 100)}%` : '—' }}
      </span>
    </div>
    <HEmpty v-if="!data?.length" icon="i-carbon-chart-bar" label="无数据" />
  </div>
</template>
