<script setup lang="ts">
import { useForm } from '@cool-vue/crud'
import { CircleClose } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { cloneDeep, isArray, isEmpty } from 'lodash-es'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { CrudProps } from '/#/crud'
import { useCool } from '/@/cool'
import { deepTree } from '/@/cool/utils'

defineOptions({
  name: 'cl-user-select',
})

const props = defineProps({
  ...CrudProps,
  modelValue: null,
  multiple: Boolean,
  placeholder: String,
  title: String,
  immediate: Boolean,
  labelKey: {
    type: String,
    default: 'nickName',
  },
  maxCollapse: {
    type: Number,
    default: 10,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

interface Item {
  name: string
  isUser: boolean
  [key: string]: any
}

const { service, refs, setRefs } = useCool()
const { t } = useI18n()
const Form = useForm()

// 绑定值
const value = ref<any[]>([])

// 是否可见
const visible = ref(false)

// 搜索关键字
const keyWord = ref('')

// 选项
const tab = reactive({
  active: 'dept',

  list: [
    {
      label: t('按部门分组'),
      value: 'dept',
    },
    {
      label: t('按角色分组'),
      value: 'role',
    },
  ],
})

// 数据
const data = reactive<{ [key: string]: Item[] }>({})

// 用户
const users = ref<Eps.BaseSysUserEntity[]>([])

// 已选择
const selection = computed(() => {
  return users.value.filter(e => value.value.includes(e.id))
})

// 过滤节点
function filterNode(value: string, data: any) {
  if (!value)
    return true
  return data.name.includes(value)
}

// 关键字改变
function onKeyWordChange() {
  refs[`tree-${tab.active}`].filter(keyWord.value)
}

// 获取用户
async function getUser() {
  return service.base.sys.user.page({ size: 10000 }).then((res) => {
    users.value = res.list.map((e) => {
      e.isUser = true
      return e
    })
  })
}

// 获取部门
async function getDept() {
  return service.base.sys.department.list().then((res) => {
    res = deepTree(res)

    const deep = (arr: any[]) => {
      arr.forEach((e) => {
        const list = users.value.filter(u => u.departmentId === e.id && u.id != 1)

        if (!e.children) {
          e.children = []
        }

        e.children.push(...cloneDeep(list))

        if (e.children && !e.departmentId) {
          deep(e.children)
        }
      })
    }

    deep(res)

    data.dept = res as Item[]
  })
}

// 获取角色
async function getRole() {
  return service.base.sys.role.list().then((res) => {
    res.forEach((e) => {
      e.children = users.value.filter(u => u.roleIds.includes(u.id))
    })

    data.role = res as Item[]
  })
}

// 打开
async function open() {
  visible.value = true
  keyWord.value = ''
  await refresh()

  tab.list.forEach((e) => {
    refs[`tree-${e.value}`].setCheckedKeys(value.value)
  })
}

// 关闭
function close() {
  visible.value = false
}

// 移除
function remove() {
  value.value = []
  emit('update:modelValue', undefined)
  emit('change', undefined)
}

// 选择
function select(item: Item, node: any) {
  if (item.isUser) {
    if (props.multiple) {
      refs[`tree-${tab.active}`].setChecked(item, !node.checked, false)
    }
    else {
      value.value = [item.id]
      confirm()
    }
  }
}

// 确认
function confirm() {
  let v = value.value || []

  if (props.multiple) {
    const tree = refs[`tree-${tab.active}`]
    const list: Eps.BaseSysUserEntity[] = tree.getCheckedNodes()
    v = list.filter(e => e.isUser).map(e => e.id!)
  }

  if (isEmpty(v)) {
    return ElMessage.warning(t('请选择成员'))
  }

  value.value = v

  emit('update:modelValue', value.value)
  emit('change', selection.value)
  close()

  // 验证表单字段
  Form.value?.validateField(props.prop)
}

async function refresh() {
  await getUser()
  await getDept()
  await getRole()
}

onMounted(() => {
  if (props.immediate) {
    refresh()
  }

  watch(
    () => props.modelValue,
    (val) => {
      let v = val || []

      if (!isArray(v)) {
        v = [v]
      }

      value.value = v
    },
    {
      immediate: true,
    },
  )
})

defineExpose({
  open,
  close,
})
</script>

<template>
  <div class="cl-user-select" @click="open()">
    <template v-if="!isEmpty(selection)">
      <template v-if="multiple">
        <cl-avatar
          v-for="(item, index) in selection"
          :key="index"
          :src="item.headImg"
          :size="24"
          class="mr-[5px]"
        />
      </template>

      <template v-else>
        <cl-avatar :size="24" :src="selection[0]?.headImg" />
        <span class="mr-auto ml-[6px]">{{ selection[0]?.[labelKey] }}</span>
      </template>

      <el-icon class="ml-auto" @click.stop="remove">
        <circle-close />
      </el-icon>
    </template>

    <span v-else class="placeholder">{{ placeholder || $t('请选择') }}</span>
  </div>

  <cl-dialog v-model="visible" :title="title || $t('选择成员')" :controls="['close']" width="600px">
    <el-tabs v-model="tab.active" type="card">
      <el-tab-pane
        v-for="(item, index) in tab.list"
        :key="index"
        :label="item.label"
        :name="item.value"
      >
        <el-input v-model="keyWord" :placeholder="$t('搜索名称')" @input="onKeyWordChange" />

        <el-scrollbar max-height="600px" class="scrollbar">
          <el-tree
            :ref="setRefs(`tree-${item.value}`)"
            node-key="id"
            :show-checkbox="multiple"
            :data="data[item.value]"
            :props="{
              label: 'name',
              class: data => {
                if (multiple) {
                  return '';
                }

                if (!data.isUser) {
                  return '';
                }

                return value.includes(data.id) ? 'is-selected' : '';
              },
            }"
            default-expand-all
            :filter-node-method="filterNode"
            @node-click="select"
          >
            <template #default="{ data }">
              <div class="cl-user-select__item">
                <cl-avatar v-if="data.isUser" class="mr-[6px]" :src="data.headImg" :size="24" />

                <span class="select-none">{{ data.name }}</span>
              </div>
            </template>
          </el-tree>
        </el-scrollbar>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="close">
        {{ $t('取消') }}
      </el-button>
      <el-button type="success" @click="confirm()">
        {{ $t('确定') }}
      </el-button>
    </template>
  </cl-dialog>
</template>

<style lang="scss" scoped>
.scrollbar {
  border: 1px solid var(--el-border-color);
  margin-top: 10px;
  border-radius: var(--el-border-radius-base);
  padding: 5px;

  :deep(.el-tree) {
    .el-tree-node {
      &.is-selected {
        .el-tree-node__content {
          background-color: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
        }
      }
    }

    .el-tree-node__content {
      line-height: normal;
      height: 35px;
      border-radius: 6px;
    }
  }
}

.cl-user-select {
  display: flex;
  align-items: center;
  box-shadow: 0 0 0 1px var(--el-border-color) inset;
  border-radius: var(--el-border-radius-base);
  padding: 0 11px;
  min-height: 32px;
  cursor: pointer;
  position: relative;
  box-sizing: border-box;
  background-color: var(--el-bg-color);
  transition: var(--el-transition-duration);

  .placeholder {
    color: var(--el-text-color-placeholder);
  }

  &:hover {
    box-shadow: 0 0 0 1px var(--el-text-color-disabled) inset;
  }

  &__item {
    display: flex;
    align-items: center;
    width: 100%;

    .checked {
      color: var(--el-color-success);
      margin-left: auto;
      margin-right: 10px;
      font-size: 16px;
    }
  }
}
</style>
