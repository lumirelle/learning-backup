<script lang="ts" setup>
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import BackHome from '/@/components/back-home.vue'
import { useCool, useStore } from '/@/cool'

const { service, router } = useCool()
const { meal } = useStore()

const doctor = ref<any>({})
const loading = ref(false)

// 获取医生详情
async function getDetail(id: string) {
  loading.value = true

  try {
    // 获取医生信息
    const info = await service.hospital.doctor.info({
      id,
    })
    doctor.value = info
  }
  finally {
    loading.value = false
  }
}


// 查看陪诊服务
function viewMealList() {
  meal.setQueryParam('doctorId', doctor.value.id)
  router.push('/pages/index/meal')
}

// 页面加载
onLoad((options) => {
  if (options?.id) {
    getDetail(options.id)
  }
})
</script>

<template>
  <cl-page fullscreen>
    <cl-topbar title="医生详情" />

    <!-- 加载中 -->
    <cl-loading-mask v-if="loading" />

    <template v-else>
      <!-- 医生基本信息 -->
      <view class="doctor-info">
        <cl-card>
          <view class="info-header">
            <cl-avatar :src="doctor.avatar" :size="160" />

            <view class="info-content">
              <view class="name-row">
                <cl-text :value="doctor.name" size="36" bold />
                <cl-tag
                  type="primary"
                  size="small"
                  :margin="[0, 0, 0, 10]"
                >
                  {{ doctor.title }}
                </cl-tag>
              </view>

              <view class="info-row">
                <cl-text :value="`工号：${doctor.jobCode}`" color="#666" size="28" />
              </view>
            </view>
          </view>

          <cl-divider :margin="[30, 0]">
            专业特长
          </cl-divider>

          <cl-text
            :value="doctor.specialty"
            color="#666"
            :line-height="1.6"
            size="28"
          />

          <cl-divider :margin="[30, 0]">
            个人简介
          </cl-divider>

          <cl-text
            :value="doctor.introduction"
            color="#666"
            :line-height="1.6"
            size="28"
          />
        </cl-card>
      </view>

      <!-- 查看陪诊服务按钮 -->
      <view class="action-btn">
        <cl-button
          type="primary"
          :width="600"
          round
          @tap="viewMealList"
        >
          查看陪诊服务
        </cl-button>
      </view>
    </template>

    <!-- 回到首页按钮 -->
    <back-home />
  </cl-page>
</template>

<style lang="scss" scoped>
.doctor-info {
  padding: 20rpx;

  .info-header {
    display: flex;
    padding: 20rpx 0;

    .info-content {
      flex: 1;
      margin-left: 30rpx;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .name-row {
        display: flex;
        align-items: center;
      }

      .info-row {
        margin-top: 20rpx;
      }
    }
  }
}

.action-btn {
  display: flex;
  justify-content: center;
  margin: 30rpx 0;
}
</style>
