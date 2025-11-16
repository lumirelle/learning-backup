<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, ref, useModel } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'patient-select',
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
      return service.patient.info.page(queryParams)
    },
  }
})

const columns = ref([
  {
    prop: 'id',
    label: t('患者 ID'),
    minWidth: 100,
  },
  {
    prop: 'name',
    label: t('姓名'),
    minWidth: 100,
  },
  {
    prop: 'phone',
    label: t('电话'),
    minWidth: 100,
  },
  {
    label: t('年龄'),
    prop: 'birthday',
    minWidth: 100,
    sortable: 'custom',
    formatter: (row: any) => {
      return row.birthday ? dayjs().diff(dayjs(row.birthday), 'year') : ''
    },
  },
  {
    prop: 'gender',
    label: t('性别'),
    minWidth: 100,
    dict: dict.get('gender'),
  },
  { label: t('病历号'), prop: 'medicalRecordNumber', minWidth: 100 },
  { label: t('类型'), prop: 'type', minWidth: 120, dict: dict.get('patient-type') },
])

// 定义搜索项
const searchItems = ref([
  {
    label: t('患者 ID'),
    prop: 'id',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入患者 ID'),
      },
    },
  },
  {
    label: t('姓名'),
    prop: 'name',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入姓名'),
      },
    },
  },
  {
    label: t('电话'),
    prop: 'phone',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入电话'),
      },
    },
  },
  {
    label: t('性别'),
    prop: 'gender',
    component: {
      name: 'el-select',
      props: {
        clearable: true,
        placeholder: t('请选择性别'),
      },
      options: dict.get('gender'),
    },
  },
  {
    label: t('病历号'),
    prop: 'medicalRecordNumber',
    component: { name: 'el-input', props: { clearable: true } },
  },
  {
    label: t('类型'),
    prop: 'type',
    component: { name: 'el-select', options: dict.get('patient-type'), props: { clearable: true } },
  },
])
</script>

<template>
  <!-- 使用 `name` 字段作为选择器的文本 -->
  <cl-select-table
    v-model="value"
    :title="t('选择患者')"
    :service="customService"
    :columns="columns"
    :multiple="multiple"
    :search-items="searchItems"
    picker-type="text"
    :dict="{ text: 'name' }"
  />
</template>
