<script setup lang="ts">
useHead({ title: '任职奖惩' })
const { $api } = useNuxtApp()

interface RewPun {
  id: string
  employee_id: string
  employee_name?: string
  kind: string
  category: string
  title: string
  reason?: string
  amount?: number
  effective_date?: string
  created_at: string
}

const kind = ref('')
// 列表用普通 ref + 显式 load（不走 useAsyncData：其 refresh 在本版本会命中缓存而不重新执行 handler）
const data = ref<RewPun[] | null>(null)
async function load() {
  data.value = await $api<RewPun[]>('/v1/rewards-punishments', { query: kind.value ? { kind: kind.value } : {} })
}
await load()
watch(kind, load)

const showForm = ref(false)
const err = ref('')
const form = reactive({ employee_id: '', kind: 'reward', category: '', title: '', reason: '', amount: '', effective_date: '' })

const kindMap: Record<string, { label: string, tone: string }> = {
  reward: { label: '奖励', tone: 'green' },
  punishment: { label: '惩罚', tone: 'red' },
}

async function submit() {
  err.value = ''
  if (!form.employee_id || !form.title) {
    err.value = '请选择员工并填写标题'
    return
  }
  try {
    await $api('/v1/rewards-punishments', {
      method: 'POST',
      body: { ...form, amount: form.amount ? Number(form.amount) : undefined },
    })
    showForm.value = false
    form.title = ''
    form.reason = ''
    form.category = ''
    form.amount = ''
    form.effective_date = ''
    await load()
  }
  catch (e: any) {
    err.value = e?.data?.message || e?.message || '提交失败'
  }
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="任职奖惩" desc="奖惩记录台账，录入后自动写入员工时间线。">
      <template #actions>
        <button :class="showForm ? 'btn-secondary' : 'btn-primary'" @click="showForm = !showForm">
          <span :class="showForm ? 'i-carbon-chevron-up' : 'i-carbon-add'" />{{ showForm ? '收起' : '记一笔' }}
        </button>
      </template>
    </HPageHeader>

    <HCard v-if="showForm" title="录入奖惩">
      <div class="gap-4 grid grid-cols-1 md:grid-cols-2">
        <div class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">员工</span>
          <HPersonPicker v-model="form.employee_id" status="active" placeholder="请选择员工" />
        </div>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">类型</span>
          <select v-model="form.kind" class="input-base">
            <option value="reward">
              奖励
            </option>
            <option value="punishment">
              惩罚
            </option>
          </select>
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">类别</span>
          <input v-model="form.category" placeholder="嘉奖 / 记功 / 警告 …" class="input-base">
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">标题</span>
          <input v-model="form.title" placeholder="如 Q2 优秀员工" class="input-base">
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">金额（奖金 / 罚金，可选）</span>
          <input v-model="form.amount" type="number" min="0" step="100" placeholder="如 2000" class="input-base">
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">生效日期</span>
          <input v-model="form.effective_date" type="date" class="input-base">
        </label>
        <label class="text-sm flex flex-col gap-1.5 md:col-span-2">
          <span class="text-13px text-truegray-500 font-medium">事由</span>
          <input v-model="form.reason" placeholder="具体事由" class="input-base">
        </label>
      </div>
      <p v-if="err" class="text-sm text-rose-500 mt-3 flex gap-1.5 items-center">
        <span class="i-carbon-warning-alt" />{{ err }}
      </p>
      <div class="mt-4 flex justify-end">
        <button class="btn-primary" @click="submit">
          提交
        </button>
      </div>
    </HCard>

    <HCard>
      <div class="mb-4 flex gap-3 items-center">
        <select v-model="kind" aria-label="奖惩类型筛选" class="input-base">
          <option value="">
            全部
          </option>
          <option value="reward">
            奖励
          </option>
          <option value="punishment">
            惩罚
          </option>
        </select>
        <span class="tnum text-sm text-truegray-400 ml-auto">共 {{ data?.length ?? 0 }} 条</span>
      </div>
      <table class="text-sm w-full">
        <thead>
          <tr class="border-b border-black/6 dark:border-white/8">
            <th class="th-base">
              员工
            </th>
            <th class="th-base">
              类型
            </th>
            <th class="th-base">
              类别
            </th>
            <th class="th-base">
              标题
            </th>
            <th class="th-base">
              事由
            </th>
            <th class="th-base">
              金额
            </th>
            <th class="th-base">
              生效日期
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in data" :key="r.id" class="row-base">
            <td class="td-base">
              <NuxtLink :to="`/roster/${r.employee_id}`" class="font-medium hover:text-primary">
                {{ r.employee_name || '—' }}
              </NuxtLink>
            </td>
            <td class="td-base">
              <HBadge dot :tone="kindMap[r.kind]?.tone" :label="kindMap[r.kind]?.label || r.kind" />
            </td>
            <td class="text-truegray-500 td-base">
              {{ r.category || '—' }}
            </td>
            <td class="td-base">
              {{ r.title }}
            </td>
            <td class="text-truegray-500 td-base">
              {{ r.reason || '—' }}
            </td>
            <td class="tnum td-base" :class="r.kind === 'punishment' ? 'text-rose-600' : 'text-emerald-600'">
              {{ r.amount != null ? `¥ ${r.amount.toLocaleString()}` : '—' }}
            </td>
            <td class="tnum text-truegray-500 td-base">
              {{ fmtDate(r.effective_date) }}
            </td>
          </tr>
        </tbody>
      </table>
      <HEmpty v-if="!data?.length" icon="i-carbon-trophy" label="暂无记录" hint="点击右上角「记一笔」录入第一条奖惩" />
    </HCard>
  </div>
</template>
