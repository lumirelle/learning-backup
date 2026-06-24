<script setup lang="ts">
// 人员选择器：搜索 + 组织树浏览，替代「几百项的下拉」。
// source='employees' 返回员工 id（支持按部门下钻 + 关键字搜索）；
// source='users' 返回登录账号 id（审批人等，关键字搜索为主）。
const props = withDefaults(defineProps<{
  modelValue: string
  source?: 'employees' | 'users'
  placeholder?: string
  status?: string // 仅 employees：按在职状态过滤（如 active）
  clearable?: boolean
}>(), { source: 'employees', placeholder: '选择人员', clearable: true })

const emit = defineEmits<{ 'update:modelValue': [string] }>()
const { $api } = useNuxtApp()

interface Person { id: string, name: string, sub?: string }

const open = ref(false)
const label = ref('') // 已选显示名
const keyword = ref('')
const results = ref<Person[]>([])
const searching = ref(false)

// 外部清空 modelValue 时同步清掉显示名（表单提交后 reset 场景）
watch(() => props.modelValue, (v) => {
  if (!v)
    label.value = ''
})

// ---- 搜索（防抖 250ms）----
let timer: ReturnType<typeof setTimeout> | null = null
watch(keyword, () => {
  if (timer)
    clearTimeout(timer)
  timer = setTimeout(runSearch, 250)
})
async function runSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    results.value = []
    return
  }
  searching.value = true
  try {
    if (props.source === 'users') {
      const list = await $api<Hr.User[]>('/v1/users', { query: { keyword: kw } })
      results.value = list.map(u => ({ id: u.id, name: u.name, sub: u.username }))
    }
    else {
      const q: Record<string, any> = { keyword: kw, page: 1, page_size: 50 }
      if (props.status)
        q.status = props.status
      const page = await $api<Hr.Page<Hr.Employee>>('/v1/employees', { query: q })
      results.value = (page.list || []).map(e => ({ id: e.id, name: e.name, sub: e.dept_name }))
    }
  }
  finally {
    searching.value = false
  }
}

// ---- 组织树浏览（仅 employees）----
const tree = ref<Hr.OrgNode[]>([])
const expanded = ref<Set<string>>(new Set())
const membersByDept = ref<Record<string, Person[]>>({})
const loadingDept = ref<Set<string>>(new Set())

async function ensureTree() {
  if (props.source !== 'employees' || tree.value.length)
    return
  tree.value = await $api<Hr.OrgNode[]>('/v1/org-structure').catch(() => [])
}

async function toggleDept(node: Hr.OrgNode) {
  const s = new Set(expanded.value)
  if (s.has(node.id)) {
    s.delete(node.id)
  }
  else {
    s.add(node.id)
    if (node.type === 'dept' && !membersByDept.value[node.id])
      await loadMembers(node.id)
  }
  expanded.value = s
}

async function loadMembers(deptId: string) {
  const s = new Set(loadingDept.value)
  s.add(deptId)
  loadingDept.value = s
  try {
    const q: Record<string, any> = { dept_id: deptId, page: 1, page_size: 200 }
    if (props.status)
      q.status = props.status
    const page = await $api<Hr.Page<Hr.Employee>>('/v1/employees', { query: q })
    membersByDept.value = { ...membersByDept.value, [deptId]: (page.list || []).map(e => ({ id: e.id, name: e.name, sub: e.employee_no })) }
  }
  finally {
    const d = new Set(loadingDept.value)
    d.delete(deptId)
    loadingDept.value = d
  }
}

// 拍平成「部门 / 员工」渲染行（按展开状态 + 已加载成员）
interface Row { kind: 'dept' | 'emp', id: string, name: string, sub?: string, depth: number, node?: Hr.OrgNode }
const treeRows = computed<Row[]>(() => {
  const out: Row[] = []
  const walk = (nodes: Hr.OrgNode[] = [], depth = 0) => {
    for (const n of nodes) {
      out.push({ kind: 'dept', id: n.id, name: n.name, depth, node: n })
      if (expanded.value.has(n.id)) {
        walk(n.children, depth + 1)
        for (const m of membersByDept.value[n.id] || [])
          out.push({ kind: 'emp', id: m.id, name: m.name, sub: m.sub, depth: depth + 1 })
      }
    }
  }
  walk(tree.value)
  return out
})

// 点击组件外部关闭面板（onClickOutside 按 DOM 包含判断，不依赖遮罩几何，
// 避免祖先 transform/filter 让 position:fixed 遮罩盖不满屏导致关不掉）
const root = ref<HTMLElement | null>(null)
onClickOutside(root, () => {
  open.value = false
})

async function toggle() {
  if (open.value) {
    open.value = false
    return
  }
  open.value = true
  await ensureTree()
}
function pick(p: Person) {
  emit('update:modelValue', p.id)
  label.value = p.name
  open.value = false
  keyword.value = ''
  results.value = []
}
function clear() {
  emit('update:modelValue', '')
  label.value = ''
}
</script>

<template>
  <div ref="root" class="relative">
    <!-- 触发器 -->
    <button
      type="button"
      class="input-base flex gap-2 w-full items-center justify-between"
      @click="toggle"
    >
      <span :class="label ? 'text-truegray-800 dark:text-truegray-100' : 'text-truegray-400'" class="truncate">
        {{ label || placeholder }}
      </span>
      <span class="flex shrink-0 gap-1 items-center">
        <span
          v-if="clearable && modelValue"
          class="i-carbon-close-filled text-truegray-300 hover:text-truegray-500"
          role="button"
          aria-label="清除"
          @click.stop="clear"
        />
        <span class="i-carbon-chevron-down text-truegray-400" />
      </span>
    </button>

    <!-- 下拉面板 -->
    <template v-if="open">
      <div class="mt-1 outline-black/8 outline rounded-xl bg-white max-h-90 w-80 shadow-xl absolute z-40 overflow-hidden dark:outline-white/10 dark:bg-ink-900">
        <div class="p-2 border-b border-black/6 dark:border-white/8">
          <div class="relative">
            <span class="i-carbon-search text-truegray-300 left-2.5 top-1/2 absolute -translate-y-1/2" />
            <input
              v-model="keyword" :placeholder="source === 'users' ? '输入姓名 / 账号搜索' : '搜索姓名 / 工号'"
              class="input-base text-sm pl-8 w-full"
            >
          </div>
        </div>

        <div class="max-h-72 overflow-y-auto">
          <!-- 搜索结果 -->
          <template v-if="keyword.trim()">
            <div v-if="searching" class="text-sm text-truegray-400 py-6 text-center">
              搜索中…
            </div>
            <ul v-else-if="results.length" class="py-1">
              <li
                v-for="p in results" :key="p.id"
                class="text-sm px-3 py-1.5 flex gap-2 cursor-pointer items-center justify-between hover:bg-truegray-50 dark:hover:bg-white/5"
                @click="pick(p)"
              >
                <span class="text-truegray-800 dark:text-truegray-100">{{ p.name }}</span>
                <span class="text-xs text-truegray-400 truncate">{{ p.sub }}</span>
              </li>
            </ul>
            <div v-else class="text-sm text-truegray-400 py-6 text-center">
              无匹配人员
            </div>
          </template>

          <!-- 组织树浏览（employees） -->
          <template v-else-if="source === 'employees'">
            <ul class="py-1">
              <li
                v-for="r in treeRows" :key="r.kind + r.id"
                class="text-sm px-3 py-1.5 flex gap-1.5 items-center"
                :class="r.kind === 'emp' ? 'cursor-pointer hover:bg-truegray-50 dark:hover:bg-white/5' : 'cursor-pointer hover:bg-truegray-50/60 dark:hover:bg-white/3'"
                :style="{ paddingLeft: `${r.depth * 14 + 12}px` }"
                @click="r.kind === 'dept' ? toggleDept(r.node!) : pick({ id: r.id, name: r.name })"
              >
                <template v-if="r.kind === 'dept'">
                  <span class="text-truegray-400 shrink-0" :class="expanded.has(r.id) ? 'i-carbon-chevron-down' : 'i-carbon-chevron-right'" />
                  <span class="i-carbon-folder text-truegray-300 shrink-0 dark:text-truegray-500" />
                  <span class="text-truegray-700 font-medium dark:text-truegray-200">{{ r.name }}</span>
                  <span v-if="loadingDept.has(r.id)" class="i-carbon-circle-dash text-truegray-300 animate-spin" />
                </template>
                <template v-else>
                  <span class="shrink-0 w-4" />
                  <span class="i-carbon-user text-truegray-300 shrink-0 dark:text-truegray-500" />
                  <span class="text-truegray-800 dark:text-truegray-100">{{ r.name }}</span>
                  <span class="text-xs text-truegray-400 ml-auto">{{ r.sub }}</span>
                </template>
              </li>
            </ul>
            <div v-if="!treeRows.length" class="text-sm text-truegray-400 py-6 text-center">
              暂无组织数据
            </div>
          </template>

          <!-- users 模式无关键字时的提示 -->
          <div v-else class="text-sm text-truegray-400 py-6 text-center">
            输入姓名或账号开始搜索
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
