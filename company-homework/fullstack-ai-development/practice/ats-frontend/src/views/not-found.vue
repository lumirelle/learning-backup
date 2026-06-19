<script setup lang="ts">
import { NButton } from 'naive-ui'
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

/**
 * 8s 倒计时自动跳首页 —— 用户操作（hover 卡片 / 焦点在卡片）时暂停，
 * 离开后继续；提供"暂停"按钮显式控制。reduce-motion 用户也保留（无动画，纯文本提示）。
 */
const remaining = ref(8)
const paused = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

function startTimer() {
  if (timer)
    return
  timer = setInterval(() => {
    if (paused.value)
      return
    remaining.value -= 1
    if (remaining.value <= 0) {
      stopTimer()
      router.replace('/home')
    }
  }, 1000)
}

function stopTimer() {
  if (timer)
    clearInterval(timer)
  timer = null
}

function togglePause() {
  paused.value = !paused.value
}

function goBack() {
  stopTimer()
  router.back()
}
function goHome() {
  stopTimer()
  router.replace('/home')
}
function goJobs() {
  stopTimer()
  router.replace('/jobs')
}

onMounted(startTimer)
onBeforeUnmount(stopTimer)
</script>

<template>
  <main
    min-h-screen flex-col flex="~ items-center justify-center"
    bg-app px-4 pt-60px text-center
    @mouseenter="paused = true"
    @mouseleave="paused = false"
    @focusin="paused = true"
    @focusout="paused = false"
  >
    <p kicker mb-4>
      404 · Not Found
    </p>
    <div text-7rem font-display font-bold leading-none text-gradient mb-4 select-none aria-hidden="true">
      404
    </div>
    <h1 text-2xl font-bold text-primary mb-3>
      Page Not Found
    </h1>
    <p text-tertiary mb-2 max-w-420px>
      你访问的页面不存在或还没建。
    </p>
    <p text-tertiary mb-8 max-w-420px text-sm>
      <template v-if="!paused">
        <span text-brand-500 text-mono aria-live="polite">{{ remaining }}s</span> 后自动回首页 ·
        <button
          type="button"
          text-brand-700 hover:underline ml-1
          @click="togglePause"
        >
          暂停
        </button>
      </template>
      <template v-else>
        <span text-tertiary>已暂停 ·</span>
        <button
          type="button"
          text-brand-700 hover:underline ml-1
          @click="togglePause"
        >
          继续倒计时
        </button>
      </template>
    </p>
    <div flex="~ items-center wrap" gap-3 justify-center>
      <NButton size="medium" @click="goBack">
        返回上一页
      </NButton>
      <NButton size="medium" type="primary" @click="goHome">
        立即回首页
      </NButton>
      <NButton size="medium" tertiary @click="goJobs">
        去岗位市场
      </NButton>
    </div>
  </main>
</template>
