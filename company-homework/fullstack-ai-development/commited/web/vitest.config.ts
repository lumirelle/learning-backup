import { defineVitestProject } from '@nuxt/test-utils/config'
import { isWindows } from 'std-env'
import { defineConfig } from 'vitest/config'

const rootDir = import.meta.dirname

export default defineConfig({
  resolve: {
    alias: {
      '~': `${rootDir}/app`,
      '~~': rootDir,
      '#shared': `${rootDir}/shared`,
      '#server': `${rootDir}/server`,
    },
  },
  test: {
    projects: [
      {
        test: {
          name: 'unit',
          include: ['./test/unit/*.{test,spec}.ts'],
          benchmark: { include: ['./test/unit/*.{bench,benchmark}.ts'] },
        },
      },
      await defineVitestProject({
        test: {
          name: 'nuxt',
          include: ['./test/nuxt/*.{test,spec}.ts'],
          benchmark: { include: ['./test/nuxt/*.{bench,benchmark}.ts'] },
          environment: 'nuxt',
        },
      }),
      {
        test: {
          name: 'e2e',
          include: ['./test/e2e/*.{test,spec}.ts'],
          benchmark: {
            include: ['./test/e2e/*.{bench,benchmark}.ts'],
          },
          testTimeout: isWindows ? 36_000 : 12_000,
        },
      },
    ],
    coverage: {
      enabled: true,
      exclude: ['**/node_modules/**', '**/*.json'],
    },
  },
})
