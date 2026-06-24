// @ts-check
import { antfu } from '@antfu/eslint-config'
import oxlint from 'eslint-plugin-oxlint'
import nuxt from './.nuxt/eslint.config.mjs'

export default antfu(
  {
    unocss: true,
    perfectionist: {
      overrides: {
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
    },
  },
  /**
   * Don't forget to set `standalone: false` in your Nuxt ESLint module options.
   */
  nuxt(),
  ...oxlint.buildFromOxlintConfigFile('.oxlintrc.json'),
)
