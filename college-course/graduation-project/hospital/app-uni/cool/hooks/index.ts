import { router } from '../router'
import { service } from '../service'
import { upload } from '../upload'
import { storage } from '../utils'
import { useRefs } from './comm'

export function useCool() {
  return {
    router,
    service,
    upload,
    storage,
    ...useRefs(),
  }
}

export * from './app'
export * from './comm'
export * from './hmr'
export * from './pager'
export * from './wx'
