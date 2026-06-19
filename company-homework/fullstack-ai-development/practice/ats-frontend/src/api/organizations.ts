import { del, get, post, put } from './request'

export type OrgNodeType = 'ROOT' | 'DEPARTMENT' | 'SUB_DEPARTMENT'

export interface OrgTreeNodeVO {
  key: string
  nodeType: OrgNodeType
  id: number
  label: string
  location?: string | null
  parentDepartmentId?: number | null
  editable?: boolean
  children?: OrgTreeNodeVO[]
}

export interface DepartmentCreateReq {
  name: string
  parentDepartmentId?: number | null
}

export interface DepartmentUpdateReq {
  name: string
}

export interface SubDepartmentCreateReq {
  parentDepartmentId: number
  name: string
  location: string
}

export interface SubDepartmentUpdateReq {
  name: string
  location: string
}

export const organizationsApi = {
  tree: () => get<OrgTreeNodeVO>('/admin/departments/tree'),

  createDepartment: (data: DepartmentCreateReq) =>
    post<OrgTreeNodeVO, DepartmentCreateReq>('/admin/departments', data),

  updateDepartment: (id: number, data: DepartmentUpdateReq) =>
    put<OrgTreeNodeVO, DepartmentUpdateReq>(`/admin/departments/${id}`, data),

  deleteDepartment: (id: number) => del<void>(`/admin/departments/${id}`),

  createSubDepartment: (data: SubDepartmentCreateReq) =>
    post<OrgTreeNodeVO, SubDepartmentCreateReq>('/admin/sub-departments', data),

  updateSubDepartment: (id: number, data: SubDepartmentUpdateReq) =>
    put<OrgTreeNodeVO, SubDepartmentUpdateReq>(`/admin/sub-departments/${id}`, data),

  deleteSubDepartment: (id: number) => del<void>(`/admin/sub-departments/${id}`),
}
