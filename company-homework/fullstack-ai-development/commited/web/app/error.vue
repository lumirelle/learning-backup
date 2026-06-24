<!-- This page is base on built-in Nuxt error page, you can customize it to fit your needs -->

<script setup lang="ts">
import type { NuxtError } from '#app'

const { error } = defineProps<{
  error: NuxtError
}>()

useHead({
  title: `${error.status} - ${error.statusText || 'Internal Server Error'}`,
})

const { t } = useI18n()
</script>

<template>
  <div text="black dark:white" font-sans px-10 py-12 bg="white dark:[#020420]" flex="~ col" min-h-screen antialiased>
    <h1 text="6xl sm:8xl" font-medium mb-4 v-text="error.status" />
    <p text="xl sm:lg" leading-tight font-light mb-8>
      <span v-text="error.statusText" />&nbsp;&nbsp;<NuxtLinkLocale to="index" underline hover:text="[#00DC82]">
        {{ t('error_page.back-home') }}
      </NuxtLinkLocale>
    </p>
    <a
      href="https://nuxt.com/docs/4.x/getting-started/error-handling?utm_source=nuxt-error-dev-page"
      target="_blank"
      text-sm font-medium mx-auto underline-offset-3 inline-block top-6 absolute hover:text="[#00DC82]" hover:underline sm:right-6
    >{{ t('error_page.customize') }}</a>
    <div code-block tabindex="0">
      <pre code-pre v-html="error.stack" />
    </div>
  </div>
</template>
