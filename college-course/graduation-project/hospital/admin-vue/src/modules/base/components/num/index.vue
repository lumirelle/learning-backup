<script setup lang="ts">
import type { PropType } from 'vue'
import { useTransition } from '@vueuse/core'
import { computed } from 'vue'

defineOptions({
  name: 'cl-number',
})

const props = defineProps({
  modelValue: Number,
  value: Number,
  fixed: Number,
  duration: {
    type: Number,
    default: 1000,
  },
  type: {
    type: String as PropType<'amount' | 'number'>,
    default: 'number',
  },
  suffix: String,
})

const transitionedValue = useTransition(
  computed(() => props.modelValue || props.value! || 0),
  {
    duration: props.duration,
  },
)

const text = computed(() => {
  const val = transitionedValue.value

  if (props.type === 'amount') {
    // 若需要小数位数控制，可以使用 toFixed 再转数值：
    const fixedVal = props.fixed !== undefined ? Number(val.toFixed(props.fixed)) : val

    // 利用 toLocaleString 增加千分位分隔符
    return fixedVal.toLocaleString(undefined, {
      minimumFractionDigits: props.fixed || 0,
      maximumFractionDigits: props.fixed || 0,
    })
  }
  else {
    return val.toFixed(0)
  }
})
</script>

<template>
  <span class="cl-number">
    {{ text }} <span v-if="suffix" class="cl-number__suffix">{{ suffix }}</span>
  </span>
</template>

<style lang="scss" scoped>
.cl-number {
  &__suffix {
    font-size: 14px;
  }
}
</style>
