import type { AxiosRequestConfig } from 'axios'
import type { JobLevel, JobStatus, JobWorkType } from './jobs'
import { get, post } from './request'

// ─────────────────────────── 枚举：与后端 enum 对齐 ───────────────────────────

/**
 * 招聘漏斗 8 态。状态机（前端镜像，避免每次拖拽都问后端）：
 *   APPLIED         → SCREENING_PASS, REJECTED
 *   SCREENING_PASS  → PHONE_INTERVIEW, REJECTED
 *   PHONE_INTERVIEW → TECH_INTERVIEW, REJECTED
 *   TECH_INTERVIEW  → HR_INTERVIEW, REJECTED
 *   HR_INTERVIEW    → OFFER, REJECTED
 *   OFFER           → HIRED, REJECTED
 *   HIRED, REJECTED → 终态（不可离开）
 */
export type ApplicationStage =
  | 'APPLIED'
  | 'SCREENING_PASS'
  | 'PHONE_INTERVIEW'
  | 'TECH_INTERVIEW'
  | 'HR_INTERVIEW'
  | 'OFFER'
  | 'HIRED'
  | 'REJECTED'

export const STAGE_ORDER: ApplicationStage[] = [
  'APPLIED',
  'SCREENING_PASS',
  'PHONE_INTERVIEW',
  'TECH_INTERVIEW',
  'HR_INTERVIEW',
  'OFFER',
  'HIRED',
  'REJECTED',
]

export const STAGE_LABEL: Record<ApplicationStage, string> = {
  APPLIED: '已投递',
  SCREENING_PASS: '简历通过',
  PHONE_INTERVIEW: '电话面试',
  TECH_INTERVIEW: '技术面试',
  HR_INTERVIEW: 'HR 面试',
  OFFER: '已发 Offer',
  HIRED: '已入职',
  REJECTED: '已拒绝',
}

/**
 * 状态机转移图（前端镜像，与后端 ApplicationStageMachine 一一对应）。
 * 用途：拖拽落点合法性预校验、详情页动态展示「下一步」选项。
 */
export const STAGE_TRANSITIONS: Record<ApplicationStage, ApplicationStage[]> = {
  APPLIED: ['SCREENING_PASS', 'REJECTED'],
  SCREENING_PASS: ['PHONE_INTERVIEW', 'REJECTED'],
  PHONE_INTERVIEW: ['TECH_INTERVIEW', 'REJECTED'],
  TECH_INTERVIEW: ['HR_INTERVIEW', 'REJECTED'],
  HR_INTERVIEW: ['OFFER', 'REJECTED'],
  OFFER: ['HIRED', 'REJECTED'],
  HIRED: [],
  REJECTED: [],
}

export const TERMINAL_STAGES: ApplicationStage[] = ['HIRED', 'REJECTED']

export function canTransition(from: ApplicationStage, to: ApplicationStage): boolean {
  if (from === to) return false
  return STAGE_TRANSITIONS[from]?.includes(to) ?? false
}

export function isTerminal(stage: ApplicationStage): boolean {
  return TERMINAL_STAGES.includes(stage)
}

// ─────────────────────────── DTO ───────────────────────────

export interface ApplicationCreateReq {
  jobId: number
  resumeUrl?: string
  yearsExp?: number
  phone?: string
}

export interface StageTransitionReq {
  toStage: ApplicationStage
  note?: string
}

export interface StageLogVO {
  id: number
  fromStage: ApplicationStage | null
  toStage: ApplicationStage
  note: string | null
  operatedBy: number | null
  operatedByName: string | null
  operatedByRole: 'ADMIN' | 'HR' | 'CANDIDATE' | null
  operatedAt: string
}

export interface ApplicationListItemVO {
  id: number
  jobId: number
  jobTitle: string
  jobStatus: JobStatus | null
  candidateId: number | null
  candidateName: string | null
  candidateEmail: string | null
  stage: ApplicationStage
  yearsExp: number | null
  appliedAt: string
  updatedAt: string
}

export interface ApplicationDetailVO {
  id: number
  jobId: number
  jobTitle: string
  jobStatus: JobStatus | null

  /** M6：岗位组织信息 */
  subDepartmentId?: number | null
  subDepartmentName?: string | null
  departmentId?: number | null
  departmentName?: string | null
  rootOrgId?: number | null
  rootOrgName?: string | null
  jobLocation?: string | null

  candidateId: number | null
  candidateName: string | null
  candidateEmail: string | null

  stage: ApplicationStage
  resumeUrl: string | null
  yearsExp: number | null
  phone: string | null
  rejectReason: string | null

  appliedAt: string
  updatedAt: string

  stageLogs: StageLogVO[]
  /** 当前用户对该投递可执行的下一阶段；候选人为 null（不可管理） */
  allowedTransitions: ApplicationStage[] | null
}

export interface BoardColumnVO {
  stage: ApplicationStage
  count: number
  items: ApplicationListItemVO[]
  hasMore?: boolean
}

export interface BoardVO {
  jobId: number | null
  jobTitle: string | null
  /** 后端固定按 STAGE_ORDER 顺序返回，前端无需再排序 */
  columns: BoardColumnVO[]
  totalApplications: number
  jobsTruncated?: boolean
}

/**
 * 招聘看板 query 参数（与后端 BoardQueryReq 对齐）。
 * - jobId 给定时走单岗位看板（保留 owner/admin 鉴权），其余 filter 字段被忽略
 * - 否则按 keyword / workType[] / level[] / departmentId / location / salary / tagSlugs[] 过滤岗位，
 *   再聚合这些岗位的投递
 */
export interface BoardQueryReq {
  jobId?: number
  keyword?: string
  workType?: JobWorkType[]
  level?: JobLevel[]
  tagSlugs?: string[]
  /** 按"上层部门"筛（命中该部门下所有子部门的岗位） */
  departmentId?: number
  /** M6：按"子部门"精确筛（叶子节点） */
  subDepartmentId?: number
  /** 工作地点模糊（M6 起从 sub_departments.location 取） */
  location?: string
  salaryMin?: number
  salaryMax?: number
  itemsPerColumn?: number
  columnOffset?: number
  stage?: ApplicationStage
}

// ─────────────────────────── API ───────────────────────────

/**
 * 与 jobs.ts 同样的 paramsSerializer：把数组展开为 `workType=A&workType=B`，
 * 而不是 axios 默认的 `workType[]=A&workType[]=B`。Spring `@ModelAttribute` 才能正确绑定 List。
 */
function boardConfig(req: BoardQueryReq): AxiosRequestConfig {
  return {
    params: req,
    paramsSerializer: {
      indexes: null,
    },
  }
}

export const applicationsApi = {
  apply: (data: ApplicationCreateReq) =>
    post<ApplicationDetailVO, ApplicationCreateReq>('/applications', data),

  listMine: () => get<ApplicationListItemVO[]>('/applications/me'),

  /** 招聘看板（多维筛选）。 */
  board: (req: BoardQueryReq = {}) =>
    get<BoardVO>('/applications/board', boardConfig(req)),

  detail: (id: number) => get<ApplicationDetailVO>(`/applications/${id}`),

  transition: (id: number, req: StageTransitionReq) =>
    post<ApplicationDetailVO, StageTransitionReq>(`/applications/${id}/transitions`, req),
}
