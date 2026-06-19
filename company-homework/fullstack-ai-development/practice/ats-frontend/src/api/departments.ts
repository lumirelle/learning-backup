import { get } from './request'

// ─────────────────────────── DTO ───────────────────────────

export interface DepartmentVO {
  id: number
  name: string
  rootOrgId?: number | null
  rootOrgName?: string | null
  parentDepartmentId?: number | null
  parentDepartmentName?: string | null
}

/**
 * M6：子部门字典（叶子节点）。新建岗位下拉、Board 子部门筛选用。
 * label 建议拼成「{name} / {location}」。
 */
export interface SubDepartmentVO {
  id: number
  name: string
  location: string
  parentDepartmentId: number
  parentDepartmentName: string
  rootOrgId: number
  rootOrgName: string
}

// ─────────────────────────── API ───────────────────────────

export const departmentsApi = {
  /** 中间部门字典（按上层部门筛选下拉用）。 */
  listAll: () => get<DepartmentVO[]>('/departments'),

  /** 子部门字典（M6 新增；新建岗位、Board 子部门筛选用）。 */
  listAllSubDepartments: () => get<SubDepartmentVO[]>('/sub-departments'),
}
