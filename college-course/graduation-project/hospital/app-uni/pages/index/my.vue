<script lang="ts" setup>
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import Tabbar from './components/tabbar.vue'
import { service, useCool, useStore } from '/@/cool'

const { router } = useCool()
const { user, order: orderStore } = useStore()

const { t } = useI18n()

const totalOrder = ref(0)
const waitingPaymentOrder = ref(0)
const waitingUseOrder = ref(0)
const completeOrder = ref(0)

async function refresh() {
  if (user.token) {
    await user.get()
  }  
  else {
    user.logout();
  }

  await service.order.info.personCount()
    .then((res) => {
      totalOrder.value = res
    })
    .catch(() => {
      totalOrder.value = 0
    })
  
  await service.order.info.personCountWaitingPayment()
    .then((res) => {
      waitingPaymentOrder.value = res
    })
    .catch(() => {
      waitingPaymentOrder.value = 0
    })

  await service.order.info.personCountWaitingUse()
    .then((res) => {
      waitingUseOrder.value = res
    })
    .catch(() => {
      waitingUseOrder.value = 0
    })

  await service.order.info.personCountComplete()
    .then((res) => {
      completeOrder.value = res
    })
    .catch(() => {
      completeOrder.value = 0
    })
}

const order = reactive({
  list: [
    {
      icon: 'order-paid',
      label: t('待支付'),
      value: '0',
    },
    {
      icon: 'order-not-shipped',
      label: t('待使用'),
      value: '2',
    },
    {
      icon: 'order-received',
      label: t('已完成'),
      value: '3',
    },
    {
      icon: 'order-refund',
      label: t('已取消'),
      value: '4',
    },
  ],

  toLink(value: string) {
    orderStore.setQueryParam('status', value)
    router.push('/pages/index/order')
  },
})

function toSet() {
  router.push('/pages/user/set')
}

function toEdit() {
  router.push('/pages/user/edit')
}

onPullDownRefresh(async () => {
  await refresh()
  uni.stopPullDownRefresh()
})

onShow(() => {
  refresh()
})
</script>

<template>
  <cl-page status-bar-background="transparent">
    <!-- #ifdef MP -->
    <cl-sticky>
      <cl-topbar :show-back="false" :border="false" background-color="transparent" />
    </cl-sticky>
    <!-- #endif -->

    <view class="page-my">
      <view class="header">
        <view class="icon" @tap="toEdit">
          <text class="cl-icon-edit" />
        </view>

        <view class="icon" @tap="toSet">
          <text class="cl-icon-set" />
        </view>
      </view>

      <view class="user" @tap="toEdit">
        <cl-avatar :src="user.info?.avatarUrl" :size="100" />

        <view class="det">
          <cl-text :size="32" block :margin="[0, 0, 8, 0]">
            {{
              user.info?.nickName || t("未登录")
            }}
          </cl-text>
          <cl-text :size="24" color="info">
            {{ t("写签名会更容易获得别人的关注哦～") }}
          </cl-text>
        </view>
      </view>

      <view class="count">
        <view class="item">
          <text>{{ totalOrder }}</text>
          <text>{{ t("总订单") }}</text>
        </view>

        <view class="item">
          <text>{{ waitingPaymentOrder }}</text>
          <text>{{ t("待支付") }}</text>
        </view>

        <view class="item">
          <text>{{ waitingUseOrder }}</text>
          <text>{{ t("待使用") }}</text>
        </view>

        <view class="item">
          <text>{{ completeOrder }}</text>
          <text>{{ t("已完成") }}</text>
        </view>
      </view>

      <view class="status">
        <cl-text block :size="30" bold>
          {{ t("我的订单") }}
        </cl-text>

        <view class="list">
          <view
            v-for="(item, index) in order.list"
            :key="index"
            class="item"
            @tap="order.toLink(item.value)"
          >
            <cl-icon :name="item.icon" :size="50" />
            <cl-text :margin="[18, 0, 0, 0]" :size="24" color="info">
              {{ t(item.label) }}
            </cl-text>
          </view>
        </view>
      </view>
    </view>

    <tabbar />
  </cl-page>
</template>

<style lang="scss" scoped>
$gap: 24rpx;

.page-my {
  padding: $gap;

  .header {
    display: flex;

    .icon {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 60rpx;
      width: 60rpx;
      background-color: rgba(150, 150, 150, 0.1);
      border-radius: 16rpx;
      font-size: 32rpx;
      margin-right: $gap;

      &:last-child {
        margin-left: auto;
        margin-right: 0;
      }
    }
  }

  .user {
    display: flex;
    align-items: center;
    padding: 48rpx 12rpx;

    .det {
      flex: 1;
      margin-left: 32rpx;
    }
  }

  .count {
    display: flex;
    margin-bottom: 32rpx;

    .item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      flex: 1;

      text {
        &:nth-child(1) {
          font-size: 40rpx;
          margin-bottom: 4rpx;
          font-weight: bold;
        }

        &:nth-child(2) {
          font-size: 24rpx;
          color: $cl-color-info;
        }
      }
    }
  }

  .status {
    background-color: #fff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;

    .list {
      display: flex;

      .item {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        flex: 1;
        padding: 48rpx 0 24rpx 0;
      }
    }
  }
}
</style>
