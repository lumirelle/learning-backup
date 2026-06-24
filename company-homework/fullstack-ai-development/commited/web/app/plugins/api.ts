/**
 * 自定义 `$fetch`（`$api`），统一对接 Go 后端（/api/v1）：
 * 1. baseURL = `/api`（开发期由 nitro.devProxy 反代 /api/v1 → Go）；
 * 2. 注入 `Authorization: Bearer <token>`（token 存 cookie）；
 * 3. 解包统一响应信封 `{ code, message, data }` → 直接返回 `data`；
 * 4. 401/403 跳转登录页。
 *
 * @see https://nuxt.com/docs/4.x/guide/recipes/custom-usefetch#recipe-custom-fetch-instance
 */
import type { FetchRequest } from 'ofetch'
import type { Envelope } from '~/utils/api'
import { unwrapEnvelope } from '~/utils/api'

const BASE_URL = '/api'

export type ApiNitroFetchRequest = FetchRequest | (string & {})

// $api 的类型：传入路径，返回解包后的 data。
type ApiFetch = (<T = unknown>(
  request: ApiNitroFetchRequest,
  opts?: Parameters<typeof $fetch>[1],
) => Promise<T>) & { raw: any, create: any }

declare module '#app' {
  interface NuxtApp {
    $api: ApiFetch
  }
}
declare module 'vue' {
  interface ComponentCustomProperties {
    $api: ApiFetch
  }
}

export default defineNuxtPlugin((nuxtApp) => {
  const api = $fetch.create({
    baseURL: BASE_URL,
    onRequest({ options }) {
      const token = useCookie('token').value
      if (token) {
        options.headers.set('Authorization', `Bearer ${token}`)
      }
    },
    // 成功响应：解包信封，把 data 提到顶层
    onResponse({ response }) {
      response._data = unwrapEnvelope(response._data)
    },
    async onResponseError({ response }) {
      const body = response._data as Envelope | undefined
      const msg = body?.message || response.statusText
      if (response.status === 401 || response.status === 403) {
        const token = useCookie('token')
        token.value = null
        await nuxtApp.runWithContext(() => navigateTo('/login'))
      }
      console.error('API error:', response.status, msg)
    },
  }) as unknown as ApiFetch

  return { provide: { api } }
})
