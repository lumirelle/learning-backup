import type { ModuleConfig } from '/@/cool'
import { useTheme } from './hooks'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './static/css/index.scss'
import { t } from '/#/i18n'

export default (): ModuleConfig => {
  return {
    enable: true,
    order: 99,
    options: {
      name: 'default',

      // 自定义主题色
      // color: "#4165d7",

      // 主题列表
      list: [
        {
          label: t('默认'),
          name: 'default',
          color: '#4165d7',
        },
        {
          label: t('翠绿'),
          name: 'cuilv',
          color: '#51C21A',
        },
        {
          label: t('紫檀'),
          name: 'zitan',
          color: '#d0378d',
        },
        {
          label: t('金橙'),
          name: 'jincheng',
          color: '#FFA500',
        },
        {
          label: t('樱桃'),
          name: 'yingtao',
          color: '#FF69B4',
        },
        {
          label: t('薄荷'),
          name: 'bohe',
          color: '#3EB489',
        },
        {
          label: t('青灰'),
          name: 'qinghui',
          color: '#708090',
        },
        {
          label: t('珊瑚'),
          name: 'shanhu',
          color: '#FF4500',
        },
      ],
    },
    install() {
      useTheme()
    },

    label: '主题',
    description: '自定义主色、菜单分组、暗黑模式',
    author: 'COOL',
    version: '1.0.0',
    updateTime: '2024-07-22',
  }
}
