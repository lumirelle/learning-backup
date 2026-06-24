<!-- Application Footer -->
<!-- For clarity, we use filename catches its name, so we use <AppFooter> instead of <Footer> -->
<!-- As Nuxt will prefix component name with its folder name -->
<!-- See https://nuxt.com/docs/4.x/directory-structure/app/components#component-names -->
<!-- This helps: -->
<!-- 1. When we import the component manually, it will share the same name with auto-imported -->
<!-- 2. Since Vue generated component name from its filename, this helps keep the component name consistent -->

<template>
  <footer>
    <div text="xl gray-400" m-5 flex gap-3 justify-center>
      <NuxtLinkLocale i-carbon-campsite to="index" aria-label="Go Home" />
      <a i-carbon-logo-github href="https://github.com/lumirelle/starter-vitesse-nuxt" target="_blank" aria-label="View on GitHub" />
      <ToggleColorMode />
      <ToggleLanguage />
    </div>
    <slot />
  </footer>
</template>
