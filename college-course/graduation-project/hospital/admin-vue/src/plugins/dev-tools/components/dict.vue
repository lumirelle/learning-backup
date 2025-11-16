<script setup lang="ts">
import { isEmpty } from 'lodash-es'
import { onMounted, ref } from 'vue'
import { useCool } from '/@/cool'
import { deepTree } from '/@/cool/utils'

interface Item {
  label: string
  children: Item[]
}

const { service, refs, setRefs, router } = useCool()

const list = ref<Item[]>([{ label: '', children: [] }])
const keyWord = ref('')

function refresh() {
  service.dict.type.list().then((types) => {
    service.dict.info.list().then((res) => {
      list.value = types.map((e) => {
        return {
          label: `${e.name!} - ${e.key}`,
          children: deepTree(
            res.map((a) => {
              return {
                ...a,
                label: `${a.name} = ${a.value}`,
              }
            }),
          ).filter(a => a.typeId == e.id),
        }
      })
    })
  })
}

function reload() {
  refresh()
}

function add() {
  router.push('/dict/list')
}

function filterNode(value: string, data: any) {
  if (!value)
    return true
  return data.label.includes(value)
}

function onKeyWordChange(val: string) {
  refs.tree?.filter(val)
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="dt-dict">
    <div class="top">
      <el-tooltip :content="$t('刷新')">
        <div class="cl-comm__icon" @click="reload">
          <cl-svg name="refresh" />
        </div>
      </el-tooltip>

      <el-input
        v-model="keyWord"
        class="search"
        :placeholder="$t('搜索关键字')"
        clearable
        @input="onKeyWordChange"
      >
        <template #prefix>
          <cl-svg name="search" />
        </template>
      </el-input>
    </div>

    <el-result
      v-if="isEmpty(list)"
      icon="warning"
      :title="$t('没有可用的字典')"
      :sub-title="$t('请添加新的字典条目')"
    >
      <template #extra>
        <el-button type="success" @click="add">
          {{ $t('去添加') }}
        </el-button>
        <el-button type="primary" @click="reload">
          {{ $t('刷新') }}
        </el-button>
      </template>
    </el-result>

    <el-tree v-else :ref="setRefs('tree')" :data="list" :filter-node-method="filterNode" />
  </div>
</template>

<style lang="scss" scoped>
.dt-dict {
  .top {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
    position: sticky;
    top: 10px;
    z-index: 2;
    background-color: var(--el-bg-color);

    .search {
      margin-left: 5px;

      :deep(.el-input__wrapper) {
        height: 32px;
        border-radius: 6px;
      }
    }

    .cl-comm__icon {
      height: 32px;
      width: 32px;
    }
  }

  :deep(.el-tree-node__content) {
    border-radius: 4px;
    height: 32px;
  }

  :deep(.el-tree-node__label) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 450px;
  }
}
</style>
