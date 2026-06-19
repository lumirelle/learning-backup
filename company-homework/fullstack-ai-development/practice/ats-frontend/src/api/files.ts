import { post } from './request'

export interface UploadVO {
  url: string
  size: number
  originalName: string
  contentType: string
}

export type FileCategory = 'RESUME'

/**
 * 后端只接受 PDF + 5MB，前端再做一次本地校验给即时反馈，避免无效请求白跑后端 + 占用网络。
 * 与后端 {@code FileCategory.RESUME} 白名单保持一致。
 */
export const UPLOAD_LIMITS = {
  RESUME: {
    accept: 'application/pdf,.pdf',
    maxBytes: 5 * 1024 * 1024,
    label: 'PDF',
  },
} as const

export class FileValidationError extends Error {
  constructor(public readonly reason: 'TYPE' | 'SIZE', message: string) {
    super(message)
  }
}

export const filesApi = {
  /**
   * 上传文件 · multipart/form-data。axios 在检测到 FormData 时会自动生成 boundary，
   * 我们仍显式声明 Content-Type 以覆盖 request.ts 默认的 application/json。
   */
  async upload(file: File, category: FileCategory = 'RESUME'): Promise<UploadVO> {
    // 本地预校验：类型 + 大小，命中直接抛 FileValidationError，避免白跑后端
    const limits = UPLOAD_LIMITS[category]
    if (file.size > limits.maxBytes) {
      throw new FileValidationError(
        'SIZE',
        `文件超过限制（最大 ${(limits.maxBytes / 1024 / 1024).toFixed(0)}MB）`,
      )
    }
    if (category === 'RESUME' && file.type !== 'application/pdf') {
      throw new FileValidationError('TYPE', '仅支持 PDF 格式简历')
    }

    const form = new FormData()
    form.append('file', file)
    form.append('category', category)

    return post<UploadVO>('/files/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
