import { createSSRApp } from 'vue'
import App from './App.vue'
import { i18n } from './locale'
import './router'
import { bootstrap } from '/@/cool/bootstrap'

export function createApp() {
  const app = createSSRApp(App)
  app.use(i18n)

  bootstrap(app)

  return {
    app,
  }
}
