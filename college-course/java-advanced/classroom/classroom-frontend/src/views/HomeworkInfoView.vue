<template>
  <div id="homeworkInfo">
    <div style="display: flex;
                    padding: 4px 0;
                    height: 72px;
                    justify-content: space-between;
                    align-items: center;">
      <div style="margin-left: 35px;
                        height: 32px;
                        display: flex;
                        align-items: center;
                        cursor: pointer;
                        z-index: 2">
        <i style="font-size: 25px;margin-left: -2px" @click="$router.go(-1)" class="el-icon-back"></i>
        <a @click="$router.push('/courseInfo')" style="height: 32px;
                        background-color: rgb(44,88,171);
                        padding: 0 10px;
                        margin-left: 10px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        cursor: pointer;
                        color: white;
                        border-radius: 16px;">{{ course.name }}</a>
      </div>
      <el-tabs v-model="homeworkView" style="position:absolute;top: 60px;left: 60px;width: 100%; " v-if="flag === true">
        <el-tab-pane name="0" v-if="identity === 0" label="提交作业" style="margin-left: -60px">
          <HomeworkSubmit :info="{user,identity:this.identity}"></HomeworkSubmit>
        </el-tab-pane>
        <el-tab-pane name="0" v-else label="学生作业" style="margin-top: -60px;margin-left: -60px">
          <HomeworkManage></HomeworkManage>
        </el-tab-pane>
        <el-tab-pane name="1" v-if="identity !== '' && flag === true" label="作业讨论" style="margin-left: -60px">
          <HomeworkDiscussion :info="{user,identity:this.identity,course}"></HomeworkDiscussion>
        </el-tab-pane>
      </el-tabs>
      <div style="margin-right: 68px;z-index: 2">
        <AccountTools :info="{user}"></AccountTools>
      </div>
    </div>
  </div>
</template>

<script>
import AccountTools from "../components/tools/AccountTools.vue";
import HomeworkManage from "../components/homework/HomeworkManage.vue";
import NoData from "../components/common/NoData.vue";
import HomeworkSubmit from "../components/homework/HomeworkSubmit.vue";
import HomeworkDiscussion from "../components/homework/HomeworkDiscussion.vue";

export default {
  name: "HomeworkInfo",
  components: {HomeworkDiscussion, HomeworkSubmit, HomeworkManage, AccountTools},
  data() {
    return {
      homeworkView: localStorage.getItem('homeworkView'),
      homeworkId: localStorage.getItem('homeworkId'),
      user: {},
      identity: '',
      course: {},
      flag: false,
    }
  },
  watch: {
    homeworkView: function (homeworkView) {
      localStorage.setItem('homeworkView', homeworkView)
    },
  },
  mounted() {
    this.getUser()
  },
  methods: {
    choosePage(c) {//选择页面
      this.page = c;
    },
    getUser() {
      this.$axios.post('/start/user', {
        token: this.$store.state.Authorization
      }, {
        headers: {'Authorization': this.$store.state.Authorization}
      }).then(resp => {
        if (resp.status === 200) {//status=200 代表成功
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
        }
        this.flag = true
      })
    },
  }
}
</script>

<style scoped>
#homeworkInfo .el-tabs__nav-scroll {
  display: flex;
  justify-content: center;
  border: 0;
  font-size: 20px;
  text-align: center;
}

#homeworkInfo .el-tabs__header {
  padding: 0;
  position: relative;
  line-height: 0 !important;
  margin: 0 !important;
  border: 0 !important;
  outline: none !important;
}

#homeworkInfo .el-tabs__nav-wrap::after, .el-tabs__active-bar {
  height: 0 !important;
}

#homeworkInfo .el-tabs__item {
  line-height: 50px;
  text-align: center;
  border: 0;
  min-width: 80px;
  height: 62px;
  padding: 0 20px;
  margin: 3px 0 0 20px;
  font-size: 16px;
  font-weight: 400;
  color: rgba(95, 99, 104, 1);
}

#homeworkInfo .el-tabs__item.is-active {
  border-bottom: 4px solid #2C58AB;
  border-left: 2px solid transparent;
  border-right: 2px solid transparent;
  font-size: 16px;
  font-weight: 500;
  color: rgba(59, 61, 69, 1);
}
</style>
