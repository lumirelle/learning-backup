<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue'
import { useCool } from '/@/cool'
import { useUi } from '/$/cool-ui'
import { useI18n } from 'vue-i18n'

const { service, router } = useCool()
const { t } = useI18n()
const ui = useUi()

// 角色选项
const roleOptions = [
  { label: t('患者'), value: 1 },
  { label: t('陪诊人员'), value: 2 },
]

// 当前角色
const role = ref<1 | 2>(1)

// 患者档案表单
const PatientForm = ref<ClForm.Ref>()
const patientForm = ref<Eps.PatientInfoEntity>({
  name: '',
  gender: 0,
  birthday: undefined,
  phone: '',
  address: '',
  type: 0,
  medicalRecordNumber: '',
  medicalHistory: '',
  allergyHistory: '',
  remark: '',
  height: undefined,
  weight: undefined,
  systolicPressure: undefined,
  diastolicPressure: undefined,
})

// 陪诊员档案表单
const StaffForm = ref<ClForm.Ref>()
const staffForm = ref<Eps.AccompanyStaffEntity>({
  name: '',
  gender: 0,
  birthday: undefined,
  phone: '',
  status: 0,
  introduction: '',
  remark: '',
})

const rules = reactive({
  name: {
    required: true,
    message: t('请输入姓名'),
  },
  gender: {
    required: true,
    message: t('请选择性别'),
  },
  birthday: {
    required: true,
    message: t('请选择生日'),
  },
  phone: {
    required: true,
    message: t('请输入手机号'),
    pattern: /^1[3-9]\d{9}$/,
    messagePattern: t('请输入正确的手机号'),
  },
  address: {
    required: true,
    message: t('请输入地址'),
  },
  type: {
    required: true,
    message: t('请选择类型'),
  },
  medicalRecordNumber: {
    required: true,
    message: t('请输入病历号'),
  },
  medicalHistory: {
    required: true,
    message: t('请输入病史'),
  },
  allergyHistory: {
    required: true,
    message: t('请输入过敏史'),
  },
  height: {
    required: true,
    message: t('请输入身高'),
  },
  weight: {
    required: true,
    message: t('请输入体重'),
  },
  systolicPressure: {
    required: true,
    message: t('请输入收缩压'),
  },
  diastolicPressure: {
    required: true,
    message: t('请输入舒张压'),
  },
  level: {
    required: true,
    message: t('请选择级别'),
  },
  status: {
    required: true,
    message: t('请选择状态'),
  },
  introduction: {
    required: true,
    message: t('请输入简介'),
  },
})

// 是否已存在档案
const hasProfile = ref(false)

// 保存
async function submit() {
  if (role.value === 1) {
    PatientForm.value?.validate(async (valid, errors) => {
      if (!valid) {
        return
      }

      if (!hasProfile.value) {
        await service.user.info.addProfile({
          ...patientForm.value,
          role: 1,
        })
      } else {
        await service.user.info.updateProfile({
          ...patientForm.value,
          role: 1,
        })
      }

      ui.showToast(t('保存成功'))

      // 保存后跳转到主页
      router.push('/pages/index/my')
    })
  } else if (role.value === 2) {
    StaffForm.value?.validate(async (valid, errors) => {
      if (!valid) {
        return
      }

      if (!hasProfile.value) {
        await service.user.info.addProfile({
          ...staffForm.value,
          role: 2,
        })
      } else {
        await service.user.info.updateProfile({
          ...staffForm.value,
          role: 2,
        })
      }

      ui.showToast(t('保存成功'))

      // 保存后跳转到主页
      router.push('/pages/index/my')
    })
  }
}

// 拉取档案
async function fetchProfile() {
  try {
    const res = await service.user.info.profile()
    if (res && res.role) {
      role.value = res.role
      hasProfile.value = true
      if (role.value === 1) Object.assign(patientForm, res)
      else if (role.value === 2) Object.assign(staffForm, res)
    }
  } catch (e) {
    // 无档案
    hasProfile.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<template>
  <cl-page background-color="#fff">
    <cl-topbar :border="false" background-color="transparent" />
    <view class="profile-page">
      <view class="profile-title">
        <cl-text block bold :size="36">{{ t('创建用户档案') }}</cl-text>
      </view>
      <view class="role-select">
        <cl-radio-group v-model="role">
          <cl-radio v-for="item in roleOptions" :key="item.value" :label="item.value">
            {{ item.label }}
          </cl-radio>
        </cl-radio-group>
      </view>
      <div class="form-container">
        <!-- 患者表单 -->
        <template v-if="role === 1">
          <cl-form ref="PatientForm" v-model="patientForm" :rules="rules">
            <cl-form-item :label="t('姓名')" prop="name">
              <cl-input v-model="patientForm.name" :placeholder="t('请输入姓名')"  />
            </cl-form-item>
            <cl-form-item :label="t('性别')" prop="gender">
              <cl-radio-group v-model="patientForm.gender">
                <cl-radio :label="1">{{ t('男') }}</cl-radio>
                <cl-radio :label="2">{{ t('女') }}</cl-radio>
                <cl-radio :label="0">{{ t('未知') }}</cl-radio>
              </cl-radio-group>
            </cl-form-item>
            <cl-form-item :label="t('生日')" prop="birthday">
              <cl-select-date v-model="patientForm.birthday" :placeholder="t('请选择生日')"></cl-select-date>
            </cl-form-item>
            <cl-form-item :label="t('手机号')" prop="phone">
              <cl-input v-model="patientForm.phone" :placeholder="t('请输入手机号')" maxlength="11" />
            </cl-form-item>
            <cl-form-item :label="t('地址')" prop="address">
              <cl-input v-model="patientForm.address" :placeholder="t('请输入地址')" />
            </cl-form-item>
            <cl-form-item :label="t('病历号')" prop="medicalRecordNumber">
              <cl-input v-model="patientForm.medicalRecordNumber" :placeholder="t('请输入病历号')" />
            </cl-form-item>
            <cl-form-item :label="t('病史')" prop="medicalHistory">
              <cl-input v-model="patientForm.medicalHistory" :placeholder="t('请输入病史')" />
            </cl-form-item>
            <cl-form-item :label="t('过敏史')" prop="allergyHistory">
              <cl-input v-model="patientForm.allergyHistory" :placeholder="t('请输入过敏史')" />
            </cl-form-item>
            <cl-form-item :label="t('备注')" prop="remark">
              <cl-input v-model="patientForm.remark" :placeholder="t('备注')" />
            </cl-form-item>
          </cl-form>
        </template>
        <!-- 陪诊员表单 -->
        <template v-else-if="role === 2">
          <cl-form ref="StaffForm" v-model="staffForm" :rules="rules">
            <cl-form-item :label="t('姓名')" prop="name">
              <cl-input v-model="staffForm.name"  :placeholder="t('请输入姓名')" />
            </cl-form-item>
            <cl-form-item :label="t('性别')" prop="gender">
              <cl-radio-group v-model="staffForm.gender">
                <cl-radio :label="1">{{ t('男') }}</cl-radio>
                <cl-radio :label="2">{{ t('女') }}</cl-radio>
                <cl-radio :label="0">{{ t('未知') }}</cl-radio>
              </cl-radio-group>
            </cl-form-item>
            <cl-form-item :label="t('生日')" prop="birthday">
              <cl-select-date v-model="staffForm.birthday" :placeholder="t('请选择生日')"></cl-select-date>
            </cl-form-item>
            <cl-form-item :label="t('手机号')" prop="phone">
              <cl-input v-model="staffForm.phone" :placeholder="t('请输入手机号')" maxlength="11" />
            </cl-form-item>
            <cl-form-item :label="t('简介')" prop="introduction">
              <cl-input v-model="staffForm.introduction" :placeholder="t('请输入简介')" />
            </cl-form-item>
            <cl-form-item :label="t('备注')" prop="remark">
              <cl-input v-model="staffForm.remark" :placeholder="t('备注')" />
            </cl-form-item>
          </cl-form>
        </template>
        <cl-button type="primary" @tap="submit" :margin="[40,0,0,0]">
          {{ t('保存') }}
        </cl-button>
      </div>
    </view>
  </cl-page>
</template>

<style lang="scss" scoped>
.profile-page {
  padding: 40rpx 20rpx;
  .profile-title {
    text-align: center;
    margin-bottom: 30rpx;
  }
  .role-select {
    margin-bottom: 40rpx;
    text-align: center;
  }
  .form-container {
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.04);
    padding: 32rpx 20rpx 40rpx 20rpx;
  }
}
</style>
