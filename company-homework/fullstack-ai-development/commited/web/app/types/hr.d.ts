// 组织人事后端实体（与 Go /api/v1 对齐）。id 类字段均为字符串（后端已做精度处理）。
declare global {
  namespace Hr {
    interface Page<T> {
      list: T[]
      total: number
      page: number
      page_size: number
    }

    interface User {
      id: string
      username: string
      name: string
      roles: string[]
      is_admin: boolean
      org_id: string
      org_path: string
      is_active: boolean
    }

    interface OrgNode {
      id: string
      parent_id?: string
      code?: string
      name: string
      short_name?: string
      type?: string
      path: string
      status?: string
      ext_source?: string
      children?: OrgNode[]
    }

    interface DeptNode extends OrgNode {
      org_id: string
    }

    interface Employee {
      id: string
      employee_no: string
      name: string
      en_name?: string
      gender: string
      birthday?: string
      phone?: string
      work_email?: string
      education?: string
      org_path: string
      dept_id?: string
      dept_name?: string
      position_name?: string
      job_level?: string
      employment_type?: string
      employment_status: string
      hired_at?: string
      regular_at?: string
      left_at?: string
    }

    interface Event {
      id: string
      employee_id?: string
      event_type: string
      title: string
      org_path?: string
      occurred_at: string
    }

    interface ApprovalStep {
      id: string
      step_no: number
      approver_id: string
      action: string
      comment?: string
      acted_at?: string
    }

    interface Approval {
      id: string
      status: string
      current_step: number
      total_steps: number
      steps?: ApprovalStep[]
    }

    interface Process {
      id: string
      process_no: string
      type: string
      employee_id?: string
      applicant_id: string
      payload: Record<string, any>
      status: string
      effective_date?: string
      approval?: Approval
      created_at: string
    }

    interface Contract {
      id: string
      contract_no: string
      employee_id: string
      employee_name?: string
      type: string
      status: string
      sign_date?: string
      start_date?: string
      end_date?: string
      salary_band?: string
      days_left?: number
    }

    interface Overview {
      headcount: number
      probation: number
      this_month_hires: number
      this_month_leaves: number
      pending_processes: number
      total_employees: number
    }

    interface Bucket {
      label: string
      count: number
    }

    interface AuditLog {
      id: string
      username: string
      method: string
      path: string
      status: number
      created_at: string
    }
  }
}

export {}
