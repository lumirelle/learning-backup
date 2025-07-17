import "element-ui/lib/theme-chalk/index.css";
import "./css/none.css";

import './store/interceptor.js'

import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import ElementUI from "element-ui";
import VueDND from 'awe-dnd'

Vue.config.productionTip = false; // 不显示生产模式的消息

Vue.prototype.$axios = require('axios')
Vue.prototype.$axios.defaults.baseURL = 'http://localhost:8888/api'

Vue.use(VueDND)

Vue.use(ElementUI);

router.beforeEach((to, from, next) => {
  if (to.meta.requireAuth) { // 判断目标路径是否需要权限
    if (store.state.Authorization) { // 判断是否已经登录，已登录则放行
      next()
    } else { // 未登录则跳转到登录页面
      next({
        path: '/login',
      })
    }
  } else { // 不需要权限则放行
    next()
  }
})

new Vue({
  router, store, render: h => h(App)
}).$mount("#app");
