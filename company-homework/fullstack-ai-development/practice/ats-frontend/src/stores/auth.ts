import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth';
import type { MeVO } from '@/api/auth';

export type UserRole = 'ADMIN' | 'HR' | 'CANDIDATE'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const user = ref<MeVO | null>(null)
  /** 应用启动后是否已经尝试过 silent refresh —— 避免每次路由跳转都重复调 /auth/refresh */
  let initializePromise: Promise<void> | null = null

  const isLoggedIn = computed(() => !!accessToken.value)
  const role = computed<UserRole | null>(() => user.value?.role ?? null)
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isHr = computed(() => role.value === 'HR')
  const isCandidate = computed(() => role.value === 'CANDIDATE')

  function setTokens(token: string, u: MeVO) {
    accessToken.value = token
    user.value = u
  }

  function clearTokens() {
    accessToken.value = null
    user.value = null
  }

  async function login(email: string, password: string) {
    const data = await authApi.login({ email, password })
    setTokens(data.accessToken, data.user)
    return data
  }

  async function logout() {
    try {
      await authApi.logout()
    }
    finally {
      clearTokens()
    }
  }

  /** silent refresh: 用 cookie 换新 access token，失败则清状态 */
  async function silentRefresh(): Promise<boolean> {
    try {
      const data = await authApi.refresh()
      setTokens(data.accessToken, data.user)
      return true
    }
    catch {
      clearTokens()
      return false
    }
  }

  /**
   * 应用启动 / 页面刷新后调用一次：用 httpOnly cookie 中的 refresh token 兑换 access token。
   * - 全局只跑一次：用 `initializePromise` 缓存正在进行 / 已完成的调用，多次并发调用安全
   * - 已登录则直接返回（避免不必要的网络请求）
   */
  async function initialize(): Promise<void> {
    if (isLoggedIn.value) return
    if (initializePromise) return initializePromise
    initializePromise = (async () => {
      await silentRefresh()
    })()
    return initializePromise
  }

  return {
    accessToken,
    user,
    isLoggedIn,
    role,
    isAdmin,
    isHr,
    isCandidate,
    login,
    logout,
    silentRefresh,
    initialize,
    setTokens,
    clearTokens,
  }
})
