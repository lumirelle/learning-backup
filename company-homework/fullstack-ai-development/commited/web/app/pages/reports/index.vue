<script setup lang="ts">
useHead({ title: '人事报表' })
const { $api } = useNuxtApp()

interface Tpl { key: string, name: string, desc: string }
interface Table { title: string, columns: string[], rows: string[][] }

const { data: tpls } = await useAsyncData('rpt-tpls', () => $api<Tpl[]>('/v1/reports'))
const active = ref('roster')
const { data: table, pending, error } = await useAsyncData(
  'rpt-data',
  () => $api<Table>(`/v1/reports/${active.value}`),
  { watch: [active] },
)

function exportExcel() {
  return downloadAuthed(
    `/api/v1/reports/${active.value}?format=excel`,
    dateStampedName(active.value),
  )
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="人事报表" desc="选择内置模板在线预览，并可导出 Excel。" />

    <div class="flex flex-wrap gap-2.5">
      <button
        v-for="t in tpls" :key="t.key"
        class="px-4 py-3 text-left border rounded-xl cursor-pointer transition-colors relative"
        :class="active === t.key
          ? 'border-primary/50 bg-primary-50/60 dark:border-primary-400/40 dark:bg-primary-500/10'
          : 'border-black/8 bg-white hover:border-primary/30 dark:border-white/10 dark:bg-ink-900'"
        @click="active = t.key"
      >
        <span v-if="active === t.key" class="i-carbon-checkmark-filled text-sm text-primary right-2.5 top-2.5 absolute" />
        <div class="text-sm font-medium" :class="active === t.key ? 'text-primary dark:text-primary-300' : ''">
          {{ t.name }}
        </div>
        <div class="text-xs text-truegray-400 mt-0.5">
          {{ t.desc }}
        </div>
      </button>
    </div>

    <HCard :title="table?.title" :desc="`${table?.rows?.length ?? 0} 行数据`">
      <template #actions>
        <button class="btn-primary" @click="exportExcel">
          <span class="i-carbon-download" />导出 Excel
        </button>
      </template>

      <div class="overflow-auto">
        <table class="text-sm w-full">
          <thead>
            <tr class="border-b border-black/6 dark:border-white/8">
              <th v-for="col in table?.columns" :key="col" class="th-base">
                {{ col }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, i) in table?.rows" :key="i" class="row-base">
              <td v-for="(cell, j) in row" :key="j" class="tnum td-base">
                {{ cell }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="pending" class="text-sm text-truegray-400 py-6 text-center">
        加载中…
      </div>
      <HEmpty v-else-if="error" icon="i-carbon-cloud-offline" label="加载失败，请稍后重试" />
      <HEmpty v-else-if="!table?.rows?.length" icon="i-carbon-report" label="无数据" />
    </HCard>
  </div>
</template>
