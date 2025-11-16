import { keys, orderBy } from 'lodash-es'
import { module } from '../module'

export async function createModules() {
  // 加载 uni_modules 插件
  const files: any = import.meta.glob('/uni_modules/cool-*/config.ts', {
    eager: true,
  })

  const modules = orderBy(
    keys(files).map((k) => {
      const [, , name] = k.split('/')

      return {
        name,
        value: files[k]?.default,
      }
    }),
    'order',
    'desc',
  )

  for (const i in modules) {
    const { name, value } = modules[i]
    const data = value ? value() : undefined

    // 添加模块
    module.add({
      name,
      ...data,
    })

    // 触发加载事件
    if (data) {
      await data.onLoad?.(data.options)
    }
  }
}
