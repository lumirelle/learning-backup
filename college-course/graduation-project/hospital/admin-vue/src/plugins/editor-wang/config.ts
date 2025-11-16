import type { ModuleConfig } from '/@/cool'

export default (): ModuleConfig => {
  return {
    enable: true,
    components: [() => import('./components/wang.vue')],

    label: 'Wang 编辑器',
    description: '基于 wangEditor 封装的富文本编辑器',
    author: 'COOL',
    version: '1.0.0',
    updateTime: '2024-02-01',
    demo: [
      {
        name: '基础用法',
        component: () => import('./demo/base.vue'),
      },
    ],
    doc: 'https://www.wangeditor.com',
  }
}
