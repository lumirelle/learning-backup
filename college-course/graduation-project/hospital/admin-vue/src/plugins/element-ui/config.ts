import type { ModuleConfig } from '/@/cool'
import ElementPlus from 'element-plus'
import en from 'element-plus/es/locale/lang/en'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/theme-chalk/display.css'
// import fr from 'element-plus/es/locale/lang/fr';
// ...
import './css/index.scss'
import { config } from '/@/config'

export default (): ModuleConfig => {
  const lang = {
    'zh-cn': zhCn,
    en,
    // fr...
  }

  return {
    order: 100,
    install(app) {
      app.use(ElementPlus, {
        locale: lang[config.i18n.locale],
      })
    },

    label: 'Element Ui',
    description: 'Element Plus 变量、样式配置',
    author: 'COOL',
    version: '1.0.0',
    updateTime: '2024-07-22',
    doc: 'https://element-plus.org/zh-CN/component/overview.html',
  }
}
