<script setup lang="ts">
import { computed, ref, useModel } from 'vue'
import { useI18n } from 'vue-i18n'
import { CrudProps } from '/#/crud'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'order-info-select',
})

const props = defineProps({
  ...CrudProps,
  modelValue: null,
  multiple: Boolean,
  payOrderOnly: Boolean,
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

const value = useModel(props, 'modelValue')

const customService = computed(() => {
  return {
    page: (params: any) => {
      const queryParams = { ...params }
      if (props.scope?.patientId) {
        queryParams.patientId = props.scope.patientId
      }
      if (props.scope?.mealId) {
        queryParams.mealId = props.scope.mealId
      }
      if (props.payOrderOnly) {
        queryParams.payOrderOnly = true
      }
      return service.order.info.page(queryParams)
    },
  }
})

const columns = ref([
  { label: t('订单编号'), prop: 'orderNumber', minWidth: 160 },
  {
    label: t('状态'),
    prop: 'status',
    minWidth: 120,
    dict: dict.get('order-status'),
  },
  { label: t('总金额'), prop: 'totalAmount', minWidth: 120 },
  { label: t('实付金额'), prop: 'actualAmount', minWidth: 120 },
  { label: t('优惠金额'), prop: 'discountAmount', minWidth: 120 },
  {
    label: t('支付方式'),
    prop: 'payType',
    minWidth: 120,
    dict: dict.get('pay-type'),
  },
  { label: t('支付时间'), prop: 'payTime', minWidth: 160 },
  { label: t('备注'), prop: 'remark', minWidth: 160 },
  { label: t('创建时间'), prop: 'createTime', minWidth: 160 },
])

// 定义搜索项
const searchItems = ref([
  {
    label: t('订单编号'),
    prop: 'orderNumber',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入订单编号'),
      },
    },
  },
  {
    label: t('状态'),
    prop: 'status',
    component: {
      name: 'el-select',
      props: {
        clearable: true,
        placeholder: t('请选择订单状态'),
        dict: dict.get('order-status'),
      },
    },
  },
  {
    label: t('支付方式'),
    prop: 'payType',
    component: {
      name: 'el-select',
      props: {
        clearable: true,
        placeholder: t('请选择支付方式'),
        dict: dict.get('pay-type'),
      },
    },
  },
])
</script>

<template>
  <cl-select-table
    v-model="value"
    :title="props.payOrderOnly ? t('选择已支付订单') : t('选择订单')"
    :service="customService"
    :columns="columns"
    :multiple="multiple"
    :search-items="searchItems"
    picker-type="text"
    :dict="{ text: 'orderNumber' }"
  />
</template>
