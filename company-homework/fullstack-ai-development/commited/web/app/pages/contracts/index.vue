<script setup lang="ts">
useHead({ title: '合同管理' })
const { $api } = useNuxtApp()

const status = ref('')
const page = ref(1)
const pageSize = 15

// 列表用普通 ref + 显式 load（不走 useAsyncData：其 refresh 在本版本会命中缓存而不重新执行 handler）
const data = ref<Hr.Page<Hr.Contract> | null>(null)
const pending = ref(false)
async function load() {
  pending.value = true
  try {
    data.value = await $api<Hr.Page<Hr.Contract>>('/v1/contracts', {
      query: { page: page.value, page_size: pageSize, ...(status.value ? { status: status.value } : {}) },
    })
  }
  finally {
    pending.value = false
  }
}
await load()

const totalPages = computed(() => Math.max(1, Math.ceil((data.value?.total ?? 0) / pageSize)))
function applyFilter() {
  page.value = 1
  load()
}
function goPage(delta: number) {
  page.value += delta
  load()
}

// ---- 新建合同 ----
interface Template { id: string, name: string, type: string }
const { data: templates } = await useAsyncData('contract-tpls', () => $api<Template[]>('/v1/contract-templates'))

const showForm = ref(false)
const formErr = ref('')
const form = reactive({
  employee_id: '',
  template_id: '',
  type: 'fixed_term',
  sign_date: '',
  start_date: '',
  end_date: '',
  salary_band: '',
})

async function create() {
  formErr.value = ''
  if (!form.employee_id || !form.start_date || !form.end_date) {
    formErr.value = '请选择员工并填写起止日期'
    return
  }
  try {
    await $api('/v1/contracts', { method: 'POST', body: { ...form } })
    showForm.value = false
    form.employee_id = ''
    form.sign_date = ''
    form.start_date = ''
    form.end_date = ''
    form.salary_band = ''
    await load()
  }
  catch (e: any) {
    formErr.value = e?.data?.message || e?.message || '创建失败'
  }
}

// ---- 续签 / 终止 ----
const renewFor = ref<Hr.Contract | null>(null)
const renewEnd = ref('')
const renewBand = ref('')
const actErr = ref('')

async function renew() {
  actErr.value = ''
  if (!renewEnd.value) {
    actErr.value = '请填写新的到期日'
    return
  }
  try {
    await $api(`/v1/contracts/${renewFor.value!.id}/renew`, {
      method: 'POST',
      body: { end_date: renewEnd.value, salary_band: renewBand.value },
    })
    renewFor.value = null
    renewEnd.value = ''
    renewBand.value = ''
    await load()
  }
  catch (e: any) {
    actErr.value = e?.data?.message || e?.message || '续签失败'
  }
}

async function terminate(c: Hr.Contract) {
  // oxlint-disable-next-line no-alert
  if (!window.confirm(`确认终止合同 ${c.contract_no}（${c.employee_name || ''}）？`))
    return
  try {
    await $api(`/v1/contracts/${c.id}`, { method: 'DELETE' })
    await load()
  }
  catch (e: any) {
    actErr.value = e?.data?.message || e?.message || '终止失败'
  }
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="合同管理" desc="劳动合同台账、到期预警与续签 / 终止操作。">
      <template #actions>
        <button :class="showForm ? 'btn-secondary' : 'btn-primary'" @click="showForm = !showForm">
          <span :class="showForm ? 'i-carbon-chevron-up' : 'i-carbon-add'" />{{ showForm ? '收起' : '新建合同' }}
        </button>
      </template>
    </HPageHeader>

    <HCard v-if="showForm" title="新建合同" desc="为在职员工签订新合同">
      <div class="gap-4 grid grid-cols-1 md:grid-cols-3">
        <div class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">员工 *</span>
          <HPersonPicker v-model="form.employee_id" status="active" placeholder="请选择员工" />
        </div>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">合同模板</span>
          <select v-model="form.template_id" class="input-base">
            <option value="">
              （不使用模板）
            </option>
            <option v-for="t in templates" :key="t.id" :value="t.id">
              {{ t.name }}
            </option>
          </select>
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">类型</span>
          <select v-model="form.type" class="input-base">
            <option v-for="(label, k) in contractTypeMap" :key="k" :value="k">
              {{ label }}
            </option>
          </select>
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">签订日期</span>
          <input v-model="form.sign_date" type="date" class="input-base">
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">开始日期 *</span>
          <input v-model="form.start_date" type="date" class="input-base">
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">到期日期 *</span>
          <input v-model="form.end_date" type="date" class="input-base">
        </label>
        <label class="text-sm flex flex-col gap-1.5 md:col-span-3">
          <span class="text-13px text-truegray-500 font-medium">薪酬档位</span>
          <input v-model="form.salary_band" placeholder="如 B2 / 12-15k" class="input-base">
        </label>
      </div>
      <p v-if="formErr" class="text-sm text-rose-500 mt-3 flex gap-1.5 items-center">
        <span class="i-carbon-warning-alt" />{{ formErr }}
      </p>
      <div class="mt-4 flex justify-end">
        <button class="btn-primary" @click="create">
          创建合同
        </button>
      </div>
    </HCard>

    <!-- 续签表单 -->
    <HCard v-if="renewFor" :title="`续签：${renewFor.contract_no}`" :desc="`${renewFor.employee_name || ''} · 原到期 ${fmtDate(renewFor.end_date)}，旧合同转为「已续签」，新合同自动衔接`">
      <div class="flex flex-wrap gap-3 items-end">
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">新的到期日 *</span>
          <input v-model="renewEnd" type="date" class="input-base">
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">薪酬档位</span>
          <input v-model="renewBand" :placeholder="renewFor.salary_band || '保持不变'" class="input-base">
        </label>
        <button class="btn-primary" @click="renew">
          确认续签
        </button>
        <button class="btn-secondary" @click="renewFor = null">
          取消
        </button>
      </div>
    </HCard>

    <HCard>
      <div class="mb-4 flex gap-3 items-center">
        <select v-model="status" aria-label="合同状态筛选" class="input-base" @change="applyFilter">
          <option value="">
            全部状态
          </option>
          <option value="active">
            在用
          </option>
          <option value="renewed">
            已续签
          </option>
          <option value="terminated">
            已终止
          </option>
        </select>
        <p v-if="actErr" class="text-sm text-rose-500 flex gap-1.5 items-center">
          <span class="i-carbon-warning-alt" />{{ actErr }}
        </p>
        <span class="tnum text-sm text-truegray-400 ml-auto">共 {{ data?.total ?? 0 }} 份</span>
      </div>

      <table class="text-sm w-full">
        <thead>
          <tr class="border-b border-black/6 dark:border-white/8">
            <th class="th-base">
              合同号
            </th>
            <th class="th-base">
              员工
            </th>
            <th class="th-base">
              类型
            </th>
            <th class="th-base">
              状态
            </th>
            <th class="th-base">
              起止
            </th>
            <th class="th-base">
              剩余
            </th>
            <th class="th-base" />
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in data?.list" :key="c.id" class="row-base">
            <td class="text-xs text-truegray-500 font-mono td-base">
              {{ c.contract_no }}
            </td>
            <td class="font-medium td-base">
              {{ c.employee_name || '—' }}
            </td>
            <td class="text-truegray-500 td-base">
              {{ contractTypeMap[c.type] || c.type }}
            </td>
            <td class="td-base">
              <HBadge dot :tone="contractStatusMap[c.status]?.tone" :label="contractStatusMap[c.status]?.label || c.status" />
            </td>
            <td class="tnum text-truegray-500 td-base">
              {{ fmtDate(c.start_date) }} ~ {{ fmtDate(c.end_date) }}
            </td>
            <td class="td-base">
              <HBadge
                v-if="c.status === 'active' && c.days_left != null"
                :tone="c.days_left <= 15 ? 'red' : c.days_left <= 30 ? 'amber' : 'gray'"
                :label="`${c.days_left} 天`"
              />
              <span v-else class="text-truegray-300 dark:text-truegray-600">—</span>
            </td>
            <td class="td-base text-right whitespace-nowrap">
              <template v-if="c.status === 'active'">
                <button class="link-action mr-3" @click="renewFor = c; renewEnd = ''; renewBand = ''">
                  续签
                </button>
                <button class="text-sm text-rose-500 font-medium underline-offset-3 cursor-pointer hover:underline" @click="terminate(c)">
                  终止
                </button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="pending" class="text-sm text-truegray-400 py-6 text-center">
        加载中…
      </div>
      <HEmpty v-else-if="!data?.list?.length" icon="i-carbon-document-signed" label="没有符合条件的合同" />
      <HPager :page="page" :total-pages="totalPages" @go="goPage($event)" />
    </HCard>
  </div>
</template>
