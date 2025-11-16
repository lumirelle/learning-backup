<script lang="ts" setup>
import type { PropType } from 'vue'
import { PictureFilled } from '@element-plus/icons-vue'
import { isArray, isNumber, isString } from 'lodash-es'
import { computed } from 'vue'
import { parsePx } from '/@/cool/utils'

defineOptions({
  name: 'cl-image',
})

const props = defineProps({
  modelValue: [String, Array],
  src: [String, Array],
  size: {
    type: [Number, Array],
    default: 100,
  },
  radius: {
    type: [Number, String],
    default: 0,
  },
  lazy: Boolean,
  fit: {
    type: String as PropType<'' | 'contain' | 'cover' | 'none' | 'fill' | 'scale-down'>,
    default: 'cover',
  },
  compress: String as PropType<'oss' | 'none'>,
  preview: {
    type: Boolean,
    default: true,
  },
})

const urls = computed(() => {
  const urls: any = props.modelValue || props.src

  if (isArray(urls)) {
    return urls
  }

  if (isString(urls)) {
    if (urls.startsWith('data:image')) {
      return [urls]
    }

    return (urls || '').split(',').filter(Boolean)
  }

  return []
})

const style = computed(() => {
  const [h, w]: any[] = isNumber(props.size) ? [props.size, props.size] : props.size
  return {
    h: parsePx(h),
    w: parsePx(w),
  }
})

const url = computed(() => {
  const v = urls.value[0]
  return props.compress === 'oss'
    ? `${v}?x-oss-process=image/resize,m_fill,h_${style.value.h},w_${style.value.w}`
    : v
})
</script>

<template>
  <div
    class="cl-image"
    :style="{
      height: style.h,
    }"
  >
    <el-image
      :src="url"
      :fit="fit"
      :lazy="lazy"
      :preview-src-list="preview ? urls : undefined"
      :style="{
        height: style.h,
        width: style.w,
      }"
      preview-teleported
    >
      <template #error>
        <div class="cl-image__slot">
          <el-icon :size="18">
            <picture-filled />
          </el-icon>
        </div>
      </template>
    </el-image>
  </div>
</template>

<style lang="scss" scoped>
.cl-image {
  &__slot {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    border-radius: 6px;
    background-color: var(--el-fill-color-lighter);
    color: var(--el-text-color-regular);
  }

  :deep(.el-image__inner) {
    border-radius: v-bind('parsePx(props.radius)');
  }
}
</style>
