import antfu from '@antfu/eslint-config'

export default antfu({
  formatters: true,
  ignores: ['**/static', '**/uni_modules'],
})
  // Override rules of `@antfu/config`
  .override('antfu/javascript/rules', {
    rules: {
      // We need to use `console` in development environment, we can use build plugin to remove it in production environment
      'no-console': 'off',
    },
  })
  // FIXME: Fix these warnings progressively, because they are too many
  .override('antfu/javascript/rules', {
    rules: {
      'array-callback-return': 'warn',
      'eqeqeq': 'warn',
      'unused-imports/no-unused-vars': 'warn',
      'unused-imports/no-unused-imports': 'warn',
      'prefer-promise-reject-errors': 'warn',
    },
  })
  .override('antfu/typescript/rules', {
    rules: {
      'ts/method-signature-style': ['error', 'method'],
      'ts/no-use-before-define': 'warn',
    },
  })
  .override('antfu/vue/rules', {
    rules: {
      'vue/eqeqeq': 'warn',
      'vue/component-definition-name-casing': ['error', 'kebab-case'],
      'vue/component-name-in-template-casing': ['error', 'kebab-case'],
      'vue/custom-event-name-casing': ['warn', 'kebab-case'],
      'vue/no-mutating-props': 'warn',
    },
  })
