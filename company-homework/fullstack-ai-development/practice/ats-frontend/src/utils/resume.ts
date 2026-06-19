/**
 * 简历 URL 工具：候选人投递时上传到 /uploads/resumes/yyyy-MM/<uuid>.pdf，
 * 下载走 /files/... 路径（带鉴权 + 限流）。
 *
 * 设计目的：避免 hr/board.vue 与 me/applications.vue 各写一份导致漂移。
 */
const FILES_BASE = (() => {
  const base = import.meta.env.VITE_API_BASE_URL || '/api/v1'
  return base.replace(/\/+$/, '')
})()

export function resumeDownloadUrl(uploadsUrl: string): string {
  if (uploadsUrl.startsWith('/uploads/')) {
    return `${FILES_BASE}/files/${uploadsUrl.substring('/uploads/'.length)}`
  }
  return uploadsUrl
}

export function isResumeFile(url: string | null | undefined): boolean {
  return !!url && url.startsWith('/uploads/')
}
