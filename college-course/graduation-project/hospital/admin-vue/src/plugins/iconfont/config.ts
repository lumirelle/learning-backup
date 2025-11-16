import type { ModuleConfig } from '/@/cool'
import { createLink } from './utils'

export default (): ModuleConfig => {
  return {
    enable: true,
    install() {
      const urls = [
        // 如：at.alicdn.com/t/c/font_4803959_e2to11yi7pu.css
      ]
      urls.forEach(url => createLink(url))
    },

    label: 'Iconfont',
    description: '图标字体插件，提供多种图标字体的支持',
    author: 'COOL',
    version: '1.0.0',
    updateTime: '2025-01-11',
    doc: 'https://www.iconfont.cn',
  }
}
