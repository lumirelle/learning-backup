<template>
  <!--  公告详情页面-->
  <div id="InformInfo">
    <div
        style="display: flex;
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
        <a @click="$router.push('/courseInfo')"
           style="height: 32px;
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
      <el-tabs v-model="infoView" style="position:absolute;top: 6px;left: 0;width: 100%; " v-if="flag === true">
        <el-tab-pane name="1" v-if="identity !== '' && flag === true" label="公告详情">
          <InformDiscussion :info="{user,identity:this.identity,course}"></InformDiscussion>
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
import NoData from "../components/common/NoData.vue";
import InformDiscussion from "../components/inform/InformDiscussion.vue";

export default {
  name: "InformInfo",
  components: {InformDiscussion, AccountTools},
  data() {
    return {
      infoView: '1',
      InformId: localStorage.getItem('InformId'),
      user: {},
      identity: '',
      course: {},
      flag: false,
    }
  },
  mounted() {
    this.getUser()
  },
  methods: {

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
#InformInfo .el-tabs__nav-scroll {
  display: flex;
  justify-content: center;
  border: 0;
  font-size: 20px;
  text-align: center;
}

#InformInfo .el-tabs__header {
  padding: 0;
  position: relative;
  line-height: 0 !important;
  margin: 0 !important;
  border: 0 !important;
  outline: none !important;
}

#InformInfo .el-tabs__nav-wrap::after, .el-tabs__active-bar {
  height: 0 !important;
}

#InformInfo .el-tabs__item {
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

#InformInfo .el-tabs__item.is-active {
  border-bottom: 4px solid #2C58AB;
  border-left: 2px solid transparent;
  border-right: 2px solid transparent;
  font-size: 16px;
  font-weight: 500;
  color: rgba(59, 61, 69, 1);
}
</style>
