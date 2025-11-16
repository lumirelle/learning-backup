import type { ModuleConfig } from '/@/cool'
import { usePlugin } from './hooks'

export default (): ModuleConfig => {
  return {
    options: {
      index: 'https://cool-js.com',
      api: 'https://service.cool-js.com/api',
    },
    onLoad() {
      const { register } = usePlugin()
      register()
    },
  }
}
