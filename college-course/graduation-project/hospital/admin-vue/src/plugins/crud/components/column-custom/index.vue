<script lang="ts" setup>
import type { PropType } from 'vue'
import { has, isBoolean, orderBy } from 'lodash-es'
import { computed, nextTick, ref, watch } from 'vue'
import Draggable from 'vuedraggable/src/vuedraggable'
import { storage } from '/@/cool'

defineOptions({
  name: 'cl-column-custom',
})

const props = defineProps({
  name: String,
  columns: {
    type: Array as PropType<any[]>,
    required: true,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

// 是否可见
const visible = ref(false)

// 名称
const name = `column-custom__${props.name || location.pathname}`

// 列数据
const list = ref<{ label: string, prop: any, checked?: boolean, orderNum?: number }[]>([])

// 列配置
const columns = computed(() => {
  return props.columns.filter(e => !e.type && e.prop)
})

// 改变列
function change() {
  nextTick(() => {
    columns.value.forEach((e) => {
      e.hidden = !list.value.find(a => a.prop == e.prop)?.checked
      e.orderNum = list.value.findIndex(a => a.prop == e.prop)
    })
  })
}

// 保存
function save() {
  storage.set(name, list.value)
  change()
  close()
}

// 重置
function reset() {
  columns.value.forEach((e) => {
    if (e.orderNum !== undefined) {
      e.hidden = false
      e.orderNum = 0
    }
  })

  storage.remove(name)
  refresh()
  close()
}

// 打开
function open() {
  visible.value = true
  refresh()
}

// 关闭
function close() {
  visible.value = false
}

// 排序
function sort(list: any[]) {
  return orderBy(list, 'orderNum', 'asc')
}

// 刷新
function refresh() {
  if (!props.columns) {
    return false
  }

  const selection: any[] = storage.get(name)

  list.value = sort(
    columns.value.map((e) => {
      let checked = true
      let orderNum = e.orderNum || 0

      if (isBoolean(e.hidden)) {
        checked = !e.hidden
      }

      if (selection) {
        checked = selection.find(a => a.prop == e.prop)?.checked
        orderNum = selection.findIndex(a => a.prop == e.prop)
      }

      return {
        label: e.label,
        prop: e.prop,
        checked,
        orderNum,
      }
    }),
  )

  change()
}

// 合计
function summary(subData: { [key: string]: any }) {
  return sort(columns.value.filter(e => !e.hidden)).map((e) => {
    if (has(subData, e.prop)) {
      return subData[e.prop]
    }
    else {
      return ''
    }
  })
}

watch(
  () => props.columns,
  () => {
    refresh()
  },
)

defineExpose({
  summary,
})
</script>

<template>
  <div class="cl-column-custom__wrap">
    <el-button @click="open">
      {{ $t('自定义列') }}
    </el-button>

    <cl-dialog v-model="visible" :title="$t('自定义列')">
      <div class="cl-column-custom__dialog">
        <div class="left">
          <draggable v-model="list" item-key="prop">
            <template #item="{ element: item }">
              <el-checkbox v-model="item.checked" border>
                {{ item.label }}
              </el-checkbox>
            </template>
          </draggable>
        </div>

        <div class="right" />
      </div>

      <template #footer>
        <el-button @click="close">
          {{ $t('取消') }}
        </el-button>
        <el-button type="danger" @click="reset">
          {{ $t('重置') }}
        </el-button>
        <el-button type="success" @click="save">
          {{ $t('保存') }}
        </el-button>
      </template>
    </cl-dialog>
  </div>
</template>

<style lang="scss" scoped>
.cl-column-custom {
  &__wrap {
    margin-left: 10px;
  }

  &__dialog {
    .left {
      .el-checkbox {
        margin: 5px 10px 5px 0;
      }
    }

    .right {
      border-left: 1px solid #eee;
    }
  }
}
</style>
