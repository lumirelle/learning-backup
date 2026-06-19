import type { ApplicationStage } from './applications'
import { get } from './request'

/**
 * 数据看板 API · M5
 *
 * 与后端 com.ats.controller.StatsController 一一对应。
 * 仅 HR / ADMIN 可见，路由层 + controller 层双重拦截。
 */

export interface FunnelItem {
  stage: ApplicationStage
  count: number
}

export interface FunnelVO {
  /** 8 态固定顺序，缺失 stage 也会有 count=0 项 */
  items: FunnelItem[]
  total: number
  /** 漏斗最大 stage 的 count（前端按比例渲染条形图，避免再算） */
  max: number
}

export interface OverviewVO {
  newApplicationsThisMonth: number
  offersThisMonth: number
  hiresThisMonth: number
  activeJobs: number
}

/**
 * 公开聚合统计（permitAll，未登录可访问）—— 登录 / 注册页"水位"展示。
 * 字段对应后端 com.ats.stats.dto.PublicStatsVO。
 */
export interface PublicStatsVO {
  /** 简历筛选阶段候选人数（APPLIED + SCREENING_PASS） */
  screeningCount: number
  /** 面试阶段候选人数（PHONE/TECH/HR_INTERVIEW） */
  interviewCount: number
  /** Offer 阶段候选人数（OFFER） */
  offerCount: number
  /** 当前发布中的岗位数 */
  publishedJobs: number
  /** 已被在招岗位覆盖到的部门数 */
  coveredDepartments: number
}

export const statsApi = {
  /** 招聘漏斗（可选 jobId 限定单岗位） */
  funnel: (jobId?: number) =>
    get<FunnelVO>('/stats/funnel', jobId == null ? undefined : { params: { jobId } }),
  /** 本月概览 4 指标 */
  overview: () => get<OverviewVO>('/stats/overview'),
  /** 公开聚合统计 · 登录 / 注册页用 · 不需要 access token */
  publicStats: () => get<PublicStatsVO>('/stats/public'),
}
