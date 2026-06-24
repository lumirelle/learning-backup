// 枚举中文文案 + Badge 语义色 + 通用格式化（集中维护，避免散落硬编码）。

export const employmentStatusMap: Record<string, { label: string, tone: string }> = {
  probation: { label: '试用', tone: 'amber' },
  active: { label: '正式', tone: 'green' },
  leaving: { label: '离职中', tone: 'orange' },
  left: { label: '已离职', tone: 'red' },
}

export const processTypeMap: Record<string, string> = {
  onboard: '入职',
  offboard: '离职',
  transfer: '调动',
  regularize: '转正',
}

export const processStatusMap: Record<string, { label: string, tone: string }> = {
  draft: { label: '草稿', tone: 'gray' },
  pending: { label: '待审批', tone: 'blue' },
  approved: { label: '已通过', tone: 'green' },
  rejected: { label: '已驳回', tone: 'red' },
  effective: { label: '已生效', tone: 'green' },
  cancelled: { label: '已撤销', tone: 'gray' },
}

export const contractStatusMap: Record<string, { label: string, tone: string }> = {
  draft: { label: '草稿', tone: 'gray' },
  active: { label: '在用', tone: 'green' },
  expired: { label: '已到期', tone: 'red' },
  terminated: { label: '已终止', tone: 'gray' },
  renewed: { label: '已续签', tone: 'blue' },
}

export const contractTypeMap: Record<string, string> = {
  fixed_term: '固定期限',
  open_term: '无固定期限',
  intern: '实习',
  labor: '劳务',
}

export const eventTypeMap: Record<string, string> = {
  onboard: '入职',
  offboard: '离职',
  leave: '离职',
  transfer: '调动',
  regularize: '转正',
  promote: '晋升',
  contract: '合同',
  reward: '奖惩',
  care: '关怀',
  org_change: '组织变动',
}

export const genderMap: Record<string, string> = { male: '男', female: '女' }
export const educationMap: Record<string, string> = {
  college: '大专',
  bachelor: '本科',
  master: '硕士',
  phd: '博士',
}

/** Badge 色调 → UnoCSS class（柔和底色 + 同色系 ring 描边） */
export function toneClass(tone: string): string {
  const m: Record<string, string> = {
    green: 'bg-emerald-50 text-emerald-700 ring-emerald-600/15 dark:bg-emerald-500/10 dark:text-emerald-300 dark:ring-emerald-400/20',
    red: 'bg-rose-50 text-rose-700 ring-rose-600/15 dark:bg-rose-500/10 dark:text-rose-300 dark:ring-rose-400/20',
    amber: 'bg-amber-50 text-amber-700 ring-amber-600/20 dark:bg-amber-500/10 dark:text-amber-300 dark:ring-amber-400/20',
    orange: 'bg-orange-50 text-orange-700 ring-orange-600/20 dark:bg-orange-500/10 dark:text-orange-300 dark:ring-orange-400/20',
    blue: 'bg-primary-50 text-primary-700 ring-primary-600/15 dark:bg-primary-500/10 dark:text-primary-300 dark:ring-primary-400/20',
    gray: 'bg-truegray-50 text-truegray-500 ring-truegray-500/15 dark:bg-white/6 dark:text-truegray-300 dark:ring-white/10',
  }
  return m[tone] || m.gray!
}

/** YYYY-MM-DD（截断 RFC3339） */
export function fmtDate(s?: string | null): string {
  if (!s)
    return '—'
  return s.slice(0, 10)
}

export function fmtDateTime(s?: string | null): string {
  if (!s)
    return '—'
  return s.slice(0, 16).replace('T', ' ')
}
