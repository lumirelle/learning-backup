<script setup lang="ts">
import { NButton } from 'naive-ui'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function goBack() {
  router.back()
}
function goHome() {
  router.replace('/home')
}
async function switchAccount() {
  // 退出当前登录并跳到登录页（保留 redirect 让登录后回到目标页或首页）
  await auth.logout()
  router.replace({ path: '/login', query: { redirect: '/home' } })
}
</script>

<template>
  <main min-h-screen flex-col flex="~ items-center justify-center" bg-app px-4 pt-60px text-center>
    <p kicker mb-4>
      403 · Forbidden
    </p>
    <div text-7rem font-display font-bold leading-none text-gradient mb-4 select-none aria-hidden="true">
      403
    </div>
    <h1 text-2xl font-bold text-primary mb-3>
      无权访问
    </h1>
    <p text-tertiary mb-8 max-w-420px leading-relaxed>
      您的角色 <strong text-primary>{{ auth.role ?? '未知' }}</strong> 没有访问此页面的权限。<br>
      可以返回上一页继续操作，或切换账号试试看。
    </p>
    <div flex="~ items-center wrap" gap-3 justify-center>
      <NButton size="medium" @click="goBack">
        返回上一页
      </NButton>
      <NButton size="medium" type="primary" @click="goHome">
        回到首页
      </NButton>
      <NButton v-if="auth.isLoggedIn" size="medium" tertiary @click="switchAccount">
        切换账号 →
      </NButton>
    </div>
  </main>
</template>
