<script lang="ts" setup>
import { useCrud, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'order-info',
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

// cl-upsert
const Upsert = useUpsert({
  items: [
    {
      label: t('支付方式'),
      prop: 'payType',
      component: {
        name: 'el-select',
        options: dict.get('pay-type'),
        props: { clearable: true },
      },
      span: 24,
      required: true,
    },
    // 备注
    {
      label: t('备注（支付的订单号或其他证明）'),
      prop: 'remark',
      component: { name: 'el-input', props: { type: 'textarea', rows: 4 } },
      span: 24,
      required: true,
    },
  ],
  onSubmit: (data, { next }) => {
    next({
      ...data,
      status: dict.getByLabel('order-status', '已支付') || 1,
    })
  },
})

// cl-table
const Table = useTable({
  contextMenu: [
    'refresh',
    (row) => {
      return {
        label: t('支付'),
        type: 'primary',
        hidden: row.status !== (dict.getByLabel('order-status', '待支付') || 0),
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
    { label: t('订单 ID'), prop: 'id', minWidth: 140 },
    { label: t('编号'), prop: 'orderNumber', minWidth: 140 },
    { label: t('套餐ID'), prop: 'mealId', minWidth: 140 },
    { label: t('套餐'), prop: 'mealName', minWidth: 140 },
    { label: t('患者ID'), prop: 'patientId', minWidth: 140 },
    { label: t('患者名称'), prop: 'patientName', minWidth: 140 },
    {
      label: t('总金额'),
      prop: 'totalAmount',
      minWidth: 140,
      sortable: 'custom',
    },
    { label: t('优惠金额'), prop: 'discountAmount', minWidth: 140 },
    { label: t('实付金额'), prop: 'actualAmount', minWidth: 140 },
    { label: t('就诊时间'), prop: 'visitTime', minWidth: 140 },
    { label: t('状态'), prop: 'status', minWidth: 120, dict: dict.get('order-status') },
    { label: t('支付方式'), prop: 'payType', minWidth: 120, dict: dict.get('pay-type') },
    { label: t('支付时间'), prop: 'payTime', minWidth: 160 },
    {
      label: t('验证码'),
      prop: 'verifyCode',
      minWidth: 140,
    },
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
    {
      type: 'op',
      buttons: ({ scope }) => [
        {
          label: t('支付'),
          type: 'primary',
          hidden: scope.row.status !== (dict.getByLabel('order-status', '待支付') || 0),
          onClick: () => {
            Upsert.value?.edit({
              ...scope.row,
            })
          },
        },
      ],
    },
  ],
})

// cl-search
const Search = useSearch({
  resetBtn: true,
  items: [
    {
      label: t('订单 ID'),
      prop: 'id',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('编号'),
      prop: 'orderNumber',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('套餐 ID'),
      prop: 'mealId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('患者 ID'),
      prop: 'patientId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('支付方式'),
      prop: 'payType',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('pay-type') },
    },
    {
      label: t('状态'),
      prop: 'status',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('order-status') },
    },
  ],
})

// cl-crud
const Crud = useCrud(
  {
    service: service.order.info,
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

    <!-- 新增 -->
    <cl-upsert ref="Upsert" />
  </cl-crud>
</template>
