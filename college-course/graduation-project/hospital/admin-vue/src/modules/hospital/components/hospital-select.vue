<script setup lang="ts">
import { computed, ref, useModel } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'hospital-select',
})

const props = defineProps({
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
      return service.hospital.info.page(queryParams)
    },
  }
})

const columns = ref([
  {
    prop: 'id',
    label: t('医院 ID'),
    minWidth: 100,
  },
  {
    prop: 'name',
    label: t('医院名称'),
    minWidth: 100,
  },
  {
    prop: 'address',
    label: t('地址'),
    minWidth: 120,
  },
  {
    prop: 'phone',
    label: t('联系电话'),
    minWidth: 150,
  },
])

// 定义搜索项
const searchItems = ref([
  {
    label: t('医院 ID'),
    prop: 'id',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入医院 ID'),
      },
    },
  },
  {
    label: t('医院名称'),
    prop: 'name',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入医院名称'),
      },
    },
  },
  {
    label: t('地址'),
    prop: 'address',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入地址'),
      },
    },
  },
])
</script>

<template>
  <cl-select-table
    v-model="value"
    :title="t('选择医院')"
    :service="customService"
    :columns="columns"
    :multiple="multiple"
    :search-items="searchItems"
    picker-type="text"
    :dict="{ text: 'name' }"
  />
</template>
