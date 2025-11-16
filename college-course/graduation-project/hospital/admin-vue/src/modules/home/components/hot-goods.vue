<script lang="ts" setup>
import { useCrud, useTable } from '@cool-vue/crud'
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

// 类型
const type = ref('day')

// 选项
const options = reactive({
  type: [
    {
      label: t('今日'),
      value: 'day',
    },
    {
      label: t('本周'),
      value: 'week',
    },
    {
      label: t('本月'),
      value: 'month',
    },
    {
      label: t('全年'),
      value: 'year',
    },
  ],
})

const Crud = useCrud(
  {
    service: {
      page() {
        return Promise.resolve({
          list: [
            {
              keyWord: '普通套餐',
              count: 983,
              ud: 5,
              launchDate: '2023-01-01',
              price: 299,
            },
            {
              keyWord: '中级套餐',
              count: 763,
              ud: -3,
              launchDate: '2023-02-15',
              price: 199,
            },
            {
              keyWord: '高级套餐',
              count: 328,
              ud: 7,
              launchDate: '2023-03-10',
              price: 399,
            },
            {
              keyWord: 'VIP套餐',
              count: 144,
              ud: 4,
              launchDate: '2023-04-05',
              price: 999,
            },
          ],
        })
      },
    },
  },
  (app) => {
    app.refresh()
  },
)

const Table = useTable({
  autoHeight: false,
  contextMenu: ['order-asc', 'order-desc'],
  columns: [
    {
      label: t('排名'),
      type: 'index',
      width: 60,
    },
    {
      label: t('商品名称'),
      prop: 'keyWord',
      minWidth: 120,
    },
    {
      label: t('商品金额'),
      prop: 'price',
      minWidth: 100,
    },
    {
      label: t('下单次数'),
      prop: 'count',
      minWidth: 100,
      sortable: true,
    },
    {
      label: t('日涨幅'),
      prop: 'ud',
      sortable: true,
      minWidth: 100,
    },
    {
      label: t('上架时间'),
      prop: 'launchDate',
      minWidth: 120,
    },
  ],
})
</script>

<template>
  <div class="card">
    <div class="card__header">
      <span class="label">{{ $t('热门套餐排行') }}</span>

      <cl-select-button v-model="type" :options="options.type" small />
    </div>

    <div class="card__container">
      <cl-crud ref="Crud" padding="0">
        <cl-table ref="Table" />
      </cl-crud>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.card {
  padding-bottom: 20px;
}
</style>
