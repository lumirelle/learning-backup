<script setup lang="ts">
import { isEmpty, isObject, isString } from 'lodash-es'
import { eps } from 'virtual:eps'
import { onMounted, ref } from 'vue'
import { useCool } from '/@/cool'

interface Item {
  label: string
  children: Item[]
}

const { service, refs, setRefs } = useCool()

const list = ref<Item[]>([])
const keyWord = ref('')

function refresh() {
  const deep = (data: any, parent: Item | null = null) => {
    const item = eps.list.find(e => e.prefix == `/${data.namespace}`)

    const text = {}

    if (item) {
      item.api.forEach((e) => {
        text[e.path.replace('/', '')] = e.summary
      })
    }

    for (const key in data) {
      if (['namespace', 'permission', '_permission', 'request'].includes(key)) {
        continue
      }

      const item: Item = {
        label: key,
        children: [],
      }

      if (key == 'search') {
        item.label += '（搜索配置）'
      }

      if (isString(data[key])) {
        item.label += `（${data[key]}）`
      }

      if (text[key]) {
        item.label += `（${text[key]}）`
      }

      if (parent) {
        parent.children.push(item)
      }
      else {
        list.value.push(item)
      }

      if (isObject(data[key])) {
        deep(data[key], item)
      }
    }
  }

  deep(service)
}

function reload() {
  service.request({
    url: '/__cool_eps',
    method: 'POST',
    proxy: false,
  })
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
  <div class="dt-eps">
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
      icon="error"
      :title="$t('Eps 错误，请在后端代码中设置：')"
      sub-title="/src/config/config.local.ts → { eps: true }"
    >
      <template #extra>
        <el-button type="primary" @click="reload">
          {{ $t('刷新') }}
        </el-button>
      </template>
    </el-result>

    <el-tree v-else :ref="setRefs('tree')" :data="list" :filter-node-method="filterNode" />
  </div>
</template>

<style lang="scss" scoped>
.dt-eps {
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
}
</style>
