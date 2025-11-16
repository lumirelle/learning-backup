<script lang="ts" setup>
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import BackHome from '/@/components/back-home.vue'
import { useCool, useStore } from '/@/cool'

const { service, router } = useCool()

const { user } = useStore()

// 详情数据
const detail = ref<Eps.MealInfoEntity>({})
const loading = ref(false)
const submitting = ref(false)

// 加载详情
async function loadDetail(id: string) {
  // loading.value = true
  try {
    const res = await service.meal.info.info({
      id,
    })
    detail.value = res
  }
  finally {
    loading.value = false
  }
}

// 立即购买
async function handleBuy() {
  if (!detail.value.id) {
    return uni.showToast({
      title: '套餐信息不存在',
      icon: 'none',
    })
  }

  submitting.value = true
  try {
    const res = await service.order.info.add({
      mealId: detail.value.id,
      totalAmount: detail.value.price,
      actualAmount: detail.value.price,
      discountAmount: '0',
      status: 0, // 待支付
    })

    console.log('[meal] 生成订单：', res)

    // 跳转到支付页面
    router.push({
      path: '/pages/order/pay',
      query: {
        id: res.id
      }
    })
  }
  catch (err) {
    uni.showToast({
      title: '购买失败，请重试',
      icon: 'none',
    })
  }
  finally {
    submitting.value = false
  }
}

// 页面加载
onLoad((options) => {
  if (options?.id) {
    loadDetail(options.id)
  }
})
</script>

<template>
  <cl-page fullscreen>
    <cl-topbar title="套餐详情" />

    <cl-loading-mask v-if="loading" />

    <cl-scroller>
      <!-- 封面图 -->
      <cl-image
        :src="detail.cover"
        mode="aspectFill"
        :height="300"
      />

      <!-- 基本信息 -->
      <view class="info-section">
        <view v-if="user.info?.role !== 2" class="price-row">
          <cl-text :value="`¥${detail.price || 0}`" color="danger" size="48" bold />
          <cl-button
            type="primary"
            round
            :width="240"
            :loading="submitting"
            @tap="handleBuy"
          >
            立即购买
          </cl-button>
        </view>

        <cl-text :value="detail.name" size="36" bold :margin="[20, 0]" />

        <view class="info-row">
          <cl-icon name="location" :size="32" color="primary" />
          <cl-text
            :value="detail.hospitalName"
            color="#666"
            :margin="[0, 0, 0, 10]"
            size="28"
          />
        </view>

        <view class="info-row">
          <cl-icon name="doc-fill" :size="32" color="primary" />
          <cl-text
            :value="detail.departmentName"
            color="#666"
            :margin="[0, 0, 0, 10]"
            size="28"
          />
        </view>

        <view class="info-row">
          <cl-icon name="face-auth" :size="32" color="primary" />
          <cl-text
            :value="'医生：' + detail.doctorName"
            color="#666"
            :margin="[0, 0, 0, 10]"
            size="28"
          />
        </view>

        <view class="info-row">
          <cl-icon name="face-auth" :size="32" color="primary" />
          <cl-text
            :value="'陪诊员：' + detail.staffName"
            color="#666"
            :margin="[0, 0, 0, 10]"
            size="28"
          />
        </view>

        <view class="info-row">
          <cl-icon name="time" :size="32" color="primary" />
          <cl-text
            :value="`服务次数: ${detail.serviceCount || 0}次`"
            color="#666"
            :margin="[0, 0, 0, 10]"
            size="28"
          />
        </view>
      </view>

      <!-- 服务范围 -->
      <view class="service-section">
        <cl-text value="服务范围" size="32" bold :margin="[0, 0, 30, 0]" />

        <view class="service-list">
          <view
            v-for="(item, index) in JSON.parse(detail.serviceArea || '[]')"
            :key="index"
            class="service-item"
          >
            <cl-icon name="check" color="primary" :size="32" />
            <cl-text
              :value="['代预约', '代走流程', '医嘱分析', '健康跟踪', '其他定制服务'][Number(item)]"
              :margin="[0, 0, 0, 10]"
              size="28"
            />
          </view>
        </view>
      </view>

      <!-- 套餐简介 -->
      <view class="intro-section">
        <cl-text value="套餐简介" size="32" bold :margin="[0, 0, 30, 0]" />
        <br>
        <cl-text :value="detail.intro" color="#666" size="28" />
      </view>
    </cl-scroller>

    <!-- 回到首页按钮 -->
    <back-home />
  </cl-page>
</template>

<style lang="scss" scoped>
.info-section {
  padding: 30rpx;
  background-color: #fff;

  .price-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
  }

  .info-row {
    display: flex;
    align-items: center;
    margin-top: 20rpx;

    .cl-icon {
      flex-shrink: 0;
    }

    .cl-text {
      flex: 1;
    }
  }
}

.service-section {
  margin-top: 20rpx;
  padding: 30rpx;
  background-color: #fff;

  .service-list {
    .service-item {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.intro-section {
  margin-top: 20rpx;
  padding: 30rpx;
  background-color: #fff;
}
</style>
