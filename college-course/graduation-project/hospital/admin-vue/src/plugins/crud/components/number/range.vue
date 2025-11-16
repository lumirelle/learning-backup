<script setup lang="ts">
import type { PropType } from 'vue'
import { useModel } from 'vue'

defineOptions({
  name: 'cl-number-range',
})

const props = defineProps({
  modelValue: {
    type: Array as PropType<any[]>,
  },
  startPlaceholder: String,
  endPlaceholder: String,
  min: {
    type: Number,
    default: 0,
  },
  max: {
    type: Number,
    default: 100000,
  },
})

const emit = defineEmits(['update:modelValue'])

const value = useModel(props, 'modelValue', {
  get() {
    return props.modelValue ?? []
  },
})

function onFocus() {
  emit('update:modelValue', value.value || [])
}

function onChange() {
  emit('update:modelValue', value.value)
}
</script>

<template>
  <div class="cl-number-range">
    <el-input-number
      v-model="value[0]"
      :controls="false"
      :placeholder="startPlaceholder || $t('起')"
      :min="min"
      :max="value[1] ?? max"
      @focus="onFocus"
      @change="onChange"
    />
    <span class="cl-number-range__separator">~</span>
    <el-input-number
      v-model="value[1]"
      :controls="false"
      :placeholder="endPlaceholder || $t('止')"
      :min="value[0] ?? min"
      :max="max"
      @focus="onFocus"
      @change="onChange"
    />
  </div>
</template>

<style lang="scss" scoped>
.cl-number-range {
  display: flex;
  align-items: center;

  &__separator {
    margin: 0 10px;
  }

  .el-input-number {
    width: 100px;
  }
}
</style>
