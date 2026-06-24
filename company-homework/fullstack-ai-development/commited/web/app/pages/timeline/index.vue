<script setup lang="ts">
useHead({ title: '人事动态' })
const { $api } = useNuxtApp()

const tab = ref<'events' | 'audit'>('events')
const pageSize = 15

// 列表用普通 ref + 显式 load（不走 useAsyncData：其 refresh 在本版本会命中缓存而不重新执行 handler）
const evPage = ref(1)
const events = ref<Hr.Page<Hr.Event> | null>(null)
async function loadEvents() {
  events.value = await $api<Hr.Page<Hr.Event>>('/v1/timeline', { query: { page: evPage.value, page_size: pageSize } })
}
const evTotalPages = computed(() => Math.max(1, Math.ceil((events.value?.total ?? 0) / pageSize)))
function goEvPage(delta: number) {
  evPage.value += delta
  loadEvents()
}

const auPage = ref(1)
const audit = ref<Hr.Page<Hr.AuditLog> | null>(null)
async function loadAudit() {
  audit.value = await $api<Hr.Page<Hr.AuditLog>>('/v1/audit-logs', { query: { page: auPage.value, page_size: pageSize } })
}
const auTotalPages = computed(() => Math.max(1, Math.ceil((audit.value?.total ?? 0) / pageSize)))
function goAuPage(delta: number) {
  auPage.value += delta
  loadAudit()
}

await Promise.all([loadEvents(), loadAudit()])
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="人事动态" desc="组织内人事变动时间线与系统操作日志。" />

    <div class="seg-group">
      <button :class="tab === 'events' ? 'seg-item-active' : 'seg-item'" @click="tab = 'events'">
        变动时间线
      </button>
      <button :class="tab === 'audit' ? 'seg-item-active' : 'seg-item'" @click="tab = 'audit'">
        操作日志
      </button>
    </div>

    <HCard v-if="tab === 'events'">
      <ol v-if="events?.list?.length" class="flex flex-col">
        <li v-for="(e, i) in events.list" :key="e.id" class="flex gap-3.5">
          <div class="flex shrink-0 flex-col w-2.5 items-center">
            <span class="mt-2 rounded-full bg-primary-400 shrink-0 h-2 w-2 ring-3 ring-primary/12" />
            <span v-if="i < events.list.length - 1" class="my-1 bg-black/8 flex-1 w-px dark:bg-white/10" />
          </div>
          <div class="text-sm pb-4 flex flex-1 gap-3 min-w-0 items-center justify-between">
            <div class="flex gap-2.5 min-w-0 items-center">
              <HBadge tone="gray" :label="eventTypeMap[e.event_type] || e.event_type" />
              <span class="truncate">{{ e.title }}</span>
            </div>
            <span class="tnum text-xs text-truegray-400 shrink-0">{{ fmtDateTime(e.occurred_at) }}</span>
          </div>
        </li>
      </ol>
      <HEmpty v-else icon="i-carbon-time" label="暂无动态" />
      <HPager :page="evPage" :total-pages="evTotalPages" @go="goEvPage($event)" />
    </HCard>

    <HCard v-else>
      <table class="text-sm w-full">
        <thead>
          <tr class="border-b border-black/6 dark:border-white/8">
            <th class="th-base">
              用户
            </th>
            <th class="th-base">
              方法
            </th>
            <th class="th-base">
              路径
            </th>
            <th class="th-base">
              状态
            </th>
            <th class="th-base">
              时间
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in audit?.list" :key="a.id" class="row-base">
            <td class="font-medium td-base">
              {{ a.username || '—' }}
            </td>
            <td class="text-xs font-mono td-base">
              {{ a.method }}
            </td>
            <td class="text-xs text-truegray-500 font-mono td-base">
              {{ a.path }}
            </td>
            <td class="td-base">
              <HBadge :tone="a.status < 400 ? 'green' : 'red'" :label="String(a.status)" />
            </td>
            <td class="tnum text-xs text-truegray-400 td-base">
              {{ fmtDateTime(a.created_at) }}
            </td>
          </tr>
        </tbody>
      </table>
      <HEmpty v-if="!audit?.list?.length" icon="i-carbon-catalog" label="暂无日志" />
      <HPager :page="auPage" :total-pages="auTotalPages" @go="goAuPage($event)" />
    </HCard>
  </div>
</template>
