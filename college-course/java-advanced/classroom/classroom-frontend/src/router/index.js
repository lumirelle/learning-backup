import Vue from "vue";
import Router from "vue-router";
import Login from "../views/LoginView.vue";
import Register from "../views/RegisterView.vue";
import CourseInfo from "../views/CourseInfoView.vue";
import MemberManage from "../views/MemberManageView.vue";
import HomeworkInfo from "../views/HomeworkInfoView.vue";
import HomeworkCheck from "../views/HomeworkCheckView.vue";
import InformInfo from "../views/InformInfoView.vue";
import MajorHome from "../views/MajorHomeView.vue";
import CourseManage from "../components/course/CourseManage.vue";
import NoData from "../components/common/NoData.vue";

// 使用vue-router
Vue.use(Router);

// router实现页面跳转
export default new Router({
  // 历史模式
  mode: "history",
  // 路由跳转  路径、名称、跳转组件
  routes: [
    {path: "/", redirect: "/login", meta: {requireAuth: true}},
    {path: "/login", name: 'LoginView.vue', component: Login},
    {path: "/register", name: 'register', component: Register},
    {
      path: '/courseInfo',
      name: 'courseInfo',
      component: CourseInfo,
      meta: {requireAuth: true}
    },
    {
      path: '/memberManage',
      name: 'memberManage',
      component: MemberManage,
      meta: {requireAuth: true}
    },
    {
      path: '/homeworkInfo',
      name: 'homeworkInfo',
      component: HomeworkInfo,
      meta: {requireAuth: true}
    },
    {
      path: '/homeworkCheck',
      name: 'homeworkCheck',
      component: HomeworkCheck,
      meta: {requireAuth: true}
    },
    {
      path: '/informInfo',
      name: 'informInfo',
      component: InformInfo,
      meta: {requireAuth: true}
    },
    {
      path: "/home",
      component: MajorHome,
      redirect: '/courseManage',
      meta: {requireAuth: true},
      // 子路由
      children: [
        {
          path: '/courseManage',
          name: 'courseManage',
          component: CourseManage,
          meta: {requireAuth: true}
        },
        {
          path: '/prepareLesson',
          name: 'prepareLesson',
          component: NoData,
          meta: {requireAuth: true}
        },
        {
          path: '/wellLesson',
          name: 'wellLesson',
          component: NoData,
          meta: {requireAuth: true}
        },
        {
          path: '/myWell',
          name: 'myWell',
          component: NoData,
          meta: {requireAuth: true}
        },
        {
          path: '/double',
          name: 'double',
          component: NoData,
          meta: {requireAuth: true}
        },
        {
          path: '/paperManage',
          name: 'paperManage',
          component: NoData,
          meta: {requireAuth: true}
        },
      ]
    },
    {path: '/future', component: NoData},
  ]
});

