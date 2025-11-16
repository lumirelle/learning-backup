<script lang="ts" setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import Tabbar from './components/tabbar.vue'
import { useCool, usePager } from '/@/cool'

const { service, router } = useCool()

const { onRefresh } = usePager()

const list = ref<any[]>([])
const loading = ref(false)
const showAdvanced = ref(false)

// 搜索条件
const searchForm = ref({
  name: '',
  code: '',
  address: '',
  phone: '',
})

// 刷新列表
async function refresh() {
  const { data, next } = onRefresh()

  loading.value = true

  try {
    const res = await next(
      service.hospital.info.page({
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
    code: '',
    address: '',
    phone: '',
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
    path: '/pages/hospital/detail',
    query: {
      id: item.id
    }
  })
}

// 页面加载时刷新
onShow(() => {
  refresh()
})
</script>

<template>
  <cl-page fullscreen>
    <cl-topbar title="医院列表" />

    <!-- 搜索区域 -->
    <cl-filter-bar>
      <cl-form :model="searchForm" class="search-form">
        <!-- 基础搜索项 -->
        <view class="basic-search">
          <cl-form-item label="医院名称" :margin="[0, 20, 20, 0]">
            <cl-input v-model="searchForm.name" placeholder="请输入医院名称" />
          </cl-form-item>

          <cl-form-item label="医院地址" :margin="[0, 20, 20, 0]">
            <cl-input v-model="searchForm.address" placeholder="请输入医院地址" />
          </cl-form-item>
        </view>

        <!-- 高级搜索项 -->
        <view v-show="showAdvanced" class="advanced-search">
          <cl-form-item label="医院编码" :margin="[0, 20, 20, 0]">
            <cl-input v-model="searchForm.code" placeholder="请输入医院编码" />
          </cl-form-item>

          <cl-form-item label="联系电话" :margin="[0, 20, 20, 0]">
            <cl-input v-model="searchForm.phone" placeholder="请输入联系电话" />
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
      <view class="hospital-list">
        <cl-card
          v-for="item in list"
          :key="item.id"
          :margin="[0, 20, 20, 20]"
          :radius="16"
          @tap="viewDetail(item)"
        >
          <!-- 医院信息 -->
          <view class="hospital-item">
            <cl-image :src="item.coverImage" :size="160" radius="12" />

            <view class="hospital-info">
              <view class="title-row">
                <cl-text :value="item.name" size="36" bold />
              </view>

              <view class="info-row">
                <cl-icon name="location" :size="32" color="primary" />
                <cl-text
                  :value="item.address"
                  color="#666"
                  :margin="[0, 0, 0, 10]"
                  size="28"
                />
              </view>

              <view class="info-row">
                <cl-icon name="phone" :size="32" color="primary" />
                <cl-text
                  :value="item.phone"
                  color="#666"
                  :margin="[0, 0, 0, 10]"
                  size="28"
                />
              </view>

              <view class="info-row">
                <cl-icon name="doc-fill" :size="32" color="primary" />
                <cl-text
                  :value="item.code"
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

.hospital-list {
  padding: 20rpx 0;
}

.hospital-item {
  display: flex;
  align-items: flex-start;
  padding: 12rpx;

  .hospital-info {
    flex: 1;
    margin-left: 30rpx;

    .title-row {
      display: flex;
      align-items: center;
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
