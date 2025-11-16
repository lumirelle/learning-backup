<script lang="ts" setup>
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import BackHome from '/@/components/back-home.vue'
import { router, useCool, useStore } from '/@/cool'

const { service } = useCool()
const { meal } = useStore()

const hospital = ref<any>({})
const departments = ref<any[]>([])
const loading = ref(false)

// 获取医院详情
async function getDetail(id: string) {
  loading.value = true

  try {
    // 获取医院信息
    const info = await service.hospital.info.info({
      id,
    })
    hospital.value = info

    // 获取科室列表
    const { list } = await service.hospital.department.page({
      hospitalId: id,
    })
    departments.value = list
  }
  finally {
    loading.value = false
  }
}

// 查看科室详情
function viewDepartment(item: any) {
  router.push({
    path: '/pages/hospital/department',
    query: {
      id: item.id,
      hospitalId: hospital.value.id
    }
  })
}

// 查看陪诊服务
function viewMealList() {
  meal.setQueryParam('hospitalId', hospital.value.id)
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
    <cl-topbar title="医院详情" />

    <!-- 加载中 -->
    <cl-loading-mask v-if="loading" />

    <template v-else>
      <!-- 医院信息 -->
      <view class="hospital-info">
        <cl-banner :list="[{ url: hospital.detailImage }]" :height="300" />

        <view class="info-content">
          <cl-text
            :value="hospital.name"
            size="36"
            bold
            block
            :margin="[0, 0, 20, 0]"
          />

          <view class="info-row">
            <cl-icon name="location" :size="28" color="#999" />
            <cl-text
              :value="hospital.address"
              color="#666"
              :margin="[0, 0, 0, 10]"
            />
          </view>

          <view class="info-row">
            <cl-icon name="phone" :size="28" color="#999" />
            <cl-text
              :value="hospital.phone"
              color="#666"
              :margin="[0, 0, 0, 10]"
            />
          </view>

          <cl-divider :margin="[30, 0]">
            医院简介
          </cl-divider>

          <cl-text
            :value="hospital.introduction"
            color="#666"
            :line-height="1.6"
          />
        </view>
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

      <!-- 科室列表 -->
      <view class="department-list">
        <cl-divider :margin="[30, 0]">
          科室列表
        </cl-divider>

        <cl-grid :column="3">
          <cl-grid-item
            v-for="item in departments"
            :key="item.id"
            @tap="viewDepartment(item)"
          >
            <view class="department-item">
              <cl-image :src="item.coverImage" :size="120" radius="6" />
              <cl-text :value="item.name" :margin="[20, 0, 0, 0]" :ellipsis="1" />
            </view>
          </cl-grid-item>
        </cl-grid>
      </view>
    </template>

    <!-- 回到首页按钮 -->
    <back-home />
  </cl-page>
</template>

<style lang="scss" scoped>
.hospital-info {
  .info-content {
    padding: 30rpx;

    .info-row {
      display: flex;
      align-items: center;
      margin-top: 16rpx;
    }
  }
}

.action-btn {
  display: flex;
  justify-content: center;
  margin: 30rpx 0;
}

.department-list {
  padding: 0 30rpx 30rpx;

  .department-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 30rpx;
    background-color: #fff;
    border-radius: 12rpx;
    margin: 10rpx;
  }
}
</style>
