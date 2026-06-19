/**
 * Admin 用户管理 API · 仅 ADMIN 角色可调用。
 * 后端：com.ats.controller.AdminController
 */
import { get, patch, post } from './request'
import type { MeVO } from './auth'

/** 单个创建用户的请求体 · role 只能是 HR 或 CANDIDATE */
export interface CreateUserReq {
  email: string
  password: string
  fullName: string
  role: 'HR' | 'CANDIDATE'
  /** HR 必填：绑定的子部门 id 列表（M6 多对多） */
  subDepartmentIds?: number[]
}

/** 批量创建结果中的单行结果 */
export interface BatchCreateItem {
  rowIndex: number
  email: string
  success: boolean
  userId?: number
  role?: 'HR' | 'CANDIDATE'
  errorCode?: number
  errorMsg?: string
}

/** 批量创建汇总结果 */
export interface BatchCreateResult {
  successCount: number
  failureCount: number
  items: BatchCreateItem[]
}

export interface AdminUserListItemVO {
  id: number
  email: string
  fullName: string
  role: 'HR' | 'CANDIDATE' | 'ADMIN'
  active: boolean
  subDepartmentIds: number[]
  createdAt: string
}

export interface UpdateUserReq {
  fullName?: string
  role?: 'HR' | 'CANDIDATE'
  active?: boolean
  subDepartmentIds?: number[]
  newPassword?: string
}

export const adminApi = {
  listUsers: (params?: { role?: string, activeOnly?: boolean }) =>
    get<AdminUserListItemVO[]>('/admin/users', { params }),

  updateUser: (id: number, data: UpdateUserReq) =>
    patch<AdminUserListItemVO>(`/admin/users/${id}`, data),

  /** POST /admin/users · 单个创建 HR / CANDIDATE 账号 */
  createUser: (data: CreateUserReq) => post<MeVO>('/admin/users', data),

  /**
   * POST /admin/users/batch · 批量创建（最多 100 条 / 批）。
   * 单行失败不会回滚整批；前端按 items 逐行展示成功 / 失败结果。
   */
  batchCreate: (users: CreateUserReq[]) =>
    post<BatchCreateResult>('/admin/users/batch', { users }),
}
