<script lang="ts" setup>
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useBase } from '/$/base'
import { useCool } from '/@/cool'

defineOptions({
  name: 'my-info',
})

const { t } = useI18n()
const { service } = useCool()
const { user } = useBase()

// 表单数据
const form = reactive({
  headImg: '',
  nickName: '',
  password: '',
  oldPassword: '',
})

// 保存状态
const loading = ref(false)

// 保存
async function save() {
  loading.value = true

  await service.base.comm
    .personUpdate(form)
    .then(() => {
      form.password = ''
      form.oldPassword = ''

      ElMessage.success(t('修改成功'))
      user.get()
    })
    .catch((err) => {
      ElMessage.error(err.message)
    })

  loading.value = false
}

onMounted(() => {
  form.headImg = user.info?.headImg || ''
  form.nickName = user.info?.nickName || ''
})
</script>

<template>
  <div class="view-my">
    <el-scrollbar>
      <div class="p-[20px]">
        <div class="title">
          {{ $t('基本信息') }}
        </div>

        <el-form label-width="100px" :model="form" :disabled="loading" label-position="top">
          <el-form-item :label="$t('头像')">
            <cl-upload v-model="form.headImg" />
          </el-form-item>

          <el-form-item :label="$t('昵称')">
            <el-input v-model="form.nickName" :placeholder="$t('请填写昵称')" clearable />
          </el-form-item>

          <el-form-item :label="$t('原密码')">
            <el-input
              v-model="form.oldPassword"
              type="password"
              :placeholder="$t('请填写原密码')"
              clearable
            />
          </el-form-item>

          <el-form-item :label="$t('新密码')">
            <el-input
              v-model="form.password"
              type="password"
              :placeholder="$t('请填写新密码')"
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :disabled="loading" @click="save">
              {{
                $t('保存修改')
              }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
.view-my {
  background-color: var(--el-bg-color);
  height: 100%;
  box-sizing: border-box;
  border-radius: 6px;

  .el-form {
    width: 400px;
    max-width: 100%;
  }

  .title {
    margin-bottom: 30px;
    font-size: 15px;
    font-weight: bold;
  }
}
</style>
