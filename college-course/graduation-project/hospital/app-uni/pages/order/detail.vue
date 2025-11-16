<script lang="ts" setup>
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import BackHome from '/@/components/back-home.vue'
import { useCool, useStore } from '/@/cool'


const { service } = useCool()
const { dict, user } = useStore()

// 详情数据
const detail = ref<Eps.OrderInfoEntity>({})
const mealDetail = ref<Eps.MealInfoEntity>({})
const staffDetail = ref<Eps.AccompanyStaffEntity>({})
const patientDetail = ref<Eps.PatientInfoEntity>({})

const loading = ref(false)

// 支付方式映射
const payTypeMap: Record<number, string> = {
  0: '微信',
  1: '支付宝',
  2: '线下银行卡',
  3: '线下现金',
}

// 加载详情
async function refresh(id: string) {
  loading.value = true
  try {
    const res = await service.order.info.info({
      id,
    })
    detail.value = res

    if (detail.value.mealId) {
      const mealRes = await service.meal.info.info({
        id: detail.value.mealId,
      })
      mealDetail.value = mealRes
    }

    if (mealDetail.value.staffId) {
      const staffRes = await service.accompany.staff.info({
        id: mealDetail.value.staffId,
      })
      staffDetail.value = staffRes
    }

    if (detail.value.patientId) {
      const patientRes = await service.patient.info.info({
        id: detail.value.patientId,
      })
      patientDetail.value = patientRes
    }
  }
  finally {
    loading.value = false
  }
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
          refresh(detail.value.id)
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

// 页面加载
onLoad((options) => {
  if (options?.id) {
    refresh(options.id)
  }
})
</script>

<template>
  <cl-page fullscreen>
    <cl-topbar title="订单详情" />

    <cl-loading-mask v-if="loading" />

    <cl-confirm ref="Confirm">
      <cl-input ref="ConfirmInput"></cl-input>
    </cl-confirm>

    <cl-scroller>
      <!-- 订单状态 -->
      <view class="status-section">
        <cl-text
          :value="dict.getLabel('order-status', detail.status)"
          :color="dict.getType('order-status', detail.status)"
          :size="48"
          bold
        />
        <br>
        <br>
        <cl-text v-if="user.info?.role == 1" :value="`核销码：${detail.verifyCode}`" color="success" :size="36" bold />
      </view>

      <!-- 订单信息 -->
      <view class="info-section">
        <view class="info-row">
          <cl-text value="订单编号" color="#999" />
          <cl-text :value="detail.orderNumber" />
        </view>

        <view class="info-row">
          <cl-text value="下单时间" color="#999" />
          <cl-text :value="detail.createTime" />
        </view>

        <view class="info-row">
          <cl-text value="支付方式" color="#999" />
          <cl-text :value="payTypeMap[detail.payType || 0]" />
        </view>

        <view v-if="detail.payTime" class="info-row">
          <cl-text value="支付时间" color="#999" />
          <cl-text :value="detail.payTime" />
        </view>

        <view class="info-row">
          <cl-text value="备注信息" color="#999" />
          <cl-text :value="detail.remark || '无'" />
        </view>
      </view>

      <!-- 金额信息 -->
      <view class="amount-section">
        <view class="info-row">
          <cl-text value="订单金额" color="#999" />
          <cl-text :value="`¥${detail.totalAmount || '0'}`" />
        </view>

        <view class="info-row">
          <cl-text value="优惠金额" color="#999" />
          <cl-text :value="`¥${detail.discountAmount || '0'}`" />
        </view>

        <view class="info-row total">
          <cl-text value="实付金额" color="#999" :size="32" />
          <cl-text :value="`¥${detail.actualAmount || '0'}`" color="danger" :size="32" bold />
        </view>
      </view>

      <!-- 患者显示陪诊员档案 -->
      <view v-if="user.info?.role == 1" class="staff-section">
        <cl-text class="section-title" value="陪诊员信息" :size="32" bold />
        <view class="info-row">
          <cl-text value="姓名" color="#999" />
          <cl-text :value="staffDetail.name || '无'" />
        </view>

        <view class="info-row">
          <cl-text value="手机号" color="#999" />
          <cl-text :value="staffDetail.phone || '无'" />
        </view>

        <view class="info-row">
          <cl-text value="级别" color="#999" />
          <cl-tag :type="dict.getType('acc-staff-level', staffDetail.status)">
            {{ dict.getLabel('acc-staff-level', staffDetail.status) }}
          </cl-tag>
        </view>

        <view class="info-row">
          <cl-text value="简介" color="#999" />
          <cl-text :value="staffDetail.introduction || '无'" />
        </view>
      </view>

      <!-- 陪诊员显示患者档案 -->
      <view v-if="user.info?.role == 2" class="staff-section">
        <cl-text class="section-title" value="患者信息" :size="32" bold />
        <view class="info-row">
          <cl-text value="姓名" color="#999" />
          <cl-text :value="patientDetail.name || '无'" />
        </view>

        <view class="info-row">
          <cl-text value="手机号" color="#999" />
          <cl-text :value="patientDetail.phone || '无'" />
        </view>

        <view class="info-row">
          <cl-text value="病历号" color="#999" />
          <cl-text :value="patientDetail.medicalRecordNumber || '无'" />
        </view>
        <view class="info-row">
          <cl-text value="病史" color="#999" />
          <cl-text :value="patientDetail.medicalHistory || '无'" />
        </view>
        <view class="info-row">
          <cl-text value="过敏史" color="#999" />
          <cl-text :value="patientDetail.allergyHistory || '无'" />
        </view>
        <view class="info-row">
          <cl-text value="地址" color="#999" />
          <cl-text :value="patientDetail.address || '无'" />
        </view>
      </view>

      <!-- 核销订单按钮 -->
      <cl-button
        v-if="user.info?.role == 2 && detail.status === 2"
        type="primary"
        round
        :width="160"
        @tap.stop="handleWriteOff(detail)"
      >
        核销订单
      </cl-button>
    </cl-scroller>

    <!-- 回到首页按钮 -->
    <back-home />
  </cl-page>
</template>

<style lang="scss" scoped>
.status-section {
  padding: 60rpx 30rpx;
  background-color: #fff;
  text-align: center;
}

.info-section {
  margin-top: 20rpx;
  padding: 30rpx;
  background-color: #fff;

  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.amount-section {
  margin-top: 20rpx;
  padding: 30rpx;
  background-color: #fff;

  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    &.total {
      margin-top: 30rpx;
      padding-top: 30rpx;
      border-top: 2rpx solid #f5f5f5;
    }
  }
}

.staff-section {
  margin-top: 20rpx;
  padding: 30rpx;
  background-color: #fff;

  .section-title {
    margin-bottom: 20rpx;
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }

  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }
}
</style>
