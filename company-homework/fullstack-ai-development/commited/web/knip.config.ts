import type { KnipConfig } from 'knip'

export default {
  entry: [
    './app/**/*.{ts,vue}',
    './shared/types/**/*',
  ],
  ignoreBinaries: ['mise'],
  ignoreDependencies: [
    // Icons.
    '@iconify-json/*',

    // Frontend libraries.
    '@takumi-rs/core',
    '@vueuse/core',
    'vue-router',

    // We uses their types, but we don't import them directly.
    'nitropack',
    'ofetch',

    // Code quality tools.
    '@lumirelle/oxlint-config',
    '@unocss/eslint-config',
    'vue-tsc',

    // Vitest environment for Nuxt.
    'vitest-environment-nuxt',
  ],
} satisfies KnipConfig
