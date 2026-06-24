<script setup lang="ts">
useHead({ title: '员工关怀' })
const { $api } = useNuxtApp()

interface Item { employee_id: string, name: string, dept_name: string, date: string, years?: number }
interface Upcoming { month: number, birthdays: Item[], anniversaries: Item[] }

const month = ref(new Date().getMonth() + 1)
const months = Array.from({ length: 12 }, (_, i) => i + 1)

const { data } = await useAsyncData(
  'care',
  () => $api<Upcoming>('/v1/care/upcoming', { query: { month: month.value } }),
  { watch: [month] },
)
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="员工关怀" desc="按月查看生日与入职周年名单，便于提前安排关怀。">
      <template #actions>
        <select v-model.number="month" aria-label="选择月份" class="input-base">
          <option v-for="m in months" :key="m" :value="m">
            {{ m }} 月
          </option>
        </select>
      </template>
    </HPageHeader>

    <div class="gap-4 grid grid-cols-1 lg:grid-cols-2">
      <HCard title="本月生日" desc="按花名册出生日期统计">
        <template #actions>
          <span class="i-carbon-gift text-amber-400" />
        </template>
        <ul v-if="data?.birthdays?.length" class="flex flex-col">
          <li v-for="b in data.birthdays" :key="b.employee_id" class="text-sm py-2.5 row-base flex items-center justify-between">
            <div class="flex gap-2.5 items-center">
              <NuxtLink :to="`/roster/${b.employee_id}`" class="font-medium hover:text-primary">
                {{ b.name }}
              </NuxtLink>
              <span class="text-xs text-truegray-400">{{ b.dept_name }}</span>
            </div>
            <HBadge tone="amber" :label="b.date" />
          </li>
        </ul>
        <HEmpty v-else icon="i-carbon-gift" label="本月暂无生日" />
      </HCard>

      <HCard title="入职周年" desc="按入职日期计算周年数">
        <template #actions>
          <span class="i-carbon-celebrate text-emerald-400" />
        </template>
        <ul v-if="data?.anniversaries?.length" class="flex flex-col">
          <li v-for="a in data.anniversaries" :key="a.employee_id" class="text-sm py-2.5 row-base flex items-center justify-between">
            <div class="flex gap-2.5 items-center">
              <NuxtLink :to="`/roster/${a.employee_id}`" class="font-medium hover:text-primary">
                {{ a.name }}
              </NuxtLink>
              <span class="text-xs text-truegray-400">{{ a.dept_name }}</span>
            </div>
            <div class="flex gap-2.5 items-center">
              <span class="tnum text-xs text-truegray-400">{{ a.date }}</span>
              <HBadge tone="green" :label="`${a.years} 周年`" />
            </div>
          </li>
        </ul>
        <HEmpty v-else icon="i-carbon-celebrate" label="本月暂无入职周年" />
      </HCard>
    </div>
  </div>
</template>
