<template>
  <!--    课程详情-->
  <div id="courseInfo">
    <!--    title  -->
    <DrawerTools :info="{user,course}"></DrawerTools>
    <!--  课程详情框  -->
    <div style="margin: 105px auto 0;width: 1224px;">
      <!--   head body   -->
      <div :style="{'background':'url(' + course.address + ')'}"
           style="margin: 105px auto 0;height: 200px;border-radius: 8px 8px 0 0;
           padding: 24px; box-sizing: border-box;">
        <div>
          <div style="font-size: 36px;color: #fff;display: flex">
            {{ course.name }}
            <div v-if="identity === 1">
              <i @click="$store.commit('setShowCourseEdit',true)"
                 style="display: inline-block;width: 24px;height: 24px;
               background: url(https://www.ketangpai.com/Public/Home/img/edit-course.png) 0/24px no-repeat;
               cursor: pointer;margin: 0 0 0 13px;"></i>
            </div>
          </div>
          <span
              style="display: block;margin-top: 9px;font-size: 20px;color: rgba(255,255,255,1);line-height: 28px;width: 600px;">{{
              course.className
            }}</span>
          <!--   小选项   -->
          <div style="height: 35px;color: white;display:flex;justify-content: space-between;margin: 20px auto 0;
             font-size: 12px;font-weight: 500;text-align: center;">
            <!--      老师    -->
            <div v-if="identity === 1" class="flex-align"
                 style="font-size:14px;font-weight:400;margin-top: 50px">
              <i @click="jump(0,0)" class="font_family icon-qr2_outline"
                 style="margin-right: 5px"></i>
              <el-dropdown @command="handleCommand" trigger="click">
                <div class="jump" style="color: white">
                  加课码:
                  <span>{{ course.deleting === '1' ? "已停用" : course.courseKey }}</span>
                  <i class="el-icon-arrow-down el-icon--right" style=""></i>
                </div>
                <el-dropdown-menu>
                  <div v-if="course.deleting === '1'">
                    <el-dropdown-item command="1">启用</el-dropdown-item>
                  </div>
                  <div v-else>
                    <el-dropdown-item command="1">停用</el-dropdown-item>
                    <el-dropdown-item command="2">重置</el-dropdown-item>
                  </div>
                </el-dropdown-menu>
              </el-dropdown>
              <span class="jump" @click="jump(1,0)">
              已有  {{ student.length + teacher.length }} 人加入
              </span>
            </div>
            <!--      学生    -->
            <div v-else class="flex-align" style="font-size:14px;font-weight:400;margin-top: 50px">
              <i @click="jump(0, 0)" class="font_family icon-qr2_outline"
                 style="margin-right: 5px"></i>
              <span class="jump">
              加课码：{{ course.courseKey }}
              </span>
              <span class="jump" @click="jump(1,0)">
              已有{{ student.length + teacher.length }}人加入
            </span>
            </div>
            <div v-if="identity === 1" style="margin:-48px 30px;display: flex">
            <span style="width: 80px;margin:-1px 0 0 15px;padding: 0 10px;">
              <span
                  style="font-size: 44px;font-weight: 500;display: block;line-height: 48px;height: 50px;">99+</span>互动个数</span>
              <span style="width: 80px;margin:-1px 0 0 15px;padding: 0 10px;">
              <span
                  style="font-size: 44px;font-weight: 500;display: block;line-height: 48px;height: 50px;">{{
                  homework.length
                }}</span>发布作业</span>
              <span style="width: 80px;margin:-1px 0 0 15px;padding: 0 10px;">
              <span
                  style="font-size: 44px;font-weight: 500;display: block;line-height: 48px;height: 50px;">99+</span>发布测试</span>
            </div>
          </div>
          <div v-if="identity === 1" style="color: #FFF;background: url(https://www.ketangpai.com/Public/Home/img/panneltools/theme.png) 0 no-repeat;
               position: absolute;padding-left: 30px;line-height: 18px;z-index: 4;margin: -130px 0 0 1082px;cursor: pointer;"
               @click="todoAlert">
            更换皮肤
          </div>
        </div>
      </div>
      <!-- head bottom -->
      <div class="head-bottom flex-align">
        <div class="bar flex-align">
          <div ref="barit1" class="bar-item active" @click="handleBar(1)"> 课程学习</div>
          <div ref="barit2" class="bar-item" @click="handleBar(2)">学情分析</div>
          <div ref="barit3" class="bar-item" @click="handleBar(3)">成绩管理</div>
          <div ref="barit4" class="bar-item" @click="handleBar(4)">课程介绍</div>
        </div>
      </div>
      <!--   tabs   -->
      <el-tabs v-model="view">
        <!--     课程学习   -->
        <template>
          <el-tab-pane name="0" label="目录">
            <NoData></NoData>
          </el-tab-pane>
          <el-tab-pane name="1" label="互动课件">
            <NoData></NoData>
          </el-tab-pane>
          <el-tab-pane name="2" label="作业" v-if="flag">
            <Homework :info="{user,homework,identity}"></Homework>
          </el-tab-pane>
          <el-tab-pane name="3" label="测试">
            <NoData></NoData>
          </el-tab-pane>
          <el-tab-pane name="4" label="资料">
            <NoData></NoData>
          </el-tab-pane>
          <el-tab-pane name="5" label="腾讯会议">
            <NoData></NoData>
          </el-tab-pane>
          <el-tab-pane name="6" label="公告" v-if="flag">
            <Inform :info="{user,identity,course}"></Inform>
          </el-tab-pane>
          <el-tab-pane name="7" label="话题">
            <NoData></NoData>
          </el-tab-pane>
          <el-tab-pane name="8" label="互动答题">
            <NoData></NoData>
          </el-tab-pane>
        </template>
      </el-tabs>
    </div>
    <FloatTools></FloatTools>
    <CourseEdit :info="{master:user.id,course,new:'0'}" @getCourse="getCourse"></CourseEdit>
    <el-dialog :visible.sync="showQRCode" fullscreen :show-close="false"
               custom-class="dialog-qrbox">
      <QRCode :info="{course,size:student.length+teacher.length}"
              @close="showQRCode = false"></QRCode>
    </el-dialog>
  </div>
</template>

<script>
import DrawerTools from "../components/tools/DrawerTools.vue";
import FloatTools from "../components/tools/FloatTools.vue";
import NoData from "../components/common/NoData.vue";
import CourseEdit from "../components/course/CourseEdit.vue";
import Homework from "../components/homework/Homework.vue";
import Inform from "../components/inform/Inform.vue";
import QRCode from "@/components/common/QRCode.vue";

export default {
  name: "CourseInfo",
  components: {QRCode, Homework, NoData, FloatTools, DrawerTools, CourseEdit, Inform},
  data() {
    return {
      view: localStorage.getItem('courseView') ? localStorage.getItem('courseView') : '1',
      user: {},
      identity: '',
      course: {},
      teacher: [],
      student: [],
      homework: [],
      flag: false,
      showQRCode: false,
      // courseId:localStorage.getItem('courseId')
    }
  },
  mounted() {
    this.getUser()
  },
  // 监听页面切换，保存页面状态
  watch: {
    view: function (view) {
      localStorage.setItem('courseView', view)
    },
  },
  methods: {
    jump(index, view) {
      if (index === 0) {
        this.showQRCode = true
      } else if (index === 1) {
        localStorage.setItem('view', view);
        this.$router.push('/memberManage')
      }
    },
    todoAlert() {
      this.$message({
        center: true,
        message: '该功能暂未开放',
        type: 'warning'
      })
    },
    handleBar(index) {
      if (this.$refs['barit' + index].classList.contains('active')) {
        return
      }
      if (index === 1) {
        this.$router.push('/courseInfo')
      } else if (index === 2) {
        this.todoAlert()
        return;
      } else if (index === 3) {
        // 跳转成绩页面
        localStorage.setItem('view', '2');
        this.$router.push('/memberManage')
      } else if (index === 4) {
        this.todoAlert()
        return;
      }
      this.$refs['barit' + index].classList.add('active')
      for (let i = 1; i < 5; i++) {
        if (i !== index) {
          this.$refs['barit' + i].classList.remove('active')
        }
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
          this.user.address = resp.data.address
          this.identity = resp.data.user.account.startsWith('s') ? 0 : 1
          this.getCourse()
        }
      }).catch(resp => {
        console.log(resp)
      })
    },
    getCourse() {
      this.$axios.post('/start/courseInfo',
          {courseId: localStorage.getItem('courseId')},
          {headers: {'Authorization': this.$store.state.Authorization}}
      ).then(resp => {
        if (resp.status === 200) {
          this.course = resp.data.course
          this.course.address = resp.data.address
          if (this.identity === 1) {
            for (let i = 0; i < resp.data.tc.length; i++) {
              if (this.user.id === resp.data.tc[i].teacher) {
                if (resp.data.tc[i].role === '学生') {
                  this.identity = 0
                }
              }
            }
          }
          this.homework = resp.data.homework
          this.teacher = resp.data.teacher
          this.student = resp.data.student
          this.flag = true;
        }
      })

    },
    changeCourseKey() {
      this.$axios.post("/teacher/changeCourseKey",
          {id: this.course.id, deleting: this.course.deleting},
          {headers: {'Authorization': this.$store.state.Authorization}}
      ).then(resp => {
        this.$message({
          showClose: true,
          center: true,
          offset: this.$store.state.tip,
          message: resp.data
        })
        this.course.deleting = this.course.deleting === '0' ? '1' : '0'
      })
    },
    resetCourseKey() {
      this.$confirm('重置后原来的6位邀请码将失效', '要重置邀请码吗?', {
        showClose: false, lockScroll: false, closeOnClickModal: false
      }).then(() => {
        this.$axios.post("/teacher/resetCourseKey",
            {id: this.course.id},
            {headers: {'Authorization': this.$store.state.Authorization}}
        ).then(resp => {
          this.$message({
            showClose: true,
            center: true,
            offset: this.$store.state.tip,
            message: resp.data
          })
          // this.$router.go(0)
        })
      })
    },
    handleCommand(command) {
      command === '1' ? this.changeCourseKey() : this.resetCourseKey()
    }
  }
}
</script>

<style scoped>
#courseInfo .el-tabs__nav-scroll {
  padding-left: 35px;
  height: 72px;
  font-size: 16px;
  width: 1224px;
  color: #818181;
  background: rgba(241, 243, 244, 1);
  border-radius: 0 0 8px 8px;
  margin: 0 auto;
}

#courseInfo .el-tabs__header {
  padding: 0;
  position: relative;
  line-height: 0 !important;
  margin: 0 !important;
  border: 0 !important;
  outline: none !important;
}

#courseInfo .el-tabs__nav-wrap::after, .el-tabs__active-bar {
  height: 0 !important;
}

#courseInfo .el-tabs__item {
  min-width: 100px;
  height: 71px;
  padding: 0 10px;
  margin-left: 20px;
  font-size: 16px;
  font-weight: 400;
  line-height: 70px;
  position: relative;
  display: inline-block;
  text-align: center;
}

#courseInfo .el-tabs__item.is-active {
  border-left: 2px solid transparent;
  border-right: 2px solid transparent;
  font-size: 16px;
  font-weight: 500;
  color: rgba(59, 61, 69, 1);
  border-bottom: 4px solid #328eeb;
}

#courseInfo .jump {
  margin-right: 16px;
  height: 24px;
  line-height: 24px;
  cursor: pointer;
}

.head-bottom {
  width: 1224px;
  height: 64px;
  color: #3c4043;
  font-size: 14px;
  font-weight: 400;
  justify-content: center;
  background: #fff;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, .1);
}

.head-bottom .bar .active {
  background: #e8f0ff;
  color: #4285f4;
  border-radius: 10px;
}

.head-bottom .bar .bar-item {
  cursor: pointer;
  height: 40px;
  line-height: 40px;
  margin: 0 24px;
  text-align: center;
  font-size: 20px;
  padding-left: 5px;
  padding-right: 5px;
}

::v-deep .dialog-qrbox .el-dialog__header {
  padding: 0;
}
</style>
