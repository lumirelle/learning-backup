<script lang="ts" setup>
import { range } from 'lodash-es'
import { onMounted, reactive, ref } from 'vue'
import { useCool } from '/@/cool'

const { service } = useCool()

const num = ref(0)
const totalComplaintsThisYear = reactive(range(12).map(() => 0))

const chartOption = reactive({
  grid: {
    left: 0,
    top: 1,
    right: 0,
    bottom: 0,
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    axisLine: {
      show: false,
    },
    data: [
      '00:00',
      '2:00',
      '4:00',
      '6:00',
      '8:00',
      '10:00',
      '12:00',
      '14:00',
      '16:00',
      '18:00',
      '20:00',
      '22:00',
    ],
  },
  yAxis: {
    type: 'value',
    splitLine: {
      show: false,
    },
    axisTick: {
      show: false,
    },
    axisLine: {
      show: false,
    },
    axisLabel: {
      show: false,
    },
  },
  series: [
    {
      type: 'line',
      smooth: true,
      showSymbol: false,
      symbol: 'circle',
      symbolSize: 6,
      data: totalComplaintsThisYear,
      itemStyle: {
        color: '#4165d7',
      },
      lineStyle: {
        width: 2,
      },
    },
  ],
})

onMounted(() => {
  service.feedback.complaint.countUser().then((res) => {
    num.value = res
  })
  service.feedback.complaint.countThisYear().then((res) => {
    totalComplaintsThisYear.forEach((item, index) => {
      totalComplaintsThisYear[index] = res[index]
    })
  })
})
</script>

<template>
  <div class="count-views">
    <div class="card">
      <div class="card__header">
        <span class="label">{{ $t('投诉客单量') }}</span>
        <cl-svg name="trend" class="icon" />
      </div>

      <div class="card__container">
        <v-chart :option="chartOption" autoresize />
      </div>

      <div class="card__footer">
        <span class="mr-2">{{ $t('投诉客户数') }}</span>
        <span>{{ num }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.count-views {
  .card {
    .echarts {
      height: 50px;
      width: 100%;
    }

    &__container {
      padding: 0;
    }

    &__footer {
      border-top: 0;
    }
  }
}
</style>
