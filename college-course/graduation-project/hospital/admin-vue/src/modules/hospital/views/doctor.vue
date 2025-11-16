<script lang="ts" setup>
import { useCrud, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import DepartmentSelect from '/$/hospital/components/department-select.vue'
import HospitalSelect from '/$/hospital/components/hospital-select.vue'
import { useCool } from '/@/cool'

defineOptions({
  name: 'hospital-doctor',
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

// cl-upsert
const Upsert = useUpsert({
  items: [
    {
      label: t('姓名'),
      prop: 'name',
      component: { name: 'el-input', props: { clearable: true } },
      span: 12,
      required: true,
    },
    {
      label: t('工号'),
      prop: 'jobCode',
      component: { name: 'el-input', props: { clearable: true } },
      span: 12,
      required: true,
    },
    {
      label: t('职称'),
      prop: 'title',
      component: { name: 'el-input', props: { clearable: true } },
      span: 12,
      required: true,
    },
    () => {
      return {
        label: t('选择医院'),
        prop: 'hospitalId',
        component: { vm: HospitalSelect },
        span: 12,
        required: true,
        hidden: Upsert.value?.mode === 'update',
      }
    },
    () => {
      return {
        label: t('选择科室（关联科室）'),
        prop: 'departmentId',
        component: { vm: DepartmentSelect },
        span: 12,
        required: true,
        hidden: true, // 默认隐藏,等选择医院后显示
      }
    },
    {
      label: t('专长'),
      prop: 'specialty',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
      required: true,
    },
    {
      label: t('简介'),
      prop: 'introduction',
      component: { name: 'el-input', props: { type: 'textarea', rows: 4 } },
      span: 24,
      required: true,
    },
    {
      label: t('头像'),
      prop: 'avatar',
      component: { name: 'cl-upload' },
      span: 12,
      required: true,
    },
    {
      label: t('状态'),
      prop: 'status',
      value: dict.getByLabel('base-status', '启用'),
      component: { name: 'el-radio-group', options: dict.get('base-status') },
      span: 12,
      required: true,
    },
  ],
})

// 监听医院ID变化
watch(
  () => Upsert.value?.form.hospitalId,
  (val) => {
    // 清空科室选择
    Upsert.value?.setForm('departmentId', undefined)
    // 显示/隐藏科室选择
    if (val && Upsert.value?.mode === 'add') {
      Upsert.value?.showItem('departmentId')
    }
    else {
      Upsert.value?.hideItem('departmentId')
    }
  },
)

// cl-table
const Table = useTable({
  columns: [
    { type: 'selection' },
    { label: t('医生 ID'), prop: 'id', minWidth: 140 },
    { label: t('姓名'), prop: 'name', minWidth: 140 },
    { label: t('工号'), prop: 'jobCode', minWidth: 140 },
    { label: t('职称'), prop: 'title', minWidth: 140 },
    {
      label: t('专长'),
      prop: 'specialty',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    { label: t('医院 ID'), prop: 'hospitalId', minWidth: 140 },
    { label: t('医院'), prop: 'hospitalName', minWidth: 140 },
    { label: t('科室 ID'), prop: 'departmentId', minWidth: 140 },
    { label: t('科室'), prop: 'departmentName', minWidth: 140 },
    { label: t('简介'), prop: 'introduction', minWidth: 200, showOverflowTooltip: true },
    { label: t('头像'), prop: 'avatar', minWidth: 140, component: { name: 'cl-image' } },
    { label: t('状态'), prop: 'status', component: { name: 'cl-switch' }, minWidth: 100 },
    {
      label: t('创建时间'),
      prop: 'createTime',
      minWidth: 170,
      sortable: 'desc',
      component: { name: 'cl-date-text' },
    },
    {
      label: t('更新时间'),
      prop: 'updateTime',
      minWidth: 170,
      sortable: 'custom',
      component: { name: 'cl-date-text' },
    },
    { type: 'op', buttons: ['edit', 'delete'] },
  ],
})

// cl-search
const Search = useSearch({
  resetBtn: true,
  items: [
    {
      label: t('医生 ID'),
      prop: 'id',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('姓名'),
      prop: 'name',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('工号'),
      prop: 'jobCode',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('职称'),
      prop: 'title',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('医院 ID'),
      prop: 'hospitalId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('科室 ID'),
      prop: 'departmentId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('状态'),
      prop: 'status',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('base-status') },
    },
  ],
})

// cl-crud
const Crud = useCrud(
  {
    service: service.hospital.doctor,
  },
  (app) => {
    app.refresh()
  },
)

// 刷新
function refresh(params?: any) {
  Crud.value?.refresh(params)
}
</script>

<template>
  <cl-crud ref="Crud">
    <cl-row>
      <!-- 刷新按钮 -->
      <cl-refresh-btn />
      <!-- 新增按钮 -->
      <cl-add-btn />
      <!-- 删除按钮 -->
      <cl-multi-delete-btn />
      <!-- 导出按钮 -->
      <cl-export-btn :columns="Table?.columns" />
    </cl-row>

    <cl-row>
      <!-- 条件搜索 -->
      <cl-search ref="Search" />
    </cl-row>

    <cl-row>
      <!-- 数据表格 -->
      <cl-table ref="Table" />
    </cl-row>

    <cl-row>
      <cl-flex1 />
      <!-- 分页控件 -->
      <cl-pagination />
    </cl-row>

    <!-- 新增、编辑 -->
    <cl-upsert ref="Upsert" />
  </cl-crud>
</template>
