<script lang="ts" setup>
import { computed, reactive } from 'vue'
import { parsePx } from '/@/cool/utils'

defineOptions({
  name: 'cl-svg',
})

const props = defineProps({
  name: String,
  className: String,
  color: String,
  size: [String, Number],
})

const style = reactive({
  fontSize: parsePx(props.size!),
  fill: props.color,
})

const iconName = computed(() => `#icon-${props.name}`)
const svgClass = computed(() => {
  return ['cl-svg', `cl-svg__${props.name}`, String(props.className || '')]
})
</script>

<template>
  <svg :class="svgClass" :style="style" aria-hidden="true">
    <use :xlink:href="iconName" />
  </svg>
</template>

<style lang="scss" scoped>
.cl-svg {
  display: inline-block;
  width: 1em;
  height: 1em;
  fill: currentColor;
  overflow: hidden;
}
</style>
