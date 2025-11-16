<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useBase } from '/$/base'
import { proxy, value } from '/@/config/proxy'
import { storage, useCool } from '/@/cool'

interface Item {
  name: string
  url: string
  enable: boolean
}

const { service } = useCool()
const { user } = useBase()

const list = ref<Item[]>([])

function refresh() {
  for (const i in proxy) {
    const { target, rewrite } = proxy[i]
    const name = i.replace(/\//g, '')

    list.value.push({
      name,
      url: target + rewrite(`/${name}`),
      enable: value == name,
    })
  }

  const d = list.value.find(e => e.enable)

  if (!d) {
    ElMessageBox.confirm('当前代理异常，是否切换为默认环境？', '提示', {
      type: 'warning',
    })
      .then(() => {
        update(list.value[0]?.name)
      })
      .catch(() => null)
  }
}

function onChange(item: Item) {
  list.value.forEach((e) => {
    e.enable = e.name == item.name
  })

  update(item.name)
}

async function update(name: string) {
  await service.request({
    url: '/__cool_updateProxy',
    method: 'POST',
    data: {
      name,
    },
    proxy: false,
  })

  user.clear()
  storage.remove('devTools.account')

  setTimeout(() => {
    location.reload()
  }, 300)
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="dt-proxy">
    <div v-for="(item, index) in list" :key="index" class="flex items-center mb-[10px]">
      <el-tag disable-transitions effect="dark" class="mr-[8px]">
        {{ item.name }}
      </el-tag>
      <el-link class="select-none mr-auto" target="_blank" :href="item.url">
        {{ item.url }}
      </el-link>

      <el-switch v-model="item.enable" :disabled="item.enable" @change="onChange(item)" />
    </div>
  </div>
</template>
