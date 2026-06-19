<script setup lang="ts">
import type { ApplicationStage } from '@/api/applications'
import type { FunnelVO, OverviewVO } from '@/api/stats'
import { NButton, useMessage } from 'naive-ui'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { downloadStageLogsCsv } from '@/api/audit'
import { STAGE_LABEL } from '@/api/applications'
import { statsApi } from '@/api/stats'
import { BizError } from '@/api/request'
import ErrorBlock from '@/components/ErrorBlock.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const msg = useMessage()

const loading = ref(true)
const exportingAudit = ref(false)
const errMsg = ref<string | null>(null)
const overview = ref<OverviewVO | null>(null)
const funnel = ref<FunnelVO | null>(null)

const monthLabel = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
})

const conversionRate = computed(() => {
  if (!funnel.value) return null
  const hired = funnel.value.items.find(i => i.stage === 'HIRED')?.count ?? 0
  const rejected = funnel.value.items.find(i => i.stage === 'REJECTED')?.count ?? 0
  const totalEnded = hired + rejected
  if (totalEnded === 0) return null
  return Math.round((hired / totalEnded) * 100)
})

/**
 * stage → 渐变色映射。语义：
 * - APPLIED：起点 brand 青绿
 * - SCREENING_PASS / *_INTERVIEW：渐进蓝绿（成长）
 * - OFFER：warm 琥珀（即将摘果）
 * - HIRED：金绿（成果）
 * - REJECTED：灰红（终止）
 */
const STAGE_GRADIENT: Record<ApplicationStage, string> = {
  APPLIED: 'linear-gradient(90deg, #10b981, #34d399)',
  SCREENING_PASS: 'linear-gradient(90deg, #14b8a6, #2dd4bf)',
  PHONE_INTERVIEW: 'linear-gradient(90deg, #06b6d4, #22d3ee)',
  TECH_INTERVIEW: 'linear-gradient(90deg, #0891b2, #06b6d4)',
  HR_INTERVIEW: 'linear-gradient(90deg, #6366f1, #818cf8)',
  OFFER: 'linear-gradient(90deg, #f59e0b, #fbbf24)',
  HIRED: 'linear-gradient(90deg, #16a34a, #4ade80)',
  REJECTED: 'linear-gradient(90deg, #6b7280, #ef4444)',
}

async function load() {
  loading.value = true
  errMsg.value = null
  try {
    const [ov, fn] = await Promise.all([
      statsApi.overview(),
      statsApi.funnel(),
    ])
    overview.value = ov
    funnel.value = fn
  }
  catch (e) {
    const err = e as { response?: { data?: { msg?: string } }, message?: string }
    errMsg.value = err.response?.data?.msg ?? err.message ?? '加载数据看板失败'
  }
  finally {
    loading.value = false
  }
}

function widthOf(count: number, max: number) {
  if (max === 0) return 0
  // 最小可见宽度 4%，避免 count=0 完全消失
  return Math.max(count === 0 ? 0 : 4, (count / max) * 100)
}

/** 漏斗 / metric 卡跳看板 —— 带 stage 参数让 board 自动定位列 */
function jumpToBoard(stage?: ApplicationStage) {
  router.push({ path: '/hr/board', query: stage ? { stage } : {} })
}

/** "在招岗位"卡片跳岗位管理 */
function jumpToJobs() {
  router.push({ path: '/hr/jobs' })
}

async function exportAuditCsv() {
  exportingAudit.value = true
  try {
    await downloadStageLogsCsv(auth.accessToken)
    msg.success('阶段流转审计 CSV 已开始下载')
  }
  catch (e) {
    msg.error(e instanceof BizError ? e.message : '导出失败')
  }
  finally {
    exportingAudit.value = false
  }
}

/** 漏斗 / metric 行支持键盘 Enter 触发 */
function onRowKeydown(e: KeyboardEvent, stage: ApplicationStage) {
  if (e.key === 'Enter' || e.key === ' ') {
    e.preventDefault()
    jumpToBoard(stage)
  }
}

onMounted(load)
</script>

<template>
  <div class="dashboard-page">
    <header class="page-hero">
      <div flex="~ items-start justify-between gap-4">
        <div>
          <div class="hero-kicker">
            <span class="kicker-dot" />
            OPERATIONS · {{ monthLabel }}
          </div>
          <h1 class="hero-title">
            数据看板
          </h1>
          <p class="hero-subtitle">
            本月招聘漏斗、Offer / 入职动态、在招岗位 ·
            <span text-primary>{{ auth.role === 'ADMIN' ? '全平台视角' : '我的岗位视角' }}</span>
          </p>
        </div>
        <NButton
          v-if="auth.isHr || auth.isAdmin"
          tertiary
          :loading="exportingAudit"
          @click="exportAuditCsv"
        >
          导出阶段审计 CSV
        </NButton>
      </div>
    </header>

    <!-- 错误占位 —— 加载失败时整页 retry -->
    <ErrorBlock
      v-if="errMsg && !loading"
      :description="errMsg"
      :retrying="loading"
      @retry="load"
    />

    <template v-else>
      <section class="metric-grid">
        <article
          class="metric-card metric-applied"
          tabindex="0"
          role="button"
          :aria-label="`本月新增投递 ${overview?.newApplicationsThisMonth ?? 0} 条 · 点击跳转看板 APPLIED 列`"
          @click="jumpToBoard('APPLIED')"
          @keydown="(e) => onRowKeydown(e, 'APPLIED')"
        >
          <span class="metric-label">本月新增投递</span>
          <span class="metric-value">
            <span v-if="loading" class="skeleton-num" />
            <template v-else>{{ overview?.newApplicationsThisMonth ?? 0 }}</template>
          </span>
          <span class="metric-foot">applications · this month</span>
        </article>

        <article
          class="metric-card metric-offer"
          tabindex="0"
          role="button"
          :aria-label="`本月已发 Offer ${overview?.offersThisMonth ?? 0} 条 · 点击跳转看板 OFFER 列`"
          @click="jumpToBoard('OFFER')"
          @keydown="(e) => onRowKeydown(e, 'OFFER')"
        >
          <span class="metric-label">本月已发 Offer</span>
          <span class="metric-value">
            <span v-if="loading" class="skeleton-num" />
            <template v-else>{{ overview?.offersThisMonth ?? 0 }}</template>
          </span>
          <span class="metric-foot">offers · this month</span>
        </article>

        <article
          class="metric-card metric-hired"
          tabindex="0"
          role="button"
          :aria-label="`本月入职 ${overview?.hiresThisMonth ?? 0} 人 · 点击跳转看板 HIRED 列`"
          @click="jumpToBoard('HIRED')"
          @keydown="(e) => onRowKeydown(e, 'HIRED')"
        >
          <span class="metric-label">本月入职</span>
          <span class="metric-value">
            <span v-if="loading" class="skeleton-num" />
            <template v-else>{{ overview?.hiresThisMonth ?? 0 }}</template>
          </span>
          <span class="metric-foot">hires · this month</span>
        </article>

        <article
          class="metric-card metric-jobs"
          tabindex="0"
          role="button"
          :aria-label="`在招岗位 ${overview?.activeJobs ?? 0} 个 · 点击跳转岗位管理`"
          @click="jumpToJobs"
          @keydown="(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); jumpToJobs() } }"
        >
          <span class="metric-label">在招岗位</span>
          <span class="metric-value">
            <span v-if="loading" class="skeleton-num" />
            <template v-else>{{ overview?.activeJobs ?? 0 }}</template>
          </span>
          <span class="metric-foot">active jobs · now</span>
        </article>
      </section>

      <section class="funnel-card">
        <header class="funnel-header">
          <div>
            <h2 class="funnel-title">
              招聘漏斗
            </h2>
            <p class="funnel-sub">
              点击或 Enter 跳转看板对应列
            </p>
          </div>
          <div class="funnel-meta">
            <span class="meta-pill">
              <strong>{{ funnel?.total ?? 0 }}</strong>
              <small>投递总数</small>
            </span>
            <span v-if="conversionRate != null" class="meta-pill">
              <strong>{{ conversionRate }}%</strong>
              <small>HIRED / (HIRED + REJECTED)</small>
            </span>
          </div>
        </header>

        <div v-if="loading" class="funnel-skeleton">
          <div v-for="i in 8" :key="i" class="skeleton-row" />
        </div>

        <ul v-else-if="funnel" class="funnel-list">
          <li
            v-for="item in funnel.items"
            :key="item.stage"
            class="funnel-row"
            :class="{ 'funnel-row-empty': item.count === 0 }"
            tabindex="0"
            role="button"
            :aria-label="`${STAGE_LABEL[item.stage]} ${item.count} 条 · 点击跳转看板`"
            @click="jumpToBoard(item.stage)"
            @keydown="(e) => onRowKeydown(e, item.stage)"
          >
            <span class="row-label">{{ STAGE_LABEL[item.stage] }}</span>
            <div class="row-bar-wrap">
              <span
                class="row-bar"
                :style="{
                  width: `${widthOf(item.count, funnel.max)}%`,
                  background: STAGE_GRADIENT[item.stage],
                }"
              />
              <span class="row-count">{{ item.count }}</span>
            </div>
            <span class="row-arrow">→</span>
          </li>
        </ul>
      </section>
    </template>
  </div>
</template>

<style scoped>
.dashboard-page {
  --card-radius: 14px;
  max-width: 1200px;
  margin: 0 auto;
  /* 60px navbar + 32px 顶部留白；避免 hero 被 fixed 顶栏遮挡 */
  padding: calc(60px + 32px) 24px 80px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

/* ── Hero ─────────────────────────────────────────────────── */
.page-hero {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 24px 28px 28px;
  border-radius: var(--card-radius);
  background:
    radial-gradient(circle at 0% 0%, rgba(16, 185, 129, 0.10), transparent 50%),
    radial-gradient(circle at 100% 0%, rgba(99, 102, 241, 0.08), transparent 50%),
    var(--bg-elevated);
  border: 1px solid var(--border-default);
}
.hero-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  letter-spacing: 0.18em;
  color: var(--text-secondary);
  font-weight: 600;
  text-transform: uppercase;
}
.kicker-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--brand-500);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}
.hero-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.2;
  margin: 0;
}
.hero-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* ── Metric Grid ─────────────────────────────────────────── */
.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}
.metric-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px 22px 22px;
  border-radius: var(--card-radius);
  background: var(--bg-elevated);
  border: 1px solid var(--border-default);
  overflow: hidden;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.metric-card::before {
  content: '';
  position: absolute;
  inset: auto 0 0 0;
  height: 3px;
  transform-origin: left center;
  transform: scaleX(0.18);
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}
.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}
.metric-card:hover::before {
  transform: scaleX(1);
}
.metric-applied::before { background: linear-gradient(90deg, #10b981, #34d399); }
.metric-offer::before { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.metric-hired::before { background: linear-gradient(90deg, #16a34a, #4ade80); }
.metric-jobs::before { background: linear-gradient(90deg, #6366f1, #818cf8); }

.metric-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: 0.04em;
}
.metric-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}
.metric-foot {
  font-size: 11px;
  color: var(--text-tertiary);
  letter-spacing: 0.06em;
}
.skeleton-num {
  display: inline-block;
  width: 80px;
  height: 36px;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--bg-secondary) 25%, var(--bg-tertiary) 50%, var(--bg-secondary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ── Funnel Card ─────────────────────────────────────────── */
.funnel-card {
  padding: 24px 28px 28px;
  border-radius: var(--card-radius);
  background: var(--bg-elevated);
  border: 1px solid var(--border-default);
}
.funnel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}
.funnel-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--text-primary);
}
.funnel-sub {
  font-size: 12px;
  color: var(--text-tertiary);
  margin: 0;
}
.funnel-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.meta-pill {
  display: inline-flex;
  flex-direction: column;
  gap: 2px;
  padding: 6px 14px 8px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-default);
}
.meta-pill strong {
  font-size: 18px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--text-primary);
  line-height: 1;
}
.meta-pill small {
  font-size: 10px;
  color: var(--text-tertiary);
  letter-spacing: 0.06em;
}

.funnel-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  list-style: none;
  padding: 0;
  margin: 0;
}
.funnel-row {
  display: grid;
  grid-template-columns: 100px 1fr 24px;
  align-items: center;
  gap: 14px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}
.funnel-row:hover {
  background: var(--bg-secondary);
  transform: translateX(2px);
}
.funnel-row-empty {
  opacity: 0.45;
}
.row-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 600;
}
.row-bar-wrap {
  position: relative;
  height: 28px;
  background: var(--bg-tertiary);
  border-radius: 6px;
  overflow: hidden;
}
.row-bar {
  display: block;
  height: 100%;
  border-radius: 6px;
  transition: width 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
}
.row-bar::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.18) 50%, transparent 100%);
}
.row-count {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.35);
}
.row-arrow {
  font-size: 14px;
  color: var(--text-tertiary);
  opacity: 0;
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.funnel-row:hover .row-arrow {
  opacity: 1;
  transform: translateX(2px);
}

.funnel-skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.skeleton-row {
  height: 44px;
  border-radius: 8px;
  background: linear-gradient(90deg, var(--bg-secondary) 25%, var(--bg-tertiary) 50%, var(--bg-secondary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}

@media (prefers-reduced-motion: reduce) {
  .metric-card,
  .metric-card::before,
  .funnel-row,
  .row-bar,
  .row-arrow {
    transition: none !important;
    animation: none !important;
  }
}

@media (max-width: 640px) {
  .dashboard-page {
    padding: 16px;
    gap: 18px;
  }
  .hero-title {
    font-size: 24px;
  }
  .funnel-row {
    grid-template-columns: 88px 1fr 18px;
    gap: 10px;
  }
  .metric-value {
    font-size: 28px;
  }
}
</style>
