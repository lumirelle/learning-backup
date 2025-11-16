import { useEventListener } from '@vueuse/core'
import { reactive, watch } from 'vue'
import { getBrowser } from '../utils'

// 使用 reactive 创建一个响应式的浏览器对象
const browser = reactive(getBrowser())

// 存储屏幕变化事件的回调函数数组
const events: (() => void)[] = []

// 监听浏览器屏幕属性的变化
watch(
  () => browser.screen, // 监听的属性
  () => {
    // 当屏幕属性变化时，执行所有注册的回调函数
    events.forEach(ev => ev())
  },
)

// 监听窗口的 resize 事件，并更新浏览器对象
useEventListener(window, 'resize', () => {
  // 使用 Object.assign 更新响应式对象的属性
  Object.assign(browser, getBrowser())
})

// 导出一个自定义的 hook
export function useBrowser() {
  return {
    browser, // 返回响应式的浏览器对象
    // 注册屏幕变化的回调函数
    onScreenChange(ev: () => void, immediate = true) {
      // 将回调函数添加到事件数组中
      events.push(ev)

      // 如果 immediate 为 true，立即执行回调函数
      if (immediate) {
        ev()
      }
    },
  }
}
