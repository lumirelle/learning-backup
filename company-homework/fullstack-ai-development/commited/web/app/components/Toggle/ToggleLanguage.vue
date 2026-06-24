<script setup lang="ts">
const { locale, locales } = useI18n()

function getNationFlag(code: string): string {
  if (code === 'zh') {
    return 'i-twemoji-flag-china'
  }
  else if (code === 'en') {
    return 'i-twemoji-flag-united-kingdom'
  }
  return 'i-twemoji-flag-united-nations'
}
const availableLocales = computed(() =>
  locales.value.filter(i => i.code !== locale.value)
    .map(i => ({ ...i, flag: getNationFlag(i.code) })),
)
</script>

<template>
  <SwitchLocalePathLink v-for="availableLocale in availableLocales" :key="availableLocale.code" :class="availableLocale.flag" :locale="availableLocale.code" aria-label="Toggle Language" />
</template>
