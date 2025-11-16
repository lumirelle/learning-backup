import type { Ref } from 'vue'
import { getCurrentInstance, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { service } from '../service'
import { useBrowser } from './browser'
import { useMitt } from './mitt'

// 创建一个响应式的 refs 对象，并提供设置 refs 的方法
export function useRefs() {
  const refs = reactive<{ [key: string]: any }>({})

  // 设置 refs 的方法，返回一个函数用于更新特定 ref
  function setRefs(name: string) {
    return (el: any) => {
      refs[name] = el
      return () => refs[name] // 返回一个函数用于获取当前 ref
    }
  }

  return { refs, setRefs }
}

// 获取指定名称的父组件实例，并将其暴露的属性赋值给传入的 Ref
export function useParent(name: string, r: Ref) {
  const instance = getCurrentInstance()

  if (instance) {
    let parent = instance.proxy?.$.parent

    // 遍历父组件链，直到找到匹配的组件名称
    while (parent && parent.type?.name !== name) {
      parent = parent?.parent
    }

    // 如果找到匹配的父组件，将其暴露的属性赋值给 Ref
    if (parent && parent.type.name === name) {
      r.value = parent.exposed
    }
  }

  return r
}

// 组合多个功能模块，返回一个包含服务、路由、事件总线等的对象
export function useCool() {
  return {
    service,
    route: useRoute(),
    router: useRouter(),
    mitt: useMitt(),
    ...useBrowser(),
    ...useRefs(),
  }
}

// 导出其他模块的功能
export * from './browser'
export * from './hmr'
export * from './mitt'
