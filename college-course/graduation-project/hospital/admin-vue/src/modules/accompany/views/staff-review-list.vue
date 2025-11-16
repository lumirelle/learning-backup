<script lang="ts" setup>
import { useCrud, useForm, useTable } from '@cool-vue/crud'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'accompany-staff-review-list',
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

// 详情表单
const Info = useForm()

// 打开详情
function openInfo(row: any) {
  Info.value?.open({
    title: '审核详情',
    width: '600px',
    props: {
      labelWidth: '120px',
    },
    items: [
      {
        label: '陪诊员姓名',
        prop: 'staffName',
        component: {
          name: 'el-input',
          props: {
            disabled: true,
          },
        },
      },
      {
        label: '审核前等级',
        prop: 'oldLevel',
        component: {
          name: 'el-input',
          props: {
            disabled: true,
          },
        },
        hook: {
          bind: (value) => {
            return dict.get('acc-staff-level').value.find((e: any) => e.value === value)?.label || value
          },
        },
      },
      {
        label: '审核后等级',
        prop: 'level',
        component: {
          name: 'el-input',
          props: {
            disabled: true,
          },
        },
        hook: {
          bind: (value) => {
            return dict.get('acc-staff-level').value.find((e: any) => e.value === value)?.label || value
          },
        },
      },
      {
        label: '审核意见',
        prop: 'remark',
        component: {
          name: 'el-input',
          props: {
            type: 'textarea',
            rows: 4,
            disabled: true,
          },
        },
      },
      {
        label: '审核时间',
        prop: 'createTime',
        component: {
          name: 'el-input',
          props: {
            disabled: true,
          },
        },
      },
    ],
    form: row,
  })
}

// cl-table
const Table = useTable({
  columns: [
    { label: t('记录 ID'), prop: 'id', minWidth: 100 },
    { label: t('陪诊员 ID'), prop: 'staffId', minWidth: 100 },
    { label: t('陪诊员姓名'), prop: 'staffName', minWidth: 120 },
    {
      label: t('审核前等级'),
      prop: 'oldLevel',
      dict: dict.get('acc-staff-level'),
      minWidth: 120,
    },
    {
      label: t('审核后等级'),
      prop: 'level',
      dict: dict.get('acc-staff-level'),
      minWidth: 120,
    },
    {
      label: t('审核意见'),
      prop: 'remark',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    {
      label: t('审核时间'),
      prop: 'createTime',
      minWidth: 170,
      sortable: 'desc',
      component: { name: 'cl-date-text' },
    },
    {
      type: 'op',
      buttons: [
        {
          label: '详情',
          onClick: ({ scope }) => {
            openInfo(scope.row)
          },
        },
      ],
      width: 100,
    },
  ],
})

// cl-crud
const Crud = useCrud(
  {
    service: service.accompany.review,
  },
  (app) => {
    app.refresh()
  },
)
</script>

<template>
  <cl-crud ref="Crud">
    <cl-row>
      <!-- 刷新按钮 -->
      <cl-refresh-btn />
      <cl-flex1 />
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

    <!-- 详情表单 -->
    <cl-form ref="Info" />
  </cl-crud>
</template>

<style lang="scss" scoped>
.cl-crud {
  height: 100%;
}
</style>
