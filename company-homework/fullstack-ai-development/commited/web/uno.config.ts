import type { PresetWind4Theme } from 'unocss'
import type { IconsOptions } from 'unocss/preset-icons'
import { createRequire } from 'node:module'
import { FileSystemIconLoader } from '@iconify/utils/lib/loader/node-loaders'
import {
  defineConfig,
  presetAttributify,
  presetIcons,
  presetTypography,
  presetWind4,
  transformerDirectives,
  transformerVariantGroup,
} from 'unocss'
import { navItems } from './app/utils/nav'

export default defineConfig<PresetWind4Theme>({
  presets: [
    /**
     * Tailwind CSS v4 only supports modern browsers. If you need to support legacy browsers, please use `presetWind3` instead.
     *
     * @see https://tailwindcss.com/docs/compatibility
     */
    presetWind4(),
    presetAttributify(),
    presetIcons({
      ...presetLocalIcons(),
      extraProperties: {
        'display': 'inline-block',
        'vertical-align': 'middle',
      },
    }),
    presetTypography(),
  ],
  transformers: [transformerDirectives(), transformerVariantGroup()],

  // nav.ts 里以字符串声明导航 icon 类，不在模板扫描范围内 —— 直接 safelist，随 nav 配置自动维护
  safelist: navItems.map(i => i.icon),

  theme: {
    // 组织人事主色（沉稳蓝，见 docs/05-ui-design.md 设计 token）
    colors: {
      primary: {
        DEFAULT: '#2f54eb',
        50: '#eef3ff',
        100: '#dfe8ff',
        200: '#c6d5ff',
        300: '#a3baff',
        400: '#7d97f9',
        500: '#5b74f0',
        600: '#2f54eb',
        700: '#2342c8',
        800: '#20389d',
        900: '#1f317b',
        950: '#161f4a',
      },
      // presetWind4（Tailwind v4 调色板）没有 truegray —— 显式补一份（≈ neutral），
      // 否则全站 truegray-* 工具类静默失效（文字回退黑色、边框回退 currentColor）。
      truegray: {
        50: '#fafafa',
        100: '#f5f5f5',
        200: '#e5e5e5',
        300: '#d4d4d4',
        400: '#a3a3a3',
        500: '#737373',
        600: '#525252',
        700: '#404040',
        800: '#262626',
        900: '#171717',
        950: '#0a0a0a',
      },
      // 侧栏 / 品牌深墨蓝（带一点冷灰，区别于纯黑）
      ink: {
        DEFAULT: '#151a26',
        100: '#e8eaef',
        200: '#d3d7e0',
        300: '#aab1c2',
        400: '#7a839b',
        500: '#586176',
        600: '#434b5e',
        700: '#333a4a',
        800: '#232938',
        900: '#1a1f2c',
        950: '#11141d',
      },
    },
    /**
     * Use fonts name directly, `@nuxt/font` will auto resolve the font resources from predefined providers.
     *
     * @see https://fonts.nuxt.com/get-started/usage#unocss
     */
    font: {
      sans: 'DM Sans',
      serif: 'DM Serif Display',
      mono: 'DM Mono',
    },
  },
  shortcuts: [
    // ---- 组织人事设计 token（卡片 / 按钮 / 输入 / 表格 / Tab，全站统一）----
    ['app-card', 'rounded-xl border border-black/6 bg-white shadow-[0_1px_2px_rgba(16,24,40,0.04)] dark:border-white/8 dark:bg-ink-900'],
    ['btn-base', 'inline-flex cursor-pointer select-none items-center justify-center gap-1.5 whitespace-nowrap rounded-lg text-sm font-medium transition-colors duration-150 disabled:pointer-events-none disabled:opacity-50'],
    ['btn-primary', 'btn-base bg-primary px-3.5 py-2 text-white shadow-sm shadow-primary/25 hover:bg-primary-700 active:bg-primary-800'],
    ['btn-secondary', 'btn-base border border-black/8 bg-white px-3.5 py-2 text-truegray-600 shadow-sm hover:border-primary/35 hover:text-primary dark:border-white/10 dark:bg-ink-800 dark:text-truegray-300 dark:hover:text-primary-300'],
    ['btn-danger', 'btn-base border border-rose-200 bg-white px-3.5 py-2 text-rose-600 shadow-sm hover:bg-rose-50 dark:border-rose-500/30 dark:bg-transparent dark:text-rose-400 dark:hover:bg-rose-500/10'],
    ['input-base', 'rounded-lg border border-black/10 bg-white px-3 py-2 text-sm outline-none transition-colors placeholder:text-truegray-400 focus:border-primary focus:ring-3 focus:ring-primary/12 dark:border-white/10 dark:bg-ink-800'],
    ['th-base', 'whitespace-nowrap py-2.5 pr-4 text-left text-xs font-medium tracking-wide text-truegray-400'],
    ['td-base', 'py-2.5 pr-4 align-middle'],
    ['row-base', 'border-b border-black/4 transition-colors last:border-0 hover:bg-truegray-50/80 dark:border-white/6 dark:hover:bg-white/4'],
    ['link-action', 'cursor-pointer text-sm font-medium text-primary underline-offset-3 hover:text-primary-700 hover:underline dark:text-primary-300'],
    ['seg-group', 'inline-flex items-center gap-0.5 self-start rounded-lg border border-black/6 bg-white p-0.5 shadow-sm dark:border-white/8 dark:bg-ink-900'],
    ['seg-item', 'cursor-pointer rounded-md px-3 py-1.5 text-sm text-truegray-500 transition-colors hover:text-truegray-800 dark:text-truegray-400 dark:hover:text-truegray-200'],
    ['seg-item-active', 'cursor-pointer rounded-md bg-primary/8 px-3 py-1.5 text-sm font-medium text-primary dark:bg-primary/15 dark:text-primary-300'],

    // Nuxt & Vite colors
    [/^(bg|text|decoration|border|fill|stroke)-(nuxt|vite)$/, ([, prefix, suffix]) => {
      const color = suffix === 'nuxt' ? 'green-400' : 'purple-500'
      return `${prefix}-${color}`
    }],
    // Vitesse colors, use Nuxt color for normal state, Vite color for hover state, with transition on hover
    [/^(bg|text|decoration|border|fill|stroke)-vitesse$/, ([, prefix]) => {
      return `${prefix}-nuxt hover:${prefix}-vite transition-colors duration-200 ease-in-out`
    }],
    // Vitesse colors with specific states
    [/^(bg|text|decoration|border|fill|stroke)-vitesse-(nuxt|vite)$/, ([, prefix, suffix]) => {
      return `hover:${prefix}-${suffix} transition-colors duration-200 ease-in-out`
    }],
  ],
})

/**
 * Preset for loading local icons from `./public/icons` directory, with width and height transformed to `em` unit for better scaling.
 */
function presetLocalIcons(): IconsOptions {
  return {
    collections: {
      public: FileSystemIconLoader('./public/icons'),
      // 显式注册 carbon：传入自定义 collections 后 iconify 包不再自动解析，
      // 不注册的话全站 i-carbon-* 图标静默不渲染。（require 读 JSON，绕开 ESM import attribute 限制）
      carbon: () => createRequire(import.meta.url)('@iconify-json/carbon/icons.json'),
    },
    processor(props, meta) {
      if (meta.collection === 'public') {
        let { width = 1, height = 1 } = props
        if (typeof width === 'string')
          width = Number.parseInt(width)
        if (typeof height === 'string')
          height = Number.parseInt(height)
        const min = Math.min(width, height)
        props.width = `${Math.round((width / min) * 100) / 100}em`
        props.height = `${Math.round((height / min) * 100) / 100}em`
      }
    },
  }
}
