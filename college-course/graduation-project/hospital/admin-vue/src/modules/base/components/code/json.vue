<script lang="tsx" setup>
import { useClipboard } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import { isObject, isString } from 'lodash-es'
import { computed, defineComponent } from 'vue'
import { useI18n } from 'vue-i18n'

defineOptions({
  name: 'cl-code-json',
})

const props = defineProps({
  content: null,
  modelValue: null,
  popover: Boolean,
  height: {
    type: [Number, String],
    default: '100%',
  },
  maxHeight: {
    type: [Number, String],
    default: 300,
  },
  title: String,
})

const { copy } = useClipboard()
const { t } = useI18n()

// 文本
const text = computed(() => {
  const v = props.modelValue || props.content

  if (isString(v)) {
    return v
  }
  else if (isObject(v)) {
    return JSON.stringify(v, null, 4)
  }
  else {
    return String(v)
  }
})

// 视图
const viewer = defineComponent({
  setup(_, { slots }) {
    function toCopy() {
      copy(text.value)
      ElMessage.success('复制成功')
    }

    return () => {
      return (
        <div class="cl-code-json">
          <div class="cl-code-json__op">
            {text.value != '{}' && (
              <el-button type="success" size="small" onClick={toCopy}>
                {t('复制')}
              </el-button>
            )}

            {slots.op && slots.op()}
          </div>

          {props.title && <div class="cl-code-json__title">{props.title}</div>}

          <el-scrollbar
            class="cl-code-json__content"
            max-height={props.maxHeight}
            height={props.height}
          >
            <pre>
              <code>{text.value}</code>
            </pre>
          </el-scrollbar>
        </div>
      )
    }
  },
})
</script>

<template v-if="text">
  <div v-if="popover" class="cl-code-json__wrap">
    <el-popover width="auto" placement="right" popper-class="cl-code-json__popper" effect="dark">
      <template #reference>
        <span class="cl-code-json__text">{{ text }}</span>
      </template>

      <viewer />
    </el-popover>
  </div>

  <viewer v-else>
    <template #op>
      <slot name="op" />
    </template>
  </viewer>
</template>

<style lang="scss">
.cl-code-json {
  position: relative;
  min-width: 200px;
  max-width: 500px;
  font-size: 14px;

  &__op {
    position: absolute;
    right: 8px;
    top: 8px;
    z-index: 9;
  }

  &__title {
    padding: 10px;
    font-size: 12px;
    color: var(--el-text-color-regular);

    & + .cl-code-json__content {
      padding-top: 0;
    }
  }

  &__content {
    padding: 10px;

    code {
      white-space: pre-wrap;
    }
  }

  &__wrap {
    .cl-code-json__text {
      display: block;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      cursor: pointer;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  &__popper {
    padding: 0 !important;
    border-radius: 8px !important;
  }
}
</style>
