import type { RouteRecordRaw } from 'vue-router'
import { nextTick } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { loadingBarRef } from '@/utils/loading-bar'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    transition?: string
    requiresAuth?: boolean
    roles?: Array<'ADMIN' | 'HR' | 'CANDIDATE'>
    hideNavbar?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home.vue'),
    meta: { title: '首页', requiresAuth: false, transition: 'fade-slide' },
  },
  {
    path: '/health',
    name: 'Health',
    component: () => import('@/views/health.vue'),
    meta: { title: 'Health · 服务检查', requiresAuth: false, transition: 'slide-up' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login.vue'),
    meta: { title: '登录', requiresAuth: false, hideNavbar: true, transition: 'slide-right' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register.vue'),
    meta: { title: '注册', requiresAuth: false, hideNavbar: true, transition: 'slide-right' },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/forbidden.vue'),
    meta: { title: '无权访问', requiresAuth: false, transition: 'fade-scale' },
  },
  // ── M2 · 岗位市场（候选人 / 公开浏览）──
  {
    path: '/jobs',
    name: 'Jobs',
    component: () => import('@/views/jobs.vue'),
    meta: {
      title: '岗位市场',
      requiresAuth: false,
      transition: 'fade-slide',
    },
  },
  // ── M2 · 岗位管理（HR / Admin）──
  {
    path: '/hr/jobs',
    name: 'HrJobs',
    component: () => import('@/views/hr/jobs.vue'),
    meta: {
      title: 'HR · 岗位管理',
      requiresAuth: true,
      roles: ['HR', 'ADMIN'],
      transition: 'fade-slide',
    },
  },
  // ── M3 · 候选人「我的投递」──
  {
    path: '/me/applications',
    name: 'MyApplications',
    component: () => import('@/views/me/applications.vue'),
    meta: {
      title: '我的投递',
      requiresAuth: true,
      roles: ['CANDIDATE'],
      transition: 'fade-slide',
    },
  },
  {
    path: '/me/profile',
    name: 'MyProfile',
    component: () => import('@/views/me/profile.vue'),
    meta: {
      title: '个人资料',
      requiresAuth: true,
      transition: 'fade-slide',
    },
  },
  {
    path: '/me/settings',
    name: 'MySettings',
    component: () => import('@/views/me/settings.vue'),
    meta: {
      title: '修改密码',
      requiresAuth: true,
      transition: 'fade-slide',
    },
  },
  // ── M3 · 招聘看板（HR / Admin · 状态机拖拽）──
  {
    path: '/hr/board',
    name: 'HrBoard',
    component: () => import('@/views/hr/board.vue'),
    meta: {
      title: 'HR · 招聘看板',
      requiresAuth: true,
      roles: ['HR', 'ADMIN'],
      transition: 'fade-slide',
    },
  },
  // ── M5 · 数据看板（HR / Admin · 招聘漏斗 + 4 指标）──
  {
    path: '/hr/dashboard',
    name: 'HrDashboard',
    component: () => import('@/views/hr/dashboard.vue'),
    meta: {
      title: 'HR · 数据看板',
      requiresAuth: true,
      roles: ['HR', 'ADMIN'],
      transition: 'fade-slide',
    },
  },
  // ── Admin · 账号管理（单个 / 批量创建 HR）──
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/users.vue'),
    meta: {
      title: 'Admin · 账号管理',
      requiresAuth: true,
      roles: ['ADMIN'],
      transition: 'fade-slide',
    },
  },
  {
    path: '/admin/departments',
    name: 'AdminDepartments',
    component: () => import('@/views/admin/departments.vue'),
    meta: {
      title: 'Admin · 部门管理',
      requiresAuth: true,
      roles: ['ADMIN'],
      transition: 'fade-slide',
    },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/not-found.vue'),
    meta: { transition: 'fade-scale' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  async scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) return savedPosition

    if (to.hash) {
      await nextTick()
      return {
        el: to.hash,
        top: 76,
        behavior: 'smooth',
      }
    }

    return { top: 0 }
  },
})

router.beforeEach(async (to, _from, next) => {
  loadingBarRef.value?.start()

  if (typeof to.meta.title === 'string') {
    document.title = `${to.meta.title} · ATS`
  }

  // 延迟引入 store 避免循环依赖
  const { useAuthStore } = await import('@/stores/auth')
  const auth = useAuthStore()

  // 应用启动 / 页面刷新后恢复登录态（initialize 内部会防重入，全局只跑一次）。
  // 必须无条件调用：公开页面（/home /jobs /health 等）也要识别已登录身份用于显示头像 / 切换 CTA
  await auth.initialize()

  const requiresAuth = to.meta.requiresAuth !== false // 默认需要登录
  const roles = to.meta.roles

  if (requiresAuth && !auth.isLoggedIn) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  if (roles && auth.role && !roles.includes(auth.role)) {
    return next({ name: 'Forbidden' })
  }

  // 已登录用户访问 login/register → 跳到首页
  if ((to.name === 'Login' || to.name === 'Register') && auth.isLoggedIn) {
    return next({ name: 'Home' })
  }

  next()
})

router.afterEach(() => {
  loadingBarRef.value?.finish()
})

router.onError(() => {
  loadingBarRef.value?.error()
})

export default router
