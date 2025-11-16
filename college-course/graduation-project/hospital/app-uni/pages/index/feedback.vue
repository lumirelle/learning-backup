<script lang="ts" setup>
import { ref } from 'vue'
import Tabbar from './components/tabbar.vue'
import { useCool, useStore } from '/@/cool'
import { onShow } from '@dcloudio/uni-app'

const { dict, feedback } = useStore()

const { service, router } = useCool()

const form = ref({
  type: '', // 反馈类型
  content: '', // 反馈内容
  contactInfo: '', // 联系方式
  images: [] as string[], // 图片列表
  orderId: '', // 订单ID
})

const orderDetail = ref<Eps.OrderInfoEntity>({})

const submitting = ref(false)

// 提交反馈
async function handleSubmit() {
  if (form.value.type === null || form.value.type === undefined || form.value.type === '') {
    return uni.showToast({
      title: '请选择反馈类型',
      icon: 'none',
    })
  }

  if (!form.value.content) {
    return uni.showToast({
      title: '请输入反馈内容',
      icon: 'none',
    })
  }

  submitting.value = true
  try {
    await service.feedback.complaint.add({
      ...form.value,
      status: 0, // 待处理
    })

    uni.showToast({
      title: '提交成功',
      icon: 'success',
    })

    if (form.value.orderId) {
      router.push({
        path: '/pages/order/detail',
        query: {
          id: form.value.orderId,
        },
      })
    }
    else {
      router.home()
    }

  }
  catch (err) {
    uni.showToast({
      title: '提交失败',
      icon: 'error',
    })
  }
  finally {
    submitting.value = false
  }
}

onShow(async() => {
  form.value = {
    type: '',
    content: '',
    contactInfo: '',
    images: [],
    orderId: '',
  }

  const orderId = feedback.getQueryParam('orderId')
  if (orderId) {
    console.log('[feedback]: 从其他页面打开', orderId)
    form.value.orderId = orderId

    // 获取订单详情
    const res = await service.order.info.info({
      id: orderId,
    })
    if (res) {
      orderDetail.value = res
    }
  } else {
    console.log('[feedback]: 从首页打开')
    orderDetail.value = {} as Eps.OrderInfoEntity
  }
  feedback.resetQueryParam('orderId')
})
</script>

<template>
  <cl-page fullscreen>
    <cl-topbar title="投诉反馈" />

    <cl-scroller>
      <view class="feedback-form">
        <cl-form :model="form">
          <!-- 订单号 -->
          <cl-form-item v-if="form.orderId" label="订单号">
            <cl-input
              v-model="orderDetail.orderNumber"
              :disabled="true"
            />
          </cl-form-item>

          <!-- 反馈类型 -->
          <cl-form-item label="反馈类型" required>
            <cl-select
              v-model="form.type"
              :options="dict.get('complaint-type')"
              placeholder="请选择反馈类型"
            />
          </cl-form-item>

          <!-- 反馈内容 -->
          <cl-form-item label="反馈内容" required>
            <cl-textarea
              v-model="form.content"
              placeholder="请详细描述您的问题或建议"
              :maxlength="500"
              show-count
            />
          </cl-form-item>

          <!-- 联系方式 -->
          <cl-form-item label="联系方式" required>
            <cl-input
              v-model="form.contactInfo"
              placeholder="请留下您的联系方式，方便我们及时回复"
            />
          </cl-form-item>

          <!-- 图片上传 -->
          <cl-form-item label="上传图片">
            <cl-upload
              v-model="form.images"
              :limit="9 - form.images.length"
              multiple
            />
          </cl-form-item>
        </cl-form>

        <!-- 提交按钮 -->
        <view class="submit-btn">
          <cl-button
            type="primary"
            :width="600"
            round
            :loading="submitting"
            @tap="handleSubmit"
          >
            提交反馈
          </cl-button>
        </view>
      </view>
    </cl-scroller>

    <tabbar />
  </cl-page>
</template>

<style lang="scss" scoped>
.feedback-form {
  padding: 30rpx;

  :deep(.cl-form-item) {
    margin-bottom: 30rpx;

    .cl-form-item__content {
      .cl-input,
      .cl-select,
      .cl-textarea {
        width: 100%;
      }
    }
  }

  .upload-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;

    .upload-item {
      position: relative;

      .delete-btn {
        position: absolute;
        top: -10rpx;
        right: -10rpx;
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        background-color: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .upload-btn {
      width: 160rpx;
      height: 160rpx;
      border: 2rpx dashed #ddd;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .submit-btn {
    margin-top: 60rpx;
    display: flex;
    justify-content: center;
  }
}
</style>
