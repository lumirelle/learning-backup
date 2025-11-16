<script lang="ts" setup>
import { useCool } from '/@/cool'

const { router } = useCool()

// 回到对应的tab页面
function handleBackHome() {
  let targetUrl = '/pages/index/hospital'

  const currentPage = getCurrentPages().pop()
  const currentPath = currentPage?.route || ''
  const pathParts = currentPath.split('/')

  if (pathParts.length > 1) {
    const modulePath = pathParts[1]
    const tabUrl = `/pages/index/${modulePath}`

    console.log('[back-home] 当前页面是二级页面：', currentPath, modulePath, tabUrl)
    if (router.isTab(tabUrl)) {
      console.log('[back-home] 当前页面所属tab页面：', tabUrl)
      targetUrl = tabUrl
    }
  }

  router.push(targetUrl)
}
</script>

<template>
  <view class="back-home">
    <cl-button
      type="primary"
      :width="80"
      :height="80"
      round
      @tap="handleBackHome"
    >
      <cl-icon name="home" :size="48" />
    </cl-button>
  </view>
</template>

<style lang="scss" scoped>
.back-home {
  position: absolute;
  bottom: 20px;
  right: 20px;

  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
