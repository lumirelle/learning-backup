<script setup lang="ts">
import { useAuthStore } from '~/composables/store/useAuthStore'

useHead({ title: '工作台' })
const { $api } = useNuxtApp()
const auth = useAuthStore()

const { data: overview } = await useAsyncData('overview', () => $api<Hr.Overview>('/v1/analytics/overview'))
const { data: todos } = await useAsyncData('todos', () => $api<Hr.Process[]>('/v1/approvals/todo'))
const { data: reminders } = await useAsyncData('reminders', () => $api<Hr.Contract[]>('/v1/contracts/reminders?days=30'))
const { data: timeline } = await useAsyncData('recent', () => $api<Hr.Page<Hr.Event>>('/v1/timeline?page=1&page_size=8'))

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6)
    return '夜深了'
  if (h < 12)
    return '早上好'
  if (h < 14)
    return '中午好'
  if (h < 18)
    return '下午好'
  return '晚上好'
})
const today = computed(() =>
  new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }),
)

const cards = computed(() => [
  { label: '在职人数', value: overview.value?.headcount ?? 0, icon: 'i-carbon-user-multiple', tone: 'primary' as const, hint: '当前在册员工' },
  { label: '试用期', value: overview.value?.probation ?? 0, icon: 'i-carbon-user-follow', tone: 'amber' as const, hint: '待转正人员' },
  { label: '本月入职', value: overview.value?.this_month_hires ?? 0, icon: 'i-carbon-user-admin', tone: 'emerald' as const, hint: '新加入的同事' },
  { label: '进行中流程', value: overview.value?.pending_processes ?? 0, icon: 'i-carbon-task', tone: 'violet' as const, hint: '待审批的人事事务' },
])
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader :title="`${greeting}，${auth.user?.name || '同事'}`" :desc="`今天是${today}，这是组织的最新概况。`">
      <template #actions>
        <NuxtLink to="/reports" class="btn-secondary">
          <span class="i-carbon-report" />查看报表
        </NuxtLink>
        <NuxtLink to="/affairs/new" class="btn-primary">
          <span class="i-carbon-add" />发起流程
        </NuxtLink>
      </template>
    </HPageHeader>

    <div class="gap-4 grid grid-cols-2 xl:grid-cols-4">
      <HStat v-for="c in cards" :key="c.label" v-bind="c" />
    </div>

    <div class="gap-4 grid grid-cols-1 lg:grid-cols-2">
      <HCard title="我的待办审批" desc="等待我处理的流程">
        <HEmpty v-if="!todos?.length" icon="i-carbon-checkmark-outline" label="暂无待办" hint="所有审批都已处理完毕" />
        <ul v-else class="flex flex-col">
          <li v-for="p in todos" :key="p.id" class="text-sm py-2.5 row-base flex items-center justify-between">
            <div class="flex gap-2.5 items-center">
              <HBadge tone="blue" :label="processTypeMap[p.type] || p.type" />
              <span class="text-xs text-truegray-500 font-mono">{{ p.process_no }}</span>
            </div>
            <NuxtLink :to="`/affairs/${p.id}`" class="link-action">
              去审批
            </NuxtLink>
          </li>
        </ul>
      </HCard>

      <HCard title="合同到期提醒" desc="30 天内到期的合同">
        <HEmpty v-if="!reminders?.length" icon="i-carbon-document-signed" label="暂无即将到期合同" />
        <ul v-else class="flex flex-col">
          <li v-for="c in reminders" :key="c.id" class="text-sm py-2.5 row-base flex items-center justify-between">
            <span class="font-medium">{{ c.employee_name || c.contract_no }}</span>
            <div class="text-truegray-400 flex gap-2.5 items-center">
              <span class="tnum text-xs">{{ fmtDate(c.end_date) }}</span>
              <HBadge dot :tone="(c.days_left ?? 99) <= 15 ? 'red' : 'amber'" :label="`剩 ${c.days_left} 天`" />
            </div>
          </li>
        </ul>
      </HCard>
    </div>

    <HCard title="最近人事动态" desc="组织内最新的人事变化">
      <template #actions>
        <NuxtLink to="/timeline" class="link-action text-xs">
          查看全部
        </NuxtLink>
      </template>
      <ol v-if="timeline?.list?.length" class="flex flex-col">
        <li v-for="e in timeline.list" :key="e.id" class="text-sm py-2.5 row-base flex items-center justify-between">
          <div class="flex gap-2.5 min-w-0 items-center">
            <HBadge tone="gray" :label="eventTypeMap[e.event_type] || e.event_type" />
            <span class="truncate">{{ e.title }}</span>
          </div>
          <span class="tnum text-xs text-truegray-400 shrink-0">{{ fmtDateTime(e.occurred_at) }}</span>
        </li>
      </ol>
      <HEmpty v-else icon="i-carbon-time" label="暂无动态" />
    </HCard>
  </div>
</template>
