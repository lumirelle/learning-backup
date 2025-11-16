import type { Merge, ModuleConfig } from '/@/cool'
// npm
import { Crud, locale } from '@cool-vue/crud'
import { Plugins } from './plugins'

import '@cool-vue/crud/dist/index.css'
import { config } from '/@/config'

// 调试、自定义crud
// import { Crud, locale } from '/~/crud/src';
// import '/~/crud/src/static/index.scss';

export default (): Merge<ModuleConfig, CrudOptions> => {
  return {
    order: 99,

    // 组件全注册
    components: Object.values(import.meta.glob('./components/**/*.{vue,tsx}')),

    // 配置参数，具体配置点 CrudOptions 查看
    options: {
      style: {
        table: {
          // 插件列表
          plugins: [],

          // 右键菜单，为空则关闭
          // contextMenu: []
        },
        form: {
          labelPosition: 'top',
          // 插件列表
          plugins: [
            // 自动聚焦插件
            Plugins.Form.setFocus(),
          ],
        },
        search: {
          // 插件列表
          plugins: [
            // 自动添加搜索组件
            Plugins.Search.setAuto(),
          ],
        },
      },
      dict: {
        // 排序字段
        sort: {
          prop: 'order',
          order: 'sort',
        },
        // 按钮及提示文案
        label: locale[config.i18n.locale],
      },
    },

    // 安装
    install: Crud.install,

    label: 'CRUD',
    description: '快速增删改查及一系列辅助组件',
    author: 'COOL',
    version: '1.1.2',
    updateTime: '2024-12-31',
    doc: 'https://vue.cool-admin.com/src/guide/plugins/crud.html',
    demo: '/demo/crud',
  }
}
