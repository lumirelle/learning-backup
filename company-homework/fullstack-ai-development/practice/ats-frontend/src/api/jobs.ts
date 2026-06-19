import type { AxiosRequestConfig } from 'axios'
import { del, get, patch, post } from './request'

// ─────────────────────────────── 枚举（与后端 enum 对齐）───────────────────────────────

export type JobStatus = 'DRAFT' | 'PUBLISHED' | 'PAUSED' | 'CLOSED' | 'ARCHIVED'
export type JobWorkType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERN' | 'REMOTE'
export type JobLevel = 'INTERN' | 'JUNIOR' | 'MID' | 'SENIOR' | 'LEAD' | 'DIRECTOR'
export type TagCategory = 'TECH' | 'SOFT' | 'CERT' | 'LANG' | 'DOMAIN'

export const STATUS_LABEL: Record<JobStatus, string> = {
  DRAFT: '草稿',
  PUBLISHED: '招聘中',
  PAUSED: '已暂停',
  CLOSED: '已关闭',
  ARCHIVED: '已归档',
}

export const WORK_TYPE_LABEL: Record<JobWorkType, string> = {
  FULL_TIME: '全职',
  PART_TIME: '兼职',
  CONTRACT: '合同制',
  INTERN: '实习',
  REMOTE: '远程',
}

export const LEVEL_LABEL: Record<JobLevel, string> = {
  INTERN: '实习生',
  JUNIOR: '初级',
  MID: '中级',
  SENIOR: '高级',
  LEAD: '资深 / Lead',
  DIRECTOR: '总监',
}

export const TAG_CATEGORY_LABEL: Record<TagCategory, string> = {
  TECH: '技术栈',
  SOFT: '软技能',
  CERT: '认证证书',
  LANG: '语言能力',
  DOMAIN: '行业领域',
}

// ─────────────────────────────── DTO ───────────────────────────────

export interface TagVO {
  id: number
  slug: string
  name: string
  category: TagCategory
}

export interface JobListItemVO {
  id: number
  title: string
  /** M6：location 从 sub_department.location 派生，依然在 VO 中暴露便于现有 UI 展示 */
  location: string | null
  workType: JobWorkType
  level: JobLevel
  salaryMin: number | null
  salaryMax: number | null
  salaryRange: string
  headcount: number
  status: JobStatus
  viewCount: number
  publishedAt: string | null
  updatedAt: string
  /** M6：组织三段（root / department / sub_department） */
  subDepartmentId: number | null
  subDepartmentName: string | null
  departmentId: number | null
  departmentName: string | null
  rootOrgId: number | null
  rootOrgName: string | null
  createdBy: number
  createdByName: string | null
  tags: TagVO[]
}

export interface JobDetailVO extends Omit<JobListItemVO, 'tags'> {
  description: string | null
  closedAt: string | null
  createdAt: string
  tags: TagVO[]
  allowedTransitions: JobStatus[] | null
}

export interface JobCreateReq {
  title: string
  description?: string
  workType?: JobWorkType
  level?: JobLevel
  salaryMin?: number | null
  salaryMax?: number | null
  headcount?: number
  /** M6：岗位必须挂在子部门（叶子节点）；location 由子部门继承，不再单独传 */
  subDepartmentId: number
  tagIds?: number[]
}

export type JobUpdateReq = Partial<JobCreateReq>

export interface JobListReq {
  keyword?: string
  status?: JobStatus[]
  workType?: JobWorkType[]
  level?: JobLevel[]
  tagSlugs?: string[]
  /** 上层部门筛 */
  departmentId?: number
  /** M6 新增：子部门精确筛 */
  subDepartmentId?: number
  /** 工作地点模糊（M6 起从 sub_departments.location 取） */
  location?: string
  mine?: boolean
  /** HR 团队视角：本人 + 绑定子部门下他人岗位 */
  team?: boolean
  includeArchived?: boolean
  page?: number
  size?: number
  sortBy?: 'publishedAt' | 'createdAt' | 'viewCount' | 'salaryMax'
  sortOrder?: 'asc' | 'desc'
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
}

// ─────────────────────────────── API ───────────────────────────────

/**
 * 后端用 Spring `@ModelAttribute` 解析数组：需要展开成
 * `status=DRAFT&status=PUBLISHED`，axios 默认会序列化成 `status[]=...`，
 * 这里用 paramsSerializer 改成 repeat 模式。
 */
function listConfig(req: JobListReq): AxiosRequestConfig {
  return {
    params: req,
    paramsSerializer: {
      indexes: null, // foo=a&foo=b 而非 foo[]=a&foo[]=b
    },
  }
}

export const jobsApi = {
  list: (req: JobListReq = {}) =>
    get<PageResult<JobListItemVO>>('/jobs', listConfig(req)),

  detail: (id: number) =>
    get<JobDetailVO>(`/jobs/${id}`),

  create: (data: JobCreateReq) =>
    post<JobDetailVO, JobCreateReq>('/jobs', data),

  update: (id: number, data: JobUpdateReq) =>
    patch<JobDetailVO, JobUpdateReq>(`/jobs/${id}`, data),

  transition: (id: number, to: JobStatus) =>
    post<JobDetailVO, { to: JobStatus }>(`/jobs/${id}/transitions`, { to }),

  remove: (id: number) =>
    del<void>(`/jobs/${id}`),
}

export const tagsApi = {
  listAll: () => get<TagVO[]>('/tags'),
}
