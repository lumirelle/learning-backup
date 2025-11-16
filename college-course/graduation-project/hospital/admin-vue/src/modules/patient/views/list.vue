<script lang="ts" setup>
import { useCrud, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import UserSelect from '/$/user/components/user-select.vue'
import { useCool } from '/@/cool'
import { phoneValidator } from '/@/cool/utils'

defineOptions({
  name: 'patient-list',
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

// cl-upsert
const Upsert = useUpsert({
  items: [
    {
      type: 'tabs',
      props: {
        type: 'card',
        labels: [
          {
            label: t('基本信息'),
            value: 'basic',
          },
          {
            label: t('健康档案'),
            value: 'health',
          },
        ],
      },
    },
    () => {
      return {
        label: t('选择关联用户'),
        prop: 'patientUserId',
        group: 'basic',
        hidden: Upsert.value?.mode === 'update',
        component: {
          vm: UserSelect,
          props: {
            role: dict.getByLabel('user-role', '患者'),
          },
        },
        required: true,
      }
    },
    {
      label: t('姓名'),
      prop: 'name',
      group: 'basic',
      component: { name: 'el-input', props: { clearable: true } },
      required: true,
      span: 12,
    },
    {
      label: t('电话'),
      prop: 'phone',
      group: 'basic',
      component: { name: 'el-input', props: { clearable: true, maxlength: 11 } },
      rules: { required: true, trigger: 'blur', validator: phoneValidator },
      span: 12,
    },
    {
      label: t('性别'),
      prop: 'gender',
      group: 'basic',
      value: dict.getByLabel('gender', '男'),
      component: { name: 'el-radio-group', options: dict.get('gender') },
      required: true,
      span: 12,
    },
    {
      label: t('类型'),
      prop: 'type',
      group: 'basic',
      value: dict.getByLabel('patient-type', '正常'),
      component: { name: 'el-radio-group', options: dict.get('patient-type') },
      required: true,
      span: 12,
    },
    {
      label: t('生日'),
      prop: 'birthday',
      group: 'basic',
      component: {
        name: 'el-date-picker',
        props: { type: 'date', valueFormat: 'YYYY-MM-DD HH:mm:ss' },
      },
      required: true,
      span: 12,
    },
    {
      label: t('居住地址'),
      prop: 'address',
      group: 'basic',
      component: { name: 'el-input', props: { clearable: true } },
      span: 12,
    },
    {
      label: t('病历号'),
      prop: 'medicalRecordNumber',
      group: 'basic',
      component: { name: 'el-input', props: { clearable: true } },
      required: true,
      span: 12,
    },
    {
      label: t('病史'),
      prop: 'medicalHistory',
      group: 'basic',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
      required: true,
    },
    {
      label: t('过敏史'),
      prop: 'allergyHistory',
      group: 'basic',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
      required: true,
    },
    {
      label: t('备注'),
      prop: 'remark',
      group: 'basic',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
    },
    {
      label: t('身高(cm)'),
      prop: 'height',
      group: 'health',
      hook: 'number',
      component: { name: 'el-input-number', props: { min: 0, max: 300 } },
    },
    {
      label: t('体重(kg)'),
      prop: 'weight',
      group: 'health',
      hook: 'number',
      component: { name: 'el-input-number', props: { min: 0, max: 300 } },
    },
    {
      label: t('收缩压(mmHg)'),
      prop: 'systolicPressure',
      group: 'health',
      hook: 'number',
      component: { name: 'el-input-number', props: { min: 0 } },
    },
    {
      label: t('舒张压(mmHg)'),
      prop: 'diastolicPressure',
      group: 'health',
      hook: 'number',
      component: { name: 'el-input-number', props: { min: 0 } },
    },
  ],
})

// cl-table
const Table = useTable({
  columns: [
    { type: 'selection' },
    { label: t('患者 ID'), prop: 'id', minWidth: 140 },
    { label: t('姓名'), prop: 'name', minWidth: 140 },
    { label: t('电话'), prop: 'phone', minWidth: 140 },
    {
      label: t('年龄'),
      prop: 'birthday',
      minWidth: 140,
      sortable: 'custom',
      formatter: (row: any) => {
        return row.birthday ? dayjs().diff(dayjs(row.birthday), 'year') : ''
      },
    },
    { label: t('性别'), prop: 'gender', minWidth: 120, dict: dict.get('gender') },
    { label: t('居住地址'), prop: 'address', minWidth: 200 },
    { label: t('类型'), prop: 'type', minWidth: 120, dict: dict.get('patient-type') },
    { label: t('病历号'), prop: 'medicalRecordNumber', minWidth: 200, showOverflowTooltip: true },
    {
      label: t('病史'),
      prop: 'medicalHistory',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    {
      label: t('过敏史'),
      prop: 'allergyHistory',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    {
      label: t('身高(cm)'),
      prop: 'height',
      minWidth: 140,
      sortable: 'custom',
    },
    {
      label: t('体重(kg)'),
      prop: 'weight',
      minWidth: 140,
      sortable: 'custom',
    },
    {
      label: t('收缩压(mmHg)'),
      prop: 'systolicPressure',
      minWidth: 150,
      sortable: 'custom',
    },
    {
      label: t('舒张压(mmHg)'),
      prop: 'diastolicPressure',
      minWidth: 150,
      sortable: 'custom',
    },
    { label: t('账户 ID'), prop: 'patientUserId', minWidth: 140 },
    { label: t('账户昵称'), prop: 'nickName', minWidth: 140 },
    {
      label: t('备注'),
      prop: 'remark',
      showOverflowTooltip: true,
      minWidth: 200,
    },
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
      prop: 'id',
      label: t('患者 ID'),
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      prop: 'name',
      label: t('姓名'),
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      prop: 'phone',
      label: t('电话'),
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      prop: 'address',
      label: t('居住地址'),
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      prop: 'type',
      label: t('类型'),
      component: { name: 'el-select', options: dict.get('patient-type'), props: { clearable: true } },
    },
    {
      prop: 'medicalRecordNumber',
      label: t('病历号'),
      component: { name: 'el-input', props: { clearable: true } },
    },
  ],
})

// cl-crud
const Crud = useCrud(
  {
    // service: service.<module name>.<controller name split by dot>
    service: service.patient.info,
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
