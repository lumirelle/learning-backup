<script lang="ts" setup>
import ErrorPage from './components/error-page.vue'

defineOptions({
  name: '500',
})
</script>

<template>
  <error-page :code="500" :desc="$t('糟糕，出了点问题')" />
</template>
