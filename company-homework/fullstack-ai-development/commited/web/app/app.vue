<script lang="ts" setup>
useHead({
  titleTemplate: title => (title ? `${title} · 组织人事` : '组织人事管理系统'),
})
</script>

<template>
  <div class="text-truegray-800 dark:text-truegray-200 font-sans min-h-screen">
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </div>
</template>
