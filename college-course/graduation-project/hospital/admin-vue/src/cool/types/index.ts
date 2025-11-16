import type { App, Component, Directive } from 'vue'
import type { RouteRecordRaw, Router as VueRouter } from 'vue-router'

export declare type Merge<A, B> = Omit<A, keyof B> & B

export declare interface ModuleConfig {
  enable?: boolean
  name?: string
  label?: string
  description?: string
  order?: number
  version?: string
  logo?: string
  author?: string
  updateTime?: string
  demo?: { name: string, component: Component }[] | string
  doc?: string
  ignore?: {
    NProgress?: string[]
    token?: string[]
  }
  options?: {
    [key: string]: any
  }
  toolbar?: {
    order?: number
    pc?: boolean
    h5?: boolean
    component: any
  }
  index?: {
    component: any
  }
  components?: Component[]
  views?: RouteRecordRaw[]
  pages?: (RouteRecordRaw & { isPage?: boolean })[]
  install?(app: App, options?: any): any
  onLoad?(events: {
    hasToken(cb: () => Promise<any> | void): Promise<any> | void
    [key: string]: any
  }): Promise<{ [key: string]: any }> | Promise<void> | void
}

export declare interface Module extends ModuleConfig {
  name: string
  options: {
    [key: string]: any
  }
  value?: any
  services?: { path: string, value: any }[]
  directives?: { name: string, value: Directive }[]
  [key: string]: any
}

export declare interface Router extends VueRouter {
  find(path: string): { route: RouteRecordRaw, isReg: boolean }
  del(name: string): void
  clear(): void
  append(data: any | any[]): void
}
