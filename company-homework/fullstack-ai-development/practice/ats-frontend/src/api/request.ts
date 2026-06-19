/* oxlint-disable promise/prefer-await-to-callbacks, promise/no-promise-in-callback, promise/avoid-new -- axios interceptors are callback-based by design */
import type {
  AxiosError,
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from 'axios'
import axios from 'axios'

/** 后端统一响应结构 */
export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T | null
}

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

const request: AxiosInstance = axios.create({
  baseURL,
  timeout: 15000,
  withCredentials: true, // 携带 HttpOnly refresh cookie
  headers: { 'Content-Type': 'application/json' },
})

// ── 防止 refresh 并发 ────────────────────────────────────────
let isRefreshing = false
let pendingQueue: Array<(token: string | null) => void> = []

function drainQueue(token: string | null) {
  pendingQueue.forEach(cb => cb(token))
  pendingQueue = []
}

/**
 * 懒加载 useAuthStore，打破 stores/auth → request → stores/auth 循环依赖。
 * Vite 会将模块缓存，首次后直接返回缓存，无性能问题。
 */
async function getAuthStore() {
  const { useAuthStore } = await import('@/stores/auth')
  return useAuthStore()
}

/* ── 请求拦截器：注入 Bearer token ─────────────────────────── */
request.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    try {
      const auth = await getAuthStore()
      if (auth.accessToken) {
        config.headers.Authorization = `Bearer ${auth.accessToken}`
      }
    }
    catch {
      // pinia 未初始化时忽略（测试 / SSR 保护）
    }
    return config
  },
  err => Promise.reject(err),
)

/* ── 响应拦截器：解包 { code, msg, data } + 401 自动 refresh ── */
request.interceptors.response.use(
  (resp: AxiosResponse<ApiResponse>) => {
    const body = resp.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0)
        return body.data as never
      return Promise.reject(new BizError(body.code, body.msg, body))
    }
    return resp.data as never
  },
  async (err: AxiosError<ApiResponse>) => {
    const status = err.response?.status
    const body = err.response?.data
    const code = body?.code ?? status ?? 0
    const msg = body?.msg ?? err.message ?? '网络错误'

    const originalConfig = err.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // 401 且不是认证端点自身 → 尝试 silent refresh
    const isAuthEndpoint
      = originalConfig?.url?.includes('/auth/refresh')
        || originalConfig?.url?.includes('/auth/login')
        || originalConfig?.url?.includes('/auth/register')

    if ((status === 401 || code === 10001) && !originalConfig?._retry && !isAuthEndpoint) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push((newToken) => {
            if (newToken) {
              originalConfig.headers.Authorization = `Bearer ${newToken}`
              originalConfig._retry = true
              resolve(request(originalConfig))
            }
            else {
              reject(new BizError(10001, '未登录或登录已过期'))
            }
          })
        })
      }

      isRefreshing = true
      if (originalConfig) originalConfig._retry = true

      try {
        const auth = await getAuthStore()
        const ok = await auth.silentRefresh()
        const newToken = ok ? auth.accessToken : null
        drainQueue(newToken)

        if (newToken && originalConfig) {
          originalConfig.headers.Authorization = `Bearer ${newToken}`
          return request(originalConfig)
        }

        redirectToLogin()
        return Promise.reject(new BizError(10001, '登录已过期，请重新登录'))
      }
      finally {
        isRefreshing = false
      }
    }

    return Promise.reject(new BizError(code, msg, body ?? undefined))
  },
)

function redirectToLogin() {
  if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
    window.location.href = '/login'
  }
}

export class BizError extends Error {
  code: number
  body?: ApiResponse | undefined
  constructor(code: number, msg: string, body?: ApiResponse) {
    super(msg)
    this.code = code
    this.body = body
  }
}

/** 类型化助手：拿到的就是 data，已剥壳 */
export function get<T = unknown>(url: string, config?: AxiosRequestConfig) {
  return request.get<unknown, T>(url, config)
}
export function post<T = unknown, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig) {
  return request.post<unknown, T>(url, data, config)
}
export function put<T = unknown, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig) {
  return request.put<unknown, T>(url, data, config)
}
export function patch<T = unknown, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig) {
  return request.patch<unknown, T>(url, data, config)
}
export function del<T = unknown>(url: string, config?: AxiosRequestConfig) {
  return request.delete<unknown, T>(url, config)
}

export default request
