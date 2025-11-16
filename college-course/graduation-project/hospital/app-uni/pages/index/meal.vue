<script lang="ts" setup>
import { onLoad, onShow } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import Tabbar from './components/tabbar.vue'
import { useCool, usePager, useStore } from '/@/cool'

const { service, router } = useCool()
const { meal, user } = useStore()

const { onRefresh } = usePager()

// 初始数据
const categoryList = ref<Eps.MealCategoryEntity[]>([])
const hospitalList = ref<Eps.HospitalInfoEntity[]>([])
const departmentList = ref<Eps.DepartmentEntity[]>([])
const doctorList = ref<Eps.DoctorEntity[]>([])
const staffList = ref<Eps.AccompanyStaffEntity[]>([])

// 分类、医院、科室、医生、陪诊人员选项
const categoryOptions = computed(() => {
  return categoryList.value.map(item => ({
    label: item.name,
    value: item.id,
  }))
})
const hospitalOptions = computed(() => {
  return hospitalList.value.map(item => ({
    label: item.name,
    value: item.id,
  }))
})
const departmentOptions = computed(() => {
  return departmentList.value.map(item => ({
    label: item.name,
    value: item.id,
  }))
})
const doctorOptions = computed(() => {
  return doctorList.value.map(item => ({
    label: item.name,
    value: item.id,
  }))
})
const staffOptions = computed(() => {
  return staffList.value.map(item => ({
    label: item.name,
    value: item.id,
  }))
})

const list = ref<Eps.MealInfoEntity[]>([])

const loading = ref(false)
const showAdvanced = ref(false)

// 搜索条件
const searchForm = ref({
  name: '',
  categoryId: '',
  hospitalId: '',
  departmentId: '',
  doctorId: '',
  staffId: '',
})

// 刷新列表
async function refresh() {
  const { data, next } = onRefresh()

  loading.value = true

  try {
    // 获取分类列表
    const resCategory = await service.meal.category.page({
      page: 1,
      size: 100,
    })
    categoryList.value = [{ id: '', name: '全部' }, ...resCategory.list]

    // 获取医院列表
    const resHospital = await service.hospital.info.page({
      page: 1,
      size: 100,
    })
    hospitalList.value = [{ id: '', name: '全部' }, ...resHospital.list]

    // 获取科室列表
    const resDepartment = await service.hospital.department.page({
      page: 1,
      size: 100,
    })
    departmentList.value = [{ id: '', name: '全部' }, ...resDepartment.list]

    // 获取医生列表
    const resDoctor = await service.hospital.doctor.page({
      page: 1,
      size: 100,
    })
    doctorList.value = [{ id: '', name: '全部' }, ...resDoctor.list]

    // 获取陪诊人员列表
    const resStaff = await service.accompany.staff.page({
      page: 1,
      size: 100,
    })
    staffList.value = [{ id: '', name: '全部' }, ...resStaff.list]

    // 获取套餐列表
    const res = await next(
      service.meal.info.page({
        ...data,
        ...searchForm.value,
        status: 1, // 默认只显示启用状态
      }),
    )

    // @ts-expect-error xxx
    list.value = res.list
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
    name: '',
    categoryId: '',
    hospitalId: '',
    departmentId: '',
    doctorId: '',
    staffId: '',
  }
  refresh()
}

// 切换高级搜索
function toggleAdvanced() {
  showAdvanced.value = !showAdvanced.value
}

// 查看详情
function viewDetail(item: any) {
  router.push({
    path: '/pages/meal/detail',
    query: {
      id: item.id
    }
  })
}

// 页面加载时刷新
onLoad(() => {
  refresh()
})

// 监听页面显示
onShow(() => {
  // 如果有医院ID参数,设置到搜索条件中
  const hospitalId = meal.getQueryParam('hospitalId')
  if (hospitalId) {
    console.log('[meal] 从其他页面打开：', hospitalId)
    searchForm.value.hospitalId = hospitalId
    meal.resetQueryParam('hospitalId')
  }

  // 如果有科室ID参数,设置到搜索条件中
  const departmentId = meal.getQueryParam('departmentId')
  if (departmentId) {
    console.log('[meal] 从其他页面打开：', departmentId)
    searchForm.value.departmentId = departmentId
    showAdvanced.value = true
    meal.resetQueryParam('departmentId')
  }

  // 如果有医生ID参数,设置到搜索条件中
  const doctorId = meal.getQueryParam('doctorId')
  if (doctorId) {
    console.log('[meal] 从其他页面打开：', doctorId)
    searchForm.value.doctorId = doctorId
    showAdvanced.value = true
    meal.resetQueryParam('doctorId')
  }

  refresh()
})
</script>

<template>
  <cl-page fullscreen>
    <cl-topbar :title="user.info?.role == 2 ? '我的套餐' : '套餐列表'" />

    <!-- 搜索区域 -->
    <cl-filter-bar>
      <cl-form :model="searchForm" class="search-form">
        <!-- 基础搜索项 -->
        <view class="basic-search">
          <cl-form-item label="套餐名称" :margin="[0, 20, 20, 0]">
            <cl-input v-model="searchForm.name" placeholder="请输入套餐名称" />
          </cl-form-item>

          <cl-form-item label="所属分类" :margin="[0, 20, 20, 0]">
            <cl-select v-model="searchForm.categoryId" :options="categoryOptions" />
          </cl-form-item>

          <cl-form-item label="所属医院" :margin="[0, 20, 20, 0]">
            <cl-select v-model="searchForm.hospitalId" :options="hospitalOptions" />
          </cl-form-item>

          <cl-form-item v-if="user.info?.role === 1" label="陪诊人员" :margin="[0, 20, 20, 0]">
            <cl-select v-model="searchForm.staffId" :options="staffOptions" />
          </cl-form-item>
        </view>

        <!-- 高级搜索项 -->
        <view v-show="showAdvanced" class="advanced-search">
          <cl-form-item label="所属科室" :margin="[0, 20, 20, 0]">
            <cl-select v-model="searchForm.departmentId" :options="departmentOptions" />
          </cl-form-item>

          <cl-form-item label="主治医生" :margin="[0, 20, 20, 0]">
            <cl-select v-model="searchForm.doctorId" :options="doctorOptions" />
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
          <cl-button :width="200" round @tap="toggleAdvanced">
            <cl-icon :name="showAdvanced ? 'arrow-top' : 'arrow-bottom'" :margin="[0, 10, 0, 0]" />
            {{ showAdvanced ? '收起筛选' : '高级筛选' }}
          </cl-button>
        </view>
      </cl-form>
    </cl-filter-bar>

    <!-- 列表区域 -->
    <cl-scroller @down="refresh">
      <view class="meal-list">
        <cl-card
          v-for="item in list"
          :key="item.id"
          :margin="[0, 20, 20, 20]"
          :radius="16"
          @tap="viewDetail(item)"
        >
          <!-- 套餐信息 -->
          <view class="meal-item">
            <cl-image :src="item.cover" :size="160" radius="12" />

            <view class="meal-info">
              <view class="title-row">
                <cl-text :value="item.name" size="36" bold />
                <cl-text :value="`¥${item.price}`" color="danger" size="32" :margin="[0, 0, 0, 20]" />
              </view>

              <view class="info-row">
                <cl-icon name="location" :size="32" color="primary" />
                <cl-text
                  :value="item.hospitalName"
                  color="#666"
                  :margin="[0, 0, 0, 10]"
                  size="28"
                />
              </view>

              <view class="info-row">
                <cl-icon name="doc-fill" :size="32" color="primary" />
                <cl-text
                  :value="item.departmentName"
                  color="#666"
                  :margin="[0, 0, 0, 10]"
                  size="28"
                />
              </view>

              <view class="info-row">
                <cl-icon name="face-auth" :size="32" color="primary" />
                <cl-text
                  :value="'医生：' + item.doctorName"
                  color="#666"
                  :margin="[0, 0, 0, 10]"
                  size="28"
                />
              </view>

              <view class="info-row">
                <cl-icon name="face-auth" :size="32" color="primary" />
                <cl-text
                  :value="'陪诊员：' + item.staffName"
                  color="#666"
                  :margin="[0, 0, 0, 10]"
                  size="28"
                />
              </view>

              <view class="info-row">
                <cl-icon name="time" :size="32" color="primary" />
                <cl-text
                  :value="`服务次数: ${item.serviceCount}次`"
                  color="#666"
                  :margin="[0, 0, 0, 10]"
                  size="28"
                />
              </view>
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

  .basic-search,
  .advanced-search {
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

  .advanced-search {
    padding-top: 20rpx;
    border-top: 2rpx solid #f5f5f5;
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

.meal-list {
  padding: 20rpx 0;
}

.meal-item {
  display: flex;
  align-items: flex-start;
  padding: 12rpx;

  .meal-info {
    flex: 1;
    margin-left: 30rpx;

    .title-row {
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
}
</style>
