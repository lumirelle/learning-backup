import process from 'node:process'
import { appDescription, appName, colorThemeDark, colorThemeLight } from './config/app'
import { SUPPORT_LOCALES } from './config/i18n'

export default defineNuxtConfig({
  /// keep-sorted
  modules: [
    '@nuxt/a11y',
    '@nuxt/eslint',
    '@nuxt/fonts',
    '@nuxt/hints',
    '@nuxt/image',
    '@nuxt/scripts',
    '@nuxt/test-utils/module',
    '@nuxtjs/color-mode',
    '@nuxtjs/i18n',
    '@nuxtjs/seo',
    '@pinia/nuxt',
    '@unocss/nuxt',
    '@vite-pwa/nuxt',
    '@vueuse/nuxt',
  ],

  $test: {
    vue: {
      runtimeCompiler: true,
    },
    experimental: {
      payloadExtraction: false,
      // Does not support now, see https://github.com/npmx-dev/npmx.dev
      viteEnvironmentApi: false,
    },
    nitro: {
      preset: 'node',
    },
    ogImage: false,
    // XXX(Lumirelle): Cause tests failure, need to investigate.
    pwa: false,
    unocss: false,
  },

  // 内网管理后台：SPA 模式，省去 SSR + 鉴权 cookie + 代理在服务端的复杂度。
  ssr: false,

  devtools: {
    enabled: true,
    timeline: { enabled: true },
  },

  app: {
    head: {
      templateParams: { separator: '·' },
      meta: [
        // `favicon.ico`, `apple-touch-icon.png` are handled by `nuxt-seo-utils`
        // See https://nuxtseo.com/docs/nuxt-seo/guides/using-the-modules#seo-utils
        { name: 'apple-mobile-web-app-status-bar-style', content: 'black-translucent' },
        // See https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Elements/meta/name/theme-color
        { name: 'theme-color', media: '(prefers-color-scheme: light)', content: colorThemeLight },
        { name: 'theme-color', media: '(prefers-color-scheme: dark)', content: colorThemeDark },
      ],
    },
  },

  css: ['~/assets/styles/main.css'],

  router: {
    options: {
      scrollBehaviorType: 'smooth',
    },
  },

  site: {
    name: appName,
    description: appDescription,
  },

  colorMode: {
    // Better eyes!
    fallback: 'dark',
  },

  runtimeConfig: {
    public: {
      XXX_API_KEY: undefined,
    },
  },

  future: {
    compatibilityVersion: 5,
  },

  /**
   * @see https://github.com/nuxt/nuxt/blob/main/packages/schema/src/config/experimental.ts for default values
   */
  experimental: {
    inlineRouteRules: true,
    typedPages: true,
    typescriptPlugin: true,
  },

  compatibilityDate: '2026-03-13',

  nitro: {
    preset: 'bun',
    // 开发期把 /api/v1 反代到 Go 后端（生产由 nginx/Caddy 反代）。
    // 目标端口与后端 APP_PORT 默认值（8090）一致；如需改端口，设 NUXT_DEV_API_TARGET 覆盖，
    // 无需改代码（例如 NUXT_DEV_API_TARGET=http://localhost:8081/api/v1）。
    devProxy: {
      '/api/v1': {
        target: process.env.NUXT_DEV_API_TARGET || 'http://localhost:8090/api/v1',
        changeOrigin: true,
      },
    },
  },

  vite: {
    optimizeDeps: {
      include: [
        '@antfu/utils',
        '@vueuse/core',
      ],
    },
  },

  typescript: {
    nodeTsConfig: {
      include: ['../*.ts'],
    },
  },

  a11y: {
    axe: {
      options: {
        rules: [
          { id: 'color-contrast', enabled: false },
        ],
      },
      runOptions: {},
    },
  },

  eslint: {
    config: {
      standalone: false,
      nuxt: { sortConfigKeys: true },
    },
  },

  i18n: {
    locales: SUPPORT_LOCALES,
    defaultLocale: 'en',
    detectBrowserLanguage: {
      cookieKey: 'locale',
    },
    experimental: {
      typedOptionsAndMessages: 'all',
    },
  },

  image: {
    format: ['avif', 'webp'],
    presets: {
      avatar: {
        modifiers: { format: 'webp', width: 48, height: 48, fit: 'cover' },
      },
      logo: {
        modifiers: { format: 'webp', width: 200, height: 80, fit: 'cover' },
      },
      // ...
    },
    /**
     * Enable image optimization on external websites.
     *
     * @see https://image.nuxt.com/get-started/configuration#domains
     */
    domains: [
      'images.unsplash.com',
      // ...
    ],
    alias: {
      unsplash: 'https://images.unsplash.com',
      // ...
    },
  },

  pwa: {
    manifest: {
      name: appName,
      short_name: appName,
      description: appDescription,
      theme_color: '#ffffff',
      icons: [
        { src: 'pwa-192x192.png', sizes: '192x192', type: 'image/png' },
        { src: 'pwa-512x512.png', sizes: '512x512', type: 'image/png' },
        { src: 'maskable-icon.png', sizes: '512x512', type: 'image/png', purpose: 'any maskable' },
      ],
    },
  },

  robots: {
    blockNonSeoBots: true,
  },

  vueuse: {
    ssrHandlers: true,
  },
})
