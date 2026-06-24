<script setup lang="ts">
useHead({ title: '统计分析' })
const { $api } = useNuxtApp()

const dim = ref('dept')
const dims = [
  { k: 'dept', label: '部门' },
  { k: 'job_level', label: '职级' },
  { k: 'gender', label: '性别' },
  { k: 'education', label: '学历' },
  { k: 'age', label: '年龄' },
]

const { data: structure } = await useAsyncData(
  'structure',
  () => $api<{ dim: string, buckets: Hr.Bucket[] }>('/v1/analytics/structure', { query: { dim: dim.value } }),
  { watch: [dim] },
)
const { data: tenure } = await useAsyncData('tenure', () => $api<Hr.Bucket[]>('/v1/analytics/tenure'))
const { data: turnover } = await useAsyncData('turnover', () => $api<{ month: string, hires: number, leaves: number, net: number }[]>('/v1/analytics/turnover'))
const { data: headcount } = await useAsyncData('headcount', () => $api<{ planned_total: number, actual_total: number, by_dept: { dept: string, planned: number, actual: number }[] }>('/v1/analytics/headcount'))

const turnoverMax = computed(() => Math.max(1, ...(turnover.value || []).flatMap(m => [m.hires, m.leaves])))
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="统计分析" desc="人员结构、司龄、入离职趋势与编制使用情况。" />

    <div class="gap-4 grid grid-cols-1 lg:grid-cols-2">
      <HCard title="人员结构" desc="按维度统计在职分布">
        <template #actions>
          <select v-model="dim" aria-label="结构统计维度" class="input-base text-xs! px-2! py-1!">
            <option v-for="d in dims" :key="d.k" :value="d.k">
              {{ d.label }}
            </option>
          </select>
        </template>
        <HBars :data="structure?.buckets" />
      </HCard>

      <HCard title="司龄分布" desc="按入职时长分段">
        <HBars :data="tenure || []" />
      </HCard>
    </div>

    <HCard title="入离职趋势" desc="近 12 个月入职与离职人数对比">
      <div class="flex gap-1.5 h-44 items-end">
        <div v-for="m in turnover" :key="m.month" class="group flex flex-1 flex-col gap-1.5 items-center justify-end">
          <div class="flex gap-1 h-34 items-end">
            <div
              class="rounded-t bg-primary-400 w-2.5 transition-colors group-hover:bg-primary-500"
              :style="{ height: `${Math.max(2, (m.hires / turnoverMax) * 100)}%` }" :title="`入职 ${m.hires}`"
            />
            <div
              class="rounded-t bg-rose-300 w-2.5 transition-colors group-hover:bg-rose-400"
              :style="{ height: `${Math.max(2, (m.leaves / turnoverMax) * 100)}%` }" :title="`离职 ${m.leaves}`"
            />
          </div>
          <span class="tnum text-10px text-truegray-400">{{ m.month.slice(5) }}</span>
        </div>
      </div>
      <div class="text-xs text-truegray-500 mt-3 flex gap-5">
        <span class="flex gap-1.5 items-center"><span class="rounded-sm bg-primary-400 h-2.5 w-2.5 inline-block" />入职</span>
        <span class="flex gap-1.5 items-center"><span class="rounded-sm bg-rose-300 h-2.5 w-2.5 inline-block" />离职</span>
      </div>
    </HCard>

    <HCard title="编制 vs 实有" :desc="`编制合计 ${headcount?.planned_total ?? '—'} · 实有合计 ${headcount?.actual_total ?? '—'}`">
      <div class="flex flex-col gap-2.5">
        <div v-for="d in headcount?.by_dept" :key="d.dept" class="text-sm flex gap-3 items-center">
          <span class="text-truegray-500 shrink-0 w-24 truncate" :title="d.dept">{{ d.dept }}</span>
          <div class="rounded-full bg-truegray-100 flex-1 h-2 dark:bg-white/8">
            <div
              class="rounded-full h-full transition-all duration-500"
              :class="d.actual > d.planned ? 'bg-amber-400' : 'bg-primary-500/90'"
              :style="{ width: `${Math.min(100, (d.actual / Math.max(1, d.planned)) * 100)}%` }"
            />
          </div>
          <span class="tnum text-truegray-500 text-right shrink-0 w-16">{{ d.actual }} / {{ d.planned }}</span>
        </div>
      </div>
    </HCard>
  </div>
</template>
