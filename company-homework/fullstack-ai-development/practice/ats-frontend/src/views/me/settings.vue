<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { NButton, NForm, NFormItem, NInput, useMessage } from 'naive-ui'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { BizError } from '@/api/request'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const msg = useMessage()

const formRef = ref<FormInst | null>(null)
const submitting = ref(false)

const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const rules: FormRules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 72, message: '密码长度 8 - 72 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_r, v: string) => v === form.newPassword,
      message: '两次输入的密码不一致',
      trigger: ['blur', 'input'],
    },
  ],
}

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await authApi.changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
    })
    msg.success('密码已更新，请重新登录')
    await auth.logout()
    router.replace('/login')
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
  }
  finally {
    submitting.value = false
  }
}
</script>

<template>
  <div max-w-560px mx-auto p="x-6 b-16" class="pt-[calc(60px+40px)]">
    <header mb-8>
      <p kicker mb-2>账户</p>
      <h1 m-0 text-2xl font-bold>修改密码</h1>
      <p mt-2 text-sm text-secondary>
        修改成功后将注销当前会话，需使用新密码重新登录。
      </p>
    </header>

    <section p-6 rounded-xl bg-elevated border="~ subtle">
      <NForm ref="formRef" :model="form" :rules="rules" label-placement="top">
        <NFormItem label="当前密码" path="currentPassword">
          <NInput v-model:value="form.currentPassword" type="password" show-password-on="click" />
        </NFormItem>
        <NFormItem label="新密码" path="newPassword">
          <NInput v-model:value="form.newPassword" type="password" show-password-on="click" />
        </NFormItem>
        <NFormItem label="确认新密码" path="confirmPassword">
          <NInput v-model:value="form.confirmPassword" type="password" show-password-on="click" />
        </NFormItem>
        <NButton type="primary" :loading="submitting" @click="submit">
          保存并重新登录
        </NButton>
      </NForm>
    </section>
  </div>
</template>
