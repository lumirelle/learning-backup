import { acceptHMRUpdate, defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  // token 存 cookie，供 $api 插件读取
  const token = useCookie<string | null>('token', { default: () => null })
  // SSO 回传的登录 token：注销时回传后端，由后端通知 SSO 失效（见 docs/sso/login.md）
  const ssoToken = useCookie<string | null>('sso_token', { default: () => null })
  const user = ref<Hr.User | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => !!user.value?.is_admin)

  async function login(username: string, password: string): Promise<void> {
    const { $api } = useNuxtApp()
    const data = await $api<{ token: string, user: Hr.User }>('/v1/auth/login', {
      method: 'POST',
      body: { username, password },
    })
    token.value = data.token
    user.value = data.user
  }

  /** SSO 回调登录：用 SSO 回传的 token 换取本系统会话 */
  async function loginWithSSO(t: string): Promise<void> {
    const { $api } = useNuxtApp()
    const data = await $api<{ token: string, user: Hr.User }>('/v1/auth/sso/login', {
      method: 'POST',
      body: { token: t },
    })
    token.value = data.token
    user.value = data.user
    ssoToken.value = t
  }

  async function fetchMe(): Promise<void> {
    if (!token.value)
      return
    const { $api } = useNuxtApp()
    try {
      user.value = await $api<Hr.User>('/v1/auth/me')
    }
    catch {
      await logout()
    }
  }

  async function logout(): Promise<void> {
    // SSO 登录态：尽力通知后端注销 SSO token，失败不阻塞本地登出
    if (ssoToken.value) {
      const { $api } = useNuxtApp()
      await $api('/v1/auth/sso/logout', { method: 'POST', body: { token: ssoToken.value } })
      ssoToken.value = null
    }
    token.value = null
    user.value = null
  }

  return { token, user, isLoggedIn, isAdmin, login, loginWithSSO, fetchMe, logout }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAuthStore, import.meta.hot))
}
