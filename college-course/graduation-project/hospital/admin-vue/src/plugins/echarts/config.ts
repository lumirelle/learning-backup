import type { ModuleConfig } from '/@/cool'
import VueECharts from 'vue-echarts'

export default (): ModuleConfig => {
  return {
    enable: true,
    order: 99,
    install(app) {
      app.component('v-chart', VueECharts)
      import('echarts')
    },

    label: 'ECharts 图表',
    description: 'echarts、vue-echarts 配置',
    author: 'COOL',
    version: '1.0.0',
    updateTime: '2024-07-22',
    doc: 'https://echarts.apache.org/examples/zh/index.html',
  }
}
