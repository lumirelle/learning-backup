<script setup lang="ts">
import { useAuthStore } from '~/composables/store/useAuthStore'

definePageMeta({ layout: 'blank' })

const auth = useAuthStore()
const route = useRoute()
const username = ref('super_admin')
const password = ref('ChangeMe@123')
const loading = ref(false)
const error = ref('')

// ---- SSO ----
const ssoEnabled = ref(false)
const ssoLoginUrl = ref('')
// 回调地址上带 ?token= 时表示 SSO 登录页跳转回来，正在换取本系统会话
const ssoLoading = ref(typeof route.query.token === 'string' && !!route.query.token)

onMounted(async () => {
  const { $api } = useNuxtApp()

  // SSO 回调：用回传 token 换本系统会话
  const cbToken = route.query.token
  if (typeof cbToken === 'string' && cbToken) {
    try {
      await auth.loginWithSSO(cbToken)
      await navigateTo('/', { replace: true })
      return
    }
    catch (e: any) {
      error.value = e?.data?.message || 'SSO 登录失败'
      ssoLoading.value = false
      await navigateTo('/login', { replace: true }) // 清掉地址栏里的一次性 token
    }
  }

  // 是否展示 SSO 入口（未配置时后端返回 enabled:false）
  try {
    const cfg = await $api<{ enabled: boolean, login_url?: string }>('/v1/auth/sso/config')
    ssoEnabled.value = cfg.enabled
    ssoLoginUrl.value = cfg.login_url || ''
  }
  catch { /* 后端不可达时仅隐藏入口 */ }
})

function gotoSSO() {
  const cb = encodeURIComponent(`${location.origin}/login`)
  location.href = `${ssoLoginUrl.value}&cb=${cb}`
}

const demos = [
  { u: 'super_admin', label: '超级管理员' },
  { u: 'hr01', label: '人事管理员' },
  { u: 'mgr01', label: '部门经理' },
  { u: 'emp01', label: '普通员工' },
]

const features = [
  { icon: 'i-carbon-tree-view-alt', text: '组织 / 花名册 / 档案，10 个功能域全覆盖' },
  { icon: 'i-carbon-flow', text: '入离调转全流程审批闭环，事务可追溯' },
  { icon: 'i-carbon-locked', text: '基于组织子树的数据权限，按角色隔离' },
]

async function onSubmit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    await navigateTo('/')
  }
  catch (e: any) {
    error.value = e?.data?.message || e?.message || '登录失败'
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="rounded-2xl grid grid-cols-1 max-w-full w-220 shadow-2xl shadow-ink/20 overflow-hidden md:grid-cols-2">
    <!-- 品牌面板 -->
    <div class="p-10 bg-ink flex-col hidden justify-between relative overflow-hidden md:flex">
      <!-- 点阵肌理 -->
      <div
        class="inset-0 absolute"
        style="background-image: radial-gradient(rgba(255,255,255,0.07) 1px, transparent 1px); background-size: 22px 22px; mask-image: radial-gradient(ellipse at 30% 20%, black 0%, transparent 70%);"
      />
      <div class="relative">
        <div class="flex gap-3 items-center">
          <div class="rounded-xl bg-primary flex h-10 w-10 shadow-lg shadow-primary/40 items-center justify-center">
            <span class="i-carbon-enterprise text-xl text-white" />
          </div>
          <div class="text-2xl text-white tracking-wide font-serif">
            OrgHR
          </div>
        </div>
        <h1 class="text-xl text-white leading-relaxed font-bold mt-10">
          组织人事管理系统
        </h1>
        <p class="text-sm text-ink-400 leading-relaxed mt-2">
          一套覆盖组织、人员、事务与洞察的<br>人力资源数字化工作台。
        </p>
      </div>
      <ul class="flex flex-col gap-3.5 relative">
        <li v-for="f in features" :key="f.text" class="text-13px text-ink-300 flex gap-2.5 items-center">
          <span class="text-primary-400 shrink-0" :class="f.icon" />
          {{ f.text }}
        </li>
      </ul>
    </div>

    <!-- 登录表单 -->
    <div class="p-10 bg-white dark:bg-ink-900">
      <div class="text-lg text-truegray-900 font-bold dark:text-white">
        欢迎回来
      </div>
      <p class="text-13px text-truegray-400 mt-1">
        请使用账号密码登录
      </p>

      <!-- SSO 回调换取会话中 -->
      <div v-if="ssoLoading" class="text-sm text-truegray-500 mt-7 flex gap-2 items-center">
        <span class="i-carbon-circle-dash animate-spin" />正在通过企业 SSO 登录…
      </div>

      <form v-show="!ssoLoading" class="mt-7 flex flex-col gap-4" @submit.prevent="onSubmit">
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">账号</span>
          <input v-model="username" autocomplete="username" class="input-base">
        </label>
        <label class="text-sm flex flex-col gap-1.5">
          <span class="text-13px text-truegray-500 font-medium">密码</span>
          <input v-model="password" type="password" autocomplete="current-password" class="input-base">
        </label>

        <p v-if="error" class="text-sm text-rose-500 flex gap-1.5 items-center">
          <span class="i-carbon-warning-alt" />{{ error }}
        </p>

        <button type="submit" :disabled="loading" class="btn-primary mt-1 w-full">
          {{ loading ? '登录中…' : '登 录' }}
        </button>

        <button
          v-if="ssoEnabled" type="button" class="btn-secondary w-full"
          @click="gotoSSO"
        >
          <span class="i-carbon-badge mr-1.5" />使用企业 SSO 登录
        </button>
      </form>

      <div class="mt-7 pt-5 border-t border-black/6 dark:border-white/8">
        <p class="text-xs text-truegray-400 mb-2.5">
          演示账号（点击填充，密码统一 ChangeMe@123）
        </p>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="d in demos" :key="d.u"
            class="text-xs text-truegray-500 px-2.5 py-1 border border-black/8 rounded-full cursor-pointer transition-colors dark:text-truegray-400 hover:text-primary dark:border-white/10 hover:border-primary/40"
            @click="username = d.u; password = 'ChangeMe@123'"
          >
            {{ d.label }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
