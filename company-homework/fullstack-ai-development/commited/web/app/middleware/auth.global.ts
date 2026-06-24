// 全局鉴权守卫：未登录跳登录页，已登录访问登录页则回首页。
export default defineNuxtRouteMiddleware((to) => {
  const token = useCookie('token').value
  if (!token && to.path !== '/login')
    return navigateTo('/login')
  if (token && to.path === '/login')
    return navigateTo('/')
})
