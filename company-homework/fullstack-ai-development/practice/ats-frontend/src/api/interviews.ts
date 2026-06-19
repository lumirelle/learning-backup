import { get, post, put } from './request'

export type InterviewConclusion = 'PASS' | 'REJECT' | 'HOLD'

export const CONCLUSION_LABEL: Record<InterviewConclusion, string> = {
  PASS: '通过',
  REJECT: '拒绝',
  HOLD: '待定',
}

export const CONCLUSION_TONE: Record<InterviewConclusion, 'success' | 'error' | 'warning'> = {
  PASS: 'success',
  REJECT: 'error',
  HOLD: 'warning',
}

export interface InterviewVO {
  id: number
  applicationId: number
  interviewerId: number | null
  interviewerName: string | null
  interviewerRole: string | null
  round: string
  rating: number | null
  strengths: string | null
  weaknesses: string | null
  conclusion: InterviewConclusion
  notes: string | null
  createdAt: string
  updatedAt: string
  /** 后端按 24h 编辑窗口 + 作者校验后预算好的标记，前端直接用，不重复判断 */
  editable: boolean
}

export interface InterviewCreateReq {
  round: string
  rating: number
  conclusion: InterviewConclusion
  strengths?: string
  weaknesses?: string
  notes?: string
}

export type InterviewUpdateReq = InterviewCreateReq

export const interviewsApi = {
  list(applicationId: number) {
    return get<InterviewVO[]>(`/applications/${applicationId}/interviews`)
  },
  create(applicationId: number, req: InterviewCreateReq) {
    return post<InterviewVO>(`/applications/${applicationId}/interviews`, req)
  },
  update(id: number, req: InterviewUpdateReq) {
    return put<InterviewVO>(`/interviews/${id}`, req)
  },
}
