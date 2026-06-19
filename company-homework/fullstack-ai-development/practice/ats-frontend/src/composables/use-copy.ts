import { useMessage } from 'naive-ui'

/**
 * 一键复制 composable —— 包装 navigator.clipboard，统一 toast 反馈 + 兜底路径。
 *
 * @example
 * const { copy } = useCopy()
 * <button @click="copy(email, '邮箱已复制')">复制</button>
 */
export function useCopy() {
  const msg = useMessage()

  async function copy(text: string, successHint = '已复制到剪贴板'): Promise<boolean> {
    if (!text) {
      msg.warning('内容为空，无可复制')
      return false
    }
    try {
      // 现代浏览器主路径（需 https / localhost）
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(text)
        msg.success(successHint)
        return true
      }
      // 兼容兜底：execCommand（旧浏览器 / file://）
      const ta = document.createElement('textarea')
      ta.value = text
      ta.style.position = 'fixed'
      ta.style.opacity = '0'
      document.body.appendChild(ta)
      ta.select()
      const ok = document.execCommand('copy')
      document.body.removeChild(ta)
      if (ok) {
        msg.success(successHint)
        return true
      }
      throw new Error('execCommand returned false')
    }
    catch (e) {
      console.warn('[copy] failed', e)
      msg.error('复制失败，请手动选中后 Ctrl+C')
      return false
    }
  }

  return { copy }
}
