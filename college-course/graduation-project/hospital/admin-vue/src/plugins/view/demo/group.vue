<script lang="ts" setup>
import DictList from '/$/dict/views/list.vue'
import { useCool } from '/@/cool'

const { service } = useCool()
</script>

<template>
  <div class="wrap">
    <dict-list />
  </div>
</template>

<style lang="scss" scoped>
.wrap {
  height: 600px;
  border: 1px solid var(--el-border-color);
}
</style>
