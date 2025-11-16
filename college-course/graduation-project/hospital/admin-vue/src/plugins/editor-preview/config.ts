import type { ModuleConfig } from '/@/cool'

export default (): ModuleConfig => {
  return {
    enable: true,
    components: [() => import('./components/preview.vue')],

    label: '编辑器内容预览',
    description: '基于 monaco、wang 等编辑器的内容预览组件',
    author: 'COOL',
    version: '1.0.1',
    updateTime: '2024-02-27',
    demo: [
      {
        name: '基础用法',
        component: () => import('./demo/base.vue'),
      },
    ],
  }
}
