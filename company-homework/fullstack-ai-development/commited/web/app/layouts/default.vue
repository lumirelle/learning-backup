<script lang="ts" setup>
import { useAuthStore } from '~/composables/store/useAuthStore'

const auth = useAuthStore()
const route = useRoute()
onMounted(() => {
  if (!auth.user)
    auth.fetchMe()
})
async function onLogout() {
  auth.logout()
  await navigateTo('/login')
}

const current = computed(() => matchNav(route.path))
const initial = computed(() => (auth.user?.name || '·').slice(0, 1))
</script>

<template>
  <div class="bg-[#f5f6f8] flex h-screen overflow-hidden dark:bg-ink-950">
    <HSidebar />
    <div class="flex flex-1 flex-col min-w-0">
      <header class="px-6 border-b border-black/6 bg-white/85 flex shrink-0 h-13 items-center justify-between backdrop-blur dark:border-white/8 dark:bg-ink-900/85">
        <div class="text-sm flex gap-2 items-center">
          <span class="text-truegray-300 dark:text-truegray-500">组织人事</span>
          <span class="i-carbon-chevron-right text-xs text-truegray-300 dark:text-truegray-600" />
          <span class="text-truegray-700 font-medium dark:text-truegray-200">{{ current?.label || '—' }}</span>
        </div>
        <div class="flex gap-3 items-center">
          <HGlobalSearch />
          <ToggleColorMode class="text-truegray-400 transition-colors hover:text-truegray-600 dark:hover:text-truegray-200" />
          <div class="bg-black/8 h-4 w-px dark:bg-white/12" />
          <div class="flex gap-2.5 items-center">
            <div class="text-13px text-primary font-semibold rounded-full bg-primary-50 flex h-7.5 w-7.5 items-center justify-center dark:text-primary-300 dark:bg-primary-500/15">
              {{ initial }}
            </div>
            <div class="leading-tight">
              <div class="text-13px text-truegray-800 font-medium dark:text-truegray-100">
                {{ auth.user?.name || '—' }}
              </div>
              <div v-if="auth.user?.roles?.length" class="text-11px text-truegray-400">
                {{ auth.user.roles[0] }}
              </div>
            </div>
          </div>
          <button
            class="text-truegray-400 p-1.5 rounded-lg cursor-pointer transition-colors hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10"
            title="退出登录"
            @click="onLogout"
          >
            <span class="i-carbon-logout block" />
          </button>
        </div>
      </header>
      <main class="p-6 flex-1 overflow-auto">
        <div class="mx-auto max-w-300">
          <slot />
        </div>
      </main>
    </div>
  </div>
</template>
