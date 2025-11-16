<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCool, useStore } from '/@/cool'

const { router } = useCool()
const { t } = useI18n()
const { user } = useStore()

// 当前页面路径
const pagePath = router.path

const list = computed(() => {
  const arr = [...router.tabs]
    .filter((e) => !e.requiredRole || e.requiredRole === user.info?.role)

  console.log('[tabbar] list:', arr)

  return arr.map((e) => {
    const active = pagePath?.includes(e.pagePath)

    return {
      ...e,
      icon: `/${active ? e.selectedIconPath : e.iconPath}`,
      active,
      number: 0,
      text: t((e.text || '')?.replace(/%/g, '')),
    }
  })
})

function toLink(pagePath: string) {
  router.push(`/${pagePath}`)
}

uni.hideTabBar()
</script>

<template>
  <cl-footer :flex="false" border :z-index="399" :padding="0">
    <view class="tabbar">
      <view
        v-for="(item, index) in list"
        :key="index"
        class="item"
        :class="{
          'is-active': item.active,
        }"
        @tap="toLink(item.pagePath)"
      >
        <view class="icon">
          <image :src="item.icon" mode="aspectFit" />
        </view>
        <text class="label">
          {{ item.text }}
        </text>
        <view v-if="item.number > 0" class="badge">
          {{ item.number || 0 }}
        </view>
      </view>
    </view>
  </cl-footer>
</template>

<style lang="scss" scoped>
$icon-size: 56rpx;

.tabbar {
  display: flex;
  height: 120rpx;
  width: 100%;

  .item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 1;
    position: relative;

    .icon {
      height: $icon-size;
      width: $icon-size;

      image {
        height: 100%;
        width: 100%;
      }
    }

    .label {
      font-size: 22rpx;
      color: #bfbfbf;
    }

    .badge {
      display: flex;
      align-items: center;
      justify-content: center;
      position: absolute;
      top: 20rpx;
      transform: translateX(20rpx);
      font-size: 20rpx;
      height: 36rpx;
      min-width: 36rpx;
      padding: 0 6rpx;
      background-color: #f56c6c;
      border: 4rpx solid #fff;
      color: #fff;
      border-radius: 18rpx;
      font-weight: bold;
      box-sizing: border-box;
    }

    &.is-active {
      .label {
        color: $cl-color-primary;
      }
    }
  }
}
</style>
