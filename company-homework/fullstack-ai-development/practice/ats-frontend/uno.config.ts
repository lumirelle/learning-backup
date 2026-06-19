import {
  defineConfig,
  presetAttributify,
  presetIcons,
  presetUno,
  transformerDirectives,
  transformerVariantGroup,
} from 'unocss'

/**
 * UnoCSS · ATS Frontend
 *
 * 设计原则（沉淀进 SKILL.md 2.6）：
 *  1. CSS custom properties（tokens.css）是 **单一真值来源**；
 *     UnoCSS theme 通过 var(--xxx) 引用 token，绝不重复维护色值。
 *  2. 现有手写 utility（text-gradient / aurora-layer / fade-* 等）
 *     全部以同名 shortcut 提供，组件零修改即可迁移。
 *  3. 微交互（hover）与 页面转场（route）严格区分两套 ease/duration token，
 *     在 theme.transitionTimingFunction / animation 里都有体现。
 *  4. 复杂 keyframes（aurora-drift / pulse / cursor-glow）仍写在
 *     global.css 里，shortcuts 只负责"组合"它们。
 */
export default defineConfig({
  /** 与 tokens.css 的 [data-theme="dark"] 对齐，供 dark: 变体使用 */
  darkMode: ['selector', '[data-theme="dark"]'],
  presets: [
    presetUno(),
    presetAttributify({ prefix: 'u-', prefixedOnly: false }),
    presetIcons({
      scale: 1.1,
      extraProperties: {
        'display': 'inline-block',
        'vertical-align': 'middle',
      },
    }),
  ],
  transformers: [
    transformerDirectives(), // @apply / @screen / theme()
    transformerVariantGroup(), // hover:(bg-x text-y) → 拆开
  ],

  /** Token 桥接：theme 里的色板/间距/字号/曲线全部引用 CSS custom properties */
  theme: {
    colors: {
      brand: {
        50: 'var(--brand-50)',
        100: 'var(--brand-100)',
        200: 'var(--brand-200)',
        300: 'var(--brand-300)',
        400: 'var(--brand-400)',
        500: 'var(--brand-500)',
        600: 'var(--brand-600)',
        700: 'var(--brand-700)',
        800: 'var(--brand-800)',
        900: 'var(--brand-900)',
        DEFAULT: 'var(--brand-500)',
      },
      gray: {
        0: 'var(--gray-0)',
        50: 'var(--gray-50)',
        100: 'var(--gray-100)',
        150: 'var(--gray-150)',
        200: 'var(--gray-200)',
        300: 'var(--gray-300)',
        400: 'var(--gray-400)',
        500: 'var(--gray-500)',
        600: 'var(--gray-600)',
        700: 'var(--gray-700)',
        800: 'var(--gray-800)',
        900: 'var(--gray-900)',
        950: 'var(--gray-950)',
      },
      success: {
        50: 'var(--success-50)',
        500: 'var(--success-500)',
        700: 'var(--success-700)',
        DEFAULT: 'var(--success-500)',
      },
      warning: {
        50: 'var(--warning-50)',
        500: 'var(--warning-500)',
        700: 'var(--warning-700)',
        DEFAULT: 'var(--warning-500)',
      },
      danger: {
        50: 'var(--danger-50)',
        500: 'var(--danger-500)',
        700: 'var(--danger-700)',
        DEFAULT: 'var(--danger-500)',
      },
      info: {
        50: 'var(--info-50)',
        500: 'var(--info-500)',
        700: 'var(--info-700)',
        DEFAULT: 'var(--info-500)',
      },
      // 张扬武器库 accent 七色（成长系）
      accent: {
        mint: 'var(--accent-mint)',
        emerald: 'var(--accent-emerald)',
        teal: 'var(--accent-teal)',
        cyan: 'var(--accent-cyan)',
        lime: 'var(--accent-lime)',
        amber: 'var(--accent-amber)',
        forest: 'var(--accent-forest)',
      },
    },
    backgroundColor: {
      app: 'var(--bg-app)',
      elevated: 'var(--bg-elevated)',
      muted: 'var(--bg-muted)',
      hover: 'var(--bg-hover)',
      active: 'var(--bg-active)',
    },
    textColor: {
      primary: 'var(--text-primary)',
      secondary: 'var(--text-secondary)',
      tertiary: 'var(--text-tertiary)',
      disabled: 'var(--text-disabled)',
      inverse: 'var(--text-inverse)',
    },
    borderColor: {
      subtle: 'var(--border-subtle)',
      default: 'var(--border-default)',
    },
    fontFamily: {
      sans: 'var(--font-sans)',
      mono: 'var(--font-mono)',
    },
    fontSize: {
      'xs': 'var(--fs-xs)',
      'sm': 'var(--fs-sm)',
      'md': 'var(--fs-md)',
      'lg': 'var(--fs-lg)',
      'xl': 'var(--fs-xl)',
      '2xl': 'var(--fs-2xl)',
      '3xl': 'var(--fs-3xl)',
      'display-sm': 'var(--fs-display-sm)',
      'display-md': 'var(--fs-display-md)',
      'display-lg': 'var(--fs-display-lg)',
    },
    borderRadius: {
      sm: 'var(--radius-sm)',
      md: 'var(--radius-md)',
      lg: 'var(--radius-lg)',
      full: 'var(--radius-full)',
    },
    boxShadow: {
      'sm': 'var(--shadow-sm)',
      'md': 'var(--shadow-md)',
      'lg': 'var(--shadow-lg)',
      'glow-brand': 'var(--glow-brand)',
      'glow-mint': 'var(--glow-mint)',
      'glow-teal': 'var(--glow-teal)',
      'glow-amber': 'var(--glow-amber)',
    },
    /** ⚠️ 曲线分两轨：page-* 给路由/模态，默认给 hover/focus */
    transitionTimingFunction: {
      'out': 'var(--ease-out)',
      'in-out': 'var(--ease-in-out)',
      'bounce': 'var(--ease-bounce)',
      'page-in': 'var(--ease-page-in)',
      'page-out': 'var(--ease-page-out)',
    },
    transitionDuration: {
      'fast': 'var(--dur-fast)',
      'base': 'var(--dur-base)',
      'slow': 'var(--dur-slow)',
      'page-in': 'var(--dur-page-in)',
      'page-out': 'var(--dur-page-out)',
      'fade': 'var(--dur-fade)',
    },
  },

  /**
   * Rules：自定义产出真实 CSS 的 utility（高于 shortcut，可控性更强）
   *
   * ⚠️ 渐变相关 utility 必须用 rule 而不是 shortcut——
   * UnoCSS 的 `bg-(--xxx)` 默认推断为 background-color，linear-gradient 会失效；
   * 这里显式输出 background-image 才能让 background-clip:text 配套渲染。
   */
  rules: [
    // text-gradient · text-gradient-spring|bloom|forest|brand|aurora（渐变文字）
    [
      /^text-gradient(?:-(spring|bloom|forest|brand|aurora))?$/,
      ([, name]) => ({
        'background-image': `var(--grad-${name || 'spring'})`,
        '-webkit-background-clip': 'text',
        'background-clip': 'text',
        'color': 'transparent',
        '-webkit-text-fill-color': 'transparent',
      }),
    ],
    // bg-grad-spring|bloom|forest|brand|aurora（渐变背景，显式 background-image）
    [
      /^bg-grad-(spring|bloom|forest|brand|aurora)$/,
      ([, name]) => ({
        'background-image': `var(--grad-${name})`,
      }),
    ],
    // bg-hero · bg-app-* 等复合 background 简写（含底色 fallback，必须 background: shorthand）
    ['bg-hero', { background: 'var(--bg-hero)' }],

    /**
     * 自定义 animation utility——keyframes 仍写在 global.css，这里只产出 utility。
     * 走 rule 而非 theme.animation 是为了：
     *   1) theme.animation 在不提供 keyframes 字段时 UnoCSS 不生成 utility（实测）
     *   2) rule 更直观，所见即所得
     */
    ['animate-pulse-ring', { animation: 'pulse-ring 2s ease-in-out infinite' }],
    ['animate-shimmer', { animation: 'shimmer 2.5s ease-in-out infinite' }],
    ['animate-aurora-shift', { animation: 'aurora-shift 18s ease-in-out infinite' }],
    ['animate-orb-float-a', { animation: 'orb-float-a 14s ease-in-out infinite' }],
    ['animate-orb-float-b', { animation: 'orb-float-b 16s ease-in-out infinite' }],
    ['animate-gradient-flow', { animation: 'gradient-flow 6s ease-in-out infinite' }],
    ['animate-card-bob', { animation: 'card-bob 5s ease-in-out infinite' }],
    ['animate-tag-float-a', { animation: 'tag-float-a 7s ease-in-out infinite' }],
    ['animate-tag-float-b', { animation: 'tag-float-b 8s ease-in-out infinite' }],

    // text-stroke（CSS 原生没 utility，UnoCSS 也没默认；走自定义 rule）
    [
      /^text-stroke-(\d+)$/,
      ([, w]) => ({
        '-webkit-text-stroke-width': `${w}px`,
        'color': 'transparent',
      }),
    ],
    [
      /^text-stroke-(\[[^\]]+\]|white|black|brand)$/,
      ([, c]) => {
        const color = c.startsWith('[') ? c.slice(1, -1) : c === 'brand' ? 'var(--brand-500)' : c
        return { '-webkit-text-stroke-color': color }
      },
    ],
  ],

  /**
   * Shortcuts：纯 atomic 组合（不涉及自定义 CSS 属性）
   *
   * 组织约定：
   *   layout-*     布局原子
   *   typo-*       排版原子
   *   surface-*    表面/容器（玻璃、卡片）
   *   btn-*        按钮变体
   *   field-*      表单原子
   *   brand-*      品牌视觉（hero / logo / orb）
   *   anim-class   仅做动画绑定（需 keyframes 在 global.css）
   */
  shortcuts: {
    // ─ Layout ─
    'card-base': 'bg-elevated border border-subtle rounded-lg shadow-sm',
    'card-hover': 'transition-[transform,box-shadow,border-color] duration-260 ease-out hover:(-translate-y-1 shadow-lg)',
    'center-grid': 'grid place-content-center',
    'center-flex': 'flex items-center justify-center',
    'between-flex': 'flex items-center justify-between',
    'col-flex': 'flex flex-col',

    // ─ Typography ─
    'heading-1': 'text-3xl font-bold tracking-tight',
    'heading-2': 'text-2xl font-bold tracking-tight',
    'kicker': 'font-mono text-xs font-semibold uppercase tracking-1.5px text-accent-emerald',
    'eyebrow': 'flex items-center gap-3 text-11px font-semibold uppercase tracking-.25em',

    // ─ Surface（玻璃质感容器）─
    'surface-glass': 'border border-default bg-app/70 backdrop-blur-xl backdrop-saturate-180',
    'surface-glass-dark': 'border border-white/8 bg-white/4 backdrop-blur-md',
    'surface-elevated': 'bg-elevated border border-subtle rounded-xl shadow-sm',

    // ─ Buttons ─
    'btn-primary':
      'relative w-full overflow-hidden rounded-xl px-18px py-14px text-sm font-semibold text-white border-none cursor-pointer '
      + 'bg-[linear-gradient(135deg,#10b981_0%,#059669_50%,#14b8a6_100%)] '
      + 'shadow-[0_0_0_1px_rgba(255,255,255,.08)_inset,0_2px_4px_rgba(0,0,0,.08),0_8px_24px_rgba(16,185,129,.4)] '
      + 'transition-[transform,box-shadow] duration-150 ease-out '
      + 'hover:(-translate-y-px shadow-[0_0_0_1px_rgba(255,255,255,.14)_inset,0_4px_8px_rgba(0,0,0,.1),0_12px_32px_rgba(16,185,129,.55)]) '
      + 'active:(translate-y-0 scale-98) '
      + 'disabled:(opacity-65 cursor-not-allowed)',
    'btn-secondary':
      'flex items-center justify-center gap-1.5 w-full px-18px py-3 text-sm font-semibold rounded-xl no-underline '
      + 'text-secondary bg-elevated border border-default '
      + 'transition-all duration-260 ease-out '
      + 'hover:(text-brand-700 border-brand-300 bg-[rgba(16,185,129,.04)] -translate-y-px)',
    'btn-cta':
      'relative inline-flex items-center gap-1.5 px-4 py-2 text-13px font-semibold text-white rounded-10px overflow-hidden no-underline '
      + 'bg-[linear-gradient(135deg,#10b981_0%,#059669_60%,#14b8a6_100%)] '
      + 'shadow-[0_0_0_1px_rgba(255,255,255,.08)_inset,0_1px_2px_rgba(0,0,0,.1),0_4px_16px_rgba(16,185,129,.35)] '
      + 'transition-[transform,box-shadow] duration-260 ease-out '
      + 'hover:(-translate-y-px shadow-[0_0_0_1px_rgba(255,255,255,.12)_inset,0_2px_4px_rgba(0,0,0,.1),0_8px_24px_rgba(16,185,129,.5)]) '
      + 'active:(translate-y-0 scale-97)',

    // 主按钮上的扫光层
    'btn-shimmer':
      'pointer-events-none absolute inset-0 -translate-x-full animate-shimmer '
      + 'bg-[linear-gradient(90deg,transparent,rgba(255,255,255,.2),transparent)]',

    // CTA hover 流光（配合 group 使用）
    'cta-glow':
      'absolute inset-0 -translate-x-full transition-transform duration-600ms ease-out '
      + 'bg-[linear-gradient(90deg,transparent,rgba(255,255,255,.25),transparent)] '
      + 'group-hover:translate-x-full',

    // ─ Form fields ─
    'kbd-hint':
      'inline-flex items-center justify-center min-w-20px h-18px px-5px font-mono text-11px font-semibold '
      + 'text-white/70 bg-white/15 border border-white/20 rounded-4px',

    'error-banner':
      'flex items-start gap-2.5 px-14px py-3 rounded-xl text-sm '
      + 'text-[#dc2626] bg-[linear-gradient(135deg,rgba(239,68,68,.08),rgba(239,68,68,.04))] '
      + 'border border-[rgba(239,68,68,.25)]',
    'error-icon':
      'inline-flex items-center justify-center flex-shrink-0 w-5 h-5 rounded-md '
      + 'bg-[rgba(239,68,68,.15)] text-[#dc2626] mt-px',

    // demo 账号卡（虚线 emerald）
    'demo-card':
      'p-2 rounded-xl border border-dashed border-[rgba(16,185,129,.3)] '
      + 'bg-[linear-gradient(135deg,rgba(16,185,129,.06),rgba(6,182,212,.04))]',
    'demo-icon':
      'inline-flex items-center justify-center flex-shrink-0 w-7 h-7 rounded-lg '
      + 'bg-[rgba(16,185,129,.12)] text-sm',
    'demo-fill':
      'flex-shrink-0 px-2.5 py-1 text-11px font-semibold rounded-md cursor-pointer '
      + 'text-brand-700 bg-[rgba(16,185,129,.12)] border border-[rgba(16,185,129,.25)] '
      + 'transition-all duration-150 ease-out hover:(bg-[rgba(16,185,129,.18)] border-[rgba(16,185,129,.4)])',

    // ─ Navbar ─
    'navbar-glass':
      'border-b border-default bg-app/75 backdrop-blur-xl backdrop-saturate-180',
    'navbar-glow-line':
      'pointer-events-none absolute inset-x-0 bottom-0 h-px opacity-0 transition-opacity duration-400ms ease-out '
      + 'bg-[linear-gradient(90deg,transparent_0%,rgba(16,185,129,.3)_25%,rgba(6,182,212,.4)_50%,rgba(16,185,129,.3)_75%,transparent_100%)]',
    'logo-mark':
      'inline-flex items-center justify-center w-7 h-7 rounded-lg '
      + 'bg-[linear-gradient(135deg,#34d399,#14b8a6)] '
      + 'shadow-[0_0_16px_rgba(16,185,129,.4),inset_0_1px_0_rgba(255,255,255,.2)] '
      + 'transition-[transform,box-shadow] duration-260 ease-out',
    'logo-mark-lg':
      'inline-flex items-center justify-center w-8 h-8 rounded-10px '
      + 'bg-[linear-gradient(135deg,#34d399,#14b8a6)] '
      + 'shadow-[0_0_24px_rgba(16,185,129,.55),inset_0_1px_0_rgba(255,255,255,.25)]',
    'version-pill':
      'text-10px font-semibold tracking-wide tabular-nums '
      + 'px-1.5 py-px rounded text-tertiary bg-hover',
    'user-trigger':
      'flex items-center gap-2.5 pr-3 pl-5px py-5px rounded-xl border border-transparent cursor-pointer bg-transparent '
      + 'transition-all duration-260 ease-out '
      + 'hover:(border-default bg-hover)',
    'avatar':
      'inline-flex items-center justify-center flex-shrink-0 w-30px h-30px rounded-10px text-xs font-bold text-white '
      + 'shadow-[0_2px_8px_rgba(0,0,0,.15),inset_0_1px_0_rgba(255,255,255,.2)]',

    // ─ Brand visual（认证页左侧）─
    'brand-pane':
      'relative hidden lg:block overflow-hidden bg-[#08090c] '
      + '[isolation:isolate]',
    'hero-display':
      'font-display font-black leading-[.85] tracking-tight text-[clamp(3.5rem,6.5vw,5.5rem)]',
    'hero-outline':
      'block text-stroke-1.5 text-stroke-[rgba(255,255,255,.85)] '
      + '[text-shadow:0_0_60px_rgba(255,255,255,.08)]',
    // 渐变描边大标题（流光 + drop-shadow）
    'hero-gradient':
      'block bg-[linear-gradient(135deg,#6ee7b7_0%,#34d399_35%,#14b8a6_65%,#22d3ee_100%)] '
      + 'bg-[length:200%_200%] bg-clip-text text-transparent animate-gradient-flow '
      + '[filter:drop-shadow(0_8px_30px_rgba(16,185,129,.35))]',
    'hero-gradient-cyan':
      'block bg-[linear-gradient(135deg,#22d3ee_0%,#14b8a6_35%,#34d399_65%,#6ee7b7_100%)] '
      + 'bg-[length:200%_200%] bg-clip-text text-transparent animate-gradient-flow '
      + '[filter:drop-shadow(0_8px_30px_rgba(6,182,212,.35))]',

    // 极光底色（依赖 .aurora-bg keyframes 在 global.css）
    'aurora-bg-login':
      'pointer-events-none absolute inset-0 [filter:blur(20px)_saturate(120%)] animate-aurora-shift '
      + 'bg-[radial-gradient(ellipse_60%_50%_at_20%_20%,rgba(16,185,129,.35)_0%,transparent_60%),radial-gradient(ellipse_70%_60%_at_80%_80%,rgba(6,182,212,.30)_0%,transparent_55%),radial-gradient(ellipse_40%_40%_at_60%_30%,rgba(168,85,247,.18)_0%,transparent_60%)]',
    'aurora-bg-register':
      'pointer-events-none absolute inset-0 [filter:blur(20px)_saturate(120%)] animate-aurora-shift '
      + 'bg-[radial-gradient(ellipse_60%_50%_at_80%_20%,rgba(6,182,212,.38)_0%,transparent_60%),radial-gradient(ellipse_70%_60%_at_20%_80%,rgba(16,185,129,.30)_0%,transparent_55%),radial-gradient(ellipse_40%_40%_at_50%_50%,rgba(168,85,247,.16)_0%,transparent_60%)]',

    // 网格 overlay（深色面板上）
    'grid-overlay':
      'pointer-events-none absolute inset-0 opacity-.045 '
      + '[background-image:linear-gradient(rgba(255,255,255,.6)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,.6)_1px,transparent_1px)] '
      + '[background-size:56px_56px] [mask-image:radial-gradient(ellipse_80%_70%_at_50%_40%,#000_40%,transparent_100%)]',

    // 光球（颜色 + 位置由各页传入 attribute；这里只给共享基线）
    'orb-base':
      'pointer-events-none absolute w-360px h-360px rounded-full [filter:blur(80px)] will-change-transform',
  },

  /** 安全名单：动态拼接的 class 必须放进来，否则会被 purge */
  safelist: [
    // 里程碑卡 accent
    ...['mint', 'emerald', 'teal', 'cyan', 'amber', 'lime', 'forest'].map(c => `accent-${c}`),
    // 状态
    'status-done',
    'status-doing',
    'status-todo',
    // 健康探针状态
    'data-state-up',
    'data-state-down',
    'data-state-pending',
  ],
})
