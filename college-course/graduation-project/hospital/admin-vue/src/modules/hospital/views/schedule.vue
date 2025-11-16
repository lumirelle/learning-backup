<script lang="ts" setup>
import { useCrud, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import { useI18n } from 'vue-i18n'
import DepartmentSelect from '/$/hospital/components/department-select.vue'
import DoctorSelect from '/$/hospital/components/doctor-select.vue'
import { useCool } from '/@/cool'

defineOptions({
  name: 'hospital-schedule',
})

const { service } = useCool()
const { t } = useI18n()

// cl-upsert
const Upsert = useUpsert({
  items: [
    {
      label: t('选择医生（关联医生）'),
      prop: 'doctorId',
      component: { vm: DoctorSelect },
      span: 12,
    },
    {
      label: t('选择科室（关联科室）'),
      prop: 'departmentId',
      component: { vm: DepartmentSelect },
      span: 12,
    },
    {
      label: t('排班日期'),
      prop: 'scheduleDate',
      component: {
        name: 'el-date-picker',
        props: { type: 'date', valueFormat: 'YYYY-MM-DD' },
      },
      span: 12,
      required: true,
    },
    {
      label: t('时段（示例：08:00-12:00）'),
      prop: 'timeSlot',
      component: { name: 'el-input', props: { clearable: true } },
      span: 12,
      required: true,
    },
    {
      label: t('号源总数'),
      prop: 'totalCapacity',
      hook: 'number',
      component: { name: 'el-input-number', props: { min: 0 } },
      span: 12,
      required: true,
    },
    {
      label: t('已约数量'),
      prop: 'bookedCapacity',
      hook: 'number',
      component: { name: 'el-input-number', props: { min: 0 } },
      span: 12,
      required: true,
    },
    {
      label: t('状态'),
      prop: 'status',
      component: { name: 'el-input', props: { clearable: true } },
      span: 12,
      required: true,
    },
  ],
})

// cl-table
const Table = useTable({
  columns: [
    { type: 'selection' },
    {
      label: t('排班日期'),
      prop: 'scheduleDate',
      minWidth: 140,
      sortable: 'custom',
      component: {
        name: 'cl-date-text',
        props: { format: 'YYYY-MM-DD' },
      },
    },
    {
      label: t('时段（示例：08:00-12:00）'),
      prop: 'timeSlot',
      minWidth: 120,
    },
    {
      label: t('号源总数'),
      prop: 'totalCapacity',
      minWidth: 140,
      sortable: 'custom',
    },
    {
      label: t('已约数量'),
      prop: 'bookedCapacity',
      minWidth: 140,
      sortable: 'custom',
    },
    { label: t('状态'), prop: 'status', minWidth: 120 },
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
const Search = useSearch()

// cl-crud
const Crud = useCrud(
  {
    service: service.hospital.schedule,
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
      <cl-flex1 />
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
