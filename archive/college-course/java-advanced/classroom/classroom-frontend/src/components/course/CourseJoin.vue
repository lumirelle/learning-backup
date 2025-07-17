<template>
  <!--    加课-->
  <div id="courseJoin">
    <el-dialog :visible.sync="$store.state.show.courseJoin" :show-close="false"
               :lock-scroll="false" destroy-on-close :close-on-click-modal="false"
               top="280px" width="295px" style="margin-left: 30px">
      <span slot="title">加入课程</span>
      <div style="padding: 20px 23px">
        <el-input placeholder="请输入课程加课验证码" v-model="key"></el-input>
      </div>
      <div slot="footer" style="height: 40px;text-align: center;padding-top: 2px;font-size: 16px">
        <el-button @click="$store.commit('setShowCourseJoin',false)"
                   style="width:98px;height: 38px;padding:0 5px;font-size: 16px;">取消
        </el-button>
        <el-button @click="courseJoin" type="primary"
                   style="width:98px;height: 38px;padding:0 5px;font-size: 16px;margin-left: 20px;">加入
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "CourseJoin",
  data() {
    return {
      key: "",
    }
  },
  props: ["info"],
  methods: {
    courseJoin() {
      if (this.key === "") {
        this.$message({
          showClose: true,
          center: true,
          offset: this.$store.state.tip,
          message: '加课码不能为空'
        })
      }
          // else if (this.key.length < 6){
          //
      // }
      else {
        let api;
        if (this.info.identity === 0) {
          api = '/student'
        } else if (this.info.identity === 1) {
          api = '/teacher'
        } else {
          alert(1)
          return
        }
        this.$axios.post(api + "/courseJoin",
            {id: this.info.user.id, key: this.key},
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
            if (resp.data === '加课成功') {
              this.$emit('getCourse')
              this.$store.commit('setShowCourseJoin', false)
            }
          }
        })
      }
    }
  }
}
</script>

<style>
#courseJoin .el-dialog__header {
  color: #a9a9a9;
  font-size: 18px;
  border-bottom: 1px solid #ececec;
  height: 66px;
  line-height: 66px;
  padding: 0 0 0 26px;
}

#courseJoin .el-input__inner {
  padding: 17px 0 14px 0;
  color: #2d2d2d;
  font-size: 14px;
  width: 100%;
  border: 0;
  height: 18px;
  line-height: 18px;
  border-bottom: 2px solid #ececec;
}

#courseJoin .el-input__inner:focus {
  padding-bottom: 14px;
  border-bottom: 2px solid #1da3c5;
}
</style>