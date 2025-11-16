<script lang="ts" setup>
import type { PropType } from 'vue'
import { useClipboard } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import { isObject, isString } from 'lodash-es'
import { computed, nextTick, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { CrudProps } from '/#/crud'
import { useCool } from '/@/cool'

defineOptions({
  name: 'cl-editor-preview',
})

const props = defineProps({
  ...CrudProps,
  modelValue: String,
  title: String,
  name: String,
  text: String,
  type: {
    type: String as PropType<'code' | 'text'>,
    default: 'text',
  },
  showBtn: {
    type: Boolean,
    default: true,
  },
  height: {
    type: String,
    default: '60vh',
  },
  width: {
    type: String,
    default: '60%',
  },
  formatter: Function,
  // 多个内容展示
  tabs: Array as PropType<TabItem[]>,
  // 组件参数
  props: Object,
})

interface TabItem {
  name: string
  data: string
  language: string
}

const { refs, setRefs } = useCool()
const { copy } = useClipboard()
const { t } = useI18n()

// 是否可见
const visible = ref(false)

// 内容
const content = ref('')

// 语言
const language = ref()

// 激活的标签
const active = ref(0)

// 列表
const list = ref<TabItem[]>([])

// 是否代码预览
const isCode = computed(() => {
  return props.type == 'code'
})

// 是否可以复制
const isCopy = computed(() => isCode)

// 编辑器配置
const editConfig = computed(() => {
  return {
    language: language.value,
    ...props.props,
  }
})

// 标题
const title = computed(() => {
  return props.title || (isCode.value ? t('代码预览') : t('文本预览'))
})

// 按钮
const btnText = computed(() => {
  return props.text || t('点击查看')
})

// 打开
async function open(data?: string | TabItem[]) {
  if (!data) {
    data = props.modelValue
  }

  if (props.formatter) {
    data = props.formatter(data)
  }

  if (props.tabs) {
    list.value = props.tabs
    onTabChange(0)
  }
  else {
    setContent(data)
  }

  visible.value = true
}

// 设置内容
function setContent(val: any) {
  if (isString(val)) {
    content.value = val
  }
  else if (isObject(val)) {
    content.value = JSON.stringify(val, null, 4)
  }
}

// 切换
async function onTabChange(index: any) {
  const item = list.value[index]

  // 设置语言
  language.value = item.language

  // 设置
  setContent(item.data)

  await nextTick()

  // 格式化代码
  if (isCode.value) {
    refs.editor?.formatCode?.()
  }
}

// 关闭
function close() {
  visible.value = false
}

// 复制
function toCopy() {
  copy(content.value)
  ElMessage.success(t('复制成功'))
}

defineExpose({
  open,
  close,
})
</script>

<template>
  <slot>
    <el-button v-if="showBtn" @click="open()">
      {{ btnText }}
    </el-button>
  </slot>

  <cl-dialog
    v-model="visible"
    :height="height"
    :width="width"
    :title="title"
    scrollbar
    append-to-body
  >
    <div class="cl-editor-preview">
      <el-tabs v-if="list.length > 1" v-model="active" type="card" @tab-change="onTabChange">
        <el-tab-pane
          v-for="(item, index) in list"
          :key="index"
          :label="item.name"
          :name="index"
          :lazy="index != 0"
        />
      </el-tabs>

      <div class="cl-editor-preview__container">
        <slot name="prepend" />

        <cl-editor
          v-if="name"
          :ref="setRefs('editor')"
          :key="active"
          v-bind="editConfig"
          v-model="content"
          :name="`cl-editor-${name}`"
          height="100%"
          preview
        />

        <div v-else class="content">
          {{ content }}
        </div>

        <slot name="append" />
      </div>
    </div>

    <template #footer>
      <el-button @click="close">
        {{ $t('关闭') }}
      </el-button>
      <el-button v-if="isCopy" type="success" @click="toCopy">
        {{ $t('复制') }}
      </el-button>
    </template>
  </cl-dialog>
</template>

<style lang="scss" scoped>
.cl-editor-preview {
  display: flex;
  flex-direction: column;
  height: 100%;

  :deep(img) {
    max-width: 100%;
  }

  &__container {
    flex: 1;

    .content {
      white-space: pre-wrap;
      background-color: var(--el-fill-color-light);
      border-radius: 8px;
      padding: 10px;
    }
  }
}
</style>
