<script setup lang="ts">
/**
 * @credits Nuxt SEO <https://nuxtseo.com/>
 */

const { title = 'title', isPro } = defineProps<{
  colorMode?: 'dark' | 'light'
  title?: string
  description?: string
  isPro?: boolean
  width?: number
  height?: number
}>()

const themeColor = computed(() => (isPro ? '124, 58, 237' : '34, 197, 94'))
</script>

<template>
  <div text="neutral-900 dark:neutral-50" p="10 lg:15" bg="white dark:neutral-900" flex="~ col" h-full w-full items-center justify-center relative>
    <!-- Gradient background -->
    <div
      bottom-0 left-0 right-0 top-0 absolute
      :style="{
        backgroundImage: `radial-gradient(at 100% 100%, rgba(${themeColor}, 0.15), transparent)`,
      }"
    />
    <div
      bottom-0 left-0 right-0 top-0 absolute
      :style="{
        backgroundImage: `radial-gradient(at 0% 0%, rgba(${themeColor}, 0.1), transparent)`,
      }"
    />

    <div text-center flex="~ col" gap="5 lg:8" items-center relative>
      <!-- Logo -->
      <div flex gap-3 items-center>
        <svg viewBox="0 0 64 64" h="10 lg:16" w="10 lg:16">
          <defs>
            <linearGradient :id="isPro ? 'nsLine2' : 'nsLine1'" x1="0%" y1="100%" x2="100%" y2="0%">
              <stop offset="0%" :stop-color="isPro ? '#7c3aed' : '#22c55e'" />
              <stop offset="100%" :stop-color="isPro ? '#c4b5fd' : '#86efac'" />
            </linearGradient>
            <linearGradient :id="isPro ? 'nsFill2' : 'nsFill1'" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" :stop-color="isPro ? '#7c3aed' : '#22c55e'" stop-opacity="0.6" />
              <stop offset="100%" :stop-color="isPro ? '#7c3aed' : '#22c55e'" stop-opacity="0" />
            </linearGradient>
          </defs>
          <path
            d="M8 52 Q20 48 24 36 T40 20 T56 12 L56 56 L8 56 Z"
            :fill="`url(#${isPro ? 'nsFill2' : 'nsFill1'})`"
          />
          <path
            d="M8 52 Q20 48 24 36 T40 20 T56 12"
            fill="none"
            :stroke="`url(#${isPro ? 'nsLine2' : 'nsLine1'})`"
            stroke-width="4"
            stroke-linecap="round"
          />
          <circle cx="56" cy="12" r="6" :fill="`url(#${isPro ? 'nsLine2' : 'nsLine1'})`" />
        </svg>
        <span text="4xl lg:[42px]" tracking-tight font-bold>
          Nuxt<span :class="isPro ? 'text-violet-500' : 'text-green-500'" ml-2>SEO{{ isPro ? ' Pro' : '' }}</span>
        </span>
      </div>

      <div w-full items-center justify-center>
        <!-- Title -->
        <h1 text="[48px] lg:[80px] balance ellipsis" max-w="[700px] lg:[1000px]" leading-tight font-bold m-0 block line-clamp-3>
          {{ title }}
        </h1>
      </div>

      <!-- Description -->
      <p
        v-if="description"
        text="[24px] lg:[32px]" leading-relaxed opacity-70 max-w="[650px] lg:max-w-[900px]"
      >
        {{ description }}
      </p>
    </div>
  </div>
</template>
