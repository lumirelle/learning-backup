<template>
  <!--  课程转让-->
  <div id="courseSent">
    <el-dialog :show-close="false" :visible.sync="$store.state.show.courseSend"
               destroy-on-close :close-on-click-modal="false"
               :lock-scroll="false" top="245px" width="450px" style="margin-left: 100px">
      <div slot="title">
        <span>确定要转让"<span style="color: blue">{{ info.course.name }}</span>"么？</span>
      </div>
      <div style="
                    height: 198px;
                    margin: -4px 26px 0;
                    display: flex;
                    flex-direction: column;
                    line-height: 32px;
                    color: #2d2d2d;
                    font-size: 14px;">
        <span>您的这个课程的任何信息或评论将被永久转让</span>
        <span>警告：此操作无法撤消</span>
        <el-input show-password placeholder="请输入登录密码验证" v-model="password"></el-input>
        <el-input placeholder="请输入转让老师的账号/手机/邮箱" v-model="account"></el-input>
      </div>
      <div class="dialog-footer" slot="footer" style="height: 28px;font-size: 16px">
        <el-button @click="$store.commit('setShowCourseSend',false)"
                   style="width:98px;height: 38px;padding:0 5px;font-size: 16px;">取消
        </el-button>
        <el-button @click="courseSend" type="primary"
                   style="width:98px;height: 38px;padding:0 5px;font-size: 16px;margin-left: 20px;">转让
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "CourseSent",
  data() {
    return {
      account: '',
      password: '',
    }
  },
  props: ['info'],
  methods: {
    courseSend() {
      this.$axios.post("/teacher/courseSend", {
        id: this.info.user.id,
        courseId: this.info.course.courseId,
        account: this.account,
        password: this.password,
      }, {
        headers: {'Authorization': this.$store.state.Authorization}
      }).then(resp => {
        if (resp.status === 200) {
          this.$message({
            showClose: true,
            center: true,
            offset: this.$store.state.tip,
            message: resp.data
          })
          if (resp.data === '转让成功') {
            this.$emit('getCourse')
            this.$store.commit('setShowCourseSend', false)
          }
        }
      }).catch(resp => {
        console.log(resp)
      })
    }
  }
}
</script>

<style>
#courseSent .el-dialog__header {
  padding: 10px 26px;
  line-height: 52px;
  color: #a9a9a9;
  font-size: 18px;
}

#courseSent .el-input__inner {
  margin: 10px 0 10px 0;
  padding: 31px 0 16px 0;
  color: #2d2d2d;
  font-size: 14px;
  width: 100%;
  border: 0;
  height: 18px;
  line-height: 18px;
  border-top: 1px solid #ececec;
  border-bottom: 2px solid #ececec;
}

#courseSent .el-input__inner:focus {
  padding-bottom: 14px;
  border-bottom: 2px solid #1da3c5;
}
</style>
