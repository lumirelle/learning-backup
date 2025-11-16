<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, ref, useModel } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'staff-select',
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
      queryParams.status = dict.getByLabel('acc-staff-status', '正常')
      return service.accompany.staff.page(queryParams)
    },
  }
})

const columns = ref([
  {
    prop: 'id',
    label: t('陪诊员 ID'),
    minWidth: 100,
  },
  {
    prop: 'name',
    label: t('陪诊员名称'),
    minWidth: 100,
  },
  {
    prop: 'gender',
    label: t('性别'),
    minWidth: 120,
    dict: dict.get('gender'),
  },
  {
    prop: 'birthday',
    label: t('年龄'),
    minWidth: 150,
    formatter: (row: any) => {
      return row.birthday ? dayjs().diff(dayjs(row.birthday), 'year') : ''
    },
  },
  {
    prop: 'phone',
    label: t('电话'),
    minWidth: 150,
  },
  {
    prop: 'level',
    label: t('级别'),
    minWidth: 150,
    dict: dict.get('acc-staff-level'),
  },
])

// 定义搜索项
const searchItems = ref([
  {
    label: t('陪诊员 ID'),
    prop: 'id',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入陪诊员 ID'),
      },
    },
  },
  {
    label: t('陪诊员名称'),
    prop: 'name',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入陪诊员名称'),
      },
    },
  },
])
</script>

<template>
  <cl-select-table
    v-model="value"
    :title="t('选择陪诊员')"
    :service="customService"
    :columns="columns"
    :multiple="multiple"
    :search-items="searchItems"
    picker-type="text"
    :dict="{ text: 'name' }"
  />
</template>
