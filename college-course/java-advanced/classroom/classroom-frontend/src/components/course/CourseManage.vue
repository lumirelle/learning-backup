<template>
  <!--  课程管理  -->
  <div class="view-home-box">
    <div class="view-home">
      <div class="home-content">
        <!--  置顶和大功能区 header common -->
        <div class="header-common-border" :class="topCourse.length !== 0 ? 'hasborder' : ''">
          <!--     top handler     -->
          <div class="top-handler">
            <!--       左边     -->
            <h2 class="left">
              <span v-if="topCourse.length!==0">置顶课程</span>
            </h2>
            <!--       右边     -->
            <div class="right">
              <el-dropdown v-if="identity === 1" class="handle" id="home-element2"
                           @command="handleCommand"
                           trigger="click">
                <el-button size="medium" type="primary">
                  <i class="el-icon-plus"></i>
                  <span>创建/加入课程</span>
                </el-button>
                <el-dropdown-menu>
                  <!-- 1为老师，2为学生-->
                  <el-dropdown-item command="1">创建课程</el-dropdown-item>
                  <el-dropdown-item command="2">加入课程</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-button v-else @click="$store.commit('setShowCourseJoin',true)"
                         style="margin-left: 20px;height: 36px;padding: 5px 15px"
                         type="primary">
                <i class="el-icon-plus"></i>
                <span>加入课程</span>
              </el-button>
            </div>
          </div>
          <div class="class-box" style="max-height: 320px">
            <!--   循环渲染 -->
            <CourseCard v-for="(item,i) in topCourse" :key="i" :info="{user,course:item}"
                        @getCourse="getCourse" @showCourseOut="showCourseOut"
                        @showCourseEdit="showCourseEdit"
                        @showCoursePigeonhole="showCoursePigeonhole"
                        @showCourseSend="showCourseSend"></CourseCard>
          </div>
        </div>
        <!--    小标头    -->
        <div class="other-header flex-between">
          <el-tabs v-model="courseType" tab-position="top" class="right">
            <template v-if="identity === 1">
              <el-tab-pane label="我教的" name="teach"/>
              <el-tab-pane label="我协助的" name="assist"/>
              <el-tab-pane label="我学的" name="learning"/>
            </template>
            <template v-else>
              <el-tab-pane label="我学的" name="learning"/>
              <el-tab-pane label="我协助的" name="assist"/>
            </template>
          </el-tabs>
          <div class="left flex-between">
            <el-button @click="choose = 'courseSort';$store.commit('setShowCourseHandle',true)"
                       v-if="otherCourse.length !== 0">
              课程排序
            </el-button>
            <el-button
                @click="choose = 'coursePigeonhole';$store.commit('setShowCourseHandle',true)">
              归档管理
            </el-button>
            <el-input v-model="searchTeachCourse" @keyup.enter.native="todoAlert" class="mgl-24"
                      suffix-icon="el-icon-search" placeholder="搜索我教的课程"/>
          </div>
        </div>
        <!--  课程区 -->
        <NoData v-if="!hasData"/>
        <div v-else class="class-box">
          <!--     根据选择展示的课程类型     -->
          <template v-if="courseType === 'teach'">
            <CourseCard v-for="(item,i) in teachCourse" :key="i" :info="{user,course:item}"
                        @getCourse="getCourse" @showCourseOut="showCourseOut"
                        @showCourseEdit="showCourseEdit"
                        @showCoursePigeonhole="showCoursePigeonhole"
                        @showCourseSend="showCourseSend"/>
          </template>
          <template v-else-if="courseType === 'assist'">
            <CourseCard v-for="(item,i) in assistCourse" :key="i" :info="{user,course:item}"
                        @getCourse="getCourse" @showCourseOut="showCourseOut"
                        @showCourseEdit="showCourseEdit"
                        @showCoursePigeonhole="showCoursePigeonhole"
                        @showCourseSend="showCourseSend"/>
          </template>
          <template v-else>
            <CourseCard v-for="(item,i) in learningCourse" :key="i" :info="{user,course:item}"
                        @getCourse="getCourse" @showCourseOut="showCourseOut"
                        @showCourseEdit="showCourseEdit"
                        @showCoursePigeonhole="showCoursePigeonhole"
                        @showCourseSend="showCourseSend"/>
          </template>
        </div>
        <!--  对话框等 -->
        <div>
          <FloatTools></FloatTools>
          <CourseEdit :info="{master:user.id,course,new:this.new,identity}"
                      @getCourse="getCourse"></CourseEdit>
          <CourseJoin :info="{user,identity}" @getCourse="getCourse"></CourseJoin>
          <CourseHandle :info="{user,otherCourse,outCourse,choose}"
                        @getCourse="getCourse" @showCourseOut="showCourseOut"
                        @showCoursePigeonhole="showCoursePigeonhole"></CourseHandle>
          <CourseOut :info="{user,course,identity}" @getCourse="getCourse"></CourseOut>
          <CoursePigeonhole :info="{user,course,identity}"
                            @getCourse="getCourse"></CoursePigeonhole>
          <CourseSent :info="{user,course}" @getCourse="getCourse"></CourseSent>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CourseCard from "./CourseCard.vue";
import CourseJoin from "./CourseJoin.vue";
import CourseHandle from "./CourseHandle.vue";
import CourseEdit from "./CourseEdit.vue";
import CourseOut from "./CourseOut.vue";
import CoursePigeonhole from "./CoursePigeonhole.vue";
import FloatTools from "../tools/FloatTools.vue";
import CourseSent from "./CourseSend.vue";
import {computed} from "vue";
import NoData from "@/components/common/NoData.vue";

export default {
  name: "CourseManage",
  components: {
    NoData,
    CourseSent,
    FloatTools,
    CourseOut, CourseEdit, CourseCard, CourseJoin, CourseHandle, CoursePigeonhole
  },
  data() {
    return {
      topCourse: [], // 置顶课程
      otherCourse: [], // 非置顶课程
      teachCourse: computed(() => {
        // 是老师且是管理员
        return this.otherCourse.filter(
            item => (item.identity === '1' && item.master === this.user.id))
      }),
      assistCourse: computed(() => {
        // 是老师且不是管理员
        return this.otherCourse.filter(
            item => (item.identity === '1' && item.master !== this.user.id))
      }),
      learningCourse: computed(() => {
        // 是学生
        return this.otherCourse.filter(item => item.identity === '0')
      }),
      hasData: computed(() => {
        if (this.courseType === 'teach') {
          return this.otherCourse.filter(
              item => (item.identity === '1' && item.master === this.user.id)).length !== 0
        } else if (this.courseType === 'assist') {
          return this.otherCourse.filter(
              item => (item.identity === '1' && item.master !== this.user.id)).length !== 0
        } else {
          return this.otherCourse.filter(item => item.identity === '0').length !== 0
        }
      }),
      outCourse: [],
      now: '',
      user: {},
      identity: -1,
      choose: '',
      course: {},
      courseType: 'learning',
      searchTeachCourse: '',
      new: '1'
    }
  },
  mounted() {
    this.getUser()
  },
  methods: {
    todoAlert() {
      this.$message({
        center: true,
        message: "该功能暂未开放",
        type: "warning"
      });
    },
    getIdentity(role) {
      if (role === '管理员') {
        return '1'
      } else if (role === '助教') {
        return '2'
      } else {
        return '0'
      }
    },
    getUser() {
      this.$axios.post('start/user', {
        token: this.$store.state.Authorization
      }, {
        headers: {'Authorization': this.$store.state.Authorization}
      }).then(resp => {
        if (resp.status === 200) {
          this.user = resp.data.user
          this.identity = resp.data.user.account.startsWith('s') ? 0 : 1
          // 根据身份确认默认展示的课程类型
          if (this.identity === 1) {
            this.courseType = 'teach'
          }
          this.getCourse()
        }
      }).catch(resp => {
        console.log(resp)
      })
    },
    getCourse() {
      let api;
      if (this.identity === 0) {
        api = '/student'
      } else if (this.identity === 1) {
        api = '/teacher'
      } else {
        return
      }
      this.$axios.post(api + '/course',
          {token: this.$store.state.Authorization},
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          this.topCourse = []
          this.otherCourse = []
          this.outCourse = []
          for (let i = 0; i < resp.data.length; i++) {
            resp.data[i].identity = this.getIdentity(resp.data[i].role)
            if (resp.data[i].pigeonhole === '0') {
              if (resp.data[i].top === '1') {
                this.topCourse.push(resp.data[i])
              } else {
                this.otherCourse.push(resp.data[i])
              }
            } else {
              this.outCourse.push(resp.data[i])
            }
          }
        }
      })
    },
    showCourseOut(course) {
      this.course = course
      this.$store.commit('setShowCourseOut', true)
    },
    showCourseEdit(course) {
      this.course = course
      this.new = course.id ? "0" : "1"
      this.$store.commit('setShowCourseEdit', true)
    },
    showCoursePigeonhole(course) {
      this.course = course
      this.$store.commit('setShowCoursePigeonhole', true)
    },
    showCourseSend(course) {
      this.course = course
      this.$store.commit('setShowCourseSend', true)
    },
    handleCommand(command) {
      if (command === '1') {
        this.showCourseEdit({})
      } else {
        this.$store.commit("setShowCourseJoin", true)
      }
    },
  },
}
</script>

<style scoped>
.view-home-box {
  max-width: 1150px;
  margin: 0 auto;
}

.view-home {
  display: flex;
  flex-flow: row nowrap;
  justify-content: space-between;
}

.view-home .home-content {
  flex: 1;
}

.view-home .header-common-border {
  border-radius: 8px;
  padding-top: 18px;
  padding-bottom: 8px;
}

.view-home .hasborder {
  border: 1px solid #dadce0;
  padding: 18px;
}

.view-home .home-content .class-box {
  margin: 8px -10px 0;
  display: flex;
  flex-flow: row wrap;
  overflow-y: auto;
}

.view-home .home-content .other-header {
  padding-top: 12px;
  padding-bottom: 12px;
  font-size: 20px;
  color: #575a5b;
  line-height: 24px;
}

.view-home .top-handler {
  display: flex;
  justify-content: space-between;
}

::v-deep .view-home .home-content .other-header .el-tabs__nav-wrap:after {
  background-color: #e8f0ff;
}

.class-box > div {
  margin: 11px;
}

::v-deep .view-home .home-content .other-header .el-input input {
  border-radius: 51px;
}
</style>

