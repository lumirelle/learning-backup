<script setup lang="ts">
import { computed, ref, useModel } from 'vue'
import { useI18n } from 'vue-i18n'
import { CrudProps } from '/#/crud'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'doctor-select',
})

const props = defineProps({
  ...CrudProps,
  modelValue: null,
  multiple: Boolean,
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

const value = useModel(props, 'modelValue')

const customService = computed(() => {
  return {
    page: (params: any) => {
      const queryParams = { ...params }
      queryParams.status = dict.getByLabel('base-status', '启用')
      if (props.scope?.hospitalId) {
        queryParams.hospitalId = props.scope.hospitalId
      }
      if (props.scope?.departmentId) {
        queryParams.departmentId = props.scope.departmentId
      }
      return service.hospital.doctor.page(queryParams)
    },
  }
})

const columns = ref([
  {
    prop: 'id',
    label: t('医生 ID'),
    minWidth: 100,
  },
  {
    prop: 'name',
    label: t('医生名称'),
    minWidth: 100,
  },
  {
    prop: 'jobCode',
    label: t('工号'),
    minWidth: 120,
  },
  {
    prop: 'title',
    label: t('职称'),
    minWidth: 150,
  },
])

// 定义搜索项
const searchItems = ref([
  {
    label: t('医生 ID'),
    prop: 'id',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入医生 ID'),
      },
    },
  },
  {
    label: t('医生名称'),
    prop: 'name',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入医生名称'),
      },
    },
  },
  {
    label: t('工号'),
    prop: 'jobCode',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入工号'),
      },
    },
  },
  {
    label: t('职称'),
    prop: 'title',
    component: { name: 'el-input', props: { clearable: true, placeholder: t('请输入职称') } },
  },
])
</script>

<template>
  <cl-select-table
    v-model="value"
    :title="t('选择医生')"
    :service="customService"
    :columns="columns"
    :multiple="multiple"
    :search-items="searchItems"
    picker-type="text"
    :dict="{ text: 'name' }"
  />
</template>
