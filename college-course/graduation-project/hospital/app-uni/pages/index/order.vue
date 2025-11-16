<script lang="ts" setup>
import { onShow } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import Tabbar from './components/tabbar.vue'
import { useCool, usePager, useStore } from '/@/cool'

const { service, router } = useCool()
const { onRefresh } = usePager()

const list = ref<Eps.OrderInfoEntity[]>([])
const loading = ref(false)

const { dict, order, user, feedback } = useStore()

// 搜索条件
const searchForm = ref({
  status: '',
  orderNumber: '',
})

const orderStatusOptions = computed(() => {
  return [
    {
      label: '全部',
      value: '',
    },
    ...dict.get('order-status'),
  ]
})

// 刷新列表
async function refresh() {
  const { data, next } = onRefresh()

  loading.value = true
  try {
    const res = await next(
      service.order.info.page({
        ...data,
        ...searchForm.value,
      }),
    )
    list.value = (res as any).list
  }
  finally {
    loading.value = false
  }
}

// 搜索
function search() {
  refresh()
}

// 重置搜索
function reset() {
  searchForm.value = {
    status: '',
    orderNumber: '',
  }
  refresh()
}

// 查看详情
function viewDetail(item: Eps.OrderInfoEntity) {
  router.push({
    path: '/pages/order/detail',
    query: {
      id: item.id
    }
  })
}

// 获取状态文本
function getStatusText(status: number | undefined) {
  if (status === undefined)
    return '未知'
  const result = dict.getLabel('order-status', status) || '未知'
  return user.info?.role === 2 ? `患者${result}` : result
}

// 获取状态样式
function getStatusType(status: number | undefined) {
  if (status === undefined)
    return 'primary'
  const result = dict.get('order-status').find(item => item.value === status)?.type
  return result || 'primary'
}

// 继续支付
function handlePay(item: Eps.OrderInfoEntity) {
  router.push({
    path: '/pages/order/pay',
    query: {
      id: item.id,
      amount: item.actualAmount
    }
  })
}

// 取消订单
async function handleCancel(item: Eps.OrderInfoEntity) {
  try {
    const { confirm } = await uni.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
    })

    if (!confirm)
      return

    await service.order.info.update({
      id: item.id,
      status: 4,
    })

    uni.showToast({
      title: '取消成功',
      icon: 'success',
    })

    refresh()
  }
  catch (err) {
    uni.showToast({
      title: '操作失败',
      icon: 'error',
    })
  }
}

// 申请退款
async function handleRefund(item: Eps.OrderInfoEntity) {
  try {
    const { confirm } = await uni.showModal({
      title: '提示',
      content: '确定要申请退款吗？',
    })

    if (!confirm)
      return

    await service.order.info.update({
      id: item.id,
      status: 5, // 已退款
    })

    uni.showToast({
      title: '申请成功',
      icon: 'success',
    })

    refresh()
  }
  catch (err) {
    uni.showToast({
      title: '操作失败',
      icon: 'error',
    })
  }
}

// 投诉反馈
async function handleFeedback(item:Eps.OrderInfoEntity) {
  feedback.setQueryParam('orderId', item.id)
  router.push('/pages/index/feedback')
}

// 核销订单
const Confirm = ref<ClConfirm.Ref>()
const ConfirmInput = ref()
async function handleWriteOff(item: Eps.OrderInfoEntity) {
  // 弹窗输入核销码
  Confirm.value?.open({
    title: '请输入核销码',
    beforeClose: async(action, { done, showLoading, hideLoading }) => {
      if (action === 'confirm') {
        showLoading()
        try {
          await service.order.info.writeOff({
            id: item.id,
            verifyCode: ConfirmInput.value.value,
          })
          uni.showToast({
            title: '核销成功',
            icon: 'success',
          })
          refresh()
        }
        catch (err) {
          uni.showToast({
            title: '操作失败',
            icon: 'error',
          })
        }
        done()
        hideLoading()
      }
      else {
        done()
      }
    },
  })
}

// 页面加载时刷新
onShow(() => {
  // 如果有状态参数,设置到搜索条件中
  const status = order.getQueryParam('status')
  console.log('[order] 从其他页面打开：', status)
  if (status) {
    searchForm.value.status = status
    order.resetQueryParam('status')
  }

  refresh()
})
</script>

<template>
  <cl-page fullscreen>
    <cl-topbar title="我的订单" />

    <cl-loading-mask v-if="loading" />

    <cl-confirm ref="Confirm">
      <cl-input ref="ConfirmInput"></cl-input>
    </cl-confirm>

    <!-- 搜索区域 -->
    <cl-filter-bar>
      <cl-form :model="searchForm" class="search-form">
        <view class="basic-search">
          <cl-form-item label="订单状态" :margin="[0, 20, 20, 0]">
            <cl-select v-model="searchForm.status" :options="orderStatusOptions" />
          </cl-form-item>

          <cl-form-item label="订单编号" :margin="[0, 20, 20, 0]">
            <cl-input v-model="searchForm.orderNumber" placeholder="请输入订单编号" />
          </cl-form-item>
        </view>

        <view class="filter-btns">
          <cl-button type="primary" :width="180" round @tap="search">
            <cl-icon name="search" :margin="[0, 10, 0, 0]" />
            搜索
          </cl-button>
          <cl-button :width="180" round :margin="[0, 20, 0, 20]" @tap="reset">
            <cl-icon name="refresh" :margin="[0, 10, 0, 0]" />
            重置
          </cl-button>
        </view>
      </cl-form>
    </cl-filter-bar>

    <!-- 列表区域 -->
    <cl-scroller @down="refresh">
      <view class="order-list">
        <cl-card
          v-for="item in list"
          :key="item.id"
          :margin="[0, 20, 20, 20]"
          :radius="16"
          @tap="viewDetail(item)"
        >
          <!-- 订单信息 -->
          <view class="order-item">
            <view class="header">
              <cl-text :value="item.orderNumber" size="28" />
              <cl-tag :type="getStatusType(item.status)">
                {{ getStatusText(item.status) }}
              </cl-tag>
            </view>

            <view class="content">
              <view class="meal-info">
                <cl-image :src="item.mealCover" :size="160" radius="12" />
                <view class="meal-detail">
                  <cl-text :value="item.mealName" size="32" bold />
                  <cl-text :value="item.hospitalName" color="#666" size="28" :margin="[10, 0, 0, 0]" />
                </view>
              </view>

              <view class="price-info">
                <view class="price-row">
                  <cl-text value="订单金额" color="#666" size="28" />
                  <cl-text :value="`¥${item.totalAmount}`" color="#333" size="28" bold />
                </view>
                <view v-if="item.discountAmount !== '0'" class="price-row">
                  <cl-text value="优惠金额" color="#666" size="28" />
                  <cl-text :value="`-¥${item.discountAmount}`" color="#333" size="28" bold />
                </view>
                <view class="price-row">
                  <cl-text value="实付金额" color="#666" size="28" />
                  <cl-text :value="`¥${item.actualAmount}`" color="danger" size="32" bold />
                </view>
                <view class="price-row">
                  <cl-text value="订单创建时间" color="#666" size="28" />
                  <cl-text :value="item.createTime" color="danger" size="32" bold />
                </view>
                <view class="price-row">
                  <cl-text value="订单支付时间" color="#666" size="28" />
                  <cl-text :value="item.payTime" color="danger" size="32" bold />
                </view>
              </view>
            </view>

            <view class="footer">
              <!-- 患者操作按钮 -->
              <template v-if="user.info?.role === 1">
                <cl-button
                  v-if="item.status === 0"
                  type="primary"
                  round
                  :width="160"
                  :margin="[0, 0, 0, 20]"
                  @tap.stop="handlePay(item)"
                >
                  继续支付
                </cl-button>
                <cl-button
                  v-if="item.status === 0"
                  round
                  :width="160"
                  @tap.stop="handleCancel(item)"
                >
                  取消订单
                </cl-button>
                <cl-button
                  v-if="item.status === 1"
                  type="danger"
                  round
                  :width="160"
                  @tap.stop="handleRefund(item)"
                >
                  申请退款
                </cl-button>
                <cl-button
                  v-if="item.status === 3 && !item.hasComplaint"
                  type="info"
                  round
                  :width="160"
                  @tap.stop="handleFeedback(item)"
                >
                  投诉反馈
                </cl-button>
              </template>
              <!-- 陪诊员操作按钮 -->
              <template v-else>
                <cl-button
                  v-if="item.status === 2"
                  type="primary"
                  round
                  :width="160"
                  :margin="[0, 0, 0, 20]"
                  @tap.stop="handleWriteOff(item)"
                >
                  核销订单
                </cl-button>
              </template>
            </view>
          </view>
        </cl-card>
      </view>

      <!-- 加载更多 -->
      <cl-loadmore :loading="loading" />
    </cl-scroller>

    <tabbar />
  </cl-page>
</template>

<style lang="scss" scoped>
.search-form {
  padding: 20rpx;
  max-width: 900rpx;
  margin: 0 auto;

  .basic-search {
    :deep(.cl-form-item) {
      margin-bottom: 20rpx;

      .cl-form-item__content {
        flex: 1;

        .cl-input,
        .cl-select {
          width: 100%;
        }
      }
    }
  }
}

.filter-btns {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20rpx;
  padding: 30rpx 20rpx;
  border-top: 2rpx solid #f5f5f5;
  margin-top: 20rpx;

  .cl-button {
    margin: 0;
  }
}

.order-list {
  padding: 20rpx 0;
}

.order-item {
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx;
    border-bottom: 2rpx solid #f5f5f5;
  }

  .content {
    padding: 20rpx;

    .meal-info {
      display: flex;
      align-items: flex-start;

      .meal-detail {
        flex: 1;
        margin-left: 20rpx;
      }
    }

    .price-info {
      margin-top: 20rpx;
      padding-top: 20rpx;
      border-top: 2rpx solid #f5f5f5;

      .price-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 10rpx;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }

  .footer {
    display: flex;
    justify-content: flex-end;
    padding: 20rpx;
    border-top: 2rpx solid #f5f5f5;
  }
}
</style>
