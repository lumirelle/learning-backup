<template>
  <div class="component-home_card">
    <!--  教学模式  -->
    <span v-if="info.course.teachingMode === '0'" class="tag-flag-pattern bg-r"> 线上 </span>
    <span v-else-if="info.course.teachingMode === '1'" class="tag-flag-pattern bg-g"> 线下 </span>
    <span v-else class="tag-flag-pattern bg-y"> 混合 </span>
    <!--  标头  -->
    <div class="header-info"
         :style="{'background':'url(' + info.course.address + ') no-repeat center'}">
      <!--   学年/学期   -->
      <p class="time" @click="courseInfo">{{ info.course.year }} {{ info.course.term }}</p>
      <!--   课程名称   -->
      <h3 :title="info.course.name" @click="courseInfo" class="name text_overflow1">
        {{ info.course.name }}
      </h3>
      <!--   班级名称   -->
      <p :title="info.course.className" @click="courseInfo" class="classname text_overflow1">
        {{ info.course.className }}</p>
      <!--  QRCode -->
      <div class="qrcode">
        <img src="../../assets/img/qr.0b314396.svg" class="img" alt="">
        <!--     老师/助教   -->
        <el-dropdown v-if="info.course.identity !== '0'" @command="handleCommand" trigger="click">
          <div>
            <span style="padding-right:5px; font-size: 14px;font-weight: 500;
                    color: rgba(255,255,255,1);line-height: 22px;text-align: center;
                    cursor: pointer;">
                  加课码:
                  <span>{{ info.course.deleting === '1' ? "已停用" : info.course.courseKey }}</span>
                  <i class="el-icon-arrow-down el-icon--right" style=""></i>
                </span>
          </div>
          <el-dropdown-menu>
            <div v-if="info.course.deleting === '1'">
              <el-dropdown-item command="7">启用</el-dropdown-item>
            </div>
            <div v-else>
              <el-dropdown-item command="7">停用</el-dropdown-item>
              <el-dropdown-item command="8">重置</el-dropdown-item>
            </div>
          </el-dropdown-menu>
        </el-dropdown>
        <!--     学生   -->
        <div v-else>
          <span style="padding-right:5px;font-size: 14px;font-weight: 500;
                 color: rgba(255,255,255,1);line-height: 22px;text-align: center;">
              加课码:{{ info.course.courseKey }}
            </span>
        </div>
      </div>
    </div>
    <!--  内容  -->
    <div class="content-info"></div>
    <!--  用户信息  -->
    <div class="user-info">
      <!--   左边   -->
      <div class="left">
        <!--     老师   -->
        <template v-if="info.course.identity === '1'">
          <span class="tag-flag role-t">教</span>
          <span @click="jump()" style="cursor: pointer;"> 学生 {{
              info.course.studentNum
            }} 人 </span>
        </template>
        <!--     学生   -->
        <template v-else>
          <span class="tag-flag role-s">学</span>
          <span style="cursor: pointer;"><span>负责人： {{ teacherName }}</span></span>
        </template>
      </div>
      <!--   右边   -->
      <div class="right">
        <div class="set-top" @click="changeTop">
          <template v-if="info.course.top === '1'">
            <span>取消置顶</span>
          </template>
          <template v-else>
            <img src="../../assets/img/top.6cb08607.svg" alt="">
            <span>置顶</span>
          </template>
        </div>
        <el-dropdown class="handle" @command="handleCommand" trigger="click" placement="top-end">
          <span class="el-dropdown-link el-dropdown-selfdefine">
            <img src="../../assets/img/etc.52da4ee0.svg" alt=""/>
          </span>
          <el-dropdown-menu style="width: 128px;margin-bottom: -5px">
            <div v-if="info.course.identity === '0'">
              <el-dropdown-item command="3">归档</el-dropdown-item>
              <el-dropdown-item command="2">退课</el-dropdown-item>
            </div>
            <div v-else>
              <el-dropdown-item command="1">编辑</el-dropdown-item>
              <el-dropdown-item command="2">{{
                  info.course.identity === '1' ? '删除' : '退课'
                }}
              </el-dropdown-item>
              <el-dropdown-item command="3">归档</el-dropdown-item>
              <el-dropdown-item command="4">复制课程</el-dropdown-item>
              <el-dropdown-item command="5">打包下载</el-dropdown-item>
              <el-dropdown-item v-if="info.course.identity === '1'" command="6">
                转让
              </el-dropdown-item>
            </div>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "CourseCard",
  data() {
    return {
      number: 0,
      teacherName: '',
    }
  },
  props: ["info"],
  mounted() {
    if (this.info.course.identity === '0') {
      this.getTeacherName()
    }
  },
  methods: {
    jump() {
      localStorage.setItem('courseId', this.info.course.courseId)
      localStorage.setItem('view', '0')
      this.$router.push('/memberManage')
    },
    courseInfo() {
      localStorage.setItem('courseView', '0')
      localStorage.setItem('courseId', this.info.course.courseId)
      this.$router.push('/courseInfo')
    },
    getTeacherName() {
      this.$axios.post("/start/teacherName",
          {id: this.info.course.master},
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          this.teacherName = resp.data
        }
      }).catch(resp => {
        console.log(resp)
      })
    },
    changeTop() {
      let api;
      if (this.info.user.account.charAt(0) === 's') {
        api = '/student'
      } else if (this.info.user.account.charAt(0) === 't') {
        api = '/teacher'
      } else {
        console.log('error')
        return
      }
      this.$axios.post(api + "/changeTop", {
            id: this.info.user.id,
            courseId: this.info.course.courseId,
            top: this.info.course.top
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          this.$message({
            showClose: true,
            center: true,
            offset: this.$store.state.tip,
            message: resp.data
          })
          this.$emit('getCourse')
        }
      }).catch(resp => {
        console.log(resp)
      })
    },
    changeCourseKey() {
      this.$axios.post("/teacher/changeCourseKey",
          {id: this.info.course.courseId, deleting: this.info.course.deleting},
          {headers: {'Authorization': this.$store.state.Authorization}}
      ).then(resp => {
        this.$message({
          showClose: true,
          center: true,
          offset: this.$store.state.tip,
          message: resp.data
        })
        this.info.course.deleting = (this.info.course.deleting === '0' ? '1' : '0')
      })
    },
    resetCourseKey() {
      this.$confirm('重置后原来的6位邀请码将失效', '要重置邀请码吗?', {
        showClose: false, lockScroll: false, closeOnClickModal: false
      }).then(() => {
        this.$axios.post("/teacher/resetCourseKey",
            {id: this.info.course.courseId},
            {headers: {'Authorization': this.$store.state.Authorization}}
        ).then(resp => {
          this.$message({
            showClose: true,
            center: true,
            offset: this.$store.state.tip,
            message: resp.data
          })
          this.$emit('getCourse')
        })
      })
    },
    handleCommand(command) {
      switch (parseInt(command)) {
        case 1:
          this.$emit('showCourseEdit', {
            master: this.info.course.master,
            id: this.info.course.courseId,
            name: this.info.course.name,
            className: this.info.course.className,
            year: this.info.course.year,
            term: this.info.course.term,
          })
          break
        case 2:
          this.$emit('showCourseOut', this.info.course)
          break
        case 3:
          this.$emit('showCoursePigeonhole', this.info.course)
          break
        case 4:
        case 5:
          this.$message({
            center: true,
            message: '该功能暂未开放',
            type: 'warning'
          })
          break;
        case 6:
          this.$emit('showCourseSend', this.info.course)
          break
        case 7:
          this.changeCourseKey()
          break
        case 8:
          this.resetCourseKey()
      }
    }
  }
}
</script>

<style scoped>
.component-home_card {
  width: 255px;
  height: 234px;
  background: #fff;
  border: 1px solid #e2e6ed;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1);
  -webkit-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1);
}

.component-home_card .tag-flag-pattern.bg-r {
  background-color: #ff6000;
}

.component-home_card .tag-flag-pattern.bg-g {
  background-color: #00ca90;
}

.component-home_card .tag-flag-pattern.bg-y {
  background: linear-gradient(270deg, #fac966 0, #ffe1ad);
  color: #6b512e;
}

.component-home_card .tag-flag-pattern {
  position: absolute;
  top: 0;
  right: 0;
  width: 62px;
  height: 26px;
  border-radius: 0 0 0 8px;
  border-bottom: 2px solid #fff;
  border-left: 2px solid #fff;
  color: #ffefe5;
  line-height: 26px;
  font-size: 14px;
  text-align: center;
}

.component-home_card .header-info {
  cursor: pointer;
  padding: 18px 24px;
  height: 152px;
  color: #fff;
  background-repeat: no-repeat;
  background-size: cover;
  background-position: 50%;
  background-color: #3367d5;
}

.component-home_card .header-info .time {
  opacity: .6;
  font-size: 12px;
}

.component-home_card .header-info .name {
  margin-top: 3px;
  font-size: 18px;
  font-family: PingFangSC, PingFangSC-Medium;
  font-weight: 500;
  text-align: left;
  color: #fff;
  line-height: 32px;
}

.component-home_card .header-info .chapter, .component-home_card .header-info .classname {
  font-family: PingFangSC, PingFangSC-Medium;
  font-weight: 500;
  text-align: left;
  color: #fff;
  line-height: 24px;
}

.component-home_card .header-info .qrcode {
  margin-top: 18px;
  display: flex;
  position: relative;
  font-size: 12px;
  font-family: PingFangSC, PingFangSC-Medium;
  font-weight: 500;
  text-align: left;
  color: #fff;
  line-height: 16px;
}

.component-home_card .header-info .qrcode .img {
  display: block;
  width: 14px;
  margin-right: 8px;
}

.component-home_card .content-info {
  cursor: pointer;
  height: 40px;
  padding: 0 20px 16px 20px;
}

.component-home_card .user-info {
  display: flex;
  justify-content: space-between;
  height: 40px;
  padding: 8px 14px 12px;
  font-size: 12px;
  line-height: 16px;
}

.component-home_card .user-info .left, .component-home_card .user-info .right {
  display: flex;
  align-items: center;
}

.tag-flag {
  min-width: 20px;
  max-width: 50px;
  height: 18px;
  line-height: 15px;
  display: inline-block;
  border-radius: 2px;
  color: #fff;
  font-size: 12px;
  text-align: center;
  margin-right: 8px;
}

.tag-flag.role-s {
  background-color: #fff;
  border: 1px solid #4285f4;
  color: #4285f4;
}

.tag-flag.role-t {
  background-color: #4285f4;
  border: 1px solid #4285f4;
}

.component-home_card .user-info .right .set-top {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.component-home_card .user-info .right .set-top img {
  margin-right: 4px;
}

.component-home_card .user-info .right img {
  display: block;
  width: 14px;
}

.component-home_card .user-info .right .handle {
  cursor: pointer;
  margin-left: 11px;
}

</style>
