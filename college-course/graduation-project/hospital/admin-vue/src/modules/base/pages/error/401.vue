<script lang="ts" setup>
import ErrorPage from './components/error-page.vue'

defineOptions({
  name: '401',
})
</script>

<template>
  <error-page :code="401" :desc="$t('认证失败，请重新登录！')" />
</template>
