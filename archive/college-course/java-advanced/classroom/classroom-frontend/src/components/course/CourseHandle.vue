<template>
  <!--    课程排序和归档管理-->
  <div id="courseHandle">
    <el-dialog :visible.sync="$store.state.show.courseHandle" :show-close="false"
               :lock-scroll="false" :close-on-click-modal="false"
               top="95px" width="810px">
      <div slot="title" style="display:flex;justify-content: center;height: 580px;width: 100%;">
        <el-tabs :active-name="info.choose">
          <!--          只对非置顶排序-->
          <el-tab-pane label="课程排序" name="courseSort"
                       v-if="info.otherCourse.length > 0 && $store.state.show.courseHandle">
            <div style="
                                width: 100%;
                                display: flex;
                                flex-direction: column;
                                align-items: baseline;
                                justify-content: center">
              <div style="width: 100%;
                                        height: 55px;
                                        padding:5px 0 0 40px;
                                        text-align: left;
                                        cursor: pointer;
                                        background: #fff;
                                        margin-bottom: 1px;
                                        line-height: 55px;
                                        font-size: 16px;
                                        border: 1px solid #ececec;
                                        margin-top: -1px;" v-for="(item,i) in info.otherCourse"
                   :key="i"
                   v-dragging="{ item: item, list: info.otherCourse}">
                <div style="display: flex;align-items: center;">
                  <el-avatar :size="20"
                             style="background: rgb(49,142,235);margin-right: 24px"></el-avatar>
                  {{ item.name }}
                  {{ item.className }}
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="归档管理" name="coursePigeonhole"
                       v-if="$store.state.show.courseHandle">
            <div style="display:flex;justify-content: center">
              <span v-if="info.outCourse.length === 0" style="margin-top: 74px;font-size: 15px;">暂无课程被归档</span>
              <div v-else style="display:flex;flex-wrap:wrap;padding: 30px 70px;width: 670px;">
                <div v-for="(item,i) in info.outCourse" :key="i"
                     :style="{'background':'url(' + address[i] + ') no-repeat center'}"
                     style="background: black;
                                        display: flex;
                                        justify-content: space-between;
                                        width: 268px;
                                        margin: 18px 10px;
                                        padding: 24px 16px 10px 16px">
                  <strong @click="courseInfo(item.courseId)"
                          style="height: 22px;
                                            margin: 3px 50px 50px 0;
                                            font-size: 20px;
                                            color: #fff;
                                            z-index: 3;
                                            cursor: pointer">
                    {{ item.name }}
                  </strong>
                  <span style="margin: 7px 0 0 0;
                                            position: absolute;
                                            display:block;
                                            font-size: 12px;
                                            font-weight: 500;
                                            text-align:right;
                                            color: rgba(255,255,255,1);
                                            line-height: 28px;">
                    <span v-if="item.identity === '0'"> <br/>角色：学生</span>
                    <span v-if="item.identity === '1'"> <br/>角色：老师</span>
                    <span v-if="item.identity === '2'"> <br/>角色：助教</span>
                    <span v-if="identity[i] === 0" style="margin-left: 22px">老师：{{
                        teacher[i]
                      }}</span>
                  </span>
                  <el-dropdown @command="handleCommand" trigger="click">
                    <div id="dots"></div>
                    <el-dropdown-menu slot="dropdown" placement="bottom-end"
                                      style="margin: -26px 0 0 -20px;width: 120px;">
                      <el-dropdown-item :command="{id:1,index:i}">恢复</el-dropdown-item>
                      <el-dropdown-item :command="{id:2,index:i}">{{
                          identity[i] === 0 ? "退课" : "删除"
                        }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
        <i id="close" @click="$store.commit('setShowCourseHandle',false)"
           style="margin: -8px 0 0 8px"></i>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "CourseHandle",
  data() {
    return {
      identity: [],
      address: [],
      teacher: [],
    }
  },
  props: ["info"],
  watch: {
    info: function (info) {
      for (let i = 0; i < info.outCourse.length; i++) {
        let n = Math.floor(Math.random() * 31 + 1)
        this.address.push(
            'https://assets.ketangpai.com/theme/min_ar/' + (n < 10 ? '0' + n : n) + '.jpg')
        if (info.outCourse[i].identity === '0') {
          this.identity.push(0)
          this.getTeacherName(info.outCourse[i].master)
        } else if (info.outCourse[i].identity === '1') {
          this.identity.push(1)
          this.teacher.push('')
        } else {
          this.identity.push(2)
          this.getTeacherName(info.outCourse[i].master)
        }
      }
    }
  },
  mounted() {
    this.$dragging.$on('dragged', ({value}) => {
      let api;
      if (this.info.user.account.charAt(0) === 's') {
        api = '/student'
      } else if (this.info.user.account.charAt(0) === 't') {
        api = '/teacher'
      } else {
        console.log('error')
        return
      }
      this.$axios.post(api + "/courseSort", {
        list: value.list,
      }, {
        headers: {'Authorization': this.$store.state.Authorization}
      }).catch(resp => {
        console.log(resp)
      })
    })
  },
  methods: {
    getTeacherName(id) {
      this.$axios.post("/start/teacherName", {id},
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          this.teacher.push(resp.data)
        }
      }).catch(resp => {
        console.log(resp)
      })
    },
    courseInfo(courseId) {
      localStorage.setItem('courseView', '0')
      localStorage.setItem('courseId', courseId)
      this.$router.push('/courseInfo')
      this.$store.commit('setShowCourseHandle', false)
    },
    handleCommand(command) {
      switch (command.id) {
        case 1:
          this.$emit('showCoursePigeonhole', this.info.outCourse[command.index])
          this.$store.commit('setShowCourseHandle', false)
          break
        case 2:
          this.$emit('showCourseOut', this.info.outCourse[command.index])
          this.$store.commit('setShowCourseHandle', false)
      }
    }
  }
}
</script>

<style>
#courseHandle .el-dialog__header, .el-dialog__body {
  padding: 0 !important;
}

#courseHandle .el-tabs__nav-scroll {
  display: flex;
  justify-content: center;
  background: #f8f8f8;
  border: 0;
  border-bottom: 1px solid #dcdcdc;
  height: 58px;
  width: 810px;
  font-size: 20px;
  text-align: center;
}

#courseHandle .el-tabs__header {
  padding: 0;
  position: relative;
  line-height: 0 !important;
  margin: 0 !important;
  border: 0 !important;
  outline: none !important;
}

#courseHandle .el-tabs__item {
  color: #818181;
  font-size: 18px;
  line-height: 50px;
  height: 55px;
  text-align: center;
  border: 0;
  padding: 0 43px 0;
  margin: 3px 0 0 0;
}

#courseHandle .el-tabs__item.is-active {
  color: #2d2d2d;
  font-size: 18px;
}


#courseHandle .el-tabs__nav-wrap::after, #courseHandle .el-tabs__active-bar {
  height: 0 !important;
}

#dots {
  margin: 12px 0 0 17px;
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  cursor: pointer;
  background: url('https://www.ketangpai.com/Public/Common/img/kczt_23.png') center center no-repeat;
}

#dots:active {
  background: url('https://www.ketangpai.com/Public/Common/img/gengduo_03.png') center center no-repeat;
  border-radius: 3px;
}
</style>
