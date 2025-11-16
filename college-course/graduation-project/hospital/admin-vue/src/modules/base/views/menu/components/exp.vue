<script lang="ts" setup>
import type { PropType } from 'vue'
import { useForm } from '@cool-vue/crud'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { isEmpty } from 'lodash-es'
import { useI18n } from 'vue-i18n'
import { useCool } from '/@/cool'

defineOptions({
  name: 'menu-exp',
})

const props = defineProps({
  data: {
    type: Array as PropType<Eps.BaseSysMenuEntity[]>,
    default: () => [],
  },
})
const { t } = useI18n()
const { service, refs, setRefs } = useCool()
const Form = useForm()

function open() {
  Form.value?.open({
    title: t('导出'),
    width: '600px',
    props: {
      labelPosition: 'top',
    },
    op: {
      saveButtonText: t('导出'),
    },
    items: [
      {
        label: t('选择菜单'),
        prop: 'ids',
        component: {
          name: 'el-tree-select',
          ref: setRefs('ids'),
          props: {
            data: props.data,
            nodeKey: 'id',
            multiple: true,
            showCheckbox: true,
            collapseTags: true,
            collapseTagsTooltip: true,
            props: {
              label: 'name',
              children: '_children',
            },
          },
        },
      },
    ],
    on: {
      submit(_, { done, close }) {
        // 取所有id
        const ids = [...refs.ids.getCheckedKeys(), ...refs.ids.getHalfCheckedKeys()]

        if (isEmpty(ids)) {
          ElMessage.warning(t('请先选择要导出的菜单'))
          done()
        }
        else {
          service.base.sys.menu
            .export({
              ids,
            })
            .then((res) => {
              close()

              // 创建 Blob 对象
              const blob = new Blob([JSON.stringify(res)], {
                type: 'application/json',
              })

              const url = URL.createObjectURL(blob)

              // 创建一个 <a> 元素
              const a = document.createElement('a')
              a.href = url
              a.download = `${t('菜单数据')} ${dayjs().format('MM-DD HH_mm_ss')}.json`

              // 模拟点击 <a> 元素以触发下载
              a.click()

              // 清理 URL 对象
              URL.revokeObjectURL(url)
            })
            .catch((err) => {
              ElMessage.error(err.message)
            })
        }
      },
    },
  })
}
</script>

<template>
  <el-button type="info" @click="open">
    <cl-svg name="export" class="mr-[5px]" />
    {{ $t('导出') }}
  </el-button>

  <cl-form ref="Form" />
</template>
