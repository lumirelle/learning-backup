<script lang="ts" setup>
import ErrorPage from './components/error-page.vue'

defineOptions({
  name: '403',
})
</script>

<template>
  <error-page :code="403" :desc="$t('您无权访问此页面')" />
</template>
