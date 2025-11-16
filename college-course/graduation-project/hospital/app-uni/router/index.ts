import { router, useStore } from '/@/cool'

const ignoreToken = [
  '/pages/index/hospital',
  '/pages/index/website',
  '/pages/index/admin',
  '/pages/index/my',
  '/pages/user/login',
  '/pages/user/captcha',
  '/pages/user/doc',
]

const ignoreRole = [
  '/pages/index/hospital',
  '/pages/index/website',
  '/pages/index/admin',
  '/pages/index/my',
  '/pages/user/login',
  '/pages/user/captcha',
  '/pages/user/doc',
  '/pages/user/profile',
  '/pages/user/set',
]

const justPatient = [
  '/pages/index/feedback',
]

router.beforeEach((to, next) => {
  const { user } = useStore()

  if (!ignoreToken.includes(to.path) && !user.token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none',
    })
    router.login()
  }
  else if (!ignoreRole.includes(to.path) && !user.info?.role) {
    uni.showToast({
      title: '请先创建个人档案',
      icon: 'none',
    })
    router.profile()
  }
  else if (justPatient.includes(to.path) && user.info?.role !== 1) {
    uni.showToast({
      title: '请使用患者身份访问',
      icon: 'none',
    })
    router.login()
  }
  else {
    next()
  }
})
