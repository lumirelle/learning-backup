<script lang="ts" setup>
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { useCool, useStore } from '/@/cool'

const { service, router } = useCool()
const { dict } = useStore()

// 订单信息
const order = ref<Eps.OrderInfoEntity>({})
const loading = ref(false)
const submitting = ref(false)

// 就诊信息
const form = ref({
  visitTime: '',
  remark: '',
  payType: '',
})

// 加载订单详情
async function loadDetail(id: string) {
  loading.value = true
  try {
    const res = await service.order.info.info({
      id,
    })
    order.value = res
  }
  finally {
    loading.value = false
  }
}

// 提交支付
async function handleSubmit() {
  if (form.value.payType === null || form.value.payType === undefined || form.value.payType === '') {
    return uni.showToast({
      title: '请选择支付方式',
      icon: 'none',
    })
  }

  submitting.value = true
  try {
    await service.order.info.update({
      id: order.value.id,
      visitTime: form.value.visitTime,
      remark: form.value.remark,
      payType: Number(form.value.payType),
      status: 2, // 待使用
    })

    uni.showToast({
      title: '支付成功',
      icon: 'success',
    })

    // 延迟返回，让用户看到成功提示
    setTimeout(() => {
      router.push({
        path: '/pages/order/detail',
        query: {
          id: order.value.id,
        },
      })
    }, 1500)
  }
  catch (err) {
    console.error(err)
    uni.showToast({
      title: '支付失败',
      icon: 'error',
    })
  }
  finally {
    submitting.value = false
  }
}

// 选择支付方式
function handleSelect(value: string) {
  console.log('select pay type', value)
  form.value.payType = value
}

// 页面加载
onLoad((options) => {
  if (options?.id) {
    console.log('[order] 页面加载：', options.id)
    loadDetail(options.id)
  }
})
</script>

<template>
  <cl-page fullscreen>
    <cl-topbar title="订单支付" />

    <cl-loading-mask v-if="loading" />

    <cl-scroller>
      <!-- 订单信息 -->
      <view class="order-info">
        <view class="amount">
          <cl-text value="支付金额" size="28" color="#666" />
          <view class="price">
            <cl-text value="¥" color="danger" size="32" />
            <cl-text :value="order.actualAmount" color="danger" size="48" bold />
          </view>
        </view>

        <view class="info-item">
          <cl-text value="订单编号" color="#666" size="28" />
          <cl-text :value="order.orderNumber" size="28" />
        </view>

        <view class="info-item">
          <cl-text value="下单时间" color="#666" size="28" />
          <cl-text :value="order.createTime" size="28" />
        </view>
      </view>

      <view class="extra-info">
        <!-- 就诊时间 -->
        <view class="info-item">
          <cl-text value="就诊时间" color="#666" size="28" />
          <cl-select-date v-model="form.visitTime" required placeholder="请选择就诊时间" />
        </view>

        <!-- 备注 -->
        <view class="info-item">
          <cl-text value="备注" color="#666" size="28" />
          <cl-input v-model="form.remark" required placeholder="请输入备注" />
        </view>
      </view>

      <!-- 支付方式 -->
      <view class="pay-methods">
        <cl-text value="支付方式" size="32" bold :margin="[0, 0, 30, 0]" />

        <view class="method-list">
          <view v-for="item in dict.get('pay-type').filter(item => !item.label.startsWith('线下'))" :key="item.value"
            class="method-item" :class="{ active: form.payType === item.value }" @tap="handleSelect(item.value)">
            <cl-icon :name="['wechat', 'alipay', 'card', 'cash'][Number(item.value)]" :size="48" />
            <cl-text :value="item.label" :margin="[10, 0, 0, 0]" />
          </view>
        </view>
      </view>

      <!-- 支付按钮 -->
      <view class="submit-btn">
        <cl-button type="primary" :width="600" :loading="submitting" round @tap="handleSubmit">
          立即支付
        </cl-button>
      </view>
    </cl-scroller>
  </cl-page>
</template>

<style lang="scss" scoped>
.order-info {
  margin: 20rpx;
  padding: 30rpx;
  background-color: #fff;
  border-radius: 16rpx;

  .amount {
    text-align: center;
    margin-bottom: 40rpx;

    .price {
      display: flex;
      align-items: baseline;
      justify-content: center;
      margin-top: 20rpx;
    }
  }

  .info-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 20rpx;

    &:first-child {
      margin-top: 0;
    }
  }
}

.extra-info {
  margin: 20rpx;
  padding: 30rpx;
  background-color: #fff;
  border-radius: 16rpx;

  .info-item {
    margin-top: 20rpx;
  }
}

.pay-methods {
  margin: 20rpx;
  padding: 30rpx;
  background-color: #fff;
  border-radius: 16rpx;

  .method-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;

    .method-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 30rpx;
      border: 2rpx solid #eee;
      border-radius: 12rpx;
      transition: all 0.3s;

      &.active {
        border-color: var(--cl-color-primary);
        background-color: var(--cl-color-primary-light);
      }
    }
  }
}

.submit-btn {
  margin: 60rpx 20rpx;
}
</style>
