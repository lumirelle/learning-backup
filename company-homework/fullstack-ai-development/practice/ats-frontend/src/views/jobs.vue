<script setup lang="ts">
import type { UploadVO } from '@/api/files'
import type {
  JobDetailVO,
  JobLevel,
  JobListItemVO,
  JobStatus,
  JobWorkType,
  TagCategory,
  TagVO,
} from '@/api/jobs'
import {
  NButton,
  NDrawer,
  NDrawerContent,
  NEmpty,
  NInput,
  NInputNumber,
  NModal,
  NPagination,
  NSkeleton,
  NSpin,
  NTag,
  useMessage,
} from 'naive-ui'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { applicationsApi } from '@/api/applications'
import { filesApi, FileValidationError, UPLOAD_LIMITS } from '@/api/files'
import {
  jobsApi,
  LEVEL_LABEL,
  STATUS_LABEL,
  TAG_CATEGORY_LABEL,
  tagsApi,
  WORK_TYPE_LABEL,
} from '@/api/jobs'
import { BizError } from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import { renderMarkdown } from '@/utils/markdown'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const message = useMessage()

// ────────────────────────── 过滤状态 ──────────────────────────

const WORK_TYPE_OPTIONS = (Object.keys(WORK_TYPE_LABEL) as JobWorkType[])
  .map(v => ({ label: WORK_TYPE_LABEL[v], value: v }))
const LEVEL_OPTIONS = (Object.keys(LEVEL_LABEL) as JobLevel[])
  .map(v => ({ label: LEVEL_LABEL[v], value: v }))

const filter = reactive({
  keyword: '',
  workType: [] as JobWorkType[],
  level: [] as JobLevel[],
  tagSlugs: [] as string[],
})

const page = ref(1)
const pageSize = ref(12)

// ────────────────────────── 列表数据 ──────────────────────────

const loading = ref(false)
const items = ref<JobListItemVO[]>([])
const total = ref(0)

async function fetchList() {
  loading.value = true
  try {
    const res = await jobsApi.list({
      keyword: filter.keyword.trim() || undefined,
      workType: filter.workType.length ? filter.workType : undefined,
      level: filter.level.length ? filter.level : undefined,
      tagSlugs: filter.tagSlugs.length ? filter.tagSlugs : undefined,
      page: page.value,
      size: pageSize.value,
      sortBy: 'publishedAt',
      sortOrder: 'desc',
    })
    items.value = res.items
    total.value = res.total
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

function resetAndFetch() {
  page.value = 1
  fetchList()
}

function clearFilters() {
  filter.keyword = ''
  filter.workType = []
  filter.level = []
  filter.tagSlugs = []
  resetAndFetch()
}

const hasActiveFilter = computed(
  () =>
    filter.keyword.trim().length > 0
    || filter.workType.length > 0
    || filter.level.length > 0
    || filter.tagSlugs.length > 0,
)

// ────────────────────────── 标签字典 ──────────────────────────

const tags = ref<TagVO[]>([])
async function loadTags() {
  try {
    tags.value = await tagsApi.listAll()
  }
  catch (e) {
    console.warn('load tags failed', e)
  }
}

const tagsByCategory = computed(() => {
  const groups = new Map<TagCategory, TagVO[]>()
  tags.value.forEach((t) => {
    if (!groups.has(t.category))
      groups.set(t.category, [])
    groups.get(t.category)!.push(t)
  })
  return Array.from(groups.entries())
})

function toggleTag(slug: string) {
  const idx = filter.tagSlugs.indexOf(slug)
  if (idx >= 0)
    filter.tagSlugs.splice(idx, 1)
  else filter.tagSlugs.push(slug)
  resetAndFetch()
}

const tagPanelOpen = ref(false)

// ────────────────────────── Detail Drawer ──────────────────────────

const drawerVisible = ref(false)
const detail = ref<JobDetailVO | null>(null)
const detailLoading = ref(false)

async function openDetail(id: number) {
  drawerVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await jobsApi.detail(id)
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

const applyDialogVisible = ref(false)
const applyForm = reactive({
  yearsExp: undefined as number | undefined,
  phone: '',
  resumeUrl: '',
})
const applying = ref(false)

// 简历上传：本地预览 + 异步上传 → 拿到 URL 写入 applyForm.resumeUrl
const resumeFile = ref<{ name: string, size: number, url: string } | null>(null)
const uploadingResume = ref(false)
const resumeInputRef = ref<HTMLInputElement | null>(null)

function openApplyDialog() {
  if (!detail.value)
    return
  if (!auth.isLoggedIn) {
    message.warning('请先登录后再投递')
    router.push({ name: 'Login', query: { redirect: '/jobs' } })
    return
  }
  if (!auth.isCandidate) {
    message.info('当前账号不是候选人，无法投递')
    return
  }
  applyForm.yearsExp = undefined
  applyForm.phone = ''
  applyForm.resumeUrl = ''
  resumeFile.value = null
  applyDialogVisible.value = true
}

function pickResume() {
  resumeInputRef.value?.click()
}

async function onResumeChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  // 重置 input value 让用户能重新选同一个文件
  input.value = ''
  if (!file)
    return
  uploadingResume.value = true
  try {
    const vo: UploadVO = await filesApi.upload(file, 'RESUME')
    resumeFile.value = { name: file.name, size: vo.size, url: vo.url }
    applyForm.resumeUrl = vo.url
    message.success('简历上传成功')
  }
  catch (err) {
    if (err instanceof FileValidationError)
      message.warning(err.message)
    else if (err instanceof BizError)
      message.error(err.message)
    else message.error('上传失败，请重试')
  }
  finally {
    uploadingResume.value = false
  }
}

function removeResume() {
  resumeFile.value = null
  applyForm.resumeUrl = ''
}

function formatFileSize(bytes: number) {
  if (bytes < 1024)
    return `${bytes} B`
  if (bytes < 1024 * 1024)
    return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

async function submitApply() {
  if (!detail.value)
    return
  applying.value = true
  try {
    await applicationsApi.apply({
      jobId: detail.value.id,
      yearsExp: applyForm.yearsExp,
      phone: applyForm.phone || undefined,
      resumeUrl: applyForm.resumeUrl || undefined,
    })
    message.success('投递成功，HR 会尽快查看你的简历 ✨')
    applyDialogVisible.value = false
    drawerVisible.value = false
    setTimeout(() => router.push('/me/applications'), 400)
  }
  catch (e) {
    if (e instanceof BizError)
      message.error(e.message)
    else throw e
  }
  finally {
    applying.value = false
  }
}

// ────────────────────────── helpers ──────────────────────────

const STATUS_TAG_TYPE: Record<JobStatus, 'success' | 'warning' | 'error' | 'info' | 'default'> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  PAUSED: 'warning',
  CLOSED: 'error',
  ARCHIVED: 'info',
}

const TAG_CATEGORY_ACCENT: Record<TagCategory, string> = {
  TECH: 'var(--accent-emerald)',
  SOFT: 'var(--accent-teal)',
  CERT: 'var(--accent-amber)',
  LANG: 'var(--accent-cyan)',
  DOMAIN: 'var(--accent-lime)',
}

function formatPublishedAt(iso: string | null) {
  if (!iso)
    return '近期'
  const d = new Date(iso)
  const diffH = (Date.now() - d.getTime()) / 36e5
  if (diffH < 1)
    return `${Math.max(1, Math.floor(diffH * 60))} 分钟前`
  if (diffH < 24)
    return `${Math.floor(diffH)} 小时前`
  if (diffH < 24 * 7)
    return `${Math.floor(diffH / 24)} 天前`
  if (diffH < 24 * 30)
    return `${Math.floor(diffH / (24 * 7))} 周前`
  return d.toISOString().slice(0, 10)
}

// ────────────────────────── lifecycle ──────────────────────────

watch(
  () => [filter.workType, filter.level],
  () => resetAndFetch(),
  { deep: true },
)

onMounted(async () => {
  loadTags()
  await fetchList()
  // 支持 /jobs?jobId=xxx 直链 / 跨页跳转 ——
  // 例如从「我的投递」点"查看岗位"过来时自动打开 drawer
  const jobIdParam = route.query.jobId
  if (typeof jobIdParam === 'string' && /^\d+$/.test(jobIdParam)) {
    openDetail(Number(jobIdParam))
  }
})

// 字数统计：filter chip 上的"已选 N"
const tagSelectedCount = computed(() => filter.tagSlugs.length)
</script>

<template>
  <main
    min-h-screen bg-app pt-60px
  >
    <!-- ──────────────── Hero ──────────────── -->
    <section relative max-w-1200px mx-auto p="t-12 b-6 x-6" overflow-hidden>
      <!-- 背景 aurora 装饰，仅在大屏显示 -->
      <div
        aria-hidden="true"
        pointer-events-none absolute inset-0 z--1 op-60
        :style="{
          background: 'radial-gradient(60% 40% at 80% 0%, rgba(110,231,183,.18), transparent 60%), radial-gradient(50% 35% at 10% 30%, rgba(20,184,166,.12), transparent 65%)',
        }"
      />

      <p kicker mb-3>
        Job Market · 岗位市场
      </p>
      <h1 m-0 class="text-[clamp(36px,5vw,60px)]" text-gray-900 font-black tracking="[-0.04em]" leading="[1.05]">
        遇见<span text-gradient>下一份机会</span>
      </h1>
      <p mt-3 text-lg text-secondary max-w-640px leading="[1.6]">
        共 <span text-primary font-semibold>{{ total }}</span> 个公开岗位 ·
        覆盖技术研发、产品设计、HR Tech 等领域 · 支持全文搜索 + 标签精筛
      </p>
    </section>

    <!-- ──────────────── Search + Quick Filter ──────────────── -->
    <section sticky top-60px z-20 bg-app backdrop-blur-md border="b subtle">
      <div max-w-1200px mx-auto p="y-4 x-6" flex="~ items-center wrap" gap-3>
        <!-- 搜索框 -->
        <div
          flex="~ items-center"
          flex-1 min-w-220px max-w-480px
          gap-2
          p="y-2.5 x-4"
          rounded-full
          bg-elevated
          border="~ subtle"
          shadow-sm
          transition="[border-color,box-shadow]"
          duration-260
          ease-out
          focus-within:border-brand-300
          focus-within:shadow-glow-mint
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" op-60 shrink-0>
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8" />
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
          </svg>
          <input
            v-model="filter.keyword"
            type="text"
            placeholder="搜索岗位名称 / 职责描述…"
            flex-1 outline-none border-none bg-transparent text-sm text-primary
            @keydown.enter="resetAndFetch"
          >
          <button
            v-if="filter.keyword"
            op-60 hover:op-100 transition-opacity cursor-pointer bg-transparent border-none text-tertiary
            type="button"
            aria-label="清空"
            @click="filter.keyword = ''; resetAndFetch()"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M6 6 L18 18 M18 6 L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
          </button>
          <span kbd-hint max-sm="hidden">⏎</span>
        </div>

        <!-- 工作类型 chip -->
        <div flex="~ items-center wrap" gap-1>
          <button
            v-for="opt in WORK_TYPE_OPTIONS"
            :key="opt.value"
            type="button"
            :class="filter.workType.includes(opt.value)
              ? 'bg-brand-500 text-white border-brand-500 shadow-glow-mint'
              : 'bg-elevated text-secondary border-subtle hover:(border-brand-300 text-primary)'"
            px-3 py-1 text-xs font-medium rounded-full border cursor-pointer transition-all duration-260 ease-out
            @click="filter.workType.includes(opt.value)
              ? filter.workType = filter.workType.filter(v => v !== opt.value)
              : filter.workType.push(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>

        <!-- 级别 chip -->
        <div flex="~ items-center wrap" gap-1 max-md="hidden">
          <button
            v-for="opt in LEVEL_OPTIONS"
            :key="opt.value"
            type="button"
            :class="filter.level.includes(opt.value)
              ? 'bg-accent-teal text-white border-accent-teal shadow-glow-teal'
              : 'bg-elevated text-secondary border-subtle hover:(border-accent-teal/40 text-primary)'"
            px-3 py-1 text-xs font-medium rounded-full border cursor-pointer transition-all duration-260 ease-out
            @click="filter.level.includes(opt.value)
              ? filter.level = filter.level.filter(v => v !== opt.value)
              : filter.level.push(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>

        <!-- 标签按钮（打开下方面板） -->
        <button
          type="button"
          px-3 py-1.5 text-xs font-medium rounded-full border cursor-pointer transition-all duration-260 ease-out flex items-center gap-1.5
          :class="tagPanelOpen || tagSelectedCount > 0
            ? 'bg-accent-emerald/10 text-brand-700 border-brand-300'
            : 'bg-elevated text-secondary border-subtle hover:(border-brand-300 text-primary)'"
          @click="tagPanelOpen = !tagPanelOpen"
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
            <path d="M3 7 H21 M6 12 H18 M9 17 H15" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
          标签
          <span v-if="tagSelectedCount" class="ml-0.5 inline-flex items-center justify-center min-w-16px h-16px px-1 rounded-full bg-brand-500 text-white text-10px font-bold">
            {{ tagSelectedCount }}
          </span>
        </button>

        <!-- 清空 -->
        <button
          v-if="hasActiveFilter"
          type="button"
          text-xs text-tertiary hover:text-danger-700 transition-colors cursor-pointer bg-transparent border-none px-2 py-1
          @click="clearFilters"
        >
          清空筛选
        </button>
      </div>

      <!-- 标签下拉面板 -->
      <Transition name="tag-panel">
        <div v-if="tagPanelOpen" max-w-1200px mx-auto p="b-4 x-6">
          <div card-base p-4>
            <div v-for="[cat, list] in tagsByCategory" :key="cat" mb-3 last:mb-0>
              <p text-11px uppercase tracking-widest text-tertiary mb-2 font-semibold>
                {{ TAG_CATEGORY_LABEL[cat] }}
              </p>
              <div flex="~ wrap" gap-1.5>
                <button
                  v-for="t in list"
                  :key="t.id"
                  type="button"
                  :class="filter.tagSlugs.includes(t.slug)
                    ? 'bg-brand-500 text-white border-brand-500'
                    : 'bg-app text-secondary border-subtle hover:(border-brand-300 text-primary bg-elevated)'"
                  px-2.5 py-1 text-xs rounded-md border cursor-pointer transition-all duration-260 ease-out
                  @click="toggleTag(t.slug)"
                >
                  {{ t.name }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </section>

    <!-- ──────────────── 列表 ──────────────── -->
    <section max-w-1200px mx-auto p="y-6 x-6">
      <NSpin :show="loading">
        <!-- 空状态 -->
        <div v-if="!loading && items.length === 0" py-16>
          <NEmpty :description="hasActiveFilter ? '没有符合条件的岗位，试试放宽筛选？' : '暂无公开岗位'">
            <template v-if="hasActiveFilter" #extra>
              <NButton size="small" @click="clearFilters">
                清空筛选
              </NButton>
            </template>
          </NEmpty>
        </div>

        <!-- 骨架屏（首次加载时） -->
        <div
          v-else-if="loading && items.length === 0"
          grid
          gap-4
          grid-cols="[repeat(auto-fill,minmax(320px,1fr))]"
        >
          <div v-for="i in 6" :key="i" card-base p-5>
            <NSkeleton text :repeat="2" />
            <NSkeleton text style="width: 60%" :repeat="1" />
            <NSkeleton text style="width: 40%" :repeat="1" />
          </div>
        </div>

        <!-- 卡片网格 -->
        <div
          v-else
          grid
          gap-4
          grid-cols="[repeat(auto-fill,minmax(320px,1fr))]"
        >
          <article
            v-for="item in items"
            :key="item.id"
            class="job-card group"
            tabindex="0"
            role="button"
            :aria-label="`查看 ${item.title} 详情`"
            @click="openDetail(item.id)"
            @keydown.enter="openDetail(item.id)"
          >
            <!-- 顶部进度条（按状态） -->
            <span
              aria-hidden="true"
              class="job-card-bar"
              :style="{
                background: item.status === 'PUBLISHED'
                  ? 'linear-gradient(90deg, var(--accent-mint), var(--accent-emerald), var(--accent-teal))'
                  : item.status === 'PAUSED'
                    ? 'linear-gradient(90deg, var(--accent-amber), var(--warning-500))'
                    : 'var(--gray-300)',
              }"
            />

            <header flex="~ items-start justify-between" gap-3 mb-3>
              <h3 m-0 font-bold text-lg text-primary leading="tight" line-clamp-2 flex-1>
                {{ item.title }}
              </h3>
              <NTag
                v-if="item.status !== 'PUBLISHED'"
                :type="STATUS_TAG_TYPE[item.status]"
                size="small"
                round
                :bordered="false"
                shrink-0
              >
                {{ STATUS_LABEL[item.status] }}
              </NTag>
            </header>

            <!-- 元信息 -->
            <div flex="~ items-center wrap" gap-2 mb-3 text-xs text-secondary>
              <span flex="~ items-center" gap-1>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <rect x="4" y="6" width="16" height="14" rx="2" stroke="currentColor" stroke-width="1.6" />
                  <path d="M9 6V4 a2 2 0 0 1 2-2 h2 a2 2 0 0 1 2 2 v2" stroke="currentColor" stroke-width="1.6" />
                </svg>
                {{ WORK_TYPE_LABEL[item.workType] }}
              </span>
              <span op-40>·</span>
              <span flex="~ items-center" gap-1>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <path d="M4 18 L10 12 L14 16 L20 8 M20 8 H15 M20 8 V13" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                {{ LEVEL_LABEL[item.level] }}
              </span>
              <span op-40>·</span>
              <span font-mono font-semibold text-brand-700>
                {{ item.salaryRange }}
              </span>
            </div>

            <!-- 标签条 -->
            <div flex="~ wrap" gap-1.5 mb-4 min-h-24px>
              <span
                v-for="t in item.tags.slice(0, 4)"
                :key="t.id"
                px-2 py-0.5 text-11px font-medium rounded border
                :style="{
                  color: TAG_CATEGORY_ACCENT[t.category],
                  borderColor: 'var(--border-subtle)',
                  background: 'var(--bg-app)',
                }"
              >
                {{ t.name }}
              </span>
              <span v-if="item.tags.length > 4" text-11px text-tertiary self-center font-mono>
                +{{ item.tags.length - 4 }}
              </span>
            </div>

            <!-- 底部 footer -->
            <div flex="~ items-center justify-between" text-xs text-tertiary>
              <span truncate>
                {{ item.departmentName ?? '未设部门' }}<template v-if="item.location"> · {{ item.location }}</template>
              </span>
              <span font-mono shrink-0>
                <span>{{ formatPublishedAt(item.publishedAt) }}</span>
                <span mx-1.5 op-50>·</span>
                <span>{{ item.viewCount }} 浏览</span>
              </span>
            </div>

            <!-- hover arrow -->
            <span aria-hidden="true" class="job-card-arrow">→</span>
          </article>
        </div>
      </NSpin>

      <!-- 分页 -->
      <div v-if="total > pageSize" mt-8 flex="~ justify-center">
        <NPagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :item-count="total"
          :page-sizes="[12, 24, 48]"
          show-size-picker
          @update:page="fetchList"
          @update:page-size="(s: number) => { pageSize = s; page = 1; fetchList() }"
        />
      </div>
    </section>

    <!-- ──────────────── Detail Drawer ──────────────── -->
    <NDrawer v-model:show="drawerVisible" :width="720" placement="right">
      <NDrawerContent
        :native-scrollbar="false"
        :title="detail?.title ?? '岗位详情'"
        closable
      >
        <NSpin :show="detailLoading">
          <template v-if="detail">
            <!-- 头部信息 -->
            <div mb-6>
              <div flex="~ items-center wrap" gap-2 mb-3>
                <NTag :type="STATUS_TAG_TYPE[detail.status]" size="small" round :bordered="false">
                  {{ STATUS_LABEL[detail.status] }}
                </NTag>
                <span text-xs text-tertiary>
                  发布于 {{ formatPublishedAt(detail.publishedAt) }}
                </span>
                <span op-40 text-xs>·</span>
                <span text-xs text-tertiary text-font-mono>
                  {{ detail.viewCount }} 浏览
                </span>
              </div>

              <!-- 关键属性 -->
              <div grid grid-cols-2 gap-3 mb-4>
                <div>
                  <p text-10px text-tertiary text-uppercase text-tracking-widest m="0 b-1" font-semibold>
                    薪资范围
                  </p>
                  <p m-0 text-2xl text-font-mono text-primary font-bold>
                    {{ detail.salaryRange }}
                  </p>
                </div>
                <div>
                  <p text-10px text-tertiary text-uppercase text-tracking-widest m="0 b-1" font-semibold>
                    招聘人数
                  </p>
                  <p m-0 text-2xl text-primary font-bold>
                    {{ detail.headcount }} <span text-sm text-tertiary>人</span>
                  </p>
                </div>
              </div>

              <div grid grid-cols-3 gap-3 p-3 rounded-md bg-muted text-xs text-secondary>
                <div>
                  <span text-tertiary>工作类型 ·</span>
                  <span text-primary font-medium ml-1>{{ WORK_TYPE_LABEL[detail.workType] }}</span>
                </div>
                <div>
                  <span text-tertiary>级别 ·</span>
                  <span text-primary font-medium ml-1>{{ LEVEL_LABEL[detail.level] }}</span>
                </div>
                <div>
                  <span text-tertiary>地点 ·</span>
                  <span text-primary font-medium ml-1>{{ detail.location ?? '不限' }}</span>
                </div>
                <div>
                  <span text-tertiary>部门 ·</span>
                  <span text-primary font-medium ml-1>{{ detail.departmentName ?? '—' }}</span>
                </div>
                <div col-span-2>
                  <span text-tertiary>发布人 ·</span>
                  <span text-primary font-medium ml-1>{{ detail.createdByName ?? '匿名' }}</span>
                </div>
              </div>
            </div>

            <!-- 标签 -->
            <div v-if="detail.tags.length" mb-6>
              <p text-10px text-tertiary text-uppercase text-tracking-widest m="0 b-2" font-semibold>
                技能 / 领域
              </p>
              <div flex="~ wrap" gap-2>
                <span
                  v-for="t in detail.tags"
                  :key="t.id"
                  px-2.5 py-1 text-xs font-medium rounded-md border
                  :style="{
                    color: TAG_CATEGORY_ACCENT[t.category],
                    borderColor: 'var(--border-subtle)',
                    background: 'var(--bg-app)',
                  }"
                >
                  {{ t.name }}
                </span>
              </div>
            </div>

            <!-- 描述（Markdown 渲染） -->
            <div mb-8>
              <p text-10px text-tertiary text-uppercase text-tracking-widest m="0 b-3" font-semibold>
                岗位描述
              </p>
              <div
                v-if="detail.description"
                class="job-md"
                v-html="renderMarkdown(detail.description)"
              />
              <p v-else text-tertiary text-sm>
                岗位发布人尚未填写描述。
              </p>
            </div>
          </template>
        </NSpin>

        <!-- 底部 CTA -->
        <template #footer>
          <div flex="~ items-center justify-between" w-full>
            <div text-xs text-tertiary>
              <template v-if="detail?.status === 'PUBLISHED'">
                <span inline-block w-2 h-2 rounded-full bg-success-500 mr-1.5 animate-pulse-ring />
                正在招聘
              </template>
              <template v-else-if="detail?.status === 'PAUSED'">
                ⏸ 暂停收件中
              </template>
              <template v-else-if="detail?.status === 'CLOSED'">
                ✓ 该岗位已关闭
              </template>
            </div>

            <div flex="~ items-center" gap-2>
              <NButton @click="drawerVisible = false">
                关闭
              </NButton>
              <!-- HR/Admin 看到「管理岗位」直达后台编辑该岗位 -->
              <NButton
                v-if="(auth.isHr || auth.isAdmin) && detail"
                type="default"
                @click="router.push({ path: '/hr/jobs', query: { editJobId: String(detail.id) } })"
              >
                到管理台编辑 →
              </NButton>
              <!-- 候选人/未登录 看到「立即投递」 -->
              <NButton
                v-else-if="detail?.status === 'PUBLISHED'"
                type="primary"
                @click="openApplyDialog"
              >
                立即投递
              </NButton>
            </div>
          </div>
        </template>
      </NDrawerContent>
    </NDrawer>

    <!-- ──────────────── Apply Modal ──────────────── -->
    <NModal
      v-model:show="applyDialogVisible"
      preset="card"
      style="max-width: 520px"
      :title="`投递「${detail?.title ?? ''}」`"
      :bordered="false"
    >
      <div flex="~ col" gap-4>
        <p text-sm text-secondary m-0 leading="[1.65]">
          填写简历信息后我们会把投递发送给该岗位 HR。HR 之后通过看板推进面试阶段，你可以在「我的投递」页面追踪进度。
        </p>

        <div>
          <label text-xs text-tertiary font-medium mb-1.5 block uppercase tracking-wider>
            工作年限
          </label>
          <NInputNumber
            v-model:value="applyForm.yearsExp"
            :min="0"
            :max="40"
            placeholder="如 3"
            w-full
          />
        </div>
        <div>
          <label text-xs text-tertiary font-medium mb-1.5 block uppercase tracking-wider>
            联系方式（可选）
          </label>
          <NInput v-model:value="applyForm.phone" placeholder="HR 联系你的电话 / 微信" />
        </div>
        <div>
          <label text-xs text-tertiary font-medium mb-1.5 block uppercase tracking-wider>
            简历 PDF（可选 · ≤5MB）
          </label>
          <!-- 隐藏 input，自定义按钮触发，便于完全控制 UI 与 axios 上传链 -->
          <input
            ref="resumeInputRef"
            type="file"
            :accept="UPLOAD_LIMITS.RESUME.accept"
            hidden
            @change="onResumeChange"
          >
          <div v-if="!resumeFile" class="resume-uploader" :class="{ 'is-loading': uploadingResume }" @click="pickResume">
            <div text-2xl class="resume-uploader-icon">
              ↑
            </div>
            <p m="0 b-1" text-sm text-primary font-semibold>
              {{ uploadingResume ? '上传中…' : '点击选择 PDF 文件' }}
            </p>
            <p m="0" text-xs text-tertiary>
              仅 PDF · 最大 5MB · 不上传也能投递，HR 会主动联系
            </p>
          </div>
          <div v-else class="resume-attached">
            <div class="resume-attached-icon" aria-hidden="true">
              PDF
            </div>
            <div flex-1 min-w-0>
              <p m="0" text-sm text-primary font-semibold truncate>
                {{ resumeFile.name }}
              </p>
              <p m="t-0.5 b-0" text-11px text-tertiary text-font-mono>
                {{ formatFileSize(resumeFile.size) }} · 已上传
              </p>
            </div>
            <NButton size="tiny" quaternary @click="removeResume">
              移除
            </NButton>
          </div>
        </div>
      </div>

      <template #footer>
        <div flex="~ items-center justify-end" gap-2>
          <NButton @click="applyDialogVisible = false">
            取消
          </NButton>
          <NButton type="primary" :loading="applying" @click="submitApply">
            确认投递
          </NButton>
        </div>
      </template>
    </NModal>
  </main>
</template>

<style scoped>
/* job-card：保留几条 scoped 是为了 ::before 进度条 + group hover 联动 + line-clamp。
 * 其余视觉一律走 token / shortcut。 */

.job-card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 20px;
  border-radius: 12px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  overflow: hidden;
  outline: none;
  transition:
    transform var(--dur-slow) var(--ease-out),
    box-shadow var(--dur-slow) var(--ease-out),
    border-color var(--dur-slow) var(--ease-out);
}

.job-card:hover,
.job-card:focus-visible {
  transform: translate3d(0, -3px, 0);
  border-color: transparent;
  box-shadow: var(--shadow-lg), 0 0 0 1px var(--brand-300);
}

.job-card-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  transform: scaleX(0.18);
  transform-origin: left;
  transition: transform var(--dur-slow) var(--ease-out);
}

.job-card:hover .job-card-bar,
.job-card:focus-visible .job-card-bar {
  transform: scaleX(1);
}

.job-card-arrow {
  position: absolute;
  bottom: 16px;
  right: 18px;
  font-size: 18px;
  color: var(--brand-500);
  opacity: 0;
  transform: translate3d(-6px, 0, 0);
  transition:
    opacity var(--dur-base) var(--ease-out),
    transform var(--dur-base) var(--ease-out);
}

.job-card:hover .job-card-arrow,
.job-card:focus-visible .job-card-arrow {
  opacity: 1;
  transform: translate3d(0, 0, 0);
}

/* 多行截断 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  overflow: hidden;
}

/* Markdown body 样式 */
.job-md :deep(h2),
.job-md :deep(h3),
.job-md :deep(h4) {
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--text-primary);
  margin: 18px 0 8px;
}
.job-md :deep(h2) { font-size: 18px; }
.job-md :deep(h3) { font-size: 15px; color: var(--brand-700); }
.job-md :deep(h4) { font-size: 13px; }
.job-md :deep(p) {
  margin: 8px 0;
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 14px;
}
.job-md :deep(ul) {
  margin: 8px 0;
  padding-left: 22px;
}
.job-md :deep(li) {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 14px;
  margin: 4px 0;
}
.job-md :deep(li)::marker {
  color: var(--brand-500);
}
.job-md :deep(code) {
  font-family: var(--font-mono);
  font-size: 12.5px;
  padding: 1.5px 6px;
  border-radius: 4px;
  background: var(--bg-muted);
  color: var(--brand-700);
}

/* tag-panel 折叠动画 */
.tag-panel-enter-active,
.tag-panel-leave-active {
  transition:
    opacity 200ms var(--ease-page-in),
    transform 240ms var(--ease-page-in),
    max-height 240ms var(--ease-page-in);
  overflow: hidden;
}
.tag-panel-enter-from,
.tag-panel-leave-to {
  opacity: 0;
  transform: translate3d(0, -8px, 0);
  max-height: 0;
}
.tag-panel-enter-to,
.tag-panel-leave-from {
  opacity: 1;
  max-height: 600px;
}

/* ─────────────── 简历上传 ─────────────── */
.resume-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  border: 1.5px dashed var(--border-strong);
  border-radius: 10px;
  cursor: pointer;
  background: color-mix(in oklab, var(--brand-500) 3%, var(--bg-elevated));
  transition:
    border-color var(--dur-base) var(--ease-out),
    background var(--dur-base) var(--ease-out),
    transform var(--dur-base) var(--ease-out);
}
.resume-uploader:hover {
  border-color: var(--brand-500);
  background: color-mix(in oklab, var(--brand-500) 8%, var(--bg-elevated));
  transform: translateY(-1px);
}
.resume-uploader.is-loading {
  pointer-events: none;
  opacity: 0.7;
}
.resume-uploader-icon {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: color-mix(in oklab, var(--brand-500) 12%, transparent);
  color: var(--brand-600, var(--brand-500));
  font-weight: 700;
  margin-bottom: 8px;
}

.resume-attached {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  background: var(--bg-elevated);
}
.resume-attached-icon {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 6px;
  background: color-mix(in oklab, var(--success-500) 14%, transparent);
  color: var(--success-700, var(--success-500));
  font-weight: 800;
  font-size: 11px;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

@media (prefers-reduced-motion: reduce) {
  .job-card,
  .job-card-bar,
  .job-card-arrow,
  .tag-panel-enter-active,
  .tag-panel-leave-active,
  .resume-uploader {
    transition: none !important;
  }
}
</style>
