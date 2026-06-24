<!-- Application Navigation -->
<!-- For clarity, we use filename catches its name, so we use <AppNav> instead of <Nav> -->
<!-- As Nuxt will prefix component name with its folder name -->
<!-- See https://nuxt.com/docs/4.x/directory-structure/app/components#component-names -->
<!-- This helps: -->
<!-- 1. When we import the component manually, it will share the same name with auto-imported -->
<!-- 2. Since Vue generated component name from its filename, this helps keep the component name consistent -->

<script lang="ts" setup>
defineProps<{
  items: ApiNav.NavItem[]
}>()
</script>

<template>
  <nav p-4>
    <ul flex="~ col" items-center>
      <li v-for="item in items" :key="item.link" text="vitesse-nuxt even:vitesse-vite" w-fit>
        <NuxtLink :to="item.link">
          <span>-> {{ item.title }}</span>
        </NuxtLink>
      </li>
    </ul>
  </nav>
</template>
