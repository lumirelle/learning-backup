<template>
  <!--    课程归档-->
  <div id="coursePigeonhole">
    <el-dialog :visible.sync="$store.state.show.coursePigeonhole" :lock-scroll="false"
               :close-on-click-modal="false" :show-close="false"
               top="235px" width="500px">
      <div slot="title">
        <span>要{{ info.course.pigeonhole === '0' ? "归档" : "恢复" }}此课程么？</span>
      </div>
      <div style="
                    height: 182px;
                    margin: -4px 26px 0;
                    line-height: 32px;
                    color: #2d2d2d;
                    font-size: 14px;">
        <div v-if="info.course.pigeonhole === '0'" style="display: flex;flex-direction: column;">
          <span>您可以在“课堂”-“归档管理”中查看此课程</span>
          <span v-if="this.info.course.identity === '1'">
            【归档全部】，学生的课程也会一起被归档
            <br/>
            【归档自己】，学生的课程不会被归档
          </span>
        </div>
        <div v-else style="display: flex;flex-direction: column">
          <span v-if="info.course.identity === '1'">
            您和您的学生将可以重新在此课程中互动。
          </span>
          <span>此课程会在课堂页上显示。</span>
        </div>
      </div>
      <div class="dialog-footer" slot="footer" style="height: 28px;font-size: 16px">
        <el-button @click="$store.commit('setShowCoursePigeonhole',false)" style="width:98px;height: 38px;padding:0 5px;font-size: 16px;">取消
        </el-button>
        <el-button @click="$confirm('归档后，全班学生的 该课程 将被移到“归档管理”里，学生也可以自行取消归档', '确定要归档全部？',{
                    showClose:false,closeOnClickModal:false,lockScroll:false
                }).then(() => {coursePigeonhole(1)})"
                   v-if="info.course.pigeonhole === '0' && this.info.course.identity === '1'"
                   style="width:98px;height: 38px;padding:0 5px;font-size: 16px;">归档全部
        </el-button>
        <el-button @click="coursePigeonhole(0)" type="primary" v-if="info.course.pigeonhole === '0'"
                   style="width:98px;height: 38px;padding:0 5px;font-size: 16px;margin-left: 20px;">
          {{ this.info.course.identity === '0' ? "归档" : "归档自己" }}
        </el-button>
        <el-button @click="coursePigeonhole(1)" type="primary" v-if="info.course.pigeonhole === '1'"
                   style="width:98px;height: 38px;padding:0 5px;font-size: 16px;margin-left: 20px;">恢复
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "CoursePigeonhole",
  props: ['info'],
  methods: {
    coursePigeonhole(all) {
      let api;
      if (this.info.course.identity === '0' && this.info.identity === 0) {
        api = '/student'
      } else if (this.info.course.identity !== '0' || (this.info.identity === 1 && this.info.course.identity === '0')) {
        api = '/teacher'
      } else {
        return
      }
      this.$axios.post(api + "/changePigeonhole", {
            all,
            id: this.info.user.id,
            courseId: this.info.course.courseId,
            pigeonhole: this.info.course.pigeonhole
          },
          {headers: {'Authorization': this.$store.state.Authorization}}
      ).then(resp => {
        this.$message({
          showClose: true,
          center: true,
          offset: this.$store.state.tip,
          message: resp.data
        })
        this.$emit('getCourse')
        this.$store.commit('setShowCoursePigeonhole', false)
      })
    }
  }
}
</script>

<style>
#coursePigeonhole .el-dialog__header {
  padding: 10px 26px;
  line-height: 52px;
  color: #a9a9a9;
  font-size: 18px;
}
</style>
