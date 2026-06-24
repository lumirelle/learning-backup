<script lang="ts" setup>
const route = useRoute()
function active(to: string) {
  return to === '/' ? route.path === '/' : route.path.startsWith(to)
}
</script>

<template>
  <aside class="bg-ink flex shrink-0 flex-col w-57 overflow-y-auto">
    <!-- 品牌区 -->
    <div class="px-5 pb-3 pt-5 flex gap-2.5 items-center">
      <div class="rounded-lg bg-primary flex h-8 w-8 shadow-lg shadow-primary/30 items-center justify-center">
        <span class="i-carbon-enterprise text-lg text-white" />
      </div>
      <div class="leading-tight">
        <div class="text-15px text-white tracking-wide font-serif">
          OrgHR
        </div>
        <div class="text-11px text-ink-400">
          组织人事管理系统
        </div>
      </div>
    </div>

    <nav class="px-3 pb-4 flex flex-1 flex-col">
      <template v-for="(group, gi) in navGroups" :key="gi">
        <div v-if="group.label" class="text-11px text-ink-500 tracking-widest px-3 pb-1.5 pt-5">
          {{ group.label }}
        </div>
        <div v-else class="h-2" />
        <NuxtLink
          v-for="item in group.items"
          :key="item.to"
          :to="item.to"
          class="text-sm mb-0.5 px-3 py-2 rounded-lg flex gap-3 transition-colors duration-150 items-center relative"
          :class="active(item.to)
            ? 'bg-white/10 text-white font-medium'
            : 'text-ink-300 hover:bg-white/5 hover:text-white'"
        >
          <span
            v-if="active(item.to)"
            class="rounded-full bg-primary-400 h-4 w-0.75 left-0 top-1/2 absolute -translate-y-1/2"
          />
          <span class="text-base shrink-0" :class="[item.icon, active(item.to) ? 'text-primary-300' : 'text-ink-400']" />
          <span>{{ item.label }}</span>
        </NuxtLink>
      </template>
    </nav>

    <div class="text-11px text-ink-500 mt-auto px-5 py-3.5 border-t border-white/6 flex gap-1.5 items-center">
      <span class="rounded-full bg-emerald-400 h-1.5 w-1.5" />
      v1.0 · 全功能域已交付
    </div>
  </aside>
</template>
