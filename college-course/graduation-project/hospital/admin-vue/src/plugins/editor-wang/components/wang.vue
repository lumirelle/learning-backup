<script lang="ts" setup>
import type { IEditorConfig } from '@wangeditor/editor'
import type { PropType } from 'vue'
import { Editor as EditorInput, Toolbar } from '@wangeditor/editor-for-vue'
import { isArray } from 'lodash-es'
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import '@wangeditor/editor/dist/css/style.css'
import { useCool } from '/@/cool'
import { parsePx } from '/@/cool/utils'

defineOptions({
  name: 'cl-editor-wang',
})

const props = defineProps({
  modelValue: String,
  // 模式
  mode: {
    type: String as PropType<'default' | 'simple'>,
    default: 'default',
  },
  // 高度
  height: {
    type: [String, Number],
    default: 500,
  },
  // 禁用
  disabled: Boolean,
  // 是否预览模式
  preview: Boolean,
  // 是否使用文件空间
  isSpace: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'change', 'focus', 'blur'])

const { refs, setRefs } = useCool()
const { t } = useI18n()

// 编辑器
const Editor = shallowRef()

// 内容
const value = ref()

watch(
  () => props.modelValue,
  (val) => {
    value.value = val || ''
  },
  {
    immediate: true,
  },
)

// 临时
const temp: { insertFn: ((url: string) => void) | null } = {
  insertFn: null,
}

// 配置
const editorConfig: Partial<IEditorConfig> = {
  placeholder: t('请输入'),
  MENU_CONF: {
    uploadImage: {},
    uploadVideo: {
      customBrowseAndUpload(fn: any) {
        temp.insertFn = fn
        refs.video.open()
      },
    },
  },
}

// 图片上传，两种模式
if (props.isSpace) {
  // 文件空间上传
  editorConfig.MENU_CONF!.uploadImage.customBrowseAndUpload = (fn: any) => {
    temp.insertFn = fn
    refs.image.open()
  }
}
else {
  // 直接上传
  editorConfig.MENU_CONF!.uploadImage.customUpload = (file: File, fn: any) => {
    temp.insertFn = fn
    refs.image.upload(file)
  }
}

// 创建后
function onCreated(editor: any) {
  Editor.value = editor
  onDisabled()
}

// 聚焦
function onFocus(editor: any) {
  emit('focus', editor)
}

// 失焦
function onBlur(editor: any) {
  emit('blur', editor)
}

// 值改变
function onChange() {
  if (value.value == '<p><br></p>') {
    value.value = ''
  }

  emit('update:modelValue', value.value)
  emit('change', value.value)
}

// 文件选择
function onFileConfirm(files: any[]) {
  if (!isArray(files)) {
    files = [files]
  }

  if (files.length > 0) {
    files.forEach((file) => {
      if (temp.insertFn) {
        temp.insertFn(file.url)
      }
    })
  }
}

// 禁用
function onDisabled() {
  if (props.disabled || props.preview) {
    Editor.value?.disable()
  }
  else {
    Editor.value?.enable()
  }
}

// 监听
watch(() => [props.disabled, props.preview], onDisabled)

// 销毁
onBeforeUnmount(() => {
  const editor = Editor.value
  if (editor == null)
    return
  editor.destroy()
})
</script>

<template>
  <div :ref="setRefs('editor')" class="cl-editor-wang" :class="{ disabled }">
    <!-- 工具栏 -->
    <toolbar v-if="!preview" :editor="Editor" :mode="mode" />

    <!-- 编辑框 -->
    <editor-input
      v-model="value"
      :default-config="editorConfig"
      :mode="mode"
      :style="{
        height: parsePx(height),
      }"
      @on-created="onCreated"
      @on-focus="onFocus"
      @on-blur="onBlur"
      @on-change="onChange"
    />

    <!-- 文件空间 - 视频 -->
    <cl-upload-space
      :ref="setRefs('video')"
      accept="video/*"
      :show-btn="false"
      @confirm="onFileConfirm"
    />

    <!-- 文件空间 - 图片 -->
    <cl-upload-space
      v-if="isSpace"
      :ref="setRefs('image')"
      accept="image/*"
      :show-btn="false"
      @confirm="onFileConfirm"
    />

    <!-- 直接上传 - 图片 -->
    <div v-else class="upload-inner">
      <cl-upload :ref="setRefs('image')" accept="image/*" @success="onFileConfirm" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.cl-editor-wang {
  border: 1px solid var(--el-border-color);
  box-sizing: border-box;
  line-height: normal;

  :deep(.w-e-toolbar) {
    border-bottom: 1px solid var(--el-border-color);
  }

  :deep(input) {
    font-size: 12px;
  }

  :deep(button) {
    font-size: 12px;

    &::before {
      font-size: 12px;
    }
  }

  .upload-inner {
    visibility: hidden;
    position: absolute;
    left: 0;
    top: 0;
  }

  &.disabled {
    :deep(.w-e-text-container) {
      background-color: var(--el-disabled-bg-color);
    }
  }

  &.w-e-full-screen-container {
    z-index: 999;
  }
}
</style>
