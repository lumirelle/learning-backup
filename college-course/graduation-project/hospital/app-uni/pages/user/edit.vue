<script lang="ts" setup>
import { onReady } from '@dcloudio/uni-app'
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUi } from '/$/cool-ui'
import { useCool, useStore } from '/@/cool'

const { router, service } = useCool()
const { user, dict } = useStore()
const ui = useUi()
const { t } = useI18n()

const loading = ref(false)

// 账户信息
const form = reactive({
  nickName: user.info?.nickName,
})

// 个人档案
const roleOptions = [
  { label: t('患者'), value: 1 },
  { label: t('陪诊人员'), value: 2 },
]

const role = ref(user.info?.role)

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
  avatarUrl: '',
})

const staffForm = ref<Eps.AccompanyStaffEntity>({
  name: '',
  gender: 0,
  birthday: undefined,
  phone: '',
  level: 0,
  status: 0,
  introduction: '',
  remark: '',
  avatarUrl: '',
})

async function save() {
  loading.value = true
  const roleForm = role.value === 1 ? { ...patientForm, role: 1 } : { ...staffForm, role: 2 }
  try {
    await user.update(form)
    await service.user.info.updateProfile(roleForm)
    ui.showTips(t('保存成功'), async () => {
      await user.get()
      router.back()
    })
  } catch (err: any) {
    ui.showToast(err.message || t('保存失败'))
  }
  loading.value = false
}

onReady(async () => {
  // 拉取当前档案
  const res = await service.user.info.profile()
  if (res) {
    if (role.value === 1) {
      patientForm.value = res
    }
    else if (role.value === 2) {
      staffForm.value = res
    }
    else {
      ui.showToast(t('角色不匹配'))
      router.back()
    }
    console.log('当前角色', role.value)
    console.log('当前档案', role.value === 1 ? patientForm : staffForm)
  }
})
</script>

<template>
  <cl-page>
    <view class="page">
      <view class="form">
        <cl-form label-position="top">
          <cl-divider :margin="[30, 0]">
            个人信息
          </cl-divider>

          <cl-form-item :label="t('昵称')">
            <cl-input
              v-model="form.nickName"
              type="nickname"
              :border="false"
              :height="80"
              :border-radius="12"
              :placeholder="t('请填写昵称')"
            />
          </cl-form-item>

          <cl-divider :margin="[30, 0]">
            个人档案
          </cl-divider>

          <cl-form-item :label="t('角色')">
            <cl-text type="info">{{ roleOptions.find(r => r.value === role)?.label }}</cl-text>
          </cl-form-item>
          <!-- 患者表单 -->
          <template v-if="role === 1">
            <cl-form-item :label="t('姓名')">
              <cl-input v-model="patientForm.name" :placeholder="t('请输入姓名')" />
            </cl-form-item>
            <cl-form-item :label="t('性别')">
              <cl-radio-group v-model="patientForm.gender">
                <cl-radio :label="1">{{ t('男') }}</cl-radio>
                <cl-radio :label="2">{{ t('女') }}</cl-radio>
              </cl-radio-group>
            </cl-form-item>
            <cl-form-item :label="t('生日')">
              <cl-select-date v-model="patientForm.birthday" :placeholder="t('请选择生日')"></cl-select-date>
            </cl-form-item>
            <cl-form-item :label="t('手机号')">
              <cl-input v-model="patientForm.phone" :placeholder="t('请输入手机号')" />
            </cl-form-item>
            <cl-form-item :label="t('地址')">
              <cl-input v-model="patientForm.address" :placeholder="t('请输入地址')" />
            </cl-form-item>
            <cl-form-item :label="t('病历号')">
              <cl-input v-model="patientForm.medicalRecordNumber" :placeholder="t('请输入病历号')" />
            </cl-form-item>
            <cl-form-item :label="t('病史')">
              <cl-input v-model="patientForm.medicalHistory" :placeholder="t('请输入病史')" />
            </cl-form-item>
            <cl-form-item :label="t('过敏史')">
              <cl-input v-model="patientForm.allergyHistory" :placeholder="t('请输入过敏史')" />
            </cl-form-item>
            <cl-form-item :label="t('备注')">
              <cl-input v-model="patientForm.remark" :placeholder="t('备注')" />
            </cl-form-item>
          </template>
          <!-- 陪诊员表单 -->
          <template v-else>
            <cl-form-item :label="t('姓名')">
              <cl-input v-model="staffForm.name" :placeholder="t('请输入姓名')" />
            </cl-form-item>
            <cl-form-item :label="t('性别')">
              <cl-radio-group v-model="staffForm.gender">
                <cl-radio :label="1">{{ t('男') }}</cl-radio>
                <cl-radio :label="2">{{ t('女') }}</cl-radio>
              </cl-radio-group>
            </cl-form-item>
            <cl-form-item :label="t('生日')">
              <cl-select-date v-model="staffForm.birthday" :placeholder="t('请选择生日')"></cl-select-date>
            </cl-form-item>
            <cl-form-item :label="t('手机号')">
              <cl-input v-model="staffForm.phone" :placeholder="t('请输入手机号')" />
            </cl-form-item>
            <cl-form-item :label="t('级别')">
              <cl-tag :type="dict.getType('acc-staff-level', staffForm.level)">
                {{ dict.getLabel('acc-staff-level', staffForm.level) }}
              </cl-tag>
            </cl-form-item>
            <cl-form-item :label="t('简介')">
              <cl-input v-model="staffForm.introduction" :placeholder="t('请输入简介')" />
            </cl-form-item>
            <cl-form-item :label="t('备注')">
              <cl-input v-model="staffForm.remark" :placeholder="t('备注')" />
            </cl-form-item>
          </template>
        </cl-form>
      </view>

      <cl-footer>
        <cl-button custom type="primary" :loading="loading" @tap="save">
          {{ t('保存') }}
        </cl-button>
      </cl-footer>
    </view>
  </cl-page>
</template>

<style lang="scss" scoped>
.page {
  .profile-title {
    text-align: center;
    margin-bottom: 30rpx;
  }
  .role-info {
    text-align: center;
    margin-bottom: 20rpx;
  }
  .form {
    padding: 20rpx 24rpx;
  }
}
</style>
