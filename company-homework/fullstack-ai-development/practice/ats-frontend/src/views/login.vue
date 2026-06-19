<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import type { PublicStatsVO } from '@/api/stats'
import { NForm, NFormItem, NInput } from 'naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BizError } from '@/api/request'
import { statsApi } from '@/api/stats'
import { formatCount } from '@/composables/use-format-count'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const errorMsg = ref('')

const model = reactive({ email: '', password: '' })

const rules: FormRules = {
  email: [
    { required: true, trigger: 'blur', message: '请输入邮箱' },
    { type: 'email', trigger: 'blur', message: '邮箱格式不正确' },
  ],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }],
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  }
  catch {
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    await auth.login(model.email, model.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/home'
    await router.replace(redirect)
  }
  catch (e) {
    errorMsg.value = e instanceof BizError ? e.message : '登录失败，请稍后重试'
  }
  finally {
    loading.value = false
  }
}

/**
 * 公开聚合统计 —— 登录页左侧 "招聘流水线" 三张卡片。
 * 未登录可访问；< 5 模糊化为"多人"，> 999 显示 "1.x k+"，避免数据稀疏时暴露具体个位数。
 *
 * 设计取舍：拉取失败时降级为静态 placeholder（"多人"/0），不阻塞登录页渲染。
 */
const publicStats = ref<PublicStatsVO | null>(null)

onMounted(async () => {
  try {
    publicStats.value = await statsApi.publicStats()
  }
  catch (e) {
    // 公开接口不应 fail 整个页面 —— 静默兜底，登录页主流程不受影响
    console.warn('[login] publicStats fetch failed, falling back to placeholders', e)
  }
})

const PIPELINE = computed(() => {
  const s = publicStats.value
  return [
    {
      stage: '初步筛选',
      desc: '简历评估中',
      count: formatCount(s?.screeningCount ?? 0),
      bg: 'rgba(251,191,36,.10)',
      accent: '#fbbf24',
    },
    {
      stage: '面试阶段',
      desc: '候选人推进中',
      count: formatCount(s?.interviewCount ?? 0),
      bg: 'rgba(16,185,129,.10)',
      accent: '#34d399',
    },
    {
      stage: 'Offer 阶段',
      desc: 'Offer 沟通中',
      count: formatCount(s?.offerCount ?? 0),
      bg: 'rgba(6,182,212,.10)',
      accent: '#22d3ee',
    },
  ]
})

/** 卡片堆叠顶部索引；点击下层卡片洗到顶部 */
const topCardIndex = ref(0)
function getStackPos(realIndex: number) {
  return (realIndex - topCardIndex.value + PIPELINE.value.length) % PIPELINE.value.length
}
function bringCardToTop(realIndex: number) {
  if (realIndex === topCardIndex.value) {
    // 点击顶部卡片 → 自动轮播下一张
    topCardIndex.value = (topCardIndex.value + 1) % PIPELINE.value.length
  }
  else {
    topCardIndex.value = realIndex
  }
}
</script>

<template>
  <div flex min-h-screen>
    <!-- ══ 左：品牌视觉面板 ══════════════════════════════════ -->
    <div brand-pane w="[52%]">
      <div aurora-bg-login />
      <div grid-overlay />

      <!-- 流动光球（颜色由 inline 渐变控制，动画走 shortcuts） -->
      <div orb-base animate-orb-float-a class="left-[-100px] top-[-120px] bg-[radial-gradient(circle,rgba(16,185,129,.55),transparent_70%)]" />
      <div orb-base animate-orb-float-b class=" right-[-80px] bottom-[-140px] bg-[radial-gradient(circle,rgba(6,182,212,.5),transparent_70%)]" />

      <!-- 顶部导航条 -->
      <div absolute inset-x-12 top-12 z-20 between-flex>
        <router-link to="/home" flex items-center gap-2.5 no-underline transition-opacity hover:opacity-75>
          <span logo-mark-lg>
            <svg width="20" height="20" viewBox="0 0 14 14" fill="none">
              <path d="M2 10 L7 3 L12 10" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
              <path d="M4.5 10 L7 6.5 L9.5 10" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" opacity="0.45" />
            </svg>
          </span>
          <span font-display text-base font-bold tracking-wide text-white>ATS</span>
        </router-link>

        <div flex items-center gap-2 rounded-full border border-emerald="400/30" bg-emerald="400/10" px-3 py-1 backdrop-blur-md>
          <span relative flex h-1.5 w-1.5>
            <span absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75 />
            <span relative inline-flex h-1.5 w-1.5 rounded-full bg-emerald-400 />
          </span>
          <span text-11px font-semibold uppercase tracking-widest text-emerald-300>Live · v0.1</span>
        </div>
      </div>

      <!-- 巨型 hero typography（绝对居中） -->
      <div absolute inset-0 z-10 col-flex justify-center px-12>
        <p eyebrow mb-4 text-emerald-400>
          <span h-px w-8 bg-emerald="400/50" />
          Applicant Tracking
        </p>

        <h1 hero-display>
          <span block class="text-white/95">Find the</span>
          <span hero-outline>good</span>
          <span hero-gradient pb-4>seedlings.</span>
        </h1>

        <p mt-8 max-w-380px text-sm leading-relaxed class="text-white/45">
          公司内部招聘追踪系统。让 HR 与候选人，<br>
          沿同一根时间线，把每一位人才送到合适的位置。
        </p>

        <!-- 浮动招聘卡片堆叠（点击下层洗到顶部，点顶层切下一张） -->
        <div class="floating-stack" mt-10>
          <button
            v-for="(c, i) in PIPELINE"
            :key="c.stage"
            type="button"
            class="float-card"
            :class="{ 'is-top': getStackPos(i) === 0 }"
            :style="`--i:${getStackPos(i)};--bg:${c.bg};--accent:${c.accent}`"
            @click="bringCardToTop(i)"
          >
            <span class="float-card-dot" />
            <div min-w-0 flex-1 text-left>
              <p text-13px font-semibold text-white>
                {{ c.stage }}
              </p>
              <p text-11px class="text-white/40">
                {{ c.desc }}
              </p>
            </div>
            <span class="float-card-badge">{{ c.count }}</span>
          </button>
        </div>
      </div>

      <!-- 底部数据条 -->
      <div absolute inset-x-12 bottom-10 z-10 between-flex border-t border="white/[.06]" pt-5 text-11px uppercase tracking-widest class="text-white/30">
        <span flex items-center gap-2>
          <span h-1 w-1 rounded-full bg-emerald-400 />
          Realtime Sync
        </span>
        <span>Powered by Spring × Vue</span>
      </div>
    </div>

    <!-- ══ 右：表单面板 ════════════════════════════════════== -->
    <div flex="~ 1 col" items-center justify-center bg-app px-6 py-12>
      <div w-full max-w-400px>
        <!-- top bar: mobile logo + 返回首页 -->
        <div mb-10 flex items-center justify-between>
          <router-link to="/home" flex items-center gap-2 no-underline lg:hidden>
            <span flex h-8 w-8 items-center justify-center rounded-xl bg-linear-to-br from-emerald-400 to-teal-500>
              <svg width="16" height="16" viewBox="0 0 14 14" fill="none">
                <path d="M2 10 L7 3 L12 10" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </span>
            <span font-display text-lg font-bold text-primary>ATS</span>
          </router-link>
          <router-link
            to="/home"
            flex items-center gap-1 text-sm text-tertiary no-underline transition-colors hover:text-primary
          >
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M8.5 3 L4 7 L8.5 11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            返回首页
          </router-link>
        </div>

        <!-- heading -->
        <div mb-8>
          <h2 mb-2 flex items-center gap-2 font-display text-28px font-bold leading-tight text-primary>
            欢迎回来
            <span text-2xl>👋</span>
          </h2>
          <p text-sm text-tertiary>
            登录以继续使用公司招聘追踪系统
          </p>
        </div>

        <!-- error -->
        <Transition name="fade">
          <div
            v-if="errorMsg"
            error-banner mb-5
          >
            <span error-icon>
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.5" />
                <path d="M7 4 V8 M7 10 V10.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
              </svg>
            </span>
            <p flex-1 text-sm>
              {{ errorMsg }}
            </p>
          </div>
        </Transition>

        <!-- form -->
        <NForm ref="formRef" :model="model" :rules="rules" :show-label="false" :show-require-mark="false" size="large">
          <NFormItem path="email">
            <NInput
              v-model:value="model.email"
              type="text"
              placeholder="邮箱地址"
              :input-props="{ autocomplete: 'email' }"
            >
              <template #prefix>
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <rect x="2" y="3.5" width="12" height="9" rx="1.5" stroke="currentColor" stroke-width="1.4" />
                  <path d="M2.5 4.5 L8 9 L13.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </template>
            </NInput>
          </NFormItem>
          <NFormItem path="password">
            <NInput
              v-model:value="model.password"
              type="password"
              placeholder="密码"
              show-password-on="click"
              :input-props="{ autocomplete: 'current-password' }"
              @keyup.enter="handleSubmit"
            >
              <template #prefix>
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <rect x="3" y="7" width="10" height="7" rx="1.5" stroke="currentColor" stroke-width="1.4" />
                  <path d="M5.5 7 V5 a2.5 2.5 0 0 1 5 0 V7" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" />
                </svg>
              </template>
            </NInput>
          </NFormItem>

          <!-- submit -->
          <button type="button" btn-primary :disabled="loading" @click="handleSubmit">
            <span v-if="!loading" relative z-10 center-flex gap-2>
              登录
              <kbd kbd-hint>⏎</kbd>
            </span>
            <span v-else relative z-10 center-flex gap-2>
              <svg animate-spin width="16" height="16" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2.5" opacity=".25" />
                <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
              </svg>
              登录中…
            </span>
            <span class="btn-shimmer" />
          </button>
        </NForm>

        <!-- demo account 提示卡 -->
        <div demo-card mt-2 flex items-start gap-3>
          <span demo-icon>🔑</span>
          <div flex-1>
            <p text-12px font-semibold text-primary>
              管理员演示账号
            </p>
            <p mt-0.5 font-mono text-11px text-tertiary>
              admin@ats.local / Admin@123
            </p>
          </div>
          <button
            type="button"
            demo-fill
            @click="model.email = 'admin@ats.local'; model.password = 'Admin@123'"
          >
            一键填充
          </button>
        </div>

        <!-- demo account 提示卡 -->
        <div demo-card mt-2 flex items-start gap-3>
          <span demo-icon>🔑</span>
          <div flex-1>
            <p text-12px font-semibold text-primary>
              HR 演示账号
            </p>
            <p mt-0.5 font-mono text-11px text-tertiary>
              hr@ats.local / Admin@123
            </p>
          </div>
          <button
            type="button"
            demo-fill
            @click="model.email = 'hr@ats.local'; model.password = 'Admin@123'"
          >
            一键填充
          </button>
        </div>

        <!-- demo account 提示卡 -->
        <div demo-card mt-2 flex items-start gap-3>
          <span demo-icon>🔑</span>
          <div flex-1>
            <p text-12px font-semibold text-primary>
              候选人演示账号
            </p>
            <p mt-0.5 font-mono text-11px text-tertiary>
              candidate@ats.local / Admin@123
            </p>
          </div>
          <button
            type="button"
            demo-fill
            @click="model.email = 'candidate@ats.local'; model.password = 'Admin@123'"
          >
            一键填充
          </button>
        </div>

        <!-- divider -->
        <div my-6 flex items-center gap-3>
          <div h-px flex-1 class="bg-(--border-default)" />
          <span text-11px uppercase tracking-widest text-tertiary>还没有账号？</span>
          <div h-px flex-1 class="bg-(--border-default)" />
        </div>

        <router-link to="/register" btn-secondary group>
          创建候选人账号
          <svg transition-transform group-hover:translate-x-0.5 width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M3 7 H11 M7 3 L11 7 L7 11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </router-link>

        <!-- 底部信任标识 -->
        <div mt-8 flex items-center justify-center gap-4 text-11px text-tertiary>
          <span flex items-center gap-1>
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M6 1 L10 3 V6.5 C10 8.5 8 10.5 6 11 C4 10.5 2 8.5 2 6.5 V3 Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round" />
            </svg>
            JWT + HttpOnly Cookie
          </span>
          <span h-3 w-px class="bg-(--border-default)" />
          <span>密码 BCrypt 加密</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ────────────────────────────────────────
 * 仅保留 UnoCSS 不擅长的部分：
 *  - 依赖 CSS 变量 (--i / --accent / --bg) 的复合 calc 变换
 *  - perspective 3D 容器
 *  - Vue Transition class（命名机制，必须 plain class）
 * ──────────────────────────────────────── */
.floating-stack {
  position: relative;
  perspective: 1200px;
  width: 100%;
  max-width: 380px;
  height: 64px;
}
.float-card {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(255,255,255,.06), rgba(255,255,255,.02));
  border: 1px solid rgba(255,255,255,.08);
  backdrop-filter: blur(16px);
  box-shadow: 0 12px 32px rgba(0,0,0,.4), inset 0 1px 0 rgba(255,255,255,.06);
  transform-origin: left center;
  transform:
    translateX(calc(var(--i) * 36px))
    translateY(calc(var(--i) * 18px))
    rotate(calc(var(--i) * -2deg))
    scale(calc(1 - var(--i) * 0.04));
  opacity: calc(1 - var(--i) * 0.18);
  animation: card-bob 5s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.4s);
  will-change: transform;
  cursor: pointer;
  /* 关键：堆叠位置切换走顺滑 spring 过渡 */
  transition:
    transform .55s cubic-bezier(0.32, 0.72, 0, 1),
    opacity   .35s ease-out,
    box-shadow .3s ease-out,
    border-color .3s ease-out;
  z-index: calc(10 - var(--i));
}

/* 顶层卡片：高亮 + 边框强化 + 自动 accent 光晕 */
.float-card.is-top {
  border-color: color-mix(in srgb, var(--accent) 35%, transparent);
  box-shadow:
    0 16px 40px rgba(0,0,0,.45),
    0 0 24px color-mix(in srgb, var(--accent) 30%, transparent),
    inset 0 1px 0 rgba(255,255,255,.12);
}

/* hover 非顶层：暂停浮动 + 向左滑出一点 + 增亮，提示可点 */
.float-card:not(.is-top):hover {
  animation-play-state: paused;
  border-color: rgba(255,255,255,.18);
  background: linear-gradient(135deg, rgba(255,255,255,.10), rgba(255,255,255,.04));
  transform:
    translateX(calc(var(--i) * 36px - 10px))
    translateY(calc(var(--i) * 18px))
    rotate(calc(var(--i) * -2deg))
    scale(calc(1 - var(--i) * 0.04 + 0.02));
}

/* hover 顶层：暂停浮动 + 轻微浮起，提示"点我换下一张" */
.float-card.is-top:hover {
  animation-play-state: paused;
  transform: translate(0, -4px);
}

.float-card:active {
  transition-duration: .15s;
  transform: scale(.97);
}
.float-card-dot {
  flex-shrink: 0;
  width: 8px; height: 8px;
  border-radius: 9999px;
  background: var(--accent);
  box-shadow: 0 0 12px var(--accent);
}
.float-card-badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px; height: 22px;
  padding: 0 8px;
  border-radius: 6px;
  background: var(--bg);
  color: var(--accent);
  font-size: 11px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  border: 1px solid color-mix(in srgb, var(--accent) 25%, transparent);
}

/* error banner fade transition（Vue Transition class）*/
.fade-enter-active, .fade-leave-active { transition: opacity .2s, transform .2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-4px); }

@media (prefers-reduced-motion: reduce) {
  .float-card { animation: none !important; }
}
</style>
