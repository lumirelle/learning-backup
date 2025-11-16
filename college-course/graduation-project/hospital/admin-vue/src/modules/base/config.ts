import type { ModuleConfig } from '/@/cool'
import { useStore } from './store'
import './static/css/index.scss'
import { config } from '/@/config'
import { t } from '/@/plugins/i18n'

export default (): ModuleConfig => {
  return {
    order: 99,
    ignore: {
      NProgress: [
        '/base/open/eps',
        '/base/comm/person',
        '/base/comm/permmenu',
        '/base/comm/upload',
        '/base/comm/uploadMode',
      ],
      token: ['/login', '/401', '/403', '/404', '/500', '/502'],
    },
    components: Object.values(import.meta.glob('./components/**/*.{vue,tsx}')),
    views: [
      {
        path: '/my/info',
        meta: {
          label: t('个人中心'),
        },
        component: () => import('./views/info.vue'),
      },
    ],
    pages: [
      {
        path: '/login',
        component: () => import('./pages/login/index.vue'),
      },
      ...['401', '403', '404', '500', '502'].map((code) => {
        return {
          path: `/${code}`,
          meta: {
            process: false,
          },
          component: () => import(`./pages/error/${code}.vue`),
        }
      }),
    ],
    install() {
      // 设置标题
      document.title = config.app.name

      // 设置加载文案
      const loading = document.querySelector('#Loading')

      if (loading) {
        loading.querySelector('.preload__name')!.innerHTML = config.app.name
        loading.querySelector('.preload__title')!.innerHTML = t('正在加载资源...')
        loading.querySelector('.preload__sub-title')!.innerHTML
          = t('初次加载资源可能需要较多时间，请耐心等待')
      }
    },
    async onLoad() {
      const { user, menu, app } = useStore()

      // token 事件
      async function hasToken(cb: () => Promise<any> | void) {
        if (cb) {
          app.addEvent('hasToken', cb)

          if (user.token) {
            await cb()
          }
        }
      }

      await hasToken(async () => {
        // 获取用户信息
        user.get()
        // 获取菜单权限
        await menu.get()
      })

      return {
        hasToken,
      }
    },
  }
}
