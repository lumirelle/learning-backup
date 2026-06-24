<script lang="ts" setup>
defineProps<{ title: string, desc?: string, back?: string, backLabel?: string }>()
</script>

<template>
  <div class="flex flex-col gap-1">
    <NuxtLink
      v-if="back" :to="back"
      class="text-xs text-truegray-400 mb-1 inline-flex gap-1 transition-colors items-center self-start hover:text-primary"
    >
      <span class="i-carbon-arrow-left" />
      {{ backLabel || '返回' }}
    </NuxtLink>
    <div class="flex flex-wrap gap-x-4 gap-y-2 items-end justify-between">
      <div class="min-w-0">
        <h1 class="text-xl text-truegray-900 tracking-tight font-bold flex gap-2.5 items-center dark:text-white">
          {{ title }}
          <slot name="title-extra" />
        </h1>
        <p v-if="desc" class="text-13px text-truegray-400 mt-1">
          {{ desc }}
        </p>
      </div>
      <div class="flex gap-2 items-center">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>
