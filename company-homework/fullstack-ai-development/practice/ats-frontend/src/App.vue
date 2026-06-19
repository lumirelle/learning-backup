<script setup lang="ts">
import { dateZhCN, darkTheme, NConfigProvider, NDialogProvider, NLoadingBarProvider, NMessageProvider, NNotificationProvider, useLoadingBar, zhCN } from 'naive-ui'
import { computed, defineComponent, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import AppNavbar from './components/AppNavbar.vue'
import ThemeToggle from './components/ThemeToggle.vue'
import { useThemeStore } from '@/stores/theme'
import { darkThemeOverrides, lightThemeOverrides } from './theme'
import { loadingBarRef } from './utils/loading-bar'

const route = useRoute()
const themeStore = useThemeStore()

const naiveTheme = computed(() => (themeStore.isDark ? darkTheme : null))
const themeOverrides = computed(() =>
  themeStore.isDark ? darkThemeOverrides : lightThemeOverrides,
)

let disposeThemeListener: (() => void) | undefined

/**
 * NLoadingBar 必须在 NLoadingBarProvider 子树内通过 useLoadingBar() 取实例。
 * 内嵌一个微组件 InnerProviders 把实例桥接到 router/index.ts 用的全局 ref。
 */
const InnerProviders = defineComponent({
  setup(_, { slots }) {
    const loadingBar = useLoadingBar()
    onMounted(() => {
      loadingBarRef.value = loadingBar
      disposeThemeListener = themeStore.init()
    })
    onUnmounted(() => disposeThemeListener?.())
    return () => slots.default?.()
  },
})
</script>

<template>
  <NConfigProvider
    :theme="naiveTheme"
    :theme-overrides="themeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <NLoadingBarProvider>
      <NDialogProvider>
        <NNotificationProvider>
          <NMessageProvider>
            <InnerProviders>
              <AppNavbar v-if="!route.meta.hideNavbar" />
              <div
                v-else
                fixed top-3 right-4 z-50
                aria-label="主题切换"
              >
                <ThemeToggle />
              </div>
              <RouterView v-slot="{ Component, route: r }">
                <Transition :name="(r.meta.transition as string) || 'fade-slide'" mode="out-in" appear>
                  <component :is="Component" :key="r.fullPath" />
                </Transition>
              </RouterView>
            </InnerProviders>
          </NMessageProvider>
        </NNotificationProvider>
      </NDialogProvider>
    </NLoadingBarProvider>
  </NConfigProvider>
</template>

<style>
/* App 级样式（极少）；其余请用 token */
</style>
