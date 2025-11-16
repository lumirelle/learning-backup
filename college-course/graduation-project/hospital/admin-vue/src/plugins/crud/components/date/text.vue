<script lang="ts" setup>
import dayjs from 'dayjs'
import { computed } from 'vue'

defineOptions({
  name: 'cl-date-text',
})

const props = defineProps({
  modelValue: [String, Number],
  format: {
    type: String,
    default: 'YYYY-MM-DD HH:mm:ss',
  },
})

const value = computed(() => {
  return props.modelValue ? dayjs(props.modelValue).format(props.format) : ''
})
</script>

<template>
  <span class="cl-date-text">{{ value }}</span>
</template>
