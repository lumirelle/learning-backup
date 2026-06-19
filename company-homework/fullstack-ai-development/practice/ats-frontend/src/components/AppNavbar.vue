<script setup lang="ts">
import { NDropdown, NIcon } from 'naive-ui'
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const ROLE_LABEL: Record<string, string> = {
  ADMIN: '管理员',
  HR: 'HR',
  CANDIDATE: '候选人',
}

const ROLE_BADGE: Record<string, string> = {
  ADMIN: 'bg-[rgba(251,191,36,.15)] text-[#d97706]',
  HR: 'bg-[rgba(16,185,129,.15)] text-[#047857]',
  CANDIDATE: 'bg-[rgba(34,211,238,.15)] text-[#0891b2]',
}

const avatarBg = computed(() => {
  const role = auth.role ?? ''
  if (role === 'ADMIN')
    return 'linear-gradient(135deg,#fbbf24,#f97316)'
  if (role === 'HR')
    return 'linear-gradient(135deg,#34d399,#14b8a6)'
  return 'linear-gradient(135deg,#22d3ee,#3b82f6)'
})

const initial = computed(() => (auth.user?.fullName ?? 'U')[0].toUpperCase())

const navLinks = computed(() => {
  const links: Array<{ to: string, label: string, icon: string }> = [
    { to: '/home', label: '首页', icon: 'M3 12 L12 4 L21 12 M5 10 V20 H19 V10' },
    { to: '/jobs', label: '岗位市场', icon: 'M4 8 H20 V20 H4 Z M9 8 V5 a2 2 0 0 1 2-2 h2 a2 2 0 0 1 2 2 V8' },
  ]
  // HR / Admin：岗位管理台 + 招聘看板 + 数据看板
  if (auth.isHr || auth.isAdmin) {
    links.push({
      to: '/hr/jobs',
      label: '岗位管理',
      icon: 'M4 7 H20 M4 12 H20 M4 17 H14',
    })
    links.push({
      to: '/hr/board',
      label: '招聘看板',
      icon: 'M5 5 H9 V19 H5 Z M11 5 H15 V14 H11 Z M17 5 H19 V11 H17 Z',
    })
    links.push({
      to: '/hr/dashboard',
      label: '数据看板',
      icon: 'M4 19 V5 M4 19 H20 M8 15 V11 M12 15 V8 M16 15 V13',
    })
  }
  // ADMIN 专属：账号管理 + 系统健康
  if (auth.isAdmin) {
    links.push({
      to: '/admin/users',
      label: '账号管理',
      icon: 'M16 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0ZM4 21a8 8 0 0 1 16 0 M19 8 V14 M16 11 H22',
    })
    links.push({
      to: '/admin/departments',
      label: '部门管理',
      icon: 'M12 3 L4 9 V21 H20 V9 Z M12 3 V9 H20 M8 13 H16 M8 17 H13',
    })
    links.push({
      to: '/health',
      label: '系统健康',
      icon: 'M3 12 H7 L9 5 L13 19 L15 12 H21',
    })
  }
  // CANDIDATE：我的投递
  if (auth.isCandidate) {
    links.push({
      to: '/me/applications',
      label: '我的投递',
      icon: 'M5 4 H15 L19 8 V20 H5 Z M14 4 V8 H19 M8 12 H16 M8 16 H13',
    })
  }
  return links
})

function renderIcon(svg: string) {
  return () => h(NIcon, null, {
    default: () => h('svg', { viewBox: '0 0 24 24', fill: 'none' }, [
      h('path', { 'd': svg, 'stroke': 'currentColor', 'stroke-width': 1.6, 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
    ]),
  })
}

/**
 * 用户菜单 —— 移除「个人资料 / 快捷键」永久 disabled 占位项（看起来像坏链）。
 * 等到对应功能上线再加回来。
 */
const menuOptions = computed(() => [
  {
    key: 'header',
    type: 'render',
    render: () => h('div', { class: 'px-3 py-2.5 border-b border-default' }, [
      h('p', { class: 'text-11px uppercase tracking-widest text-tertiary mb-0.5' }, '当前账户'),
      h('p', { class: 'text-sm font-semibold text-primary truncate' }, auth.user?.email ?? ''),
    ]),
  },
  {
    label: auth.isCandidate ? '我的投递' : '我的工作台',
    key: 'me-shortcut',
    icon: renderIcon('M5 4 H15 L19 8 V20 H5 Z M14 4 V8 H19 M8 12 H16 M8 16 H13'),
  },
  ...(auth.isCandidate
    ? [{
        label: '个人资料',
        key: 'profile',
        icon: renderIcon('M12 12 A4 4 0 1 0 12 4 A4 4 0 0 0 12 12 M4 20 A8 8 0 0 1 20 20'),
      }]
    : []),
  {
    label: '修改密码',
    key: 'settings',
    icon: renderIcon('M12 15 V17 M8 11 H16 A4 4 0 0 0 12 7 A4 4 0 0 0 8 11 H8 A4 4 0 0 0 12 15 A4 4 0 0 0 16 11'),
  },
  { type: 'divider', key: 'd1' },
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon('M15 12 H3 M3 12 L7 8 M3 12 L7 16 M11 4 H17 A2 2 0 0 1 19 6 V18 A2 2 0 0 1 17 20 H11'),
  },
])

async function handleSelect(key: string) {
  if (key === 'logout') {
    await auth.logout()
    router.replace('/login')
    return
  }
  if (key === 'me-shortcut') {
    router.push(auth.isCandidate ? '/me/applications' : '/hr/board')
    return
  }
  if (key === 'profile') {
    router.push('/me/profile')
    return
  }
  if (key === 'settings') {
    router.push('/me/settings')
  }
}

function isActive(path: string) {
  return route.path.startsWith(path)
}
</script>

<template>
  <header group fixed inset-x-0 top-0 z-50 h-60px>
    <!-- 玻璃背景 -->
    <div navbar-glass absolute inset-0 />
    <!-- 顶部 1px 发光分隔条（hover navbar 出现） -->
    <div navbar-glow-line group-hover:opacity-100 />

    <div relative flex h-full items-center justify-between px-6>
      <!-- ── Logo ───────────────────────────────── -->
      <router-link to="/home" class="group/logo" flex items-center gap-2.5 no-underline aria-label="ATS · 返回首页">
        <span class="group-hover/logo:rotate-[-8deg] group-hover/logo:scale-106 group-hover/logo:shadow-[0_0_24px_rgba(16,185,129,.6),inset_0_1px_0_rgba(255,255,255,.3)]" logo-mark>
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M2 10 L7 3 L12 10" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M4.5 10 L7 6.5 L9.5 10" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" opacity="0.5" />
          </svg>
        </span>
        <span font-display text-base font-bold tracking-wide text-primary transition-opacity class="group-hover/logo:opacity-75">
          ATS
        </span>
        <span version-pill ml-1>v0.1</span>
      </router-link>

      <!-- ── Center nav ─────────────────────────── -->
      <nav
        aria-label="主导航"
        absolute left="1/2" flex translate-x="-1/2" items-center gap-1 rounded-xl border border-default bg="elevated/40" p-1 backdrop-blur-sm
      >
        <router-link
          v-for="item in navLinks"
          :key="item.to"
          :to="item.to"
          :aria-current="isActive(item.to) ? 'page' : undefined"
          relative flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium no-underline transition-all
          :class="isActive(item.to)
            ? 'text-primary bg-elevated shadow-[0_1px_2px_rgba(0,0,0,.05),inset_0_0_0_1px_var(--border)]'
            : 'text-tertiary hover:(text-secondary bg-hover)'"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path :d="item.icon" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          {{ item.label }}
        </router-link>
      </nav>

      <!-- ── Right ──────────────────────────────── -->
      <div flex items-center gap-3>
        <ThemeToggle />
        <template v-if="auth.isLoggedIn">
          <NDropdown
            :options="menuOptions"
            trigger="click"
            placement="bottom-end"
            :show-arrow="true"
            @select="handleSelect"
          >
            <button
              type="button"
              user-trigger
              class="group/user"
              aria-haspopup="menu"
              :aria-label="`账户菜单 · ${auth.user?.fullName ?? ''}（${ROLE_LABEL[auth.role ?? ''] ?? ''}）`"
            >
              <span avatar :style="`background:${avatarBg}`" aria-hidden="true">{{ initial }}</span>
              <div hidden flex-col items-start leading-none sm:flex>
                <span text-13px font-semibold text-primary>{{ auth.user?.fullName }}</span>
                <span mt-0.5 inline-flex items-center gap-1>
                  <span
                    inline-block px-5px py-px text-10px font-semibold leading-tight rounded-3px
                    :class="ROLE_BADGE[auth.role ?? '']"
                  >
                    {{ ROLE_LABEL[auth.role ?? ''] ?? auth.role }}
                  </span>
                </span>
              </div>
              <svg ml-0.5 text-tertiary opacity-60 transition-transform class="group-hover/user:rotate-180" width="12" height="12" viewBox="0 0 12 12" fill="none">
                <path d="M3 4.5 L6 7.5 L9 4.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </button>
          </NDropdown>
        </template>

        <template v-else>
          <router-link
            to="/login"
            px-14px py-2 text-13px font-medium rounded-10px no-underline text-secondary transition-all duration-base ease-out hover:text-primary hover:bg-hover
          >
            登录
          </router-link>
          <router-link to="/register" btn-cta class="group/cta">
            <span relative z-10>免费注册</span>
            <svg relative z-10 transition-transform class="group-hover/cta:translate-x-0.5" width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M3 7 H11 M7 3 L11 7 L7 11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <span cta-glow />
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>
