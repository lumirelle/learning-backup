<script lang="ts" setup>
import { ArrowRightBold } from '@element-plus/icons-vue'
import { flattenDeep, last } from 'lodash-es'
import { computed } from 'vue'
import { useBase } from '/$/base'
import { useCool } from '/@/cool'

defineOptions({
  name: 'route-nav',
})

const { route, browser } = useCool()
const { menu } = useBase()

// 数据列表
const list = computed(() => {
  function deep(item: any) {
    if (route.path === '/') {
      return false
    }

    if (item.path == route.path) {
      return item
    }
    else {
      if (item.children) {
        const ret = item.children.map(deep).find(Boolean)

        if (ret) {
          return [item, ret]
        }
        else {
          return false
        }
      }
      else {
        return false
      }
    }
  }

  return flattenDeep(menu.group.map(deep).filter(Boolean))
})

// 最后一个节点名称
const lastName = computed(() => last(list.value)?.meta?.label)
</script>

<template>
  <div class="route-nav">
    <el-text v-if="browser.isMini" class="font-bold">
      {{ lastName }}
    </el-text>

    <template v-else>
      <el-breadcrumb :separator-icon="ArrowRightBold">
        <el-breadcrumb-item v-for="(item, index) in list" :key="index">
          <span class="text-[14px]">{{ item.meta?.label || item.name }}</span>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.route-nav {
  white-space: nowrap;
  user-select: none;
  margin-right: 10px;

  :deep(.el-breadcrumb) {
    .el-breadcrumb__separator {
      font-size: 10px;
      margin: 0 10px;
    }

    .el-breadcrumb__inner {
      color: var(--el-text-color-regular);
    }

    .el-breadcrumb__item {
      &:last-child {
        .el-breadcrumb__inner {
          color: var(--el-text-color-primary);
        }
      }
    }
  }
}
</style>
