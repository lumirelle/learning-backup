<script lang="ts" setup>
import { useCrud, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import DoctorSelect from '/$/hospital/components/doctor-select.vue'
import HospitalSelect from '/$/hospital/components/hospital-select.vue'
import PatientSelect from '/$/patient/components/patient-select.vue'
import { useCool } from '/@/cool'

defineOptions({
  name: 'medical-record',
})

const { service } = useCool()
const { t } = useI18n()

// cl-upsert
const Upsert = useUpsert({
  items: [
    () => {
      return {
        label: t('选择患者'),
        prop: 'patientId',
        hidden: Upsert.value?.mode !== 'add',
        component: { vm: PatientSelect },
        required: true,
        span: 24,
      }
    },
    // 回显患者使用
    () => {
      return {
        label: t('患者 ID'),
        prop: 'patientId',
        hidden: Upsert.value?.mode === 'add',
        component: { name: 'el-input', props: { clearable: true } },
        span: 12,
      }
    },
    () => {
      return {
        label: t('患者姓名'),
        prop: 'patientName',
        hidden: Upsert.value?.mode === 'add',
        component: { name: 'el-input', props: { clearable: true } },
        span: 12,
      }
    },
    {
      label: t('就诊日期'),
      prop: 'visitDate',
      component: {
        name: 'el-date-picker',
        props: { type: 'date', valueFormat: 'YYYY-MM-DD HH:mm:ss' },
      },
      required: true,
      span: 24,
    },
    () => {
      return {
        label: t('医院'),
        prop: 'hospitalId',
        hidden: Upsert.value?.mode !== 'add',
        component: { vm: HospitalSelect },
        required: true,
        span: 24,
      }
    },
    // 回显医院使用
    () => {
      return {
        label: t('医院 ID'),
        prop: 'hospitalId',
        hidden: Upsert.value?.mode === 'add',
        component: { name: 'el-input', props: { clearable: true } },
        span: 12,
      }
    },
    () => {
      return {
        label: t('医院名称'),
        prop: 'hospitalName',
        hidden: Upsert.value?.mode === 'add',
        component: { name: 'el-input', props: { clearable: true } },
        span: 12,
      }
    },
    () => {
      return {
        label: t('医生'),
        prop: 'doctorId',
        hidden: true,
        component: {
          vm: DoctorSelect,
          props: {
            // 传入医院ID作为过滤条件
            hospitalId: Upsert.value?.form.hospitalId,
          },
        },
        required: true,
        span: 24,
      }
    },
    // 回显医生使用
    () => {
      return {
        label: t('医生 ID'),
        prop: 'doctorId',
        hidden: Upsert.value?.mode === 'add',
        component: { name: 'el-input', props: { clearable: true } },
        span: 12,
      }
    },
    () => {
      return {
        label: t('医生姓名'),
        prop: 'doctorName',
        hidden: Upsert.value?.mode === 'add',
        component: { name: 'el-input', props: { clearable: true } },
        span: 12,
      }
    },
    {
      label: t('诊断结果'),
      prop: 'diagnosis',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
      required: true,
    },
    {
      label: t('处方内容'),
      prop: 'prescription',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
      required: true,
    },
    {
      label: t('费用'),
      prop: 'cost',
      hook: 'number',
      component: { name: 'el-input-number', props: { min: 0.01, precision: 2 } },
      required: true,
      span: 12,
    },
  ],
})

// 监听医院ID变化
watch(
  () => Upsert.value?.form.hospitalId,
  (val) => {
    // 清空医生选择
    Upsert.value?.setForm('doctorId', undefined)
    // 显示/隐藏医生选择
    if (val && Upsert.value?.mode === 'add') {
      Upsert.value?.showItem('doctorId')
    }
    else {
      Upsert.value?.hideItem('doctorId')
    }
  },
)

// cl-table
const Table = useTable({
  contextMenu: ['refresh', 'info', 'order-desc', 'order-asc'],
  columns: [
    { type: 'selection' },
    { label: t('就诊记录 ID'), prop: 'id', minWidth: 140 },
    { label: t('患者ID'), prop: 'patientId', minWidth: 140 },
    { label: t('患者姓名'), prop: 'patientName', minWidth: 140 },
    {
      label: t('就诊日期'),
      prop: 'visitDate',
      minWidth: 140,
      sortable: 'custom',
      component: {
        name: 'cl-date-text',
        props: { format: 'YYYY-MM-DD' },
      },
    },
    { label: t('医院 ID'), prop: 'hospitalId', minWidth: 140 },
    { label: t('医院名称'), prop: 'hospitalName', minWidth: 140 },
    { label: t('医生 ID'), prop: 'doctorId', minWidth: 140 },
    { label: t('医生姓名'), prop: 'doctorName', minWidth: 140 },
    {
      label: t('诊断结果'),
      prop: 'diagnosis',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    {
      label: t('处方内容'),
      prop: 'prescription',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    { label: t('费用'), prop: 'cost', minWidth: 140, sortable: 'custom' },
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
    { type: 'op', buttons: ['info'] },
  ],
})

// cl-search
const Search = useSearch({
  resetBtn: true,
  items: [
    {
      label: t('患者 ID'),
      prop: 'patientId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    // 暂不支持
    // {
    //   label: t('患者姓名'),
    //   prop: 'patientName',
    //   component: { name: 'el-input', props: { clearable: true } },
    // },
    {
      label: t('就诊日期'),
      prop: 'visitDate',
      component: { name: 'el-date-picker', props: { type: 'date', valueFormat: 'YYYY-MM-DD HH:mm:ss' } },
    },
    {
      label: t('医院 ID'),
      prop: 'hospitalId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    // {
    //   label: t('医院名称'),
    //   prop: 'hospitalName',
    //   component: { name: 'el-input', props: { clearable: true } },
    // },
    {
      label: t('医生 ID'),
      prop: 'doctorId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    // {
    //   label: t('医生姓名'),
    //   prop: 'doctorName',
    //   component: { name: 'el-input', props: { clearable: true } },
    // },
    {
      label: t('诊断结果'),
      prop: 'diagnosis',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('处方内容'),
      prop: 'prescription',
      component: { name: 'el-input', props: { clearable: true } },
    },
  ],
})

// cl-crud
const Crud = useCrud(
  {
    // service: service.<module name>.<controller name split by dot>
    service: service.patient.medical.record,
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
      <!-- 导出按钮 -->
      <cl-export-btn :columns="Table?.columns" />
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
