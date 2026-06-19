// @ts-check
import { antfu } from '@antfu/eslint-config'
import oxlint from 'eslint-plugin-oxlint'

export default antfu(
  {
    // monorepo：root 检测不到 vue/ts dep，需要显式开启
    vue: true,
    typescript: true,
    ignores: [
      '**/dist/**',
      '**/node_modules/**',
      '**/target/**',
      'ats-backend/**',
      'docs/**/*.html',
      // 运维 yaml 由 docker-compose / Spring 消费，引号风格有语义且需跨 YAML 1.1/1.2 兼容
      '**/*.{yml,yaml}',
    ],
  },
  ...oxlint.buildFromOxlintConfigFile('.oxlintrc.json'),
)
  // If you are not using `bun`, you can remove this.
  .override('antfu/perfectionist/setup', {
    rules: {
      'perfectionist/sort-imports': [
        'error',
        {
          environment: 'bun',
          groups: [
            'type-import',
            ['type-parent', 'type-sibling', 'type-index', 'type-internal'],
            'value-builtin',
            'value-external',
            'value-internal',
            ['value-parent', 'value-sibling', 'value-index'],
            'side-effect',
            'ts-equals-import',
            'unknown',
          ],
          newlinesBetween: 'ignore',
          newlinesInside: 'ignore',
          order: 'asc',
          type: 'natural',
        },
      ],
    },
  })
