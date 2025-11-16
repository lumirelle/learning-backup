<script lang="ts" setup>
import { useCrud, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import { useI18n } from 'vue-i18n'
import { useCool } from '/@/cool'

defineOptions({
  name: 'order-statistics',
})

const { service } = useCool()
const { t } = useI18n()

// cl-upsert
const Upsert = useUpsert({
  items: [
    {
      label: t('统计开始日期'),
      prop: 'startDate',
      component: {
        name: 'el-date-picker',
        props: { type: 'date', valueFormat: 'YYYY-MM-DD HH:mm:ss' },
      },
      span: 12,
      required: true,
    },
    {
      label: t('统计结束日期'),
      prop: 'endDate',
      component: {
        name: 'el-date-picker',
        props: { type: 'date', valueFormat: 'YYYY-MM-DD HH:mm:ss' },
      },
      span: 12,
      required: true,
    },
  ],
})

// cl-table
const Table = useTable({
  contextMenu: ['refresh'],
  columns: [
    { type: 'selection' },
    {
      label: t('统计开始日期'),
      prop: 'startDate',
      minWidth: 140,
      sortable: 'custom',
      component: {
        name: 'cl-date-text',
        props: { format: 'YYYY-MM-DD' },
      },
    },
    {
      label: t('统计截至日期'),
      prop: 'endDate',
      minWidth: 140,
      sortable: 'custom',
      component: {
        name: 'cl-date-text',
        props: { format: 'YYYY-MM-DD' },
      },
    },
    {
      label: t('订单总数'),
      prop: 'totalOrders',
      minWidth: 140,
      sortable: 'custom',
    },
    {
      label: t('总金额'),
      prop: 'totalAmount',
      minWidth: 140,
      sortable: 'custom',
    },
    {
      label: t('总实付金额'),
      prop: 'totalActualAmount',
      minWidth: 140,
      sortable: 'custom',
    },
    {
      label: t('退款数'),
      prop: 'refundCount',
      minWidth: 140,
      sortable: 'custom',
    },
    {
      label: t('完成数'),
      prop: 'completedCount',
      minWidth: 140,
      sortable: 'custom',
    },
    {
      label: t('取消数'),
      prop: 'cancelledCount',
      minWidth: 140,
      sortable: 'custom',
    },
    {
      label: t('支付订单数'),
      prop: 'paidOrders',
      minWidth: 140,
      sortable: 'custom',
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
  ],
})

// cl-search
const Search = useSearch()

// cl-crud
const Crud = useCrud(
  {
    service: service.order.statistics,
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
