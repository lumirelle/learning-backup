import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

/** 下载阶段流转审计 CSV（后端直接返回 text/csv，非 ApiResponse 包装） */
export async function downloadStageLogsCsv(accessToken: string | null) {
  const resp = await axios.get(`${baseURL}/admin/audit/export`, {
    responseType: 'blob',
    withCredentials: true,
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
  })
  const blob = new Blob([resp.data], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `stage-logs-${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
