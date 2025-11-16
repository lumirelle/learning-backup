<script lang="ts" setup>
import { useCrud, useForm } from '@cool-vue/crud'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useCool } from '/@/cool'

defineOptions({
  name: 'user-move',
})

const { service } = useCool()
const Form = useForm()
const Crud = useCrud()
const { t } = useI18n()

async function open(ids: any[]) {
  Form.value?.open({
    title: t('部门转移'),
    width: '500px',
    props: {
      labelWidth: '80px',
    },
    items: [
      {
        label: t('选择部门'),
        prop: 'departmentId',
        component: {
          name: 'cl-dept-select',
        },
      },
    ],
    on: {
      async submit(data, { done, close }) {
        if (!data.departmentId) {
          ElMessage.warning(t('请选择部门'))
          return done()
        }

        await ElMessageBox.confirm(t('转移到新部门，是否继续？'), t('提示'), {
          type: 'warning',
        })
          .then(() => {
            service.base.sys.user
              .move({
                ...data,
                userIds: ids,
              })
              .then(() => {
                ElMessage.success(t('转移成功'))
                Crud.value?.refresh()
                close()
              })
              .catch((err) => {
                ElMessage.error(err.message)
              })
          })
          .catch(() => null)

        done()
      },
    },
  })
}

defineExpose({
  open,
})
</script>

<template>
  <div class="dept-move">
    <cl-form ref="Form" />
  </div>
</template>
