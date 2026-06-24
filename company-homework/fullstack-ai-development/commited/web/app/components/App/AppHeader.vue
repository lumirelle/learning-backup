<!-- Application Header -->
<!-- For clarity, we use filename catches its name, so we use <AppHeader> instead of <Header> -->
<!-- As Nuxt will prefix component name with its folder name -->
<!-- See https://nuxt.com/docs/4.x/directory-structure/app/components#component-names -->
<!-- This helps: -->
<!-- 1. When we import the component manually, it will share the same name with auto-imported -->
<!-- 2. Since Vue generated component name from its filename, this helps keep the component name consistent -->

<template>
  <header text-2xl font-300 flex cursor-default justify-center>
    <h1 opacity0 absolute>
      Nuxt 4 and Vite 8
    </h1>
    <div flex="~ col" children:mx-auto>
      <img i-public-nuxt h-18 w-18 inline-block alt="Nuxt Logo">
      <span text-green-400 mt--2>Nuxt 4</span>
    </div>
    <div
      text="3xl gray-400"
      m="x-4 y-auto"
      i-carbon-add
      transform
      transition-all-500
      hover:rotate-135
    />
    <div flex="~ col" children:mx-auto>
      <img i-public-vite p-2 h-18 w-18 inline-block alt="Vite Logo">
      <span text-purple-500 mt--2>Vite 8</span>
    </div>
  </header>
</template>
