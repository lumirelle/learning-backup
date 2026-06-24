<script setup lang="ts">
const route = useRoute()
const id = route.params.id as string
const { $api } = useNuxtApp()

const { data: emp } = await useAsyncData(`emp-${id}`, () => $api<Hr.Employee>(`/v1/employees/${id}`))
const { data: events } = await useAsyncData(`emp-tl-${id}`, () => $api<Hr.Event[]>(`/v1/employees/${id}/timeline`))
useHead({ title: () => emp.value?.name || '员工档案' })

const fields = computed(() => emp.value
  ? [
      ['工号', emp.value.employee_no],
      ['性别', genderMap[emp.value.gender] || '—'],
      ['手机', emp.value.phone || '—'],
      ['工作邮箱', emp.value.work_email || '—'],
      ['学历', educationMap[emp.value.education || ''] || '—'],
      ['部门', emp.value.dept_name || '—'],
      ['职级', emp.value.job_level || '—'],
      ['入职日期', fmtDate(emp.value.hired_at)],
      ['转正日期', fmtDate(emp.value.regular_at)],
      ['在职状态', employmentStatusMap[emp.value.employment_status]?.label || emp.value.employment_status],
    ]
  : [])
</script>

<template>
  <div v-if="emp" class="flex flex-col gap-5">
    <HPageHeader :title="emp.name" back="/roster" back-label="返回花名册" :desc="`${emp.dept_name || '—'} · ${emp.job_level || '—'} · ${emp.org_path}`">
      <template #title-extra>
        <HBadge
          dot
          :tone="employmentStatusMap[emp.employment_status]?.tone"
          :label="employmentStatusMap[emp.employment_status]?.label || emp.employment_status"
        />
      </template>
      <template #actions>
        <NuxtLink :to="`/affairs/new?employee=${emp.id}`" class="btn-primary">
          <span class="i-carbon-flow" />发起人事流程
        </NuxtLink>
      </template>
    </HPageHeader>

    <div class="gap-4 grid grid-cols-1 lg:grid-cols-2">
      <HCard title="基本信息">
        <dl class="text-sm gap-x-8 gap-y-0 grid grid-cols-2">
          <div v-for="[k, v] in fields" :key="k" class="py-2.5 border-b border-black/4 flex gap-3 items-center justify-between dark:border-white/6">
            <dt class="text-truegray-400 shrink-0">
              {{ k }}
            </dt>
            <dd class="font-medium text-right truncate" :title="v || ''">
              {{ v }}
            </dd>
          </div>
        </dl>
      </HCard>

      <HCard title="动态时间线" desc="该员工的全部人事事件">
        <ol v-if="events?.length" class="flex flex-col">
          <li v-for="(e, i) in events" :key="e.id" class="flex gap-3.5">
            <div class="flex shrink-0 flex-col w-2.5 items-center">
              <span class="mt-2 rounded-full bg-primary-400 shrink-0 h-2 w-2 ring-3 ring-primary/12" />
              <span v-if="i < events.length - 1" class="my-1 bg-black/8 flex-1 w-px dark:bg-white/10" />
            </div>
            <div class="pb-4 flex-1 min-w-0">
              <div class="text-sm flex gap-2 items-center">
                <HBadge tone="gray" :label="eventTypeMap[e.event_type] || e.event_type" />
                <span class="truncate">{{ e.title }}</span>
              </div>
              <div class="tnum text-xs text-truegray-400 mt-1">
                {{ fmtDateTime(e.occurred_at) }}
              </div>
            </div>
          </li>
        </ol>
        <HEmpty v-else icon="i-carbon-time" label="暂无动态" />
      </HCard>
    </div>
  </div>
</template>
