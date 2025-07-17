import Vue from "vue"
import Vuex from "vuex"

// 判断Vue是否注册过Vuex插件
Vue.use(Vuex)

// 异步执行
export default new Vuex.Store({
  state: {
    Authorization: localStorage.getItem('Authorization') ? localStorage.getItem('Authorization') : '',
    tip: 100,
    show: {
      courseEdit: false,
      courseJoin: false,
      courseHandle: false,
      courseShortcut: false,
      coursePigeonhole: false,
      courseOut: false,
      courseSend: false
    },
  },
  mutations: {
    init(){
      this.state.show = {
        courseEdit: false,
        courseJoin: false,
        courseHandle: false,
        coursePigeonhole: false,
        courseOut: false,
        courseSend: false
      }
      localStorage.removeItem('Authorization')
    },
    // 解决刷新后数据消失的问题
    setToken(state, payload) {
      state.Authorization = payload.Authorization;
      localStorage.setItem('Authorization', payload.Authorization);
    },
    setShowCourseEdit(state, payload) {
      state.show.courseEdit = payload
    },
    setShowCourseJoin(state, payload) {
      state.show.courseJoin = payload
    },
    setShowCourseHandle(state, payload) {
      state.show.courseHandle = payload
    },
    setShowCoursePigeonhole(state, payload) {
      state.show.coursePigeonhole = payload
    },
    setShowCourseOut(state, payload) {
      state.show.courseOut = payload
    },
    setShowCourseSend(state, payload) {
      state.show.courseSend = payload
    },
    setHandlePage(state, payload) {
      state.show.courseHandle = payload
    },
  },
  actions: {},
  getters: {},
})
