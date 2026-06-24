<script setup lang="ts">
useHead({ title: '人事事务' })
const { $api } = useNuxtApp()

const tab = ref<'all' | 'todo' | 'mine'>('all')
const tabs = [
  { key: 'all', label: '全部流程' },
  { key: 'todo', label: '我的待办' },
  { key: 'mine', label: '我发起的' },
] as const

const page = ref(1)
const pageSize = 15
const total = ref(0)

// 列表用普通 ref + 显式 load（不走 useAsyncData：其 refresh 在本版本会命中缓存而不重新执行 handler）
const data = ref<Hr.Process[] | null>(null)
const pending = ref(false)
async function load() {
  pending.value = true
  try {
    if (tab.value === 'todo') {
      data.value = await $api<Hr.Process[]>('/v1/approvals/todo')
    }
    else if (tab.value === 'mine') {
      data.value = await $api<Hr.Process[]>('/v1/approvals/mine')
    }
    else {
      const p = await $api<Hr.Page<Hr.Process>>('/v1/processes', { query: { page: page.value, page_size: pageSize } })
      total.value = p.total
      data.value = p.list
    }
  }
  finally {
    pending.value = false
  }
}
await load()

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))
function switchTab(t: typeof tab.value) {
  tab.value = t
  page.value = 1
  load()
}
function goPage(delta: number) {
  page.value += delta
  load()
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="人事事务" desc="入职、转正、调动、离职的流程发起与审批。">
      <template #actions>
        <NuxtLink to="/affairs/new" class="btn-primary">
          <span class="i-carbon-add" />发起流程
        </NuxtLink>
      </template>
    </HPageHeader>

    <div class="seg-group">
      <button
        v-for="t in tabs" :key="t.key"
        :class="tab === t.key ? 'seg-item-active' : 'seg-item'"
        @click="switchTab(t.key)"
      >
        {{ t.label }}
      </button>
    </div>

    <HCard>
      <table class="text-sm w-full">
        <thead>
          <tr class="border-b border-black/6 dark:border-white/8">
            <th class="th-base">
              单号
            </th>
            <th class="th-base">
              类型
            </th>
            <th class="th-base">
              状态
            </th>
            <th class="th-base">
              审批进度
            </th>
            <th class="th-base">
              发起时间
            </th>
            <th class="th-base" />
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in data" :key="p.id" class="row-base">
            <td class="text-xs text-truegray-500 font-mono td-base">
              {{ p.process_no }}
            </td>
            <td class="td-base">
              <HBadge tone="blue" :label="processTypeMap[p.type] || p.type" />
            </td>
            <td class="td-base">
              <HBadge dot :tone="processStatusMap[p.status]?.tone" :label="processStatusMap[p.status]?.label || p.status" />
            </td>
            <td class="tnum text-truegray-500 td-base">
              {{ p.approval ? `${p.approval.current_step}/${p.approval.total_steps}` : '—' }}
            </td>
            <td class="tnum text-truegray-500 td-base">
              {{ fmtDateTime(p.created_at) }}
            </td>
            <td class="td-base text-right">
              <NuxtLink :to="`/affairs/${p.id}`" class="link-action">
                详情
              </NuxtLink>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="pending" class="text-sm text-truegray-400 py-6 text-center">
        加载中…
      </div>
      <HEmpty v-else-if="!data?.length" icon="i-carbon-flow" label="暂无流程" hint="点击右上角「发起流程」开始一项人事事务" />
      <HPager v-if="tab === 'all'" :page="page" :total-pages="totalPages" @go="goPage($event)" />
    </HCard>
  </div>
</template>
