<script lang="ts" setup>
import { useCrud, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import { useI18n } from 'vue-i18n'
import OrderInfoSelect from '../components/order-info-select.vue'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'order-order-detail',
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

// cl-upsert
const Upsert = useUpsert<Eps.OrderDetailEntity>({
  items: [
    () => {
      return {
        label: t('选择订单'),
        prop: 'orderId',
        hidden: Upsert.value?.mode === 'update',
        component: { vm: OrderInfoSelect },
        span: 12,
        required: true,
      }
    },
    () => {
      return {
        label: t('服务时长（分钟）'),
        prop: 'serviceMinutes',
        hidden: Upsert.value?.mode === 'update',
        component: { name: 'el-input-number', props: { min: 0 } },
        span: 12,
        required: true,
      }
    },
    () => {
      return {
        label: t('客户评价（0~5分）'),
        prop: 'customerEvaluation',
        hidden: Upsert.value?.mode === 'update',
        component: { name: 'el-input-number', props: { min: 0, max: 5 } },
        span: 12,
      }
    },
    {
      label: t('售后状态'),
      prop: 'afterSaleStatus',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('order-after-sale-status') },
      span: 12,
    },
  ],
})

// cl-table
const Table = useTable<Eps.OrderDetailEntity>({
  contextMenu: [
    'refresh',
    (row) => {
      return {
        label: t('更新售后状态'),
        type: 'primary',
        callback: (done) => {
          Upsert.value?.edit({
            ...row,
          })
          done()
        },
      }
    },
  ],
  columns: [
    { type: 'selection' },
    { label: t('订单ID'), prop: 'orderId', minWidth: 140 },
    {
      label: t('服务时长'),
      prop: 'serviceMinutes',
      minWidth: 140,
      sortable: 'custom',
      formatter: row => row.serviceMinutes ? `${row.serviceMinutes} 分钟` : '',
    },
    {
      label: t('客户评价'),
      prop: 'customerEvaluation',
      minWidth: 140,
      sortable: 'custom',
      formatter: row => row.customerEvaluation ? `${row.customerEvaluation} 分` : '',
    },
    {
      label: t('售后状态'),
      prop: 'afterSaleStatus',
      minWidth: 140,
      sortable: 'custom',
      dict: dict.get('order-after-sale-status'),
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
    {
      type: 'op',
      buttons: () => {
        return [
          {
            label: t('更新售后状态'),
            type: 'primary',
            onClick: ({ scope }) => {
              Upsert.value?.edit({
                ...scope.row,
              })
            },
          },
        ]
      },
    },
  ],
})

// cl-search
const Search = useSearch({
  resetBtn: true,
  items: [
    {
      label: t('订单ID'),
      prop: 'orderId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('售后状态'),
      prop: 'afterSaleStatus',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('order-after-sale-status') },
    },
  ],
})

// cl-crud
const Crud = useCrud(
  {
    service: service.order.detail,
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
