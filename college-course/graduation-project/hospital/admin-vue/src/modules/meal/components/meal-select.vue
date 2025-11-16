<script setup lang="ts">
import { computed, ref, useModel } from 'vue'
import { useI18n } from 'vue-i18n'
import { CrudProps } from '/#/crud'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'meal-select',
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
      if (props.scope?.hospitalId) {
        queryParams.hospitalId = props.scope.hospitalId
      }
      if (props.scope?.departmentId) {
        queryParams.departmentId = props.scope.departmentId
      }
      if (props.scope?.doctorId) {
        queryParams.doctorId = props.scope.doctorId
      }
      if (props.scope?.categoryId) {
        queryParams.categoryId = props.scope.categoryId
      }
      if (props.scope?.staffId) {
        queryParams.staffId = props.scope.staffId
      }
      return service.meal.info.page(queryParams)
    },
  }
})

const columns = ref([
  { label: t('套餐 ID'), prop: 'id', minWidth: 100 },
  { label: t('分类 ID'), prop: 'categoryId', minWidth: 100 },
  { label: t('分类名称'), prop: 'categoryName', minWidth: 140 },
  { label: t('名称'), prop: 'name', minWidth: 140 },
  { label: t('价格（元）'), prop: 'price', minWidth: 140 },
  { label: t('医院'), prop: 'hospitalName', minWidth: 140 },
  { label: t('科室'), prop: 'departmentName', minWidth: 140 },
  { label: t('医生'), prop: 'doctorName', minWidth: 140 },
  { label: t('陪诊员'), prop: 'staffName', minWidth: 140 },
  { label: t('服务范围'), prop: 'serviceArea', minWidth: 120, dict: dict.get('meal-service-area') },
  {
    label: t('封面图'),
    prop: 'cover',
    minWidth: 100,
    component: { name: 'cl-image', props: { size: 60 } },
  },
])

// 定义搜索项
const searchItems = ref([
  {
    label: t('套餐名称'),
    prop: 'name',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入套餐名称'),
      },
    },
  },
])
</script>

<template>
  <cl-select-table
    v-model="value"
    :title="t('选择套餐')"
    :service="customService"
    :columns="columns"
    :multiple="multiple"
    :search-items="searchItems"
    :dict="{ text: 'name', img: 'cover' }"
  />
</template>
