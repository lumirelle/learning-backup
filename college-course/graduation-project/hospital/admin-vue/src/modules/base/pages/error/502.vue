<script lang="ts" setup>
import ErrorPage from './components/error-page.vue'

defineOptions({
  name: '502',
})
</script>

<template>
  <error-page :code="502" :desc="$t('马上回来')" />
</template>
