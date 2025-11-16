import type { ModuleConfig } from '/@/cool'
import { useDict } from './index'

export default (): ModuleConfig => {
  return {
    ignore: {
      NProgress: ['/dict/info/data'],
    },
    onLoad({ hasToken }) {
      const { dict } = useDict()
      hasToken(() => {
        dict.refresh()
      })
    },
  }
}
