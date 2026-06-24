<script setup lang="ts">
useHead({ title: '员工花名册' })
const { $api } = useNuxtApp()

// 筛选项：均为「待提交」状态，切换下拉不立即查询，点「查询」才生效。
const keyword = ref('')
const status = ref('')
const deptId = ref('')
const jobLevel = ref('')
const page = ref(1)
const pageSize = 15

// 部门下拉（拍平 /org-structure 的部门节点）+ 职级下拉（/job-levels）
const deptOptions = ref<{ id: string, label: string }[]>([])
const levelOptions = ref<{ level_code: string, name: string }[]>([])

function flattenDepts(nodes: Hr.OrgNode[] = [], depth = 0, out: { id: string, label: string }[] = []) {
  for (const n of nodes) {
    if (n.type === 'dept')
      out.push({ id: n.id, label: `${'　'.repeat(depth)}${n.name}` })
    flattenDepts(n.children, n.type === 'dept' ? depth + 1 : depth, out)
  }
  return out
}

onMounted(async () => {
  const [tree, levels] = await Promise.all([
    $api<Hr.OrgNode[]>('/v1/org-structure').catch(() => []),
    $api<{ level_code: string, name: string }[]>('/v1/job-levels').catch(() => []),
  ])
  deptOptions.value = flattenDepts(tree)
  levelOptions.value = levels
})

// 仅在「查询/翻页」时显式 load，避免随关键字逐字符或下拉切换触发请求。
function params() {
  const q: Record<string, any> = { page: page.value, page_size: pageSize }
  if (keyword.value)
    q.keyword = keyword.value
  if (status.value)
    q.status = status.value
  if (deptId.value)
    q.dept_id = deptId.value
  if (jobLevel.value)
    q.job_level = jobLevel.value
  return q
}

// 列表用普通 ref + 显式 load（不走 useAsyncData：其 refresh 在本版本会命中缓存而不重新执行 handler）
const data = ref<Hr.Page<Hr.Employee> | null>(null)
const pending = ref(false)
const error = ref(false)
async function load() {
  pending.value = true
  error.value = false
  try {
    data.value = await $api<Hr.Page<Hr.Employee>>('/v1/employees', { query: params() })
  }
  catch {
    error.value = true
  }
  finally {
    pending.value = false
  }
}
await load()

const totalPages = computed(() => Math.max(1, Math.ceil((data.value?.total ?? 0) / pageSize)))
function search() {
  page.value = 1
  load()
}
function reset() {
  keyword.value = ''
  status.value = ''
  deptId.value = ''
  jobLevel.value = ''
  search()
}
function goPage(delta: number) {
  page.value += delta
  load()
}

// ---- 导入 / 导出 ----
interface ImportResult { success: number, failed: number, errors: { row: number, msg: string }[] }
const importing = ref(false)
const importResult = ref<ImportResult | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

function exportXlsx() {
  const q = new URLSearchParams()
  if (status.value)
    q.set('status', status.value)
  if (keyword.value)
    q.set('keyword', keyword.value)
  if (deptId.value)
    q.set('dept_id', deptId.value)
  if (jobLevel.value)
    q.set('job_level', jobLevel.value)
  downloadAuthed(`/api/v1/roster-export?${q.toString()}`, dateStampedName('roster'))
}
async function onFile(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file)
    return
  importing.value = true
  importResult.value = null
  try {
    const token = useCookie('token').value
    const fd = new FormData()
    fd.append('file', file)
    const res = await fetch('/api/v1/roster-import', { method: 'POST', headers: token ? { Authorization: `Bearer ${token}` } : {}, body: fd })
    const json = await res.json()
    importResult.value = json.data || json
    await load()
  }
  finally {
    importing.value = false
    if (fileInput.value)
      fileInput.value.value = ''
  }
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <HPageHeader title="员工花名册" desc="在册员工的检索、档案入口与批量导入导出。">
      <template #actions>
        <button class="btn-secondary" @click="downloadAuthed('/api/v1/roster-template', 'roster-template.xlsx')">
          <span class="i-carbon-document-blank" />导入模板
        </button>
        <button class="btn-secondary" @click="exportXlsx">
          <span class="i-carbon-download" />导出
        </button>
        <button class="btn-primary" :disabled="importing" @click="fileInput?.click()">
          <span class="i-carbon-upload" />{{ importing ? '导入中…' : '导入' }}
        </button>
        <input ref="fileInput" type="file" accept=".xlsx" class="hidden" @change="onFile">
      </template>
    </HPageHeader>

    <HCard v-if="importResult" :title="`导入结果：成功 ${importResult.success}，失败 ${importResult.failed}`">
      <template #actions>
        <button class="btn-base text-xs text-truegray-400 px-2 py-1 hover:text-truegray-600" @click="importResult = null">
          <span class="i-carbon-close" />关闭
        </button>
      </template>
      <div v-if="!importResult.errors?.length" class="text-sm text-emerald-600 flex gap-1.5 items-center">
        <span class="i-carbon-checkmark-filled" />全部导入成功
      </div>
      <ul v-else class="text-sm flex flex-col gap-1">
        <li v-for="(e, i) in importResult.errors" :key="i" class="text-rose-500 flex gap-1.5 items-center">
          <span class="i-carbon-warning-alt shrink-0" />第 {{ e.row }} 行：{{ e.msg }}
        </li>
      </ul>
    </HCard>

    <HCard>
      <div class="mb-4 flex flex-wrap gap-3 items-center">
        <div class="relative">
          <span class="i-carbon-search text-truegray-300 left-3 top-1/2 absolute -translate-y-1/2" />
          <input
            v-model="keyword" placeholder="姓名 / 工号 / 手机号"
            class="input-base pl-9 w-64"
            @keyup.enter="search"
          >
        </div>
        <select v-model="deptId" aria-label="部门筛选" class="input-base max-w-56">
          <option value="">
            全部部门
          </option>
          <option v-for="d in deptOptions" :key="d.id" :value="d.id">
            {{ d.label }}
          </option>
        </select>
        <select v-model="jobLevel" aria-label="职级筛选" class="input-base">
          <option value="">
            全部职级
          </option>
          <option v-for="l in levelOptions" :key="l.level_code" :value="l.level_code">
            {{ l.level_code }} · {{ l.name }}
          </option>
        </select>
        <select v-model="status" aria-label="在职状态筛选" class="input-base">
          <option value="">
            全部状态
          </option>
          <option value="active">
            正式
          </option>
          <option value="probation">
            试用
          </option>
          <option value="left">
            已离职
          </option>
        </select>
        <button class="btn-primary" @click="search">
          <span class="i-carbon-search" />查询
        </button>
        <button class="btn-secondary" @click="reset">
          重置
        </button>
        <span class="tnum text-sm text-truegray-400 ml-auto">共 {{ data?.total ?? 0 }} 人</span>
      </div>

      <table class="text-sm w-full">
        <thead>
          <tr class="border-b border-black/6 dark:border-white/8">
            <th class="th-base">
              工号
            </th>
            <th class="th-base">
              姓名
            </th>
            <th class="th-base">
              部门
            </th>
            <th class="th-base">
              职级
            </th>
            <th class="th-base">
              状态
            </th>
            <th class="th-base">
              入职日期
            </th>
            <th class="th-base" />
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in data?.list" :key="e.id" class="row-base">
            <td class="text-xs text-truegray-500 font-mono td-base">
              {{ e.employee_no }}
            </td>
            <td class="font-medium td-base">
              {{ e.name }}
              <span class="text-xs text-truegray-400 font-normal ml-1">{{ genderMap[e.gender] }}</span>
            </td>
            <td class="td-base">
              {{ e.dept_name }}
            </td>
            <td class="text-truegray-500 td-base">
              {{ e.job_level || '—' }}
            </td>
            <td class="td-base">
              <HBadge
                dot
                :tone="employmentStatusMap[e.employment_status]?.tone"
                :label="employmentStatusMap[e.employment_status]?.label || e.employment_status"
              />
            </td>
            <td class="tnum text-truegray-500 td-base">
              {{ fmtDate(e.hired_at) }}
            </td>
            <td class="td-base text-right">
              <NuxtLink :to="`/roster/${e.id}`" class="link-action">
                档案
              </NuxtLink>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="pending" class="text-sm text-truegray-400 py-6 text-center">
        加载中…
      </div>
      <HEmpty v-else-if="error" icon="i-carbon-cloud-offline" label="加载失败，请稍后重试">
        <button class="link-action" @click="load()">
          重试
        </button>
      </HEmpty>
      <HEmpty v-else-if="!data?.list?.length" icon="i-carbon-user-multiple" label="没有符合条件的员工" hint="试试调整关键字或筛选条件" />

      <HPager :page="page" :total-pages="totalPages" @go="goPage($event)" />
    </HCard>
  </div>
</template>
