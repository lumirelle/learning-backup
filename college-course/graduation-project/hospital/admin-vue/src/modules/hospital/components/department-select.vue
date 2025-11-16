<script setup lang="ts">
import { computed, ref, useModel } from 'vue'
import { useI18n } from 'vue-i18n'
import { CrudProps } from '/#/crud'
import { useCool } from '/@/cool'

defineOptions({
  name: 'hospital-department-select',
})

const props = defineProps({
  ...CrudProps,
  modelValue: null,
  multiple: Boolean,
})

const { service } = useCool()
const { t } = useI18n()

const value = useModel(props, 'modelValue')

const customService = computed(() => {
  return {
    page: (params: any) => {
      const queryParams = { ...params }
      if (props.scope?.hospitalId) {
        queryParams.hospitalId = props.scope.hospitalId
      }
      return service.hospital.department.page(queryParams)
    },
  }
})

const columns = ref([
  { label: t('科室 ID'), prop: 'id', minWidth: 100 },
  { label: t('名称'), prop: 'name', minWidth: 140 },
  { label: t('编码'), prop: 'code', minWidth: 140 },
  { label: t('医院 ID'), prop: 'hospitalId', minWidth: 100 },
  { label: t('医院名称'), prop: 'hospitalName', minWidth: 100 },
  { label: t('类型'), prop: 'type', minWidth: 120 },
  { label: t('状态'), prop: 'status', minWidth: 120 },
])
</script>

<template>
  <cl-select-table
    v-model="value"
    :title="t('选择科室信息')"
    :service="customService"
    :columns="columns"
    :multiple="multiple"
    :dict="{ text: 'name' }"
    picker-type="text"
  />
</template>
