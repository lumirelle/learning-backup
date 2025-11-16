<script lang="ts" setup>
import { nextTick, ref, watch } from 'vue'
import { useCool } from '/@/cool'
import { deepPaths } from '/@/cool/utils'

defineOptions({
  name: 'cl-menu-file',
})

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const { refs, setRefs } = useCool()

// 扫描文件
function findFiles() {
  const files = import.meta.glob(['/src/modules/*/{views,pages}/**/*', '!**/components'])
  const list: string[] = []

  for (const i in files) {
    if (!i.includes('base/pages')) {
      list.push(i.substring(13))
    }
  }

  return deepPaths(list)
}

// 路径
const path = ref()

// 文本
const text = ref()

// 是否编辑
const isEdit = ref(false)

// 数据列表
const data = ref(findFiles())

// 路径值改变
function onPathChange(arr: any) {
  const v = `modules/${(arr || []).join('/')}`
  emit('update:modelValue', v)
  emit('change', v)
}

// 文本值改变
function onTextChange(v: string) {
  emit('update:modelValue', v)
  emit('change', v)
}

// 切换
function toggle() {
  isEdit.value = !isEdit.value

  if (isEdit.value) {
    nextTick(() => {
      refs.input.focus()
    })
  }
}

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      if (val.includes('http')) {
        text.value = val
        isEdit.value = true
      }
      else {
        path.value = val.replace(/(modules\/|cool\/)/g, '').split('/')
      }
    }
  },
  {
    immediate: true,
  },
)
</script>

<template>
  <div class="cl-menu-file">
    <el-tooltip :content="$t('自定义输入')">
      <div
        class="cl-menu-file__icon"
        :class="{
          'is-edit': isEdit,
        }"
        @click="toggle()"
      >
        <cl-svg name="edit" />
      </div>
    </el-tooltip>

    <template v-if="isEdit">
      <el-input
        :ref="setRefs('input')"
        v-model="text"
        :placeholder="$t('请输入')"
        @change="onTextChange"
      />
    </template>

    <template v-else>
      <el-cascader v-model="path" :options="data" clearable filterable @change="onPathChange" />
    </template>
  </div>
</template>

<style lang="scss" scoped>
.cl-menu-file {
  display: flex;
  align-items: center;
  width: 100%;

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 5px;
    border: 1px solid var(--el-border-color);
    height: 32px;
    width: 32px;
    border-radius: var(--el-border-radius-base);
    box-sizing: border-box;
    flex-shrink: 0;
    cursor: pointer;

    .cl-svg {
      font-size: 16px;
    }

    &.is-edit {
      background-color: var(--el-color-primary);
      color: var(--el-color-white);
      border: 0;
    }

    &:hover:not(.is-edit) {
      .cl-svg {
        color: var(--el-color-primary);
      }
    }
  }

  :deep(.el-cascader) {
    width: 100%;
  }

  .el-icon {
    margin: 0 10px 0 0;
    font-size: 18px;
    cursor: pointer;

    &:hover {
      color: var(--el-color-primary);
    }
  }
}
</style>
