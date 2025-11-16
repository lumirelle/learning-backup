import type { ModuleConfig } from '/@/cool'
import messages from '@intlify/unplugin-vue-i18n/messages'
import { createI18n } from 'vue-i18n'
import { config } from '/@/config'

export default (): ModuleConfig => {
  return {
    order: 100,
    // Temporary disable i18n plugin, because we don't need it now
    // toolbar: {
    //   component: import('./components/switch.vue'),
    // },
    install(app) {
      const i18n = createI18n({
        legacy: false,
        globalInjection: true,
        locale: config.i18n.locale,
        fallbackLocale: 'zh-cn',
        messages,
      })

      app.use(i18n)
    },

    label: 'i18n',
    description: '国际化组件',
    author: 'COOL',
    version: '1.0.0',
    updateTime: '2025-01-11',
    doc: 'https://github.com/intlify/vue-i18n',
  }
}
