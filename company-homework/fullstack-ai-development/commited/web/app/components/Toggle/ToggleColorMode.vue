<script setup lang="ts">
const color = useColorMode()

/**
 * XXX(Lumirelle): In Nitro 3 / Nuxt 5, `app.config.ts`, `defineAppConfig`, and `useAppConfig` seem no longer supported. Maybe we should prepare to migrate to a new way?
 */
const { colorThemeDark, colorThemeLight } = useAppConfig()
/**
 * Switching the theme color.
 *
 * @see https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Elements/meta/name/theme-color
 */
useHead({
  meta: [
    {
      id: 'theme-color',
      name: 'theme-color',
      content: (): string => (color.value === 'dark' ? colorThemeDark : colorThemeLight),
    },
  ],
})

function toggleDark(): void {
  color.preference = color.value === 'dark' ? 'light' : 'dark'
}
</script>

<template>
  <a class="i-carbon-sun h-[1em] w-[1em] cursor-pointer dark:i-carbon-moon !outline-none" href="javascript:void(0)" aria-label="Toggle Color Mode" @click="toggleDark" />
</template>
