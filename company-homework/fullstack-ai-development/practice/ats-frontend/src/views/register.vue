<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import type { PublicStatsVO } from '@/api/stats'
import { NForm, NFormItem, NInput } from 'naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { BizError } from '@/api/request'
import { statsApi } from '@/api/stats'
import { formatCount } from '@/composables/use-format-count'

const router = useRouter()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const errorMsg = ref('')
const done = ref(false)

const model = reactive({ email: '', password: '', fullName: '' })

/**
 * 公开聚合统计 —— 注册页左侧三个 stat block 显示真实数据。
 * 拉取失败时降级为静态占位（formatCount 0 → "多人/多个"）。
 */
const publicStats = ref<PublicStatsVO | null>(null)

onMounted(async () => {
  try {
    publicStats.value = await statsApi.publicStats()
  }
  catch (e) {
    console.warn('[register] publicStats fetch failed, falling back to placeholders', e)
  }
})

/** 覆盖部门 · 注册页换"入驻企业"为更贴切的"覆盖部门"（此为公司内部私有平台） */
const coveredDepartmentsLabel = computed(() => formatCount(publicStats.value?.coveredDepartments ?? 0, '多个'))
/** 在招岗位 · 真实 PUBLISHED 数 */
const publishedJobsLabel = computed(() => formatCount(publicStats.value?.publishedJobs ?? 0, '多个'))

/** 候选人感兴趣的方向（点击浮动标签切换；纯前端 demo 状态，未来 M4 接 profile） */
const TAGS = [
  { id: 'fe', label: '前端工程师', cls: 'tag tag-1' },
  { id: 'pm', label: '产品经理', cls: 'tag tag-2' },
  { id: 'data', label: '数据分析', cls: 'tag tag-3' },
  { id: 'design', label: 'UI 设计', cls: 'tag tag-4' },
]
const interests = ref<Set<string>>(new Set())
function toggleInterest(id: string) {
  if (interests.value.has(id))
    interests.value.delete(id)
  else interests.value.add(id)
  // 强制触发响应（Set mutation 不被 Vue tracking 自动捕获）
  interests.value = new Set(interests.value)
}

const rules: FormRules = {
  fullName: [{ required: true, trigger: 'blur', message: '请输入姓名' }],
  email: [
    { required: true, trigger: 'blur', message: '请输入邮箱' },
    { type: 'email', trigger: 'blur', message: '邮箱格式不正确' },
  ],
  password: [
    { required: true, trigger: 'blur', message: '请输入密码' },
    { min: 8, trigger: 'blur', message: '密码至少 8 位' },
  ],
}

/** 密码强度：0=空, 1=弱, 2=中, 3=强 */
const pwStrength = computed(() => {
  const p = model.password
  if (!p)
    return 0
  let score = 0
  if (p.length >= 8)
    score++
  if (/[A-Z]/.test(p) && /[a-z]/.test(p))
    score++
  if (/\d/.test(p) && /[^A-Z0-9]/i.test(p))
    score++
  if (p.length >= 12)
    score++
  return Math.min(3, score)
})
const strengthText = computed(() => ['', '弱', '中等', '强'][pwStrength.value])
const strengthClass = computed(() => ['', 'weak', 'mid', 'strong'][pwStrength.value])

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
    await authApi.register({
      email: model.email,
      password: model.password,
      fullName: model.fullName,
      interests: interests.value.size ? [...interests.value] : undefined,
    })
    done.value = true
    setTimeout(() => router.replace('/login'), 2000)
  }
  catch (e) {
    errorMsg.value = e instanceof BizError ? e.message : '注册失败，请稍后重试'
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <div flex min-h-screen>
    <!-- ══ 左：品牌视觉面板 ══════════════════════════════════ -->
    <div brand-pane w="52%">
      <div aurora-bg-register />
      <div grid-overlay />

      <div orb-base animate-orb-float-a right--100px top--120px class="bg-[radial-gradient(circle,rgba(6,182,212,.6),transparent_70%)]" />
      <div orb-base animate-orb-float-b left--80px bottom--140px class="bg-[radial-gradient(circle,rgba(16,185,129,.5),transparent_70%)]" />

      <!-- 顶部 -->
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

        <div flex items-center gap-2 rounded-full border border-cyan="400/30" bg-cyan="400/10" px-3 py-1 backdrop-blur-md>
          <span text-base>🌱</span>
          <span text-11px font-semibold uppercase tracking-widest text-cyan-300>Candidate</span>
        </div>
      </div>

      <!-- 巨型 hero -->
      <div absolute inset-0 z-10 col-flex justify-center px-12>
        <p eyebrow mb-4 text-cyan-400>
          <span h-px w-8 bg-cyan="400/50" />
          Start Your Journey
        </p>

        <h1 hero-display>
          <span hero-outline>Grow</span>
          <span block class="text-white/95">into your</span>
          <span hero-gradient-cyan>next step.</span>
        </h1>

        <p mt-8 max-w-380px text-sm leading-relaxed class="text-white/45">
          注册一个候选人账号，把你的简历投出去。<br>
          所有进展，一目了然。
        </p>

        <!-- 数据卡片 + 旋转标签 -->
        <div mt-10 flex items-end gap-5>
          <div class="stat-block">
            <div class="stat-num">
              {{ coveredDepartmentsLabel }}
            </div>
            <div class="stat-label">
              覆盖部门
            </div>
          </div>
          <div class="stat-divider" />
          <div class="stat-block">
            <div class="stat-num">
              {{ publishedJobsLabel }}
            </div>
            <div class="stat-label">
              在招岗位
            </div>
          </div>
          <div class="stat-divider" />
          <div class="stat-block">
            <div class="stat-num">
              7<span>×24</span>
            </div>
            <div class="stat-label">
              全程追踪
            </div>
          </div>
        </div>
      </div>

      <!-- 浮动标签（可点击切换"感兴趣"状态） -->
      <button
        v-for="t in TAGS"
        :key="t.id"
        type="button"
        :class="[t.cls, { 'is-active': interests.has(t.id) }]"
        :aria-pressed="interests.has(t.id)"
        :aria-label="`${interests.has(t.id) ? '取消选择' : '选择'} ${t.label}（仅作视觉收藏，不影响注册）`"
        @click="toggleInterest(t.id)"
      >
        <span v-if="interests.has(t.id)" class="tag-check">
          <svg width="10" height="10" viewBox="0 0 12 12" fill="none">
            <path d="M2.5 6 L5 8.5 L9.5 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </span>
        {{ t.label }}
      </button>

      <!-- interests counter -->
      <Transition name="counter-fade">
        <div v-if="interests.size > 0" class="interests-counter">
          <span text-11px uppercase tracking-widest text-emerald="300/80">已收藏</span>
          <span font-display text-lg font-bold text-white tabular-nums>{{ interests.size }}</span>
          <span text-xs class="text-white/40">/ {{ TAGS.length }}</span>
        </div>
      </Transition>

      <!-- 底部 -->
      <div absolute inset-x-12 bottom-10 z-10 flex items-center justify-between border-t border="white/[.06]" pt-5 text-11px uppercase tracking-widest class="text-white/30">
        <span flex items-center gap-2>
          <span h-1 w-1 rounded-full bg-cyan-400 />
          Free Forever
        </span>
        <span>HR 账号请联系管理员创建</span>
      </div>
    </div>

    <!-- ══ 右：表单面板 ════════════════════════════════════== -->
    <div flex flex-1 flex-col items-center justify-center bg-app px-6 py-12>
      <div
        w-full max-w-400px
      >
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

        <!-- success state -->
        <div v-if="done" class="success-state" text-center>
          <div class="success-check-wrap" mx-auto mb-5>
            <svg class="success-check" width="32" height="32" viewBox="0 0 32 32" fill="none">
              <circle cx="16" cy="16" r="14" stroke="#10b981" stroke-width="2" stroke-dasharray="88" stroke-dashoffset="88" pathLength="88" />
              <path d="M9 16 L14 21 L23 11" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" stroke-dasharray="22" stroke-dashoffset="22" pathLength="22" />
            </svg>
          </div>
          <h2 mb-2 font-display text-2xl font-bold text-primary>
            注册成功 ✨
          </h2>
          <p text-sm text-tertiary>
            2 秒后自动跳转到登录页…
          </p>
        </div>

        <template v-else>
          <!-- step indicator -->
          <div class="step-indicator mb-6">
            <div class="step active">
              <span class="step-num">01</span>
              <span class="step-label">注册账号</span>
            </div>
            <div class="step-line" />
            <div class="step">
              <span class="step-num">02</span>
              <span class="step-label">验证登录</span>
            </div>
            <div class="step-line" />
            <div class="step">
              <span class="step-num">03</span>
              <span class="step-label">开始投递</span>
            </div>
          </div>

          <!-- heading -->
          <div mb-7>
            <h2 mb-2 flex items-center gap-2 font-display text-28px font-bold leading-tight text-primary>
              创建账号
              <span text-2xl>🌱</span>
            </h2>
            <p text-sm text-tertiary>
              候选人账号 · 免费 · 30 秒完成
            </p>
          </div>

          <!-- error -->
          <Transition name="fade">
            <div v-if="errorMsg" error-banner mb-5>
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
            <NFormItem path="fullName">
              <NInput
                v-model:value="model.fullName"
                placeholder="你的姓名"
                :input-props="{ autocomplete: 'name' }"
              >
                <template #prefix>
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="5.5" r="2.5" stroke="currentColor" stroke-width="1.4" />
                    <path d="M2.5 14 C2.5 11 5 9.5 8 9.5 C11 9.5 13.5 11 13.5 14" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" />
                  </svg>
                </template>
              </NInput>
            </NFormItem>
            <NFormItem path="email">
              <NInput
                v-model:value="model.email"
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
                placeholder="设置密码（至少 8 位）"
                show-password-on="click"
                :input-props="{ autocomplete: 'new-password' }"
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

            <!-- password strength -->
            <div v-if="model.password" class="strength-meter" mb-3>
              <div class="strength-bars">
                <span class="bar" :class="{ on: pwStrength >= 1, weak: pwStrength === 1 }" />
                <span class="bar" :class="{ on: pwStrength >= 2, mid: pwStrength === 2 }" />
                <span class="bar" :class="{ on: pwStrength >= 3, strong: pwStrength >= 3 }" />
              </div>
              <span class="strength-label" :class="strengthClass">{{ strengthText }}</span>
            </div>

            <!-- submit -->
            <button type="button" btn-primary :disabled="loading" @click="handleSubmit">
              <span v-if="!loading" relative z-10 center-flex gap-2>
                立即注册
                <kbd kbd-hint>⏎</kbd>
              </span>
              <span v-else relative z-10 center-flex gap-2>
                <svg animate-spin width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2.5" opacity=".25" />
                  <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
                </svg>
                注册中…
              </span>
              <span btn-shimmer />
            </button>

            <p mt-3 text-center text-11px text-tertiary>
              注册即表示同意
              <span text-brand-700 font-medium title="演示项目，暂未提供独立条款页面">服务条款</span>
              与
              <span text-brand-700 font-medium title="演示项目，暂未提供独立条款页面">隐私政策</span>
              <span ml-1 text-tertiary>· 演示版本</span>
            </p>
          </NForm>

          <div my-6 flex items-center gap-3>
            <div h-px flex-1 class="bg-(--border-default)" />
            <span text-11px uppercase tracking-widest text-tertiary>已有账号</span>
            <div h-px flex-1 class="bg-(--border-default)" />
          </div>

          <router-link to="/login" btn-secondary group>
            登录已有账号
            <svg transition-transform group-hover:translate-x-0.5 width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M3 7 H11 M7 3 L11 7 L7 11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </router-link>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ────────────────────────────────────────
 * 只保留 UnoCSS 不擅长的部分：
 *  - 浮动 tag 的具体位置 + 旋转 + 动画绑定（位置散乱不适合 utility）
 *  - 数据块的细节排版（stat-block 内的复合规则）
 *  - 注册成功的 SVG stroke 渐入动画（动画名仅本文件用）
 *  - Vue Transition class（命名机制要求）
 * ──────────────────────────────────────── */

/* 浮动职位标签（可交互按钮） */
.tag {
  position: absolute;
  z-index: 15;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 600;
  color: rgba(255,255,255,.85);
  background: linear-gradient(135deg, rgba(255,255,255,.08), rgba(255,255,255,.02));
  border: 1px solid rgba(255,255,255,.12);
  border-radius: 10px;
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 24px rgba(0,0,0,.3);
  white-space: nowrap;
  cursor: pointer;
  will-change: transform;
  /* hover/click 时叠加在 keyframes translate 之上，互不打架 */
  transition:
    color .25s ease-out,
    background .35s ease-out,
    border-color .3s ease-out,
    box-shadow .3s ease-out,
    transform .3s cubic-bezier(0.32, 0.72, 0, 1);
}
.tag:hover {
  animation-play-state: paused;
  color: #fff;
  border-color: rgba(255,255,255,.28);
  background: linear-gradient(135deg, rgba(255,255,255,.14), rgba(255,255,255,.05));
  transform: scale(1.06);
  box-shadow: 0 12px 32px rgba(0,0,0,.4), 0 0 24px rgba(255,255,255,.08);
}
.tag:active { transform: scale(.94); transition-duration: .12s; }

/* 被收藏的标签：emerald 主题持久高亮 */
.tag.is-active {
  color: var(--brand-emerald, #34d399);
  border-color: rgba(52,211,153,.55);
  background: linear-gradient(135deg, rgba(52,211,153,.18), rgba(52,211,153,.05));
  box-shadow:
    0 12px 32px rgba(0,0,0,.4),
    0 0 28px rgba(52,211,153,.35),
    inset 0 1px 0 rgba(255,255,255,.12);
}
.tag.is-active:hover { color: #fff; }

.tag-check {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px; height: 14px;
  border-radius: 50%;
  background: rgba(52,211,153,.9);
  color: #052e2b;
  animation: tag-check-pop .35s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes tag-check-pop {
  0%   { transform: scale(0); opacity: 0; }
  60%  { transform: scale(1.15); }
  100% { transform: scale(1); opacity: 1; }
}

.tag-1 { top: 22%; right: 8%;  animation: tag-float-a 7s ease-in-out infinite;
         border-color: rgba(6,182,212,.3);  box-shadow: 0 8px 24px rgba(0,0,0,.3), 0 0 16px rgba(6,182,212,.2); }
.tag-2 { top: 12%; right: 28%; animation: tag-float-b 8s ease-in-out infinite -2s; }
.tag-3 { top: 78%; right: 18%; animation: tag-float-a 9s ease-in-out infinite -1s;
         border-color: rgba(16,185,129,.3); box-shadow: 0 8px 24px rgba(0,0,0,.3), 0 0 16px rgba(16,185,129,.2); }
.tag-4 { bottom: 18%; right: 4%; animation: tag-float-b 7.5s ease-in-out infinite -3s; }

/* "已收藏 N / 4" 计数器（出现在标签下方居中，不抢戏）*/
.interests-counter {
  position: absolute;
  bottom: 28%; left: 50%;
  transform: translateX(-50%);
  z-index: 16;
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(52,211,153,.14), rgba(34,211,238,.08));
  border: 1px solid rgba(52,211,153,.3);
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 24px rgba(0,0,0,.4), 0 0 32px rgba(52,211,153,.25);
}
.counter-fade-enter-active,
.counter-fade-leave-active {
  transition: opacity .35s ease-out, transform .45s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.counter-fade-enter-from { opacity: 0; transform: translateX(-50%) translateY(8px) scale(.85); }
.counter-fade-leave-to   { opacity: 0; transform: translateX(-50%) translateY(4px) scale(.9); }

/* 数据块（hero 下方）*/
.stat-block { display: flex; flex-direction: column; gap: 4px; }
.stat-num   { font-family: var(--font-display); font-size: 28px; font-weight: 800;
              color: #fff; letter-spacing: -.02em; line-height: 1; }
.stat-num span { font-size: 16px; font-weight: 600; color: #22d3ee; margin-left: 2px; }
.stat-label { font-size: 11px; text-transform: uppercase; letter-spacing: .15em;
              color: rgba(255,255,255,.35); }
.stat-divider { width: 1px; height: 28px; align-self: end; margin-bottom: 6px;
                background: linear-gradient(to bottom, transparent, rgba(255,255,255,.15), transparent); }

/* 步骤指示器（语义重，留 plain class 更易维护） */
.step-indicator { display: flex; align-items: center; gap: 6px; }
.step { display: flex; align-items: center; gap: 6px; padding: 4px 8px; border-radius: 6px;
        font-size: 11px; color: var(--text-tertiary); background: var(--bg-hover); }
.step-num   { font-family: var(--font-mono); font-weight: 700; font-size: 10px; color: var(--text-tertiary); }
.step-label { font-weight: 500; }
.step.active { background: rgba(16,185,129,.1); color: var(--brand-700); }
.step.active .step-num { color: var(--brand-500); }
.step-line { flex: 1; height: 1px; background: var(--border); }

/* 密码强度计 */
.strength-meter { display: flex; align-items: center; gap: 8px; margin-top: -8px; }
.strength-bars  { flex: 1; display: flex; gap: 4px; }
.bar { flex: 1; height: 4px; border-radius: 2px; background: var(--bg-hover);
       transition: background .2s var(--ease-out); }
.bar.on.weak   { background: #ef4444; }
.bar.on.mid    { background: #fbbf24; }
.bar.on.strong { background: #10b981; }
.bar.on:not(.weak):not(.mid):not(.strong) { background: #10b981; }
.strength-label { font-size: 11px; font-weight: 600; min-width: 28px; text-align: right; }
.strength-label.weak   { color: #ef4444; }
.strength-label.mid    { color: #d97706; }
.strength-label.strong { color: var(--brand-700); }

/* 注册成功 SVG stroke 动画 */
.success-state      { padding: 40px 20px; }
.success-check-wrap { width: 72px; height: 72px; display: flex; align-items: center; justify-content: center;
                      border-radius: 9999px; box-shadow: 0 0 30px rgba(16,185,129,.3);
                      background: linear-gradient(135deg, rgba(16,185,129,.15), rgba(20,184,166,.1)); }
.success-check circle { animation: stroke-circle .5s var(--ease-page-in) forwards; }
.success-check path   { animation: stroke-check  .3s .35s var(--ease-page-in) forwards; }
@keyframes stroke-circle { to { stroke-dashoffset: 0; } }
@keyframes stroke-check  { to { stroke-dashoffset: 0; } }

/* Vue Transition（错误条 fade）*/
.fade-enter-active, .fade-leave-active { transition: opacity .2s, transform .2s; }
.fade-enter-from, .fade-leave-to       { opacity: 0; transform: translateY(-4px); }

@media (prefers-reduced-motion: reduce) {
  .tag, .success-check circle, .success-check path { animation: none !important; }
  .success-check circle, .success-check path { stroke-dashoffset: 0; }
}
</style>
