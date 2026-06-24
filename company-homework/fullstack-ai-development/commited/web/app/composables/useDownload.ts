// 带 Bearer token 的鉴权文件下载：拉取二进制流 → 触发浏览器“另存为”。
// 用于 $api（信封解包）不适用的二进制端点（xlsx 导出/模板下载）。

/**
 * 以当前登录态下载 path 指向的文件并保存为 filename。
 * @throws 当响应非 2xx 时抛出错误（调用方可捕获以提示用户）。
 */
export async function downloadAuthed(path: string, filename: string): Promise<void> {
  const token = useCookie('token').value
  const res = await fetch(path, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!res.ok)
    throw new Error(`下载失败（${res.status}）`)
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

/** 生成带日期后缀的导出文件名，如 roster-2026-06-11.xlsx。 */
export function dateStampedName(prefix: string, ext = 'xlsx'): string {
  return `${prefix}-${new Date().toISOString().slice(0, 10)}.${ext}`
}
