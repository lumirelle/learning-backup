// @ts-check
import { antfu } from '@antfu/eslint-config'
import oxlint from 'eslint-plugin-oxlint'

export default antfu(
  {
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
    toml: {
      overrides: {
        'toml/array-element-newline': ['error', 'consistent'],
        'toml/array-bracket-spacing': ['error', 'never'],
      },
    },
  },
  ...oxlint.buildFromOxlintConfigFile('.oxlintrc.json'),
)
