<template>
  <!--    抽屉和面包屑-->
  <div id="drawer" style="z-index: 3;
                padding: 0 50px;
                box-sizing: border-box;
                background-color: #fff;
                color: #FFF;
                display: flex;
                justify-content: space-between;
                align-items: center;
                position: fixed;
                top: 0;
                font-size: 16px;
                height: 72px;
                width: 100%;">
    <div style="display: flex;">
      <el-button @click="drawer = true" type="text" class="icon-ic_header_hamberg font_family"
                 style="margin:8px 0 0 -10px;font-size: 20px;color: #5F6368;"></el-button>
      <!--   抽屉   -->
      <el-drawer
          direction="ltr" :show-close="false" :modal="false"
          :visible.sync="drawer" :with-header="false" :size="identity === 0 ? '318px' : '350px'"
          style="cursor: pointer" :destroy-on-close="false">
        <div :style="{'width':identity === 0 ? '318px' : '350px'}" style="padding: 9px 0;
                            color: #1F2023;
                            display: flex;
                            flex-direction: column;
                            position: fixed;
                            background: white">
          <div class="top" style="display: block;
                                            height: 50px;
                                            line-height: 50px;
                                            font-size: 13px;
                                            color: #1F2023;
                                            padding: 0 30px;
                                            background-position: 33px;
                                            background-repeat: no-repeat;"
               @click="$router.push('courseManage')">
            <i class="el-icon-s-management" style="font-size:20px;margin-right: 28px"></i>课堂
          </div>
          <div class="top" v-if="identity === 1" style="display: block;
                                                        height: 50px;
                                                        line-height: 50px;
                                                        font-size: 13px;
                                                        color: #1F2023;
                                                        padding: 0 30px;
                                                        background-position: 33px;
                                                        background-repeat: no-repeat;"
               @click="$router.push('prepareLesson')">
            <i class="el-icon-s-claim" style="font-size:20px;margin-right: 28px"></i>备课区
          </div>
          <div class="top" style="display: block;
                                height: 50px;
                                line-height: 50px;
                                font-size: 13px;
                                color: #1F2023;
                                padding: 0 30px;
                                background-position: 33px;
                                background-repeat: no-repeat;" @click="openNew">
            <i class="el-icon-s-comment" style="font-size:20px;margin-right: 28px"></i>私信
          </div>
        </div>
        <div :style="{'margin-top':identity === 0 ? '110px' : '166px'}">
          <div style="color: #1F2023;">
            <span style="color: #5F6368;
                                    font-size: 12px;
                                    height: 46px;
                                    line-height: 46px;
                                    padding-left: 25px;">
              {{ identity === 0 ? '已有课程' : '教授的课程 (' + this.course.length + ')' }}
            </span>
            <div class="card" v-for="(item,i) in this.course" :key="i"
                 @click="jump(item.courseId)"
                 style="display: block;
                                    line-height: 24px;
                                    padding: 10px 25px;
                                    color: #1F2023;">
              <el-avatar :size="30" style="display: inline-block;
                                                width: 32px;
                                                height: 32px;
                                                line-height: 32px;
                                                background-repeat: no-repeat;
                                                vertical-align: middle;
                                                margin-right: 20px;
                                                background-color: #2C58AB;
                                                border-radius: 50%;
                                                font-style: normal;
                                                text-align: center;
                                                color: #fff;
                                                font-size: 14px;">{{ item.name.charAt(0) }}
              </el-avatar>
              <span style="font-size: 14px;
                                vertical-align: middle;
                                display: inline-block;">
                {{ item.name }}<span v-if="identity === 1" style="color: #5F6368;font-size: 12px;">
                <br>
                {{ item.className }}</span>
              </span>
            </div>
          </div>

          <div v-if="identity === 1">
            <span style="color: #5F6368;
                                  font-size: 12px;
                                  height: 46px;
                                  line-height: 46px;
                                  padding-left: 25px;">
              {{ '学习的课程(' + this.otherCourse.length + ')' }}</span>
            <div class="card" v-for="(item,i) in this.otherCourse" :key="i"
                 @click="jump(item.courseId)"
                 style="display: block;
                                        line-height: 24px;
                                        padding: 10px 25px;
                                        color: #1F2023;">
              <el-avatar :size="30" style="display: inline-block;
                                                    width: 32px;
                                                    height: 32px;
                                                    line-height: 32px;
                                                    background-repeat: no-repeat;
                                                    vertical-align: middle;
                                                    margin-right: 20px;
                                                    background-color: #32BAF0;
                                                    border-radius: 50%;
                                                    font-style: normal;
                                                    text-align: center;
                                                    color: #fff;
                                                    font-size: 14px;">{{ item.name.charAt(0) }}
              </el-avatar>
              <span style="font-size: 14px;
                                    vertical-align: middle;
                                    display: inline-block;">
                {{ item.name }}
                <span style="color: #5F6368;font-size: 12px;">
                <br>{{ item.className }}
                </span>
              </span>
            </div>
          </div>
        </div>
      </el-drawer>
      <!-- 面包屑 -->
      <el-breadcrumb separator-class="el-icon-arrow-right" style="margin:22px 0 0 44px">
        <el-breadcrumb-item :to="{ path: '/courseManage' }">我的课堂</el-breadcrumb-item>
        <el-breadcrumb-item style="color: #1F2023">课程内容</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <AccountTools :info="{user:info.user}"></AccountTools>
  </div>
</template>

<script>
import AccountTools from "./AccountTools.vue";

export default {
  name: "DrawerTools",
  components: {AccountTools},
  data() {
    return {
      identity: '',
      drawer: false,
      course: [],
      otherCourse: [],
      now: {},
    }
  },
  props: ['info'],
  watch: {
    info: function (info) {
      this.now = info.course
      this.identity = info.user.account.startsWith('s') ? 0 : 1
      this.getCourse()
    }
  },
  methods: {
    openNew() {
      window.open('/future')
    },
    jump(courseId) {
      localStorage.setItem('courseView', '0')
      localStorage.setItem('courseId', courseId)
      this.$router.go(0)
    },
    getCourse() {
      let api;
      if (this.identity === 0) {
        api = '/student'
      } else if (this.identity === 1) {
        api = '/teacher'
      } else {
        console.log("error")
        return
      }
      this.$axios.post(api + '/course',
          {token: this.$store.state.Authorization},
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          this.course = []
          this.otherCourse = []
          for (let i = 0; i < resp.data.length; i++) {
            if (this.identity === 1 && resp.data[i].role !== '管理员') {
              this.otherCourse.push(resp.data[i])
            } else {
              this.course.push(resp.data[i])
            }
          }
        }
      }).catch(reason => {
        console.log(reason)
      })
    },
  }
}
</script>

<style>
#drawer {
  box-shadow: 0 0 10px #ccc
}

#drawer .el-drawer {
  overflow: auto;
}

#drawer ::-webkit-scrollbar {
  width: 8px !important;
}

/*#drawer ::-webkit-scrollbar-track-piece {*/
/*    margin-top: 200px;*/
/*}*/

#drawer .card:hover {
  background: #F1F3F4;
  color: #2C58AB;
}

#drawer .top:hover {
  color: #32BAF0;
  background-color: #F1F3F4;
}
</style>
