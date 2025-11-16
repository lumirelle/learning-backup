<script lang="ts" setup>
import { useDark } from '@vueuse/core'
import dayjs from 'dayjs'
import { range } from 'lodash-es'
import { computed, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTheme } from '/#/theme'

const { t } = useI18n()
const isDark = useDark()
const theme = useTheme()

const tab = reactive({
  active: 'sales',

  list: [
    {
      label: t('销售金额'),
      value: 'sales',
    },
    {
      label: t('销售订单'),
      value: 'order',
    },
  ],
})

const chartOption = reactive({
  grid: {
    containLabel: true,
    left: '5%',
    right: '5%',
  },
  xAxis: {
    type: 'category',
    data: [] as string[],
    offset: 5,
    axisLine: {
      show: false,
    },
    axisTick: {
      show: false,
    },
  },
  yAxis: {
    type: 'value',
    offset: 20,
    splitLine: {
      show: false,
    },
    axisTick: {
      show: false,
    },
    axisLine: {
      show: false,
    },
  },
  tooltip: {
    trigger: 'axis',
    formatter: (comp: any) => {
      const name = tab.list.find(e => e.value === tab.active)?.label

      return `${name}：${comp[0]?.value || 0}`
    },
    axisPointer: {
      show: true,
      status: 'shadow',
      z: -1,
      type: 'shadow',
    },
    extraCssText: 'width:120px; white-space:pre-wrap',
  },
  series: [
    {
      barWidth: 25,
      type: 'bar',
      data: [] as number[],
      itemStyle: {
        color: computed(() => theme.color),
      },
    },
    {
      type: 'bar',
      barWidth: 25,
      xAxisIndex: 0,
      barGap: '-100%',
      data: [] as number[],
      itemStyle: {
        color: computed(() => (isDark.value ? '#f1f1f911' : '#f1f1f9')),
      },
      zlevel: -1,
    },
  ],
})

function refresh() {
  chartOption.xAxis.data = range(12).map((_, i) => i + 1 + '月')
  chartOption.series[0].data = range(12).map(() => Number.parseInt(String(Math.random() * 10000)))
  chartOption.series[1].data = range(12).map(() => 10000)
}

function onChange() {
  refresh()
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="card">
    <div class="card__header">
      <cl-select-button v-model="tab.active" :options="tab.list" @change="onChange" />

      <span class="year">{{ dayjs().year() + '年' }}</span>
    </div>

    <v-chart :option="chartOption" autoresize />
  </div>
</template>

<style lang="scss" scoped>
.card {
  &__header {
    padding: 10px 20px 10px 10px !important;

    .year {
      display: flex;
      align-items: center;
      font-size: 14px;
      line-height: 1;
      color: var(--el-text-color-primary);

      &::before {
        content: '';
        display: inline-block;
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background-color: var(--el-color-info);
        margin-right: 6px;
      }
    }
  }

  .echarts {
    height: 500px;
    width: 100%;
  }
}
</style>
