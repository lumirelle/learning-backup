import type { ModuleConfig } from '/@/cool'
import './static/index.css'

export default (): ModuleConfig => {
  return {
    order: 100,
    label: 'Tailwind',
    description: 'Tailwind 样式，提供现代化的响应式设计工具',
    author: 'COOL',
    version: '1.0.0',
    updateTime: '2025-01-11',
    doc: 'https://tailwindcss.com/docs/installation',
  }
}
