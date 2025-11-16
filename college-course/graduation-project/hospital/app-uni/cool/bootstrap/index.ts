import type { App } from 'vue'
import { createPinia } from 'pinia'
import { createEps } from './eps'
import { createModules } from './modules'

export async function bootstrap(app: App) {
  // 状态共享存储
  app.use(createPinia())

  // 创建 EPS
  createEps()

  // 创建 uni_modules
  createModules()
}
