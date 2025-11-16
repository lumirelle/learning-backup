import type { Module } from '../types'
import { hmr } from '../hooks'

// 模块列表
const list: Module[] = hmr.getData('modules', [])

// 模块对象
const module = {
  list,
  get(name: string): Module {
    return this.list.find(e => e.name == name)!
  },
  config(name: string) {
    return this.get(name).options || {}
  },
  add(data: Module) {
    this.list.push(data)
  },
}

export { module }
