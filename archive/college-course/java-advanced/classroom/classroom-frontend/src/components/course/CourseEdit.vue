<template>
  <!--    课程创建和修改-->
  <div id="courseEdit">
    <el-dialog :visible.sync="$store.state.show.courseEdit" :show-close="false"
               :lock-scroll="false" destroy-on-close :close-on-click-modal="false"
               top="179px" width="660px">
      <div slot="title" style="font-weight: 400;font-size: 18px;color: #5A5A5A;">
        <span>{{ info.new === '1' ? "新建课程" : "编辑课程" }}</span>
        <i id="close" @click="$store.commit('setShowCourseEdit',false)"></i>
      </div>
      <el-form ref="course" :model="info.course" style="padding: 0 30px;margin-top: -28px"
               :rules="rules">
        <el-form-item class="blueInput" label="课程名称:" prop="name">
          <el-input placeholder="请输入课程名称" v-model="info.course.name"></el-input>
        </el-form-item>
        <el-form-item class="blueInput" label="班级名称:" prop="className" style="margin-top: -3px;">
          <el-input placeholder="请输入班级名称（选填）" v-model="info.course.className"></el-input>
        </el-form-item>
        <el-form-item label="选择学期:" style="margin-top: -3px;">
          <el-select v-model="year" style="width: 252px;">
            <el-option v-for="(year) in years" :key="year" :value="year"></el-option>
          </el-select>
          <el-select v-model="term" style="width: 252px;margin-left: 16px;">
            <el-option value="不限"></el-option>
            <el-option value="第一学期"></el-option>
            <el-option value="第二学期"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item style="margin-top: -8px;">
          <span style="font-size: 12px">学生必须额外填写下列信息才能加入课程</span>
        </el-form-item>
        <el-form-item style="margin-top: -26px;">
          <el-checkbox label="自然班级"></el-checkbox>
          <el-checkbox label="年级"></el-checkbox>
          <el-checkbox label="入学年月"></el-checkbox>
        </el-form-item>
      </el-form>
      <div class="dialog-footer" slot="footer"
           style="height: 40px;padding-top: 2px;font-size: 16px">
        <el-button @click="$store.commit('setShowCourseEdit',false)"
                   style="width:98px;height: 38px;padding:0 5px;font-size: 16px;">取消
        </el-button>
        <el-button @click="courseEdit" type="primary"
                   style="width:98px;height: 38px;padding:0 5px;font-size: 16px;margin-left: 20px;">
          {{ info.new === '1' ? "创建" : "确定" }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "CourseEdit",
  data() {
    let years = []
    const year = new Date().getFullYear()
    for (let i = year - 10; i < year + 3; i++) {
      years.push(i + "-" + (i + 1))
    }
    return {
      years,
      year,
      term: '',
      rules: {
        name: [
          {required: true, message: '请输入课程名称', trigger: 'blur'},
          {min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur'}
        ],
        className: [
          {min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur'}
        ]
      }
    }
  },
  props: ["info"],
  watch: {
    info: function (info) {
      this.year = info.course.year !== undefined ? info.course.year : this.years[10]
      this.term = info.course.term !== undefined ? info.course.term : '第一学期'
    }
  },
  methods: {
    courseEdit() {
      this.$refs['course'].validate(validate => {
        if (validate) {
          this.$axios.post("/teacher/courseEdit", {
            new: this.info.new,
            master: this.info.master,
            id: this.info.course.id,
            name: this.info.course.name,
            className: this.info.course.className,
            year: this.year,
            term: this.term,
            token: this.$store.state.Authorization
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
              this.$emit('getCourse')
              this.$store.commit('setShowCourseEdit', false)
            }
          }).catch(resp => {
            console.log(resp)
          })
        }
      })
    }
  }
};
</script>

<style>
#courseEdit .el-dialog__header {
  padding: 28px 30px;
}

#close {
  display: inline-block;
  width: 30px;
  height: 30px;
  position: absolute;
  right: 30px;
  top: 20px;
  background: url('https://www.ketangpai.com/Public/Home/img/close.png') no-repeat;
  cursor: pointer;
}

#close:hover {
  background-position: 0 -30px;
}

.blueInput .el-input__inner {
  padding: 0;
  border: 0;
  height: 36px;
  background: rgba(0, 0, 0, 0);
}

.blueInput {
  display: flex;
  margin-top: 26px;
  height: 36px;
  border-bottom: 2px solid #c8c8c8;
}

.blueInput:focus-within {
  border-bottom: 2px solid #409eff;
}
</style>
