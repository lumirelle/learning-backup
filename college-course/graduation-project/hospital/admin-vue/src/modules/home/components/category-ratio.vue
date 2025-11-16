<script lang="ts" setup>
import { useDark } from '@vueuse/core'
import { computed, reactive } from 'vue'

const isDark = useDark()

const textColor = computed(() => (isDark.value ? '#f1f1f9' : '#000'))

const chartOption = reactive({
  legend: {
    top: 'bottom',
    textStyle: {
      color: textColor,
    },
  },
  grid: {
    top: 0,
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)',
  },
  series: [
    {
      type: 'pie',
      radius: ['35%', '50%'],
      avoidLabelOverlap: false,
      padAngle: 5,
      itemStyle: {
        borderRadius: 6,
      },
      label: {
        color: textColor,
      },
      data: [
        { value: 387, name: '基本套餐' },
        { value: 314, name: '中级套餐' },
        { value: 253, name: '高级套餐' },
        { value: 198, name: 'VIP套餐' },
      ],
    },
  ],
})
</script>

<template>
  <div class="card">
    <div class="card__header">
      <span class="label">{{ $t('类别占比') }}</span>
    </div>

    <div class="card__container">
      <v-chart :option="chartOption" autoresize />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.card {
  &__container {
    height: 456px;
  }

  .echarts {
    height: calc(100% - 50px);
  }
}
</style>
