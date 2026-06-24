<script lang="ts" setup>
defineProps<{ title?: string, desc?: string }>()
</script>

<template>
  <section class="p-5 app-card">
    <header v-if="title || desc || $slots.actions" class="mb-4 flex gap-3 items-start justify-between">
      <div class="min-w-0">
        <h3 class="text-sm text-truegray-800 tracking-tight font-semibold dark:text-truegray-100">
          {{ title }}
        </h3>
        <p v-if="desc" class="text-xs text-truegray-400 mt-0.5">
          {{ desc }}
        </p>
      </div>
      <div class="flex shrink-0 gap-2 items-center">
        <slot name="actions" />
      </div>
    </header>
    <slot />
  </section>
</template>
