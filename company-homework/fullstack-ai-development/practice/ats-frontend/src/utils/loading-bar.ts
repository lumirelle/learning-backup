import type { LoadingBarApi } from 'naive-ui'
import { ref } from 'vue'

/**
 * 全局 LoadingBar 引用 —— 在 App.vue setup 内由 useLoadingBar() 填入，
 * router beforeEach / afterEach / onError 通过该 ref 触发顶部进度条。
 *
 * 这样设计的原因：useLoadingBar() 必须在 setup 上下文中调用，
 * 而 router/index.ts 是模块顶层，无法直接拿到实例；
 * 通过模块级 ref 桥接是最简洁的解法。
 */
export const loadingBarRef = ref<LoadingBarApi | null>(null)
