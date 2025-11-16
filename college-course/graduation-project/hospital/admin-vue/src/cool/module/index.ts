import type { Module } from '../types'
import { ctx } from 'virtual:ctx'
import { hmr } from '../hooks'

// 获取模块列表，若不存在则初始化为空数组
const list: Module[] = hmr.getData('modules', [])

// 定义模块对象
const module = {
  // 模块列表
  list,

  // 模块目录
  dirs: ctx.modules,

  // 请求对象，初始化为已解决的 Promise
  req: Promise.resolve(),

  // 根据名称获取模块
  get(name: string): Module {
    // 使用 find 方法查找模块，假设模块名称是唯一的
    return this.list.find(e => e.name == name)!
  },

  // 获取模块的配置选项
  config(name: string) {
    // 如果模块存在，返回其配置选项，否则返回空对象
    return this.get(name).options || {}
  },

  // 添加新模块到列表中
  add(data: Module) {
    this.list.push(data)
  },

  // 返回请求对象
  wait() {
    return this.req
  },
}

// 导出模块对象
export { module }
