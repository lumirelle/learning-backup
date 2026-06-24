// 后端统一响应信封 `{ code, message, data }` 的解包逻辑（抽成纯函数，便于单测与复用）。

export interface Envelope<T = unknown> {
  code: number
  message: string
  data: T
  request_id?: string
}

/** 判断响应体是否为统一信封结构。 */
export function isEnvelope(body: unknown): body is Envelope {
  return !!body && typeof body === 'object' && 'code' in body && 'data' in body
}

/** 解包信封：是信封则提取 data，否则原样返回（兼容裸响应/二进制等非信封情形）。 */
export function unwrapEnvelope<T = unknown>(body: unknown): T {
  return (isEnvelope(body) ? body.data : body) as T
}
