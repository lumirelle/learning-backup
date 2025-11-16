<script lang="ts" setup>
import ErrorPage from './components/error-page.vue'

defineOptions({
  name: '404',
})
</script>

<template>
  <error-page :code="404" :desc="$t('找不到您要查找的页面')" />
</template>
