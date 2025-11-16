<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'
import { useCool } from '/@/cool'

defineOptions({
  name: 'frame-web',
})

const loading = ref(false)
const url = ref()

const { route, refs, setRefs } = useCool()

watch(
  () => route,
  (val) => {
    url.value = val.meta?.iframeUrl
  },
  {
    immediate: true,
    deep: true,
  },
)

onMounted(() => {
  loading.value = true

  refs.iframe.onload = () => {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="page-iframe" :element-loading-text="$t('拼命加载中')">
    <iframe :ref="setRefs('iframe')" :src="url" frameborder="0" />
  </div>
</template>

<style lang="scss" scoped>
.page-iframe {
  height: 100%;

  iframe {
    height: 100%;
    width: 100%;
  }
}
</style>
