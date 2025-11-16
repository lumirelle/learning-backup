<script lang="ts" setup>
import { onReady } from '@dcloudio/uni-app'
import { useI18n } from 'vue-i18n'
import { useApp } from '/@/cool'

const app = useApp()
const { t } = useI18n()

onReady(() => {
  uni.setNavigationBarTitle({
    title: `${t('关于')} ${app.info.name}`,
  })
})
</script>

<template>
  <cl-page>
    <view class="page-about">
      <view class="logo">
        <image src="/static/logo.png" />
      </view>

      <cl-text
        block
        align="center"
        :size="24"
        color="info"
        :line-height="1.2"
        :margin="[80, 0, 0, 0]"
      >
        {{ app.info.desc }}
      </cl-text>
    </view>
  </cl-page>
</template>

<style lang="scss" scoped>
.page-about {
  .logo {
    display: flex;
    justify-content: center;
    padding: 100rpx 0;

    image {
      height: 160rpx;
      width: 160rpx;
      border-radius: 24rpx;
      box-shadow: 0 25rpx 30rpx -25rpx #666666;
    }
  }
}
</style>
