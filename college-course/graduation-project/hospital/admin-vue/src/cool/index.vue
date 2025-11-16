<script setup lang="ts">
import { isFunction, orderBy } from 'lodash-es'
import { markRaw, onMounted, shallowRef } from 'vue'
import { module } from '/@/cool'

const list = shallowRef<any[]>([])

async function refresh() {
  const arr = orderBy(
    module.list.filter(e => e.enable !== false && !!e.index).map(e => e.index),
    'order',
  )

  list.value = await Promise.all(
    arr
      .filter(e => e?.component)
      .map(async (e) => {
        if (e) {
          const c = await (isFunction(e.component) ? e.component() : e.component)

          return {
            ...e,
            component: markRaw(c.default || c),
          }
        }
      }),
  )
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="cool">
    <component :is="item.component" v-for="item in list" :key="item.name" />
  </div>
</template>
