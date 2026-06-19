<script setup lang="ts">
import type {
  ApplicationDetailVO,
  ApplicationListItemVO,
  ApplicationStage,
  StageLogVO,
} from '@/api/applications'
import {
  NButton,
  NDrawer,
  NDrawerContent,
  NSkeleton,
  NSpin,
  NTag,
  NTimeline,
  NTimelineItem,
  useMessage,
} from 'naive-ui'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  applicationsApi,
  STAGE_LABEL,
  STAGE_ORDER,
} from '@/api/applications'
import { BizError } from '@/api/request'
import CopyButton from '@/components/CopyButton.vue'
import EmptyState from '@/components/EmptyState.vue'
import ErrorBlock from '@/components/ErrorBlock.vue'
import { isResumeFile, resumeDownloadUrl } from '@/utils/resume'

const router = useRouter()
const message = useMessage()

// ─────────── 列表数据 ───────────

const loading = ref(false)
const items = ref<ApplicationListItemVO[]>([])

const errMsg = ref<string | null>(null)

async function fetchList() {
  loading.value = true
  errMsg.value = null
  try {
    items.value = await applicationsApi.listMine()
  }
  catch (e) {
    const err = e as { response?: { data?: { msg?: string } }, message?: string }
    errMsg.value = err.response?.data?.msg ?? err.message ?? '加载投递记录失败'
    if (e instanceof BizError)
      message.error(e.message)
  }
  finally {
    loading.value = false
  }
}

// ─────────── 详情 Drawer ───────────

const drawerVisible = ref(false)
const detail = ref<ApplicationDetailVO | null>(null)
const detailLoading = ref(false)

async function openDetail(id: number) {
  drawerVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await applicationsApi.detail(id)
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

// ─────────── helpers ───────────

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

const STAGE_TIMELINE_TONE: Record<ApplicationStage, 'success' | 'warning' | 'error' | 'info' | 'default'> = {
  APPLIED: 'info',
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

/**
 * 进度百分比（纯视觉用），把 8 态映射到 0~100。
 * 拒绝单独标红，不在进度条上推进；入职 100%；其余按位置 / 6 推算（HIRED 之前共 7 个非终态点）。
 */
function progressOf(stage: ApplicationStage): number {
  if (stage === 'REJECTED')
    return 100
  if (stage === 'HIRED')
    return 100
  const idx = STAGE_ORDER.indexOf(stage)
  if (idx < 0)
    return 0
  // APPLIED=0/6, SCREENING_PASS=1/6, ..., OFFER=5/6
  return Math.round((idx / 6) * 100)
}

const grouped = computed(() => {
  const active = items.value.filter(a => a.stage !== 'HIRED' && a.stage !== 'REJECTED')
  const closed = items.value.filter(a => a.stage === 'HIRED' || a.stage === 'REJECTED')
  return { active, closed }
})

onMounted(() => {
  fetchList()
})
</script>

<template>
  <main min-h-screen bg-app pt-60px>
    <section max-w-1100px mx-auto p="t-12 b-6 x-6">
      <p kicker mb-3>
        My Applications · 我的投递
      </p>
      <h1 m-0 text-gray-900 font-black tracking--0.03em leading="[1.1]" class="text-[clamp(28px,4vw,44px)]">
        >
        追踪你的<span text-gradient>每一次面试旅程</span>
      </h1>
      <p mt-3 text-base text-secondary max-w-640px leading="[1.65]">
        共 <span text-primary font-semibold>{{ items.length }}</span> 个投递 ·
        进行中 <span text-primary font-semibold>{{ grouped.active.length }}</span> ·
        已完成 <span text-primary font-semibold>{{ grouped.closed.length }}</span>
      </p>
    </section>

    <section max-w-1100px mx-auto p="b-12 x-6">
      <NSpin :show="loading">
        <!-- 错误 -->
        <ErrorBlock
          v-if="errMsg && !loading"
          :description="errMsg"
          :retrying="loading"
          @retry="fetchList"
        />

        <!-- 空 -->
        <EmptyState
          v-else-if="!loading && items.length === 0"
          icon="inbox"
          title="还没有投递记录"
          description="去岗位市场逛逛吧 —— 你投递的每一份简历都会在这里有迹可循。"
        >
          <template #action>
            <NButton type="primary" @click="router.push('/jobs')">
              去岗位市场 →
            </NButton>
          </template>
        </EmptyState>

        <!-- 骨架 -->
        <div v-else-if="loading && items.length === 0" flex="~ col" gap-3>
          <div v-for="i in 3" :key="i" card-base p-5>
            <NSkeleton text :repeat="2" />
            <NSkeleton text style="width: 50%" />
          </div>
        </div>

        <!-- 进行中 -->
        <template v-else>
          <div v-if="grouped.active.length" mb-8>
            <h2 text-sm text-tertiary text-uppercase tracking-widest m="0 b-3" font-bold>
              · 进行中
            </h2>
            <div flex="~ col" gap-3>
              <article
                v-for="item in grouped.active"
                :key="item.id"
                class="app-card group"
                tabindex="0"
                role="button"
                @click="openDetail(item.id)"
                @keydown.enter="openDetail(item.id)"
              >
                <div flex="~ items-start justify-between" gap-3 mb-2>
                  <div flex-1 min-w-0>
                    <h3 m-0 font-bold text-lg text-primary leading="tight" truncate>
                      {{ item.jobTitle }}
                    </h3>
                    <p m="t-1 b-0" text-xs text-tertiary>
                      {{ formatTime(item.appliedAt) }}投递 · 最近更新 {{ formatTime(item.updatedAt) }}
                    </p>
                  </div>
                  <NTag :type="STAGE_TONE[item.stage]" size="small" round :bordered="false" shrink-0>
                    {{ STAGE_LABEL[item.stage] }}
                  </NTag>
                </div>

                <!-- 进度条（视觉简化） -->
                <div class="app-progress" :data-stage="item.stage">
                  <span class="app-progress-fill" :style="{ width: `${progressOf(item.stage)}%` }" />
                </div>
              </article>
            </div>
          </div>

          <div v-if="grouped.closed.length">
            <h2 text-sm text-tertiary text-uppercase tracking-widest m="0 b-3" font-bold>
              · 已完成
            </h2>
            <div flex="~ col" gap-3>
              <article
                v-for="item in grouped.closed"
                :key="item.id"
                class="app-card group"
                tabindex="0"
                role="button"
                @click="openDetail(item.id)"
                @keydown.enter="openDetail(item.id)"
              >
                <div flex="~ items-start justify-between" gap-3>
                  <div flex-1 min-w-0>
                    <h3 m-0 font-bold text-lg text-primary leading="tight" truncate :class="{ 'op-70': item.stage === 'REJECTED' }">
                      {{ item.jobTitle }}
                    </h3>
                    <p m="t-1 b-0" text-xs text-tertiary>
                      {{ formatTime(item.appliedAt) }}投递 · 结果于 {{ formatTime(item.updatedAt) }}
                    </p>
                  </div>
                  <NTag :type="STAGE_TONE[item.stage]" size="small" round :bordered="false" shrink-0>
                    {{ STAGE_LABEL[item.stage] }}
                  </NTag>
                </div>
              </article>
            </div>
          </div>
        </template>
      </NSpin>
    </section>

    <!-- ─────── Detail Drawer ─────── -->
    <NDrawer v-model:show="drawerVisible" :width="640" placement="right">
      <NDrawerContent
        :native-scrollbar="false"
        :title="detail?.jobTitle ?? '投递详情'"
        closable
      >
        <NSpin :show="detailLoading">
          <template v-if="detail">
            <div mb-6>
              <div flex="~ items-center wrap" gap-2 mb-2>
                <NTag :type="STAGE_TONE[detail.stage]" round :bordered="false">
                  {{ STAGE_LABEL[detail.stage] }}
                </NTag>
                <span text-xs text-tertiary font-mono>
                  · 投递 ID #{{ detail.id }}
                </span>
                <CopyButton
                  :text="String(detail.id)"
                  hint="已复制投递 ID"
                  tooltip="复制投递 ID（联系 HR 时可引用）"
                  size="tiny"
                />
              </div>
              <p m="t-1 b-0" text-sm text-secondary leading="[1.65]">
                投递时间：{{ formatTime(detail.appliedAt) }} · 最近更新：{{ formatTime(detail.updatedAt) }}
              </p>

              <!-- 投递时填的简历 / 联系方式（候选人自己回看） -->
              <div mt-3 grid grid-cols-2 gap-3 p-3 rounded-md bg-muted text-xs text-secondary>
                <div>
                  <span text-tertiary>工作年限 ·</span>
                  <span text-primary font-medium ml-1>{{ detail.yearsExp ?? '—' }} 年</span>
                </div>
                <div>
                  <span text-tertiary>联系方式 ·</span>
                  <span text-primary font-medium ml-1 font-mono>{{ detail.phone ?? '—' }}</span>
                </div>
                <div v-if="detail.resumeUrl" col-span-2 truncate>
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

            <h3 text-sm text-tertiary text-uppercase tracking-widest m="0 b-3" font-bold>
              阶段时间线
            </h3>
            <NTimeline>
              <NTimelineItem
                v-for="log in detail.stageLogs"
                :key="log.id"
                :type="STAGE_TIMELINE_TONE[log.toStage]"
                :title="stageLogTitle(log)"
                :time="formatTime(log.operatedAt)"
                :content="log.note ?? (log.fromStage ? '' : '系统记录')"
              >
                <template v-if="log.operatedByName" #default>
                  <p m-0 text-sm>
                    操作人：<span text-primary>{{ log.operatedByName }}</span>
                    <span v-if="log.operatedByRole" text-tertiary text-xs ml-1>· {{ log.operatedByRole }}</span>
                  </p>
                  <p v-if="log.note" m="t-1 b-0" text-sm text-secondary leading="[1.65]">
                    {{ log.note }}
                  </p>
                </template>
              </NTimelineItem>
            </NTimeline>
          </template>
        </NSpin>
        <template v-if="detail" #footer>
          <div flex="~ items-center justify-end" gap-2 w-full>
            <NButton @click="drawerVisible = false">
              关闭
            </NButton>
            <NButton
              type="primary"
              tertiary
              @click="router.push({ path: '/jobs', query: { jobId: String(detail.jobId) } })"
            >
              查看岗位 →
            </NButton>
          </div>
        </template>
      </NDrawerContent>
    </NDrawer>
  </main>
</template>

<style scoped>
.app-card {
  position: relative;
  display: block;
  padding: 18px 20px;
  border-radius: 10px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  cursor: pointer;
  outline: none;
  transition:
    transform var(--dur-slow) var(--ease-out),
    box-shadow var(--dur-slow) var(--ease-out),
    border-color var(--dur-slow) var(--ease-out);
}
.app-card:hover,
.app-card:focus-visible {
  transform: translate3d(0, -2px, 0);
  border-color: transparent;
  box-shadow: var(--shadow-md), 0 0 0 1px var(--brand-300);
}

.app-progress {
  margin-top: 10px;
  height: 4px;
  background: var(--bg-muted);
  border-radius: 999px;
  overflow: hidden;
}
.app-progress-fill {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, var(--accent-mint), var(--accent-emerald), var(--accent-teal));
  transition: width var(--dur-slow) var(--ease-out);
}

@media (prefers-reduced-motion: reduce) {
  .app-card,
  .app-progress-fill {
    transition: none !important;
  }
}

/* 简历 PDF 标签 —— 与 hr/board.vue 风格一致 */
.resume-pill {
  display: inline-block;
  padding: 1px 6px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.06em;
  border-radius: 4px;
  background: var(--brand-100);
  color: var(--brand-800);
  text-transform: uppercase;
}
</style>
