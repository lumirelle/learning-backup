<script setup lang="ts">
import type { HealthVO } from '@/api/health'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getHealth } from '@/api/health'
import { BizError } from '@/api/request'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

/**
 * 6 大产品/工程亮点 · 取代原"里程碑"卡片，按"萌芽→茁壮→沉淀→流动→收获→入职"
 * 同源 accent palette，让候选人/招聘方一眼读出系统能力。
 */
const features = [
  {
    id: '01',
    name: '8 态招聘状态机',
    desc: '从「已投递」到「已入职」全流程拖拽流转 · 终态保护 + 失败回滚 · 0 第三方拖拽库',
    accent: 'mint',
  },
  {
    id: '02',
    name: 'HR 招聘看板',
    desc: 'HTML5 native DnD · 合法目标列高亮 + 非法变暗 · 浮动「拒绝」投放区小屏可达',
    accent: 'emerald',
  },
  {
    id: '03',
    name: '数据看板',
    desc: '本月 4 指标 + 8 态招聘漏斗 · 0 ECharts 依赖手绘 SVG · 点击 stage 直跳看板对应列',
    accent: 'teal',
  },
  {
    id: '04',
    name: 'PDF 简历 + 面试评价',
    desc: '自定义 dropzone 上传 · UUID v4 + 路径穿越防御 · 24h 编辑窗（ADMIN 不限）',
    accent: 'cyan',
  },
  {
    id: '05',
    name: 'JWT RS256 + RBAC',
    desc: 'Access 短寿命（15 min）+ Refresh HttpOnly cookie · ADMIN / HR / CANDIDATE 三角色',
    accent: 'amber',
  },
  {
    id: '06',
    name: '一键部署',
    desc: 'docker-compose.prod.yml + 多阶段 Dockerfile · pwsh / bash 跨平台 release 脚本',
    accent: 'lime',
  },
] as const

/** 成果数字（替代原"预估工时"那种 todo 类指标，转向"已交付"叙事） */
const stats = [
  { value: '6', unit: 'M', label: '里程碑全交付' },
  { value: '282', unit: '', label: '后端测试全绿' },
  { value: '8', unit: '态', label: '状态机覆盖' },
  { value: '3', unit: 'd', label: '冲刺完成' },
]

const health = ref<HealthVO | null>(null)
const healthErr = ref(false)
const healthState = computed(() =>
  healthErr.value ? 'down' : (health.value ? 'up' : 'pending'),
)
const healthLabel = computed(() =>
  healthErr.value ? 'API DOWN' : (health.value ? 'All systems normal' : 'Pinging…'),
)

const chipClass = computed(() => ({
  up: 'text-success-700 border-success-500/30',
  down: 'text-danger-700 border-danger-500/30',
  pending: '',
}[healthState.value]))
const dotClass = computed(() => ({
  up: 'bg-success-500 animate-pulse-ring',
  down: 'bg-danger-500',
  pending: 'bg-gray-400',
}[healthState.value]))

const primaryCta = computed(() => {
  if (!auth.isLoggedIn)
    return { label: '浏览岗位市场', target: '/jobs' }
  if (auth.isCandidate)
    return { label: '我的投递', target: '/me/applications' }
  return { label: '进入数据看板', target: '/hr/dashboard' }
})

async function ping() {
  try {
    health.value = await getHealth()
    healthErr.value = false
  }
  catch (e) {
    healthErr.value = true
    if (!(e instanceof BizError))
      console.warn(e)
  }
}

const heroRef = ref<HTMLElement | null>(null)
function onMouseMove(e: MouseEvent) {
  const el = heroRef.value
  if (!el)
    return
  const r = el.getBoundingClientRect()
  el.style.setProperty('--mx', `${((e.clientX - r.left) / r.width) * 100}%`)
  el.style.setProperty('--my', `${((e.clientY - r.top) / r.height) * 100}%`)
}

let io: IntersectionObserver | null = null
function setupReveal() {
  io = new IntersectionObserver(
    (entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting) {
          e.target.classList.add('is-visible')
          io?.unobserve(e.target)
        }
      })
    },
    { rootMargin: '-10% 0px' },
  )
  document.querySelectorAll('.reveal').forEach(el => io!.observe(el))
}

onMounted(() => {
  ping()
  setupReveal()
})
onUnmounted(() => io?.disconnect())
</script>

<template>
  <main min-h-screen bg-app pt-60px>
    <!-- ════════════ HERO ════════════ -->
    <section
      ref="heroRef"
      bg-hero with-noise
      relative
      min-h-screen
      p-6
      overflow-hidden
      @mousemove="onMouseMove"
    >
      <div class="aurora-layer" aria-hidden="true" />
      <div class="cursor-glow" aria-hidden="true" />

      <!-- topbar -->
      <header
        relative
        z-1
        flex="~ items-center justify-center wrap"
        gap-4
        p="y-3 x-4"
      >
        <nav flex gap-5 text-sm font-medium max-sm:hidden>
          <a
            v-for="(item, idx) in [
              { label: '产品特性', href: '#features' },
              { label: '岗位市场', href: '/jobs' },
              { label: 'GitHub ↗', href: 'https://github.com/lumirelle/demo-homework', target: '_blank' },
            ]"
            :key="idx"
            :href="item.href"
            after:transition-right after:duration-base after:ease-out
            relative
            text-secondary
            transition-colors duration-base ease-out
            after="absolute left-0 right-full bottom-[-4px] h-2px content-empty bg-grad-spring"
            hover="text-primary after:right-0"
          >
            {{ item.label }}
          </a>
        </nav>

        <!-- status-chip · 点击进 /health 详情页 -->
        <router-link
          to="/health"
          group text-xs text-secondary no-underline
          class="transition-[color,border-color,transform,box-shadow] hover:(-translate-y-1px shadow-md border-default) focus-visible:(outline-none ring-2 ring-brand-300 ring-offset-2)"
          absolute
          top="1/2"
          translate-y="-1/2"
          right-4
          inline-flex items-center
          gap-2
          p="y-6px x-3"
          rounded-full
          bg-elevated
          border="~ subtle"
          shadow-sm
          font-medium
          duration-base
          ease-out
          :title="`查看完整健康检查 · ${healthLabel}`"
          :class="chipClass"
        >
          <span w-2 h-2 rounded-full :class="dotClass" />
          <span>{{ healthLabel }}</span>
          <span text-tertiary opacity-60 transition-transform duration-base ease-out group-hover:translate-x-2px aria-hidden="true">→</span>
        </router-link>
      </header>

      <!-- hero body -->
      <div
        relative
        z-1
        max-w-1200px
        mx-auto
        p="t-8vh b-4vh x-3"
        text-center
      >
        <h1
          text-display-lg text-gray-900
          m-0
          font-black
          tracking="[-0.05em]"
          leading="[0.95]"
        >
          <span inline-block>Grow&nbsp;</span>
          <span text-gradient inline-block>talent.</span>
          <span inline-block text-gray-600>Hire every</span>
          <span text-gradient-bloom inline-block pb-8>good seed.</span>
        </h1>

        <p
          text-lg text-secondary
          m="t-6 x-auto"
          max-w-600px
          leading="[1.6]"
        >
          一条流水线把候选人从投递追到入职 —— <br>
          <strong text-primary>状态机看板</strong>
          + <strong text-primary>数据漏斗</strong>
          + <strong text-primary>PDF 简历</strong> + <strong text-primary>面试评价</strong>。
          <br><br>
          Spring Boot 3 · Vue 3.5 · PostgreSQL · Redis · 3 天 AI 辅助开发跑完 MVP。
        </p>

        <!-- CTA -->
        <div inline-flex gap-3 mt-8>
          <button
            class="transition-[transform,box-shadow]"
            group hover:-translate-y-2px text-md text-white
            relative
            inline-flex items-center
            gap-10px
            p="y-14px x-7"
            font="sans semibold"
            rounded-full
            border-none
            cursor-pointer
            bg-gray-950
            shadow-md
            duration-base
            ease-out
            before="absolute inset-0 rounded-[inherit] bg-grad-spring opacity-0 transition-opacity duration-base ease-out content-empty"
            hover="shadow-glow-brand before:opacity-100"
            @click="router.push(primaryCta.target)"
          >
            <span relative z-1>{{ primaryCta.label }}</span>
            <span
              relative
              z-1
              inline-block
              transition-transform duration-base ease-out
              group-hover:translate-x-1
            >→</span>
          </button>

          <a
            class="transition-[color,background-color,border-color,transform]"
            hover:-translate-y-2px
            inline-flex items-center
            p="y-14px x-6"
            text-md
            text-primary
            font-medium
            rounded-full
            bg-transparent
            border="~ default"
            duration-fast
            ease-out
            hover="bg-hover border-gray-400"
            href="#features"
          >
            See the features
          </a>
        </div>

        <!-- stat strip -->
        <ul
          list-none
          p="0 t-6"
          m="t-12 x-auto"
          max-w-760px
          grid="~ cols-4"
          gap-2
          border="t subtle"
          max-sm="grid-cols-2 gap-4"
        >
          <li v-for="s in stats" :key="s.label" text-center>
            <span class="num" text-36px text-primary block font-bold tracking="[-0.03em]">
              {{ s.value }}<small text-14px text-tertiary ml-2px font-medium>{{ s.unit }}</small>
            </span>
            <span text-xs text-tertiary uppercase block mt-1 tracking-0.5px>
              {{ s.label }}
            </span>
          </li>
        </ul>
      </div>
    </section>

    <!-- ════════════ FEATURES ════════════ -->
    <section id="features" max-w-1200px mx-auto p="y-16 x-6">
      <div class="reveal" text-center mb-12>
        <p kicker mb-3>
          Capabilities
        </p>
        <h2
          class="text-[clamp(32px,5vw,56px)]"
          m-0
          font-bold
          tracking="[-0.03em]"
          leading="[1.05]"
        >
          每一块都
          <span text-gradient>能跑、能演示</span>
        </h2>
        <p m="t-4 x-auto" max-w-560px text-secondary>
          MVP 不是切片，是闭环。从候选人投递到 HR 入职决策，所有动作均有审计记录与权限边界。
        </p>
      </div>

      <div grid gap-4 grid-cols="[repeat(auto-fill,minmax(280px,1fr))]">
        <article
          v-for="(f, i) in features"
          :key="f.id"
          class="reveal group transition-[transform,border-color,box-shadow]"
          hover:-translate-y-4px
          before:transition-transform before:duration-260 before:ease-out
          relative
          p-6
          rounded-lg
          overflow-hidden
          cursor-default
          bg-elevated
          border="~ subtle"
          shadow-sm
          duration-260
          ease-out
          before="absolute top-0 left-0 right-0 h-3px content-empty origin-left scale-x-0.15
                  bg-(--accent-color)"
          hover="border-transparent shadow-lg before:scale-x-100"
          :style="{
            'transitionDelay': `${i * 60}ms`,
            '--accent-color': `var(--accent-${f.accent})`,
          }"
        >
          <div flex="~ items-center justify-between" mb-4>
            <span
              text-20px
              font="mono bold"
              tracking="[-0.02em]"
              :style="{ color: 'var(--accent-color)' }"
            >{{ f.id }}</span>
          </div>
          <h3 text-20px m="0 b-2" font-bold tracking="[-0.02em]">
            {{ f.name }}
          </h3>
          <p text-sm text-secondary m-0 leading="[1.55]">
            {{ f.desc }}
          </p>
        </article>
      </div>

      <!-- secondary CTA · 让用户读完特性后能直接入口 -->
      <div class="reveal" text-center mt-12>
        <p text-sm text-tertiary mb-4>
          想直接体验？三个角色任你切换：
        </p>
        <div inline-flex gap-3 flex-wrap justify-center>
          <router-link
            to="/jobs"
            class="transition-[color,background,border,transform]"
            hover:-translate-y-1px text-sm text-primary
            inline-flex items-center
            gap-2
            p="y-10px x-5"
            font-medium
            rounded-full
            bg-elevated
            border="~ subtle"
            no-underline
            duration-fast
            ease-out
            hover="bg-hover border-gray-400"
          >
            <span>候选人 · 投递岗位</span>
            <span text-tertiary>→</span>
          </router-link>
          <router-link
            to="/hr/board"
            class="transition-[color,background,border,transform]"
            hover:-translate-y-1px text-sm text-primary
            inline-flex items-center
            gap-2
            p="y-10px x-5"
            font-medium
            rounded-full
            bg-elevated
            border="~ subtle"
            no-underline
            duration-fast
            ease-out
            hover="bg-hover border-gray-400"
          >
            <span>HR · 招聘看板</span>
            <span text-tertiary>→</span>
          </router-link>
          <router-link
            to="/hr/dashboard"
            class="transition-[color,background,border,transform]"
            hover:-translate-y-1px text-sm text-primary
            inline-flex items-center
            gap-2
            p="y-10px x-5" font-medium
            rounded-full
            bg-elevated
            border="~ subtle"
            no-underline
            duration-fast
            ease-out
            hover="bg-hover border-gray-400"
          >
            <span>ADMIN · 数据看板</span>
            <span text-tertiary>→</span>
          </router-link>
        </div>
      </div>
    </section>

    <!-- ════════════ FOOTER ════════════ -->
    <footer class="reveal" text-center text-sm text-tertiary p="y-8 x-6" border="t subtle">
      <p>
        <span text-gradient>ATS · 招聘管理系统</span>
        · 2026 · Built with Claude in Cursor
      </p>
    </footer>
  </main>
</template>
