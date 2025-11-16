import dev from './dev'
import prod from './prod'

// 是否开发模式
export const isDev = import.meta.env.MODE === 'development'

// 代理环境
const proxy = isDev ? dev : prod

// 配置
export const config = {
  // 应用信息
  app: {
    // 应用名称
    name: '医疗陪诊小程序',
    // 应用描述
    desc: '一个简单的医疗陪诊小程序',
    // 页面配置
    pages: {
      login: '/pages/user/login',
      profile: '/pages/user/profile',
    },
    // 微信配置
    wx: {
      debug: false,
    },
  },

  // 忽略
  ignore: {
    token: [],
  },

  ...proxy,
}

export * from './proxy'
