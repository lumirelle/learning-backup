<script setup lang="ts">
import type { AutoCompleteOption, SelectOption } from 'naive-ui'
import type { ApplicationDetailVO, ApplicationListItemVO, ApplicationStage, BoardColumnVO, BoardQueryReq, BoardVO, StageLogVO } from '@/api/applications'
import type { DepartmentVO, SubDepartmentVO } from '@/api/departments'
import type { InterviewCreateReq, InterviewVO } from '@/api/interviews'
import type { JobLevel, JobListItemVO, JobWorkType, TagVO } from '@/api/jobs'
import {
  NAutoComplete,
  NButton,
  NDrawer,
  NDrawerContent,
  NInput,
  NInputNumber,
  NPopconfirm,
  NRate,
  NSelect,
  NSpin,
  NTag,
  NTimeline,
  NTimelineItem,
  useMessage,
} from 'naive-ui'
import { computed, h, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  applicationsApi,
  canTransition,
  isTerminal,
  STAGE_LABEL,
  STAGE_TRANSITIONS,
} from '@/api/applications'
import { departmentsApi } from '@/api/departments'
import {
  CONCLUSION_LABEL,
  CONCLUSION_TONE,
  interviewsApi,
} from '@/api/interviews'
import {
  jobsApi,
  LEVEL_LABEL,
  TAG_CATEGORY_LABEL,
  tagsApi,
  WORK_TYPE_LABEL,
} from '@/api/jobs'
import { BizError } from '@/api/request'
import CopyButton from '@/components/CopyButton.vue'
import EmptyState from '@/components/EmptyState.vue'
import { useAuthStore } from '@/stores/auth'
import { isResumeFile, resumeDownloadUrl } from '@/utils/resume'

const auth = useAuthStore()
const message = useMessage()
const route = useRoute()
const router = useRouter()

/** 从 dashboard 跳来时携带的 stage（用于高亮 / 滚动到对应列） */
const focusStage = computed<ApplicationStage | null>(() => {
  const s = route.query.stage
  if (typeof s !== 'string')
    return null
  // 简单白名单校验，避免 URL 注入非法 stage
  const valid: ApplicationStage[] = ['APPLIED', 'SCREENING_PASS', 'PHONE_INTERVIEW', 'TECH_INTERVIEW', 'HR_INTERVIEW', 'OFFER', 'HIRED', 'REJECTED']
  return valid.includes(s as ApplicationStage) ? s as ApplicationStage : null
})

function jumpToJobs() {
  router.push({ path: '/hr/jobs' })
}

// ───────────────────────── 字典加载（岗位 / 部门 / 标签 / 枚举） ─────────────────────────

const myJobs = ref<JobListItemVO[]>([])
const departments = ref<DepartmentVO[]>([])
const subDepartments = ref<SubDepartmentVO[]>([])
const tags = ref<TagVO[]>([])

async function loadMyJobs() {
  try {
    const res = await jobsApi.list({
      mine: !auth.isAdmin, // Admin 看全部，HR 只看自己创建的
      page: 1,
      size: 100,
      sortBy: 'publishedAt',
      sortOrder: 'desc',
    })
    myJobs.value = res.items
  }
  catch (e) {
    if (e instanceof BizError)
      message.error(e.message)
    else throw e
  }
}

async function loadDepartments() {
  try {
    departments.value = await departmentsApi.listAll()
  }
  catch (e) {
    console.warn('load departments failed', e)
  }
}

async function loadSubDepartments() {
  try {
    subDepartments.value = await departmentsApi.listAllSubDepartments()
  }
  catch (e) {
    console.warn('load sub-departments failed', e)
  }
}

async function loadTags() {
  try {
    tags.value = await tagsApi.listAll()
  }
  catch (e) {
    console.warn('load tags failed', e)
  }
}

// ───────────────────────── 筛选状态（与 BoardQueryReq 对齐） ─────────────────────────

const ALL_JOBS_SENTINEL = -1

/**
 * 看板筛选 reactive。设计：
 * - jobId === -1 时走"多维筛选"路径，其余 filter 字段生效
 * - jobId 给定具体值时走"单岗位看板"，其余 filter 字段会被后端忽略（前端 UI 自动 disable）
 */
const filter = reactive({
  jobId: ALL_JOBS_SENTINEL as number,
  keyword: '',
  location: '',
  workType: [] as JobWorkType[],
  level: [] as JobLevel[],
  tagSlugs: [] as string[],
  departmentId: null as number | null,
  subDepartmentId: null as number | null,
  salaryMin: null as number | null,
  salaryMax: null as number | null,
})

/** 是否处于"单岗位"模式（其余 filter 失效） */
const isSingleJobMode = computed(() => filter.jobId !== ALL_JOBS_SENTINEL)

/** 当前活跃的筛选条件数量（用于 UI 上的 badge 计数 + 一键清空开关） */
const activeFilterCount = computed(() => {
  if (isSingleJobMode.value)
    return 0
  let n = 0
  if (filter.keyword.trim())
    n++
  if (filter.location.trim())
    n++
  if (filter.workType.length)
    n++
  if (filter.level.length)
    n++
  if (filter.tagSlugs.length)
    n++
  if (filter.departmentId != null)
    n++
  if (filter.subDepartmentId != null)
    n++
  if (filter.salaryMin != null)
    n++
  if (filter.salaryMax != null)
    n++
  return n
})

const WORK_TYPE_OPTIONS: SelectOption[] = (Object.keys(WORK_TYPE_LABEL) as JobWorkType[])
  .map(v => ({ label: WORK_TYPE_LABEL[v], value: v }))
const LEVEL_OPTIONS: SelectOption[] = (Object.keys(LEVEL_LABEL) as JobLevel[])
  .map(v => ({ label: LEVEL_LABEL[v], value: v }))

const departmentOptions = computed<SelectOption[]>(() =>
  departments.value.map(d => ({ label: d.name, value: d.id })),
)

const subDepartmentOptionsGrouped = computed(() => {
  const groups = new Map<number, { name: string, list: SubDepartmentVO[] }>()
  subDepartments.value.forEach((sd) => {
    if (!groups.has(sd.parentDepartmentId))
      groups.set(sd.parentDepartmentId, { name: sd.parentDepartmentName, list: [] })
    groups.get(sd.parentDepartmentId)!.list.push(sd)
  })
  return Array.from(groups.entries()).map(([deptId, { name, list }]) => ({
    type: 'group' as const,
    label: name,
    key: `dept-${deptId}`,
    children: list.map(sd => ({
      label: `${sd.name} / ${sd.location}`,
      value: sd.id,
    })),
  }))
})

/** 标签按 category 分组渲染（与 hr/jobs.vue 一致的选择器交互） */
const tagOptionsGrouped = computed(() => {
  const groups = new Map<string, TagVO[]>()
  tags.value.forEach((t) => {
    if (!groups.has(t.category))
      groups.set(t.category, [])
    groups.get(t.category)!.push(t)
  })
  return Array.from(groups.entries()).map(([cat, list]) => ({
    type: 'group' as const,
    label: TAG_CATEGORY_LABEL[cat as keyof typeof TAG_CATEGORY_LABEL] ?? cat,
    key: cat,
    children: list.map(t => ({ label: t.name, value: t.slug })),
  }))
})

// ───────────────────────── 看板加载 ─────────────────────────

const board = ref<BoardVO | null>(null)
const loading = ref(false)

/** 当前生效的 jobId（用于详情区"是否显示岗位标题"等判断） */
const effectiveJobId = computed(() =>
  isSingleJobMode.value ? filter.jobId : undefined,
)

/** 把 reactive filter 物化为 BoardQueryReq（去掉空串 / 空数组 / null，让 URL 干净） */
function toBoardQuery(): BoardQueryReq {
  if (isSingleJobMode.value) {
    return { jobId: filter.jobId }
  }
  const req: BoardQueryReq = {}
  if (filter.keyword.trim())
    req.keyword = filter.keyword.trim()
  if (filter.location.trim())
    req.location = filter.location.trim()
  if (filter.workType.length)
    req.workType = [...filter.workType]
  if (filter.level.length)
    req.level = [...filter.level]
  if (filter.tagSlugs.length)
    req.tagSlugs = [...filter.tagSlugs]
  if (filter.departmentId != null)
    req.departmentId = filter.departmentId
  if (filter.subDepartmentId != null)
    req.subDepartmentId = filter.subDepartmentId
  if (filter.salaryMin != null)
    req.salaryMin = filter.salaryMin
  if (filter.salaryMax != null)
    req.salaryMax = filter.salaryMax
  return req
}

async function loadMoreColumn(stage: ApplicationStage) {
  const col = board.value?.columns.find(c => c.stage === stage)
  if (!col?.hasMore)
    return
  try {
    const extra = await applicationsApi.board({
      ...toBoardQuery(),
      stage,
      columnOffset: col.items.length,
    })
    const patch = extra.columns[0]
    if (patch) {
      col.items.push(...patch.items)
      col.hasMore = patch.hasMore ?? false
    }
  }
  catch (e) {
    if (e instanceof BizError)
      message.error(e.message)
  }
}

async function fetchBoard() {
  loading.value = true
  try {
    board.value = await applicationsApi.board(toBoardQuery())
  }
  catch (e) {
    if (e instanceof BizError)
      message.error(e.message)
    else throw e
  }
  finally {
    loading.value = false
  }
}

/** 是否处于"无任何筛选"状态：clear 按钮 disable 的依据 */
const isAllClear = computed(() =>
  filter.jobId === ALL_JOBS_SENTINEL && activeFilterCount.value === 0,
)

/** 一键清空所有筛选（包括把 jobId 重置回"所有岗位"） */
function clearFilters() {
  filter.jobId = ALL_JOBS_SENTINEL
  filter.keyword = ''
  filter.location = ''
  filter.workType = []
  filter.level = []
  filter.tagSlugs = []
  filter.departmentId = null
  filter.subDepartmentId = null
  filter.salaryMin = null
  filter.salaryMax = null
  // oxlint-disable-next-line no-use-before-define
  searchInput.value = ''
  fetchBoard()
}

// ───────────────────────── 搜索框（合并岗位选择 + 关键词全文） ─────────────────────────

/**
 * 搜索输入框文本（UI 层；不直接对应 filter 状态）。
 * 通过 select / Enter / blur 显式 commit 到 filter.jobId 或 filter.keyword。
 */
const searchInput = ref('')

/** 阻止 watcher 误触发 commit：在反向同步 / clear 等程序化修改 searchInput 时置 true */
let suppressInputCommit = false

/** AutoComplete value 前缀：`job:123` = 具体岗位；`kw:xxx` = 关键词搜索项 */
const JOB_VAL_PREFIX = 'job:'
const KW_VAL_PREFIX = 'kw:'

/**
 * 关键约束：option.label = "选中后写回 input 框的文本"（保持纯净）。
 * 装饰文案（"搜索 / 岗位"前缀、地点 meta）通过 renderSearchOption 渲染，不污染 label。
 * 这样 Enter 选中 / 点选 不会把装饰串塞回 input 框，杜绝循环嵌套。
 */
interface SearchOption extends AutoCompleteOption {
  label: string
  value: string
  kind: 'kw' | 'job'
  meta?: string
}

const searchOptions = computed<SearchOption[]>(() => {
  const raw = searchInput.value.trim()
  const ql = raw.toLowerCase()
  const opts: SearchOption[] = []

  // 头部：用户输入作为关键词搜索的快捷项（仅在有输入时显示）
  if (raw) {
    opts.push({
      label: raw,
      value: `${KW_VAL_PREFIX}${raw}`,
      kind: 'kw',
    })
  }

  // 岗位匹配：标题或地点子串命中；空输入时取最近 8 个
  const matches = !ql
    ? myJobs.value.slice(0, 8)
    : myJobs.value
        .filter(j =>
          j.title.toLowerCase().includes(ql)
          || (j.location ?? '').toLowerCase().includes(ql),
        )
        .slice(0, 8)

  for (const j of matches) {
    opts.push({
      label: j.title,
      value: `${JOB_VAL_PREFIX}${j.id}`,
      kind: 'job',
      meta: j.location ?? undefined,
    })
  }
  return opts
})

/** 自定义下拉项渲染：左侧 kicker badge + 主文本 + 可选 meta */
function renderSearchOption(option: SearchOption) {
  const isJob = option.kind === 'job'
  return h('div', { class: 'ac-row' }, [
    h(
      'span',
      { class: ['ac-kind', isJob ? 'ac-kind-job' : 'ac-kind-kw'] },
      isJob ? '岗位' : '搜索',
    ),
    h('span', { class: 'ac-label' }, option.label),
    option.meta
      ? h('span', { class: 'ac-meta' }, `· ${option.meta}`)
      : null,
  ])
}

/** 程序化设置 searchInput 同时屏蔽 onSearchBlur 的误 commit */
function setSearchInputDisplay(text: string) {
  suppressInputCommit = true
  searchInput.value = text
  nextTick(() => {
    suppressInputCommit = false
  })
}

/** 反向同步：filter 变化（如 ?jobId=xxx 跳转、clear、外部 set）时更新输入框显示文案 */
watch(
  [() => filter.jobId, () => filter.keyword, () => myJobs.value.length],
  () => {
    let display = ''
    if (filter.jobId !== ALL_JOBS_SENTINEL) {
      const job = myJobs.value.find(j => j.id === filter.jobId)
      display = job?.title ?? ''
    }
    else if (filter.keyword) {
      display = filter.keyword
    }
    if (display !== searchInput.value) {
      setSearchInputDisplay(display)
    }
  },
)

function onSearchSelect(value: string | number) {
  const v = String(value)
  if (v.startsWith(JOB_VAL_PREFIX)) {
    const jid = Number(v.slice(JOB_VAL_PREFIX.length))
    const job = myJobs.value.find(j => j.id === jid)
    if (!job)
      return
    // 切到单岗位模式：清掉关键词（关键词在单岗位下无意义）
    filter.keyword = ''
    if (filter.jobId !== jid) {
      filter.jobId = jid // 触发 watcher → fetchBoard + 反向同步 searchInput
    }
    else {
      // jobId 没变（重复选同一岗位）：watcher 不触发，手动同步 input + 不重查
      setSearchInputDisplay(job.title)
    }
  }
  else if (v.startsWith(KW_VAL_PREFIX)) {
    commitKeyword(v.slice(KW_VAL_PREFIX.length))
  }
}

/** 用户直接 Enter（NAutoComplete 在没匹配 option 时也会触发 keydown） */
function onSearchEnter() {
  if (suppressInputCommit)
    return
  // 如果有匹配 options，Enter 会先触发 @select（选中第一项）→ onSearchSelect 处理
  // 仅当无 options 时这里兜底
  if (searchOptions.value.length > 0)
    return
  commitKeyword(searchInput.value.trim())
}

/** 失焦：若文本既不匹配当前 jobId 的岗位标题、也不等于当前 keyword，则默默 commit 为关键词 */
function onSearchBlur() {
  if (suppressInputCommit)
    return
  const text = searchInput.value.trim()
  // 当前单岗位模式：若文本仍等于岗位标题，不动；否则 commit 为关键词
  if (filter.jobId !== ALL_JOBS_SENTINEL) {
    const job = myJobs.value.find(j => j.id === filter.jobId)
    if (job && text === job.title)
      return
  }
  if (text === (filter.keyword ?? ''))
    return
  commitKeyword(text)
}

function onSearchClear() {
  // 仅清空"搜索维度"（岗位 + 关键词），保留其他筛选项
  if (filter.jobId === ALL_JOBS_SENTINEL && !filter.keyword)
    return
  const hadJob = filter.jobId !== ALL_JOBS_SENTINEL
  filter.keyword = ''
  filter.jobId = ALL_JOBS_SENTINEL
  // 若原本是单岗位模式，jobId watcher 会触发 fetchBoard；否则手动 fetch
  if (!hadJob)
    fetchBoard()
}

function commitKeyword(text: string) {
  const sameKeyword = text === (filter.keyword ?? '')
  const wasSingleJob = filter.jobId !== ALL_JOBS_SENTINEL

  filter.keyword = text
  // 强制把 input 显示同步为 text（防御性，避免选中 kw 项时 NAutoComplete 默认行为污染）
  setSearchInputDisplay(text)

  if (wasSingleJob) {
    filter.jobId = ALL_JOBS_SENTINEL // 触发 watcher → fetchBoard
    return
  }
  // 同关键词不重复查；不同则触发一次
  if (!sameKeyword)
    fetchBoard()
}

// jobId 切换：立即重查
watch(() => filter.jobId, () => fetchBoard())

// 多选 / 下拉 / 数字字段：立即重查（这些是显式选择，无打字节流）
watch(
  () => [filter.workType, filter.level, filter.tagSlugs, filter.departmentId, filter.subDepartmentId, filter.salaryMin, filter.salaryMax],
  () => {
    if (!isSingleJobMode.value)
      fetchBoard()
  },
  { deep: true },
)

// location 是手输文本：只在显式触发（enter / 失焦 / 清除）时查
function triggerTextSearch() {
  if (!isSingleJobMode.value)
    fetchBoard()
}

// ───────────────────────── 拖拽 ─────────────────────────

interface DragState {
  applicationId: number
  fromStage: ApplicationStage
}

const dragState = ref<DragState | null>(null)
/** 当前 hover 的目标列（用于高亮）。 */
const hoverStage = ref<ApplicationStage | null>(null)

// 拒绝原因弹窗：状态先声明，避免被 onColumnDrop 引用时报 use-before-define
const rejectModalVisible = ref(false)
const rejectNote = ref('')
const pendingReject = ref<{ id: number, fromStage: ApplicationStage } | null>(null)

function onCardDragStart(e: DragEvent, item: ApplicationListItemVO) {
  if (isTerminal(item.stage)) {
    e.preventDefault()
    return
  }
  dragState.value = { applicationId: item.id, fromStage: item.stage }
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
    e.dataTransfer.setData('text/plain', String(item.id))
  }
}

function onCardDragEnd() {
  dragState.value = null
  hoverStage.value = null
}

function onColumnDragOver(e: DragEvent, stage: ApplicationStage) {
  if (!dragState.value)
    return
  if (!canTransition(dragState.value.fromStage, stage))
    return
  e.preventDefault()
  if (e.dataTransfer)
    e.dataTransfer.dropEffect = 'move'
  hoverStage.value = stage
}

function onColumnDragLeave(stage: ApplicationStage) {
  if (hoverStage.value === stage)
    hoverStage.value = null
}

async function onColumnDrop(e: DragEvent, target: ApplicationStage) {
  e.preventDefault()
  hoverStage.value = null
  const drag = dragState.value
  dragState.value = null
  if (!drag)
    return
  if (!canTransition(drag.fromStage, target)) {
    message.warning(`不能从「${STAGE_LABEL[drag.fromStage]}」流转到「${STAGE_LABEL[target]}」`)
    return
  }

  // REJECTED 必须填原因 → 弹小弹窗
  if (target === 'REJECTED') {
    pendingReject.value = { id: drag.applicationId, fromStage: drag.fromStage }
    rejectNote.value = ''
    rejectModalVisible.value = true
    return
  }

  await commitTransition(drag.applicationId, drag.fromStage, target, undefined)
}

async function commitTransition(
  id: number,
  from: ApplicationStage,
  target: ApplicationStage,
  note?: string,
) {
  // 乐观更新：先把卡片从旧列搬到新列
  const board0 = board.value
  if (!board0)
    return
  const fromCol = board0.columns.find(c => c.stage === from)
  const toCol = board0.columns.find(c => c.stage === target)
  if (!fromCol || !toCol)
    return

  const idx = fromCol.items.findIndex(i => i.id === id)
  if (idx < 0)
    return
  const moved = fromCol.items.splice(idx, 1)[0]
  moved.stage = target
  toCol.items.unshift(moved)
  fromCol.count -= 1
  toCol.count += 1

  try {
    await applicationsApi.transition(id, { toStage: target, note })
    message.success(`已流转：${STAGE_LABEL[from]} → ${STAGE_LABEL[target]}`)
  }
  catch (e) {
    // 失败回滚
    toCol.items.shift()
    moved.stage = from
    fromCol.items.splice(idx, 0, moved)
    fromCol.count += 1
    toCol.count -= 1
    if (e instanceof BizError)
      message.error(e.message)
    else throw e
  }
}

// ───────────────────────── Reject 弹窗 ─────────────────────────

async function submitReject() {
  if (!pendingReject.value)
    return
  if (!rejectNote.value.trim()) {
    message.warning('请填写拒绝原因')
    return
  }
  const { id, fromStage } = pendingReject.value
  rejectModalVisible.value = false
  await commitTransition(id, fromStage, 'REJECTED', rejectNote.value.trim())
  pendingReject.value = null
}

function cancelReject() {
  rejectModalVisible.value = false
  pendingReject.value = null
}

// ───────────────────────── Detail Drawer ─────────────────────────

const drawerVisible = ref(false)
const detail = ref<ApplicationDetailVO | null>(null)
const detailLoading = ref(false)

// 面试记录状态先声明（openDetail 会用到，setup script 顺序求值，否则 oxlint no-use-before-define）
const interviews = ref<InterviewVO[]>([])
const interviewFormVisible = ref(false)
const editingInterview = ref<InterviewVO | null>(null)
const interviewSubmitting = ref(false)
/** 表单 model — 与 InterviewCreateReq 同字段（rating 用 number 而非 short） */
const interviewForm = ref<InterviewCreateReq>({
  round: '',
  rating: 4,
  conclusion: 'PASS',
  strengths: '',
  weaknesses: '',
  notes: '',
})

const conclusionOptions: SelectOption[] = [
  { label: CONCLUSION_LABEL.PASS, value: 'PASS' },
  { label: CONCLUSION_LABEL.HOLD, value: 'HOLD' },
  { label: CONCLUSION_LABEL.REJECT, value: 'REJECT' },
]

async function openDetail(id: number) {
  drawerVisible.value = true
  detailLoading.value = true
  detail.value = null
  // 重置面试记录区
  interviews.value = []
  interviewFormVisible.value = false
  editingInterview.value = null
  try {
    const [d, ivs] = await Promise.all([
      applicationsApi.detail(id),
      interviewsApi.list(id),
    ])
    detail.value = d
    interviews.value = ivs
  }
  catch (e) {
    drawerVisible.value = false
    if (e instanceof BizError)
      message.error(e.message)
    else throw e
  }
  finally {
    detailLoading.value = false
  }
}

// ───────────────────────── 面试记录 actions ─────────────────────────

function openInterviewCreate() {
  editingInterview.value = null
  interviewForm.value = {
    round: '',
    rating: 4,
    conclusion: 'PASS',
    strengths: '',
    weaknesses: '',
    notes: '',
  }
  interviewFormVisible.value = true
}

function openInterviewEdit(iv: InterviewVO) {
  editingInterview.value = iv
  interviewForm.value = {
    round: iv.round,
    rating: iv.rating ?? 4,
    conclusion: iv.conclusion,
    strengths: iv.strengths ?? '',
    weaknesses: iv.weaknesses ?? '',
    notes: iv.notes ?? '',
  }
  interviewFormVisible.value = true
}

function cancelInterviewForm() {
  interviewFormVisible.value = false
  editingInterview.value = null
}

async function submitInterview() {
  if (!detail.value)
    return
  if (!interviewForm.value.round.trim()) {
    message.warning('请填写面试轮次')
    return
  }
  if (!interviewForm.value.rating || interviewForm.value.rating < 1) {
    message.warning('请给出 1-5 星评分')
    return
  }
  interviewSubmitting.value = true
  try {
    if (editingInterview.value) {
      const updated = await interviewsApi.update(editingInterview.value.id, interviewForm.value)
      // 就地替换：保持顺序
      const idx = interviews.value.findIndex(i => i.id === updated.id)
      if (idx >= 0)
        interviews.value.splice(idx, 1, updated)
      message.success('已更新面试评价')
    }
    else {
      const created = await interviewsApi.create(detail.value.id, interviewForm.value)
      interviews.value.push(created)
      message.success('已添加面试评价')
    }
    interviewFormVisible.value = false
    editingInterview.value = null
  }
  catch (e) {
    if (e instanceof BizError)
      message.error(e.message)
    else throw e
  }
  finally {
    interviewSubmitting.value = false
  }
}

// 简历 URL 工具迁移到 @/utils/resume，避免与 me/applications.vue 重复实现。

async function transitionFromDrawer(target: ApplicationStage) {
  if (!detail.value)
    return
  if (target === 'REJECTED') {
    pendingReject.value = { id: detail.value.id, fromStage: detail.value.stage }
    rejectNote.value = ''
    rejectModalVisible.value = true
    drawerVisible.value = false
    return
  }
  await commitTransition(detail.value.id, detail.value.stage, target)
  await openDetail(detail.value.id)
}

// ───────────────────────── helpers ─────────────────────────

const STAGE_ACCENT: Record<ApplicationStage, string> = {
  APPLIED: 'var(--gray-400)',
  SCREENING_PASS: 'var(--info-500)',
  PHONE_INTERVIEW: 'var(--accent-cyan)',
  TECH_INTERVIEW: 'var(--accent-teal)',
  HR_INTERVIEW: 'var(--accent-emerald)',
  OFFER: 'var(--warning-500)',
  HIRED: 'var(--success-500)',
  REJECTED: 'var(--danger-500)',
}

const STAGE_TONE: Record<ApplicationStage, 'success' | 'warning' | 'error' | 'info' | 'default'> = {
  APPLIED: 'default',
  SCREENING_PASS: 'info',
  PHONE_INTERVIEW: 'info',
  TECH_INTERVIEW: 'info',
  HR_INTERVIEW: 'info',
  OFFER: 'warning',
  HIRED: 'success',
  REJECTED: 'error',
}

function formatTime(iso: string | null) {
  if (!iso)
    return ''
  const d = new Date(iso)
  const diffH = (Date.now() - d.getTime()) / 36e5
  if (diffH < 1)
    return `${Math.max(1, Math.floor(diffH * 60))} 分钟前`
  if (diffH < 24)
    return `${Math.floor(diffH)} 小时前`
  if (diffH < 24 * 30)
    return `${Math.floor(diffH / 24)} 天前`
  return d.toISOString().slice(0, 10)
}

function stageLogTitle(log: StageLogVO) {
  if (!log.fromStage)
    return '候选人投递'
  return `${STAGE_LABEL[log.fromStage]} → ${STAGE_LABEL[log.toStage]}`
}

/** 是否在拖拽中：用来给除合法目标外的列降低亮度。 */
const isDragging = computed(() => dragState.value !== null)

/** 当前正在拖的卡片，其合法目标列集合（用于高亮）。 */
const allowedTargets = computed<Set<ApplicationStage>>(() => {
  if (!dragState.value)
    return new Set()
  return new Set(STAGE_TRANSITIONS[dragState.value.fromStage])
})

function isAllowedTarget(stage: ApplicationStage) {
  return allowedTargets.value.has(stage)
}

/**
 * 浮动拒绝区是否可放：当前在拖且 fromStage 允许转 REJECTED。
 * 终态卡片本来就 :draggable="false"，所以只要 isDragging 几乎永远为 true；
 * 多一层 canTransition 保险，未来状态机调整不破。
 */
const canRejectFromDrag = computed(() =>
  dragState.value !== null && canTransition(dragState.value.fromStage, 'REJECTED'),
)

function colClass(col: BoardColumnVO) {
  const dragging = isDragging.value
  const allowed = isAllowedTarget(col.stage)
  const hovered = hoverStage.value === col.stage
  return [
    'flow-node',
    dragging && allowed && 'is-allowed',
    dragging && !allowed && 'is-dim',
    hovered && 'is-hover',
  ].filter(Boolean).join(' ')
}

// ───────────────────────── Pipeline 图式布局 ─────────────────────────

/**
 * 主流程 7 节点（成长路径）。
 * REJECTED 作为"侧支终态"在底部独立呈现，不进入主流程网格。
 */
const MAIN_FLOW: ApplicationStage[] = [
  'APPLIED',
  'SCREENING_PASS',
  'PHONE_INTERVIEW',
  'TECH_INTERVIEW',
  'HR_INTERVIEW',
  'OFFER',
  'HIRED',
]

const mainColumns = computed<BoardColumnVO[]>(() => {
  if (!board.value)
    return []
  return MAIN_FLOW
    .map(stage => board.value!.columns.find(c => c.stage === stage))
    .filter((c): c is BoardColumnVO => Boolean(c))
})

const rejectColumn = computed<BoardColumnVO | undefined>(() =>
  board.value?.columns.find(c => c.stage === 'REJECTED'),
)

/** 每个节点在"概览"模式下最多展示几张迷你卡片，避免节点内部出现滚动。 */
const NODE_PREVIEW_LIMIT = 4

function previewItems(items: ApplicationListItemVO[]): ApplicationListItemVO[] {
  return items.slice(0, NODE_PREVIEW_LIMIT)
}

function overflowCount(items: ApplicationListItemVO[]): number {
  return Math.max(0, items.length - NODE_PREVIEW_LIMIT)
}

/** 候选人首字头像：中文取首字，英文取首字母大写。 */
function avatarChar(name: string | null): string {
  if (!name)
    return '?'
  const t = name.trim()
  if (!t)
    return '?'
  return t.charAt(0).toUpperCase()
}

/**
 * 岗位详情 URL（用于"新标签页打开"）。
 * 不用硬编码 '/jobs?jobId='，走 router.resolve 兼容 base / hash mode。
 */
/** 投递详情 · 组织面包屑：集团 / 部门 / 子部门 · 地点 */
function jobOrgBreadcrumb(d: ApplicationDetailVO): string | null {
  const parts = [d.rootOrgName, d.departmentName, d.subDepartmentName].filter(Boolean)
  if (parts.length === 0)
    return null
  const trail = parts.join(' / ')
  return d.jobLocation ? `${trail} · ${d.jobLocation}` : trail
}

function jobDetailHref(jobId: number | null): string {
  if (jobId == null)
    return '#'
  return router.resolve({ name: 'Jobs', query: { jobId: String(jobId) } }).href
}

// ───── Stage 全列抽屉（点 "+N 更多" 触发，承担"看不下的尾巴"） ─────

const stageDrawerVisible = ref(false)
const stageDrawerStage = ref<ApplicationStage | null>(null)

const stageDrawerItems = computed<ApplicationListItemVO[]>(() => {
  if (!stageDrawerStage.value || !board.value)
    return []
  return board.value.columns.find(c => c.stage === stageDrawerStage.value)?.items ?? []
})

function openStageDrawer(stage: ApplicationStage) {
  stageDrawerStage.value = stage
  stageDrawerVisible.value = true
}

function closeStageDrawer() {
  stageDrawerVisible.value = false
}

// ───────────────────────── lifecycle ─────────────────────────

onMounted(async () => {
  // 字典并行加载（看板第一次拉取需要等 myJobs，因此 await；部门/标签不阻塞）
  await Promise.all([
    loadMyJobs(),
    loadDepartments(),
    loadSubDepartments(),
    loadTags(),
  ])
  // 支持 /hr/board?jobId=xxx 跨页跳转：从 HR 岗位管理"看板"按钮带过来后自动选中
  const jobIdParam = route.query.jobId
  if (typeof jobIdParam === 'string' && /^\d+$/.test(jobIdParam)) {
    const jid = Number(jobIdParam)
    if (myJobs.value.some(j => j.id === jid)) {
      filter.jobId = jid
      // filter.jobId 的 watcher 会触发 fetchBoard
      return
    }
  }
  await fetchBoard()
})
</script>

<template>
  <main min-h-screen bg-app pt-60px>
    <!-- 顶部 -->
    <header max-w-1500px mx-auto p="t-8 b-4 x-6">
      <div>
        <p kicker mb-2>
          Hiring Pipeline · 招聘看板
        </p>
        <h1 m-0 text-3xl text-gray-900 font-black tracking="[-0.03em]" leading="tight">
          投递流转 · <span text-gradient>8 态状态机</span>
        </h1>
        <p mt-2 text-sm text-secondary leading="[1.6]">
          拖拽卡片到下一阶段即可推进 ·
          合法路径会高亮 · 拒绝需填写原因 · 终态（已入职 / 已拒绝）不可再变更
        </p>
        <p v-if="board?.jobsTruncated" mt-2 text-xs text-amber-700>
          匹配的岗位超过 500 个上限，结果可能不完整，请缩小筛选条件。
        </p>
      </div>
    </header>

    <!-- ─────── 多维筛选面板（选定具体岗位时其他筛选项整体 disable + 半透明） ─────── -->
    <section max-w-1500px mx-auto p="x-6 b-4">
      <div
        class="filter-panel"
      >
        <header class="filter-panel-header">
          <div flex="~ items-center wrap" gap-2>
            <span class="filter-panel-title">筛选条件</span>
            <span v-if="activeFilterCount > 0" class="filter-badge">
              {{ activeFilterCount }} 项
            </span>
            <span v-if="isSingleJobMode" text-11px text-tertiary>
              · 选定具体岗位后其他维度自动锁定
            </span>
          </div>
          <div flex="~ items-center" gap-3>
            <span class="total-pill" :class="{ 'is-loading': loading }">
              <span class="total-pill-label">投递</span>
              <span class="total-pill-num font-mono">{{ board?.totalApplications ?? 0 }}</span>
            </span>
            <NButton
              size="tiny"
              quaternary
              :disabled="isAllClear || loading"
              @click="clearFilters"
            >
              清空筛选
            </NButton>
          </div>
        </header>

        <div class="filter-grid">
          <!--
            搜索（合并关键词 + 岗位）：
            - 输入文字 → 下拉同步提示匹配的岗位 & "搜索 xxx" 关键词项
            - 选具体岗位 → 进入单岗位模式（其他维度自动锁定）
            - Enter / 失焦 / 点"搜索 xxx" → 走标题+描述全文模糊
          -->
          <div class="filter-cell filter-cell-wide">
            <label class="filter-label">
              搜索
              <span text-tertiary text-10px ml-1 normal-case font-normal tracking-normal>
                输入关键词全文检索；下拉精选具体岗位
              </span>
            </label>
            <NAutoComplete
              v-model:value="searchInput"
              :options="searchOptions"
              :render-label="renderSearchOption"
              :loading="loading"
              clearable
              placeholder="搜索岗位标题 / 描述，或选具体岗位…"
              :get-show="() => true"
              @select="onSearchSelect"
              @keydown.enter="onSearchEnter"
              @blur="onSearchBlur"
              @clear="onSearchClear"
            >
              <template #prefix>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8" />
                  <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
                </svg>
              </template>
            </NAutoComplete>
          </div>

          <!-- 子部门地点：ILIKE 模糊（M6 从 sub_departments.location 取） -->
          <div class="filter-cell">
            <label class="filter-label">子部门地点</label>
            <NInput
              v-model:value="filter.location"
              placeholder="上海·浦东 / 远程…"
              clearable
              :disabled="isSingleJobMode || loading"
              @keydown.enter="triggerTextSearch"
              @blur="triggerTextSearch"
              @clear="triggerTextSearch"
            />
          </div>

          <!-- 上层部门 -->
          <div class="filter-cell">
            <label class="filter-label">上层部门</label>
            <NSelect
              v-model:value="filter.departmentId"
              :options="departmentOptions"
              filterable
              clearable
              placeholder="全部部门"
              :disabled="isSingleJobMode || loading"
            />
          </div>

          <!-- 子部门：精确到叶子 -->
          <div class="filter-cell">
            <label class="filter-label">子部门</label>
            <NSelect
              v-model:value="filter.subDepartmentId"
              :options="subDepartmentOptionsGrouped"
              filterable
              clearable
              placeholder="全部子部门"
              :disabled="isSingleJobMode || loading"
            />
          </div>

          <!-- 工作类型：多选 -->
          <div class="filter-cell">
            <label class="filter-label">工作类型</label>
            <NSelect
              v-model:value="filter.workType"
              :options="WORK_TYPE_OPTIONS"
              multiple
              clearable
              placeholder="不限"
              max-tag-count="responsive"
              :disabled="isSingleJobMode || loading"
            />
          </div>

          <!-- 级别：多选 -->
          <div class="filter-cell">
            <label class="filter-label">级别</label>
            <NSelect
              v-model:value="filter.level"
              :options="LEVEL_OPTIONS"
              multiple
              clearable
              placeholder="不限"
              max-tag-count="responsive"
              :disabled="isSingleJobMode || loading"
            />
          </div>

          <!-- 薪资范围（元/月）：区间相交语义 —— 后端做了"NULL 兼容" -->
          <div class="filter-cell filter-cell-wide">
            <label class="filter-label">薪资范围（元/月，区间相交）</label>
            <div flex items-center gap-2>
              <NInputNumber
                v-model:value="filter.salaryMin"
                :min="0"
                :step="1000"
                placeholder="下限"
                clearable
                :disabled="isSingleJobMode || loading"
                flex-1
                :show-button="false"
              />
              <span text-tertiary text-xs>—</span>
              <NInputNumber
                v-model:value="filter.salaryMax"
                :min="0"
                :step="1000"
                placeholder="上限"
                clearable
                :disabled="isSingleJobMode || loading"
                flex-1
                :show-button="false"
              />
            </div>
          </div>

          <!-- 标签：分组下拉多选 -->
          <div class="filter-cell filter-cell-full">
            <label class="filter-label">技能 / 行业标签（多选）</label>
            <NSelect
              v-model:value="filter.tagSlugs"
              :options="tagOptionsGrouped"
              multiple
              filterable
              clearable
              placeholder="按标签过滤岗位（OR：命中任一即可）"
              max-tag-count="responsive"
              :disabled="isSingleJobMode || loading"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- 看板 -->
    <section max-w-1500px mx-auto p="b-8 x-6">
      <NSpin :show="loading">
        <EmptyState
          v-if="!loading && (!board || board.totalApplications === 0)"
          icon="inbox"
          title="当前范围下还没有投递记录"
          :description="activeFilterCount > 0 ? '当前筛选条件下没有匹配的投递，试试缩减条件或一键清空筛选。' : (isSingleJobMode ? '尝试切换到「所有岗位（汇总）」查看，或先去岗位管理发布更多岗位。' : '发布岗位后，候选人投递的简历会出现在这里。')"
        >
          <template #action>
            <NButton type="primary" @click="jumpToJobs">
              去岗位管理
            </NButton>
          </template>
        </EmptyState>

        <div v-else class="flow-board">
          <!-- ─────── 主流程：7 节点成长路径 ─────── -->
          <div class="flow-main">
            <div
              v-for="(col, idx) in mainColumns"
              :key="col.stage"
              :class="[colClass(col), focusStage === col.stage ? 'is-focus-target' : '']"
              :style="{ '--accent': STAGE_ACCENT[col.stage] }"
              @dragover="onColumnDragOver($event, col.stage)"
              @dragleave="onColumnDragLeave(col.stage)"
              @drop="onColumnDrop($event, col.stage)"
            >
              <header class="node-header">
                <span class="node-index font-mono">
                  {{ String(idx + 1).padStart(2, '0') }}
                </span>
                <div class="node-title-wrap">
                  <span class="node-title">{{ STAGE_LABEL[col.stage] }}</span>
                  <span class="node-count font-mono">{{ col.count }}</span>
                </div>
              </header>

              <div class="node-body">
                <div v-if="col.items.length === 0" class="node-empty">
                  <span class="empty-dot" />
                  <span>暂无</span>
                </div>

                <article
                  v-for="item in previewItems(col.items)"
                  :key="item.id"
                  :draggable="!isTerminal(item.stage)"
                  class="mini-card"
                  :class="{ 'is-dragging': dragState?.applicationId === item.id }"
                  tabindex="0"
                  role="button"
                  @dragstart="onCardDragStart($event, item)"
                  @dragend="onCardDragEnd"
                  @click="openDetail(item.id)"
                  @keydown.enter="openDetail(item.id)"
                >
                  <span class="mini-avatar" aria-hidden="true">
                    {{ avatarChar(item.candidateName) }}
                  </span>
                  <div class="mini-info">
                    <p class="mini-name">
                      {{ item.candidateName ?? '匿名' }}
                    </p>
                    <p v-if="!effectiveJobId" class="mini-job" truncate :title="item.jobTitle">
                      {{ item.jobTitle }}
                    </p>
                    <p class="mini-meta font-mono">
                      <span v-if="item.yearsExp != null">{{ item.yearsExp }}y · </span>
                      {{ formatTime(item.updatedAt) }}
                    </p>
                  </div>
                </article>
              </div>

              <footer v-if="col.hasMore || overflowCount(col.items) > 0" class="node-foot">
                <button
                  v-if="col.hasMore"
                  type="button"
                  class="more-chip"
                  @click.stop="loadMoreColumn(col.stage)"
                >
                  加载更多（{{ col.items.length }}/{{ col.count }}）
                </button>
                <button
                  v-else-if="overflowCount(col.items) > 0"
                  type="button"
                  class="more-chip"
                  @click.stop="openStageDrawer(col.stage)"
                >
                  + {{ overflowCount(col.items) }} 查看全部
                </button>
              </footer>
            </div>
          </div>

          <!-- ─────── 终态：REJECTED 侧支 ─────── -->
          <div
            v-if="rejectColumn"
            class="flow-side" :class="[colClass(rejectColumn), focusStage === 'REJECTED' ? 'is-focus-target' : '']"
            :style="{ '--accent': STAGE_ACCENT.REJECTED }"
            @dragover="onColumnDragOver($event, 'REJECTED')"
            @dragleave="onColumnDragLeave('REJECTED')"
            @drop="onColumnDrop($event, 'REJECTED')"
          >
            <header class="node-header side-header">
              <span class="node-index" font-mono>✕</span>
              <div class="node-title-wrap">
                <span class="node-title">{{ STAGE_LABEL.REJECTED }}</span>
                <span class="node-count" font-mono>{{ rejectColumn.count }}</span>
              </div>
              <span class="side-tag">终态 · 不可流转</span>
            </header>

            <div class="node-body side-body">
              <div v-if="rejectColumn.items.length === 0" class="node-empty">
                <span class="empty-dot" />
                <span>无被拒投递</span>
              </div>

              <article
                v-for="item in previewItems(rejectColumn.items)"
                :key="item.id"
                class="mini-card mini-card-side"
                tabindex="0"
                role="button"
                @click="openDetail(item.id)"
                @keydown.enter="openDetail(item.id)"
              >
                <span class="mini-avatar" aria-hidden="true">
                  {{ avatarChar(item.candidateName) }}
                </span>
                <div class="mini-info">
                  <p class="mini-name">
                    {{ item.candidateName ?? '匿名' }}
                  </p>
                  <p v-if="!effectiveJobId" class="mini-job truncate" :title="item.jobTitle">
                    {{ item.jobTitle }}
                  </p>
                  <p class="mini-meta" font-mono>
                    {{ formatTime(item.updatedAt) }}
                  </p>
                </div>
              </article>
            </div>

            <footer v-if="overflowCount(rejectColumn.items) > 0" class="node-foot">
              <button
                type="button"
                class="more-chip"
                @click.stop="openStageDrawer('REJECTED')"
              >
                + {{ overflowCount(rejectColumn.items) }} 查看全部
              </button>
            </footer>
          </div>
        </div>
      </NSpin>
    </section>

    <!-- ─────── 浮动拒绝投放区（拖拽时浮出，给小屏 / 流程靠前的卡片一条快路径） ─────── -->
    <aside
      v-show="canRejectFromDrag"
      class="reject-dropzone"
      :class="{ 'is-hover': hoverStage === 'REJECTED' }"
      role="region"
      aria-label="拖拽到此一键拒绝"
      @dragover="onColumnDragOver($event, 'REJECTED')"
      @dragleave="onColumnDragLeave('REJECTED')"
      @drop="onColumnDrop($event, 'REJECTED')"
    >
      <div class="reject-dropzone-icon" aria-hidden="true">
        ✕
      </div>
      <div class="reject-dropzone-text">
        <strong>拖到此处</strong>
        <span>一键拒绝（需填写原因）</span>
      </div>
    </aside>

    <!-- ─────── Reject Modal ─────── -->
    <NDrawer
      v-model:show="rejectModalVisible"
      :width="420"
      placement="right"
      @mask-click="cancelReject"
    >
      <NDrawerContent
        title="填写拒绝原因"
        :native-scrollbar="false"
        closable
      >
        <p text-sm text-secondary mb-4 leading="[1.65]">
          一旦拒绝，该投递将进入终态，<strong text-primary>不可再变更</strong>。请简述原因，候选人能在「我的投递」看到，体验会更好。
        </p>
        <NInput
          v-model:value="rejectNote"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-count
          placeholder="例如：技术深度未达岗位预期 / 期望薪资超出预算 / 候选人主动放弃"
        />
        <template #footer>
          <div flex="~ items-center justify-end" gap-2 w-full>
            <NButton @click="cancelReject">
              取消
            </NButton>
            <NButton type="error" :disabled="!rejectNote.trim()" @click="submitReject">
              确认拒绝
            </NButton>
          </div>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- ─────── Stage 全列抽屉（点 "+N 更多" 进入，承担节点装不下的尾巴） ─────── -->
    <NDrawer v-model:show="stageDrawerVisible" :width="520" placement="right">
      <NDrawerContent
        :native-scrollbar="false"
        :title="stageDrawerStage ? `${STAGE_LABEL[stageDrawerStage]} · 全部 ${stageDrawerItems.length} 条` : '全部投递'"
        closable
      >
        <p text-xs text-tertiary mb-4 leading="[1.6]">
          可继续在此处拖拽卡片到主流程的目标节点；点卡片查看详情。
        </p>
        <div v-if="stageDrawerItems.length === 0" class="iv-empty">
          <span text-xs text-tertiary>当前阶段暂无投递</span>
        </div>
        <div v-else flex flex-col gap-2>
          <article
            v-for="item in stageDrawerItems"
            :key="item.id"
            :draggable="!isTerminal(item.stage)"
            class="mini-card mini-card-drawer"
            :class="{ 'is-dragging': dragState?.applicationId === item.id }"
            tabindex="0"
            role="button"
            :style="{ '--accent': STAGE_ACCENT[item.stage] }"
            @dragstart="onCardDragStart($event, item)"
            @dragend="onCardDragEnd"
            @click="openDetail(item.id)"
            @keydown.enter="openDetail(item.id)"
          >
            <span class="mini-avatar" aria-hidden="true">
              {{ avatarChar(item.candidateName) }}
            </span>
            <div class="mini-info">
              <p class="mini-name">
                {{ item.candidateName ?? '匿名' }}
              </p>
              <p v-if="!effectiveJobId" class="mini-job truncate">
                {{ item.jobTitle }}
              </p>
              <p class="mini-meta" font-mono>
                <span v-if="item.yearsExp != null">{{ item.yearsExp }}y · </span>
                {{ formatTime(item.updatedAt) }}
              </p>
            </div>
          </article>
        </div>
        <template #footer>
          <NButton @click="closeStageDrawer">
            关闭
          </NButton>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- ─────── Detail Drawer ─────── -->
    <NDrawer v-model:show="drawerVisible" :width="640" placement="right">
      <NDrawerContent
        :native-scrollbar="false"
        :title="detail?.candidateName ?? '投递详情'"
        closable
      >
        <NSpin :show="detailLoading">
          <template v-if="detail">
            <!-- 顶部信息 -->
            <div mb-6>
              <div flex="~ items-center wrap" gap-2 mb-3>
                <NTag :type="STAGE_TONE[detail.stage]" round :bordered="false">
                  {{ STAGE_LABEL[detail.stage] }}
                </NTag>
                <span text-xs text-tertiary>
                  投递于 {{ formatTime(detail.appliedAt) }}
                </span>
                <span op-40 text-xs>·</span>
                <span text-xs text-tertiary>
                  最近更新 {{ formatTime(detail.updatedAt) }}
                </span>
              </div>

              <p m-0 text-lg font-bold truncate>
                <a
                  :href="jobDetailHref(detail.jobId)"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="job-link"
                  :title="`在新标签页打开「${detail.jobTitle}」岗位详情`"
                >
                  {{ detail.jobTitle }}
                  <svg
                    class="job-link-icon"
                    width="14" height="14" viewBox="0 0 24 24"
                    fill="none" stroke="currentColor" stroke-width="2"
                    stroke-linecap="round" stroke-linejoin="round"
                    aria-hidden="true"
                  >
                    <path d="M14 3h7v7" />
                    <path d="M10 14L21 3" />
                    <path d="M21 14v7H3V3h7" />
                  </svg>
                </a>
              </p>
              <p
                v-if="jobOrgBreadcrumb(detail)"
                m-0 mt-1.5 text-sm text-secondary leading-snug
              >
                <span text-tertiary>所属组织 ·</span>
                {{ jobOrgBreadcrumb(detail) }}
              </p>

              <div mt-3 grid grid-cols-2 gap-3 p-3 rounded-md bg-muted text-xs text-secondary>
                <div flex="~ items-center" gap-1 min-w-0>
                  <span text-tertiary shrink-0>邮箱 ·</span>
                  <span text-primary font-medium ml-1 class="truncate">{{ detail.candidateEmail ?? '—' }}</span>
                  <CopyButton
                    v-if="detail.candidateEmail"
                    :text="detail.candidateEmail"
                    hint="已复制邮箱"
                    tooltip="复制邮箱"
                    size="tiny"
                  />
                </div>
                <div flex="~ items-center" gap-1 min-w-0>
                  <span text-tertiary shrink-0>联系方式 ·</span>
                  <span text-primary font-medium ml-1 font-mono class="truncate">{{ detail.phone ?? '—' }}</span>
                  <CopyButton
                    v-if="detail.phone"
                    :text="detail.phone"
                    hint="已复制电话"
                    tooltip="复制电话"
                    size="tiny"
                  />
                </div>
                <div>
                  <span text-tertiary>工作年限 ·</span>
                  <span text-primary font-medium ml-1>{{ detail.yearsExp ?? '—' }} 年</span>
                </div>
                <div v-if="detail.resumeUrl" col-span-2 class="truncate">
                  <span text-tertiary>简历 ·</span>
                  <a
                    v-if="isResumeFile(detail.resumeUrl)"
                    :href="resumeDownloadUrl(detail.resumeUrl)"
                    target="_blank"
                    rel="noopener noreferrer"
                    ml-1 inline-flex items-center gap-1 text-brand-700 hover:underline font-medium
                  >
                    <span class="resume-pill">PDF</span> 在新标签页打开
                  </a>
                  <a
                    v-else
                    :href="detail.resumeUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    ml-1 text-brand-700 hover:underline font-medium
                  >
                    {{ detail.resumeUrl }}
                  </a>
                </div>
              </div>

              <p
                v-if="detail.stage === 'REJECTED' && detail.rejectReason"
                mt-3 p-3 rounded-md bg-danger-50 border border-danger-500 text-sm text-danger-700
              >
                <strong>未通过原因：</strong>{{ detail.rejectReason }}
              </p>
            </div>

            <!-- 时间线 -->
            <h3 text-10px text-tertiary text-uppercase tracking-widest m="0 b-3" font-bold>
              阶段时间线
            </h3>
            <NTimeline>
              <NTimelineItem
                v-for="log in detail.stageLogs"
                :key="log.id"
                :type="STAGE_TONE[log.toStage]"
                :title="stageLogTitle(log)"
                :time="formatTime(log.operatedAt)"
              >
                <p v-if="log.operatedByName" m-0 text-sm>
                  操作人：<span text-primary>{{ log.operatedByName }}</span>
                  <span v-if="log.operatedByRole" text-tertiary text-xs ml-1>· {{ log.operatedByRole }}</span>
                </p>
                <p v-if="log.note" m="t-1 b-0" text-sm text-secondary leading="[1.65]">
                  {{ log.note }}
                </p>
              </NTimelineItem>
            </NTimeline>

            <!-- ─────────── 面试评价（M4） ─────────── -->
            <div mt-8>
              <header flex="~ items-center justify-between" mb-3>
                <h3 m-0 text-10px text-tertiary text-uppercase tracking-widest font-bold>
                  面试评价 · {{ interviews.length }}
                </h3>
                <NButton
                  v-if="!interviewFormVisible"
                  size="tiny"
                  type="primary"
                  ghost
                  @click="openInterviewCreate"
                >
                  + 添加评价
                </NButton>
              </header>

              <!-- 表单（添加 / 编辑共用） -->
              <div v-if="interviewFormVisible" class="iv-form">
                <p text-xs text-tertiary m="0 b-3" font-semibold uppercase tracking-wider>
                  {{ editingInterview ? '编辑评价' : '添加新评价' }}
                  <span v-if="editingInterview" text-tertiary>· 24h 内可改</span>
                </p>
                <div flex flex-col gap-3>
                  <div>
                    <label text-11px text-tertiary font-medium mb-1 block uppercase tracking-wider>
                      面试轮次
                    </label>
                    <NInput
                      v-model:value="interviewForm.round"
                      placeholder="如：技术一面 / HR 终面"
                      maxlength="100"
                    />
                  </div>
                  <div flex="~ items-center wrap" gap-4>
                    <div>
                      <label text-11px text-tertiary font-medium mb-1 block uppercase tracking-wider>
                        评分
                      </label>
                      <NRate v-model:value="interviewForm.rating" :count="5" />
                    </div>
                    <div flex-1 min-w-180px>
                      <label text-11px text-tertiary font-medium mb-1 block uppercase tracking-wider>
                        结论
                      </label>
                      <NSelect
                        v-model:value="interviewForm.conclusion"
                        :options="conclusionOptions"
                      />
                    </div>
                  </div>
                  <div>
                    <label text-11px text-tertiary font-medium mb-1 block uppercase tracking-wider>
                      优势
                    </label>
                    <NInput
                      v-model:value="interviewForm.strengths"
                      type="textarea"
                      :rows="2"
                      maxlength="2000"
                      show-count
                      placeholder="技术 / 软实力 / 业务亮点"
                    />
                  </div>
                  <div>
                    <label text-11px text-tertiary font-medium mb-1 block uppercase tracking-wider>
                      不足
                    </label>
                    <NInput
                      v-model:value="interviewForm.weaknesses"
                      type="textarea"
                      :rows="2"
                      maxlength="2000"
                      show-count
                      placeholder="待补 / 风险点"
                    />
                  </div>
                  <div>
                    <label text-11px text-tertiary font-medium mb-1 block uppercase tracking-wider>
                      备注（可选）
                    </label>
                    <NInput
                      v-model:value="interviewForm.notes"
                      type="textarea"
                      :rows="2"
                      maxlength="2000"
                    />
                  </div>
                  <div flex="~ items-center justify-end" gap-2>
                    <NButton size="small" @click="cancelInterviewForm">
                      取消
                    </NButton>
                    <NButton
                      size="small"
                      type="primary"
                      :loading="interviewSubmitting"
                      @click="submitInterview"
                    >
                      {{ editingInterview ? '保存修改' : '提交评价' }}
                    </NButton>
                  </div>
                </div>
              </div>

              <!-- 列表 -->
              <div v-if="interviews.length === 0 && !interviewFormVisible" class="iv-empty">
                <span text-xs text-tertiary>暂无面试评价</span>
              </div>
              <div v-else flex flex-col gap-3>
                <article
                  v-for="iv in interviews"
                  :key="iv.id"
                  class="iv-card"
                >
                  <header flex="~ items-center justify-between wrap" gap-2 mb-2>
                    <div flex="~ items-center" gap-2>
                      <span font-bold text-sm text-primary>{{ iv.round }}</span>
                      <NTag
                        :type="CONCLUSION_TONE[iv.conclusion]"
                        round
                        :bordered="false"
                        size="small"
                      >
                        {{ CONCLUSION_LABEL[iv.conclusion] }}
                      </NTag>
                    </div>
                    <div flex="~ items-center" gap-2>
                      <NRate
                        readonly
                        :value="iv.rating ?? 0"
                        :count="5"
                        size="small"
                      />
                      <NButton
                        v-if="iv.editable"
                        size="tiny"
                        quaternary
                        @click="openInterviewEdit(iv)"
                      >
                        编辑
                      </NButton>
                    </div>
                  </header>
                  <p v-if="iv.strengths" m="0 b-1" text-sm text-secondary leading="[1.6]">
                    <strong text-primary>优势 ·</strong> {{ iv.strengths }}
                  </p>
                  <p v-if="iv.weaknesses" m="0 b-1" text-sm text-secondary leading="[1.6]">
                    <strong text-primary>不足 ·</strong> {{ iv.weaknesses }}
                  </p>
                  <p v-if="iv.notes" m="0 b-1" text-sm text-secondary leading="[1.6]">
                    <strong text-primary>备注 ·</strong> {{ iv.notes }}
                  </p>
                  <footer flex="~ items-center justify-between wrap" gap-2 mt-2 text-11px text-tertiary>
                    <span>
                      面试官 · <span text-primary font-medium>{{ iv.interviewerName ?? '—' }}</span>
                      <span v-if="iv.interviewerRole" ml-1>· {{ iv.interviewerRole }}</span>
                    </span>
                    <span font-mono>{{ formatTime(iv.createdAt) }}</span>
                  </footer>
                </article>
              </div>
            </div>
          </template>
        </NSpin>

        <!-- 操作栏：动态推进按钮 -->
        <template #footer>
          <div flex="~ items-center justify-between wrap" gap-2 w-full>
            <span v-if="detail && isTerminal(detail.stage)" text-xs text-tertiary>
              已是终态，无法继续推进
            </span>
            <span v-else-if="detail" text-xs text-tertiary>
              下一步：
            </span>

            <div flex="~ items-center wrap" gap-2>
              <template v-if="detail && detail.allowedTransitions">
                <template
                  v-for="next in detail.allowedTransitions"
                  :key="next"
                >
                  <!-- 关键流转（OFFER / HIRED / REJECTED）走二次确认；中间过程直接推进 -->
                  <NPopconfirm
                    v-if="next === 'OFFER' || next === 'HIRED' || next === 'REJECTED'"
                    :positive-text="`确认 ${STAGE_LABEL[next]}`"
                    negative-text="取消"
                    :positive-button-props="{ type: next === 'REJECTED' ? 'error' : 'primary' }"
                    @positive-click="transitionFromDrawer(next)"
                  >
                    <template #trigger>
                      <NButton
                        size="small"
                        :type="next === 'REJECTED' ? 'error' : 'primary'"
                      >
                        → {{ STAGE_LABEL[next] }}
                      </NButton>
                    </template>
                    即将把 <strong>{{ detail.candidateName }}</strong> 流转到
                    <strong>{{ STAGE_LABEL[next] }}</strong>。
                    <span v-if="next === 'HIRED'">入职后该投递进入终态，不可再变更。</span>
                    <span v-else-if="next === 'REJECTED'">下一步会要求填写拒绝原因。</span>
                  </NPopconfirm>
                  <NButton
                    v-else
                    size="small"
                    type="default"
                    @click="transitionFromDrawer(next)"
                  >
                    → {{ STAGE_LABEL[next] }}
                  </NButton>
                </template>
              </template>
            </div>
          </div>
        </template>
      </NDrawerContent>
    </NDrawer>
  </main>
</template>

<style scoped>
/* ─────────────────────────────────────────────────────────
 *  筛选面板（多维过滤）
 * ───────────────────────────────────────────────────────── */
.filter-panel {
  padding: 14px 16px 12px;
  border-radius: 14px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  transition: background var(--dur-base) var(--ease-out);
}

.filter-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--border-subtle);
}

.filter-panel-title {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--text-primary);
  text-transform: uppercase;
}

.filter-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  background: color-mix(in oklab, var(--brand-500) 14%, transparent);
  color: var(--brand-700);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

/* 投递总数 pill：替代原 header 右上角独立 metric */
.total-pill {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  background: var(--bg-app);
  border: 1px solid var(--border-subtle);
  transition: background var(--dur-base) var(--ease-out);
}
.total-pill.is-loading {
  background: color-mix(in oklab, var(--brand-500) 6%, var(--bg-app));
}
.total-pill-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-tertiary);
}
.total-pill-num {
  font-size: 16px;
  font-weight: 800;
  line-height: 1;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 14px;
}

@media (max-width: 1280px) {
  .filter-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 900px) {
  .filter-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 600px) {
  .filter-grid { grid-template-columns: 1fr; }
}

.filter-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.filter-cell-wide { grid-column: span 2; }
.filter-cell-full { grid-column: 1 / -1; }

@media (max-width: 900px) {
  .filter-cell-wide { grid-column: span 2; }
}
@media (max-width: 600px) {
  .filter-cell-wide,
  .filter-cell-full { grid-column: 1 / -1; }
}

.filter-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-tertiary);
}

/* AutoComplete 下拉项装饰（naive-ui 下拉走 teleport，须用 :global / :deep 才能命中） */
:global(.ac-row) {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
:global(.ac-kind) {
  display: inline-grid;
  place-items: center;
  height: 18px;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  flex-shrink: 0;
}
:global(.ac-kind-kw) {
  background: color-mix(in oklab, var(--brand-500) 12%, transparent);
  color: var(--brand-700);
}
:global(.ac-kind-job) {
  background: color-mix(in oklab, var(--accent-cyan, #06b6d4) 14%, transparent);
  color: #0e7490;
}
:global(.ac-label) {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
  flex: 1;
}
:global(.ac-meta) {
  font-size: 11px;
  color: var(--text-tertiary);
  flex-shrink: 0;
}

/* ─────────────────────────────────────────────────────────
 *  Pipeline Flow 图式看板
 *  - 主流程 7 节点：CSS Grid 响应式（7 → 4 → 2 → 1 列）
 *  - REJECTED 终态：full-width 侧支，与主流程视觉割裂
 *  - 节点高度自适应内容，节点内部不滚动；溢出走 Stage 抽屉
 *  - 桌面 ≥1400 显示节点间 → 连接箭头，呈现"流转感"
 * ───────────────────────────────────────────────────────── */

.flow-board {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ── 主流程网格：响应式断点 ───────────────────────────────── */
.flow-main {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 14px;
  align-items: stretch;
}

@media (max-width: 1400px) {
  .flow-main {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
@media (max-width: 1024px) {
  .flow-main {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 640px) {
  .flow-main {
    grid-template-columns: 1fr;
    gap: 10px;
  }
}

/* ── 节点（适用于主流程 & REJECTED 侧支） ────────────────── */
.flow-node {
  position: relative;
  display: flex;
  flex-direction: column;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: 14px;
  padding: 14px 12px 12px;
  min-height: 220px;
  overflow: hidden;
  transition:
    border-color var(--dur-base) var(--ease-out),
    background var(--dur-base) var(--ease-out),
    transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    opacity var(--dur-base) var(--ease-out);
}

/* 顶部 accent 色条：传达 stage 配色 + "节点感" */
.flow-node::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 3px;
  background: var(--accent);
  opacity: 0.85;
}

/* 桌面 ≥1400 显示节点之间的 → 连接箭头（仅在 7 列布局生效，避免折行错位） */
@media (min-width: 1401px) {
  .flow-main .flow-node:not(:last-child)::after {
    content: '';
    position: absolute;
    top: 50%;
    right: -14px;
    width: 14px;
    height: 2px;
    background: linear-gradient(90deg, var(--accent), transparent);
    z-index: 1;
    pointer-events: none;
  }
  .flow-main .flow-node:not(:last-child) {
    overflow: visible; /* 让箭头能溢出节点外 */
  }
}

/* 拖拽中：合法目标节点高亮 */
.flow-node.is-allowed {
  border-color: var(--accent);
  box-shadow:
    0 0 0 1px var(--accent),
    0 14px 30px -16px var(--accent);
}

/* 从 dashboard 跳来时携带 ?stage= 高亮对应节点 */
.flow-node.is-focus-target {
  border-color: var(--brand-500);
  box-shadow:
    0 0 0 2px rgba(16, 185, 129, 0.18),
    0 12px 32px -12px var(--brand-500);
  animation: focus-pulse 1.6s var(--ease-out) 2;
}
@keyframes focus-pulse {
  0%, 100% { box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.18), 0 12px 32px -12px var(--brand-500); }
  50% { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0.10), 0 16px 40px -10px var(--brand-500); }
}
@media (prefers-reduced-motion: reduce) {
  .flow-node.is-focus-target { animation: none; }
}

/* 拖拽中：非法目标节点变暗 */
.flow-node.is-dim {
  opacity: 0.42;
}

/* hover 进入合法目标节点：上扬 + 实色背景提示 */
.flow-node.is-hover {
  background: color-mix(in oklab, var(--accent) 8%, var(--bg-elevated));
  transform: translateY(-2px);
}

/* ── 节点头部：序号 + 阶段名 + 数量 ─────────────────────── */
.node-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 4px 12px;
  border-bottom: 1px dashed var(--border-subtle);
}

.node-index {
  display: inline-grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: color-mix(in oklab, var(--accent) 14%, var(--bg-app));
  color: var(--accent);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  flex-shrink: 0;
}

.node-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.node-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  letter-spacing: -0.005em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.node-count {
  font-size: 18px;
  font-weight: 800;
  line-height: 1;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}

/* ── 节点内容区：迷你卡片列表（无滚动） ─────────────────── */
.node-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 0 0;
  flex: 1;
}

.node-empty {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  font-size: 11px;
  color: var(--text-tertiary);
  border: 1px dashed var(--border-subtle);
  border-radius: 8px;
  justify-content: center;
}

.empty-dot {
  width: 4px;
  height: 4px;
  border-radius: 999px;
  background: var(--text-tertiary);
  opacity: 0.5;
}

/* ── 迷你卡片：avatar + 名字 + 时间 ─────────────────────── */
.mini-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 8px;
  background: var(--bg-app);
  border: 1px solid transparent;
  cursor: grab;
  outline: none;
  min-width: 0;
  transition:
    transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out),
    background var(--dur-base) var(--ease-out),
    opacity var(--dur-base) var(--ease-out);
}

.mini-card:hover,
.mini-card:focus-visible {
  transform: translate3d(0, -1px, 0);
  border-color: var(--accent);
  background: var(--bg-elevated);
  box-shadow: 0 4px 12px -6px var(--accent);
}

.mini-card:active {
  cursor: grabbing;
}

.mini-card.is-dragging {
  opacity: 0.45;
  transform: scale(0.98);
}

.mini-avatar {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, color-mix(in oklab, var(--accent) 90%, white 10%), var(--accent));
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  flex-shrink: 0;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.15);
}

.mini-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
  flex: 1;
}

.mini-name {
  margin: 0;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mini-job {
  margin: 0;
  font-size: 10.5px;
  color: var(--text-secondary);
  line-height: 1.3;
}

/* ── 投递详情：可跳转岗位详情的标题链接 ───────────────── */
.job-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-primary);
  text-decoration: none;
  border-bottom: 1px dashed transparent;
  transition: color 160ms ease, border-color 160ms ease;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.job-link:hover,
.job-link:focus-visible {
  color: var(--brand-700);
  border-bottom-color: color-mix(in oklab, var(--brand-500) 60%, transparent);
}
.job-link-icon {
  flex-shrink: 0;
  opacity: 0.55;
  transition: opacity 160ms ease, transform 160ms ease;
}
.job-link:hover .job-link-icon,
.job-link:focus-visible .job-link-icon {
  opacity: 1;
  transform: translate3d(1px, -1px, 0);
}

.mini-meta {
  margin: 0;
  font-size: 10px;
  color: var(--text-tertiary);
  letter-spacing: 0.01em;
  line-height: 1.3;
}

/* ── 节点底部：+N 查看全部 ──────────────────────────────── */
.node-foot {
  padding: 8px 0 0;
}

.more-chip {
  width: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 8px;
  background: color-mix(in oklab, var(--accent) 6%, transparent);
  border: 1px dashed color-mix(in oklab, var(--accent) 45%, transparent);
  color: var(--accent);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;
  cursor: pointer;
  font-family: inherit;
  transition:
    background var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out),
    transform var(--dur-base) var(--ease-out);
}

.more-chip:hover,
.more-chip:focus-visible {
  background: color-mix(in oklab, var(--accent) 14%, transparent);
  border-style: solid;
  border-color: var(--accent);
  transform: translateY(-1px);
  outline: none;
}

/* ─────────────────────────────────────────────────────────
 *  REJECTED 侧支：与主流程视觉割裂
 *  - full-width 横铺
 *  - 顶部 danger 色条 + 头部"终态"小标
 *  - 内容区横向 grid 平铺迷你卡片
 * ───────────────────────────────────────────────────────── */
.flow-side {
  position: relative;
  min-height: auto;
  background:
    linear-gradient(180deg, color-mix(in oklab, var(--danger-500) 4%, transparent) 0%, transparent 60%),
    var(--bg-elevated);
  border-color: color-mix(in oklab, var(--danger-500) 25%, var(--border-subtle));
}

.side-header {
  border-bottom-color: color-mix(in oklab, var(--danger-500) 22%, var(--border-subtle));
}

.side-tag {
  margin-left: auto;
  padding: 3px 9px;
  border-radius: 999px;
  background: color-mix(in oklab, var(--danger-500) 12%, transparent);
  color: var(--danger-700, var(--danger-500));
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  white-space: nowrap;
}

.side-body {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px;
}

@media (max-width: 1024px) {
  .side-body {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 640px) {
  .side-body {
    grid-template-columns: 1fr;
  }
}

.mini-card-side {
  background: color-mix(in oklab, var(--danger-500) 4%, var(--bg-app));
  cursor: pointer;
}
.mini-card-side:hover,
.mini-card-side:focus-visible {
  border-color: var(--danger-500);
  box-shadow: 0 4px 12px -6px var(--danger-500);
  background: var(--bg-elevated);
}
.mini-card-side .mini-avatar {
  background: linear-gradient(135deg, color-mix(in oklab, var(--danger-500) 80%, white 20%), var(--danger-500));
}

/* ── Stage 全列抽屉里的迷你卡片：更宽松的横向布局 ───────── */
.mini-card-drawer {
  padding: 10px 12px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
}
.mini-card-drawer .mini-avatar {
  width: 36px;
  height: 36px;
  font-size: 14px;
  border-radius: 10px;
}
.mini-card-drawer .mini-name {
  font-size: 13.5px;
}
.mini-card-drawer:hover,
.mini-card-drawer:focus-visible {
  border-color: var(--accent);
  box-shadow: 0 6px 16px -8px var(--accent);
}

/* ─────── 浮动拒绝投放区 ─────── */
.reject-dropzone {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1500; /* 高于看板内容、低于 NMessage（>= 4000） */
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  min-width: 240px;
  border-radius: 14px;
  background: color-mix(in oklab, var(--danger-500) 10%, var(--bg-elevated));
  border: 2px dashed color-mix(in oklab, var(--danger-500) 55%, transparent);
  color: var(--danger-700, var(--danger-500));
  box-shadow: 0 16px 40px -16px color-mix(in oklab, var(--danger-500) 60%, transparent);
  transition:
    background var(--dur-base) var(--ease-out),
    border-color var(--dur-base) var(--ease-out),
    transform var(--dur-base) var(--ease-out),
    box-shadow var(--dur-base) var(--ease-out);
  /* 入场柔和动画，避免突兀 */
  animation: rejectDropzoneIn 220ms var(--ease-out);
}

.reject-dropzone.is-hover {
  background: color-mix(in oklab, var(--danger-500) 22%, var(--bg-elevated));
  border-color: var(--danger-500);
  border-style: solid;
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 22px 48px -14px color-mix(in oklab, var(--danger-500) 70%, transparent);
}

.reject-dropzone-icon {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: var(--danger-500);
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}

.reject-dropzone-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
  font-size: 13px;
}

.reject-dropzone-text strong {
  font-weight: 700;
  font-size: 14px;
  color: var(--danger-700, var(--danger-500));
}

.reject-dropzone-text span {
  font-size: 11px;
  opacity: 0.78;
}

@keyframes rejectDropzoneIn {
  from { opacity: 0; transform: translateY(8px) scale(0.96); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* 小屏：缩短宽度 + 贴底居中，避开列横滚条 */
@media (max-width: 640px) {
  .reject-dropzone {
    right: 12px;
    bottom: 12px;
    left: 12px;
    min-width: 0;
    justify-content: center;
  }
}

/* ─────── 面试评价（M4） ─────── */
.resume-pill {
  display: inline-grid;
  place-items: center;
  padding: 1px 6px;
  border-radius: 4px;
  background: color-mix(in oklab, var(--success-500) 14%, transparent);
  color: var(--success-700, var(--success-500));
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.5px;
}

.iv-form {
  padding: 14px 16px;
  border: 1px solid var(--border-strong);
  border-radius: 10px;
  background: color-mix(in oklab, var(--brand-500) 4%, var(--bg-elevated));
  margin-bottom: 12px;
}

.iv-empty {
  padding: 18px;
  text-align: center;
  border: 1px dashed var(--border-subtle);
  border-radius: 8px;
}

.iv-card {
  padding: 12px 14px;
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  background: var(--bg-elevated);
  transition: border-color var(--dur-base) var(--ease-out);
}
.iv-card:hover {
  border-color: var(--border-strong);
}

@media (prefers-reduced-motion: reduce) {
  .flow-node,
  .mini-card,
  .more-chip,
  .reject-dropzone,
  .iv-card {
    transition: none !important;
    animation: none !important;
  }
  .flow-node.is-hover,
  .mini-card:hover,
  .more-chip:hover,
  .reject-dropzone.is-hover {
    transform: none;
  }
}
</style>
