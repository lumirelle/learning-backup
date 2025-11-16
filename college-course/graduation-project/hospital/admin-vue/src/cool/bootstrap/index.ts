import type { App } from 'vue'
import { createPinia } from 'pinia'
import { router } from '../router'
import { Loading } from '../utils'
import { createEps } from './eps'
import { createModule } from './module'
import 'virtual:svg-register'

export async function bootstrap(app: App) {
  // pinia
  app.use(createPinia())

  // 路由
  app.use(router)

  // 模块
  const { eventLoop } = createModule(app)

  // eps
  createEps()

  // 加载
  Loading.set([eventLoop()])
}
