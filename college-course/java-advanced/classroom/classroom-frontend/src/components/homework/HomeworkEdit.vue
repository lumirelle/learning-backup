<template>
  <div id="sapp">
    <!--发布作业弹窗-->
    <div class="publishWord home_work">
      <el-form ref="ruleForm" style="width: 1182px">
        <el-form-item prop="homeworkName" style="width: 1184px">
          <el-input placeholder="作业名称" v-model="homework.name"></el-input>
        </el-form-item>
        <div ref="editor">

        </div>
        <el-form-item label="截止日期" prop="endDate" class="display" style="width:400px;margin-top: 20px">
          <el-date-picker
              v-model="homework.endTime"
              type="date"
              placeholder="请选择日期"
              style="width:302px">
          </el-date-picker>
        </el-form-item>
        <el-form-item prop="endTime" class="display" v-if="homework.endTime !== ''" style="width:120px">
          <el-time-select
              v-model="homework.endTimeNext"
              :picker-options="{start: '00:00',step: '00:15',end: '23:59'}"
              placeholder="选择时间">
          </el-time-select>
        </el-form-item>
        <el-form-item label="若超时，禁止提交" prop="forbidSubmit" class="display" style="width:180px;margin-left:125px"
                      v-if="homework.endTime !== ''">
          <el-switch v-model="homework.overtime"></el-switch>
        </el-form-item>
        <el-form-item label="满分值" prop="bestScore">
          <el-input placeholder="" v-model="homework.maxScore" style="width:57px"></el-input>
        </el-form-item>
        <el-form-item label="是否查重" prop="needCheck" class="display" style="width:150px;margin-top: 20px">
          <el-switch v-model="needCheck"></el-switch>
        </el-form-item>
        <el-form-item label="查重警戒值" prop="checkAlertValue" class="display" style="width:350px"
                      v-if="needCheck === true">
          <el-input-number v-model="checkRate1" style="width:150px">
          </el-input-number>
          <span style="margin-left: 10px">%</span>
        </el-form-item>
        <el-form-item prop="duplicateCheckingRate" style="margin-left: 154px;" v-if="needCheck === true">
          <el-checkbox>查重率高于</el-checkbox>
          <el-input-number v-model="checkRate2" style="margin-left:10px;width:200px">
          </el-input-number>
          <span style="margin-left: 10px">%自动打回</span>
        </el-form-item>
        <el-form-item>
          <el-button @click="$router.push('future')" type="primary" icon="el-icon-download"
                     style="margin-top: 85px;float: left;background: #32BAF0;color: #fff;">导入作业
          </el-button>
          <el-button type="primary" @click="homeworkSubmit"
                     style="margin-top: 85px;
                                  float: right;
                                  color: #FFF;
                                  background: rgba(50,186,240,.7);
                                  cursor: default;">
            {{ info.new === '1' ? '发布个人作业' : '保存' }}
          </el-button>
          <el-button @click="homework = {};$emit('close')" style="margin-top: 85px;
                                    float: right;
                                    margin-right: 15px;
                                    color: #32BAF0;
                                    background: #FFF;">取消
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import E from 'wangeditor'

export default {
  name: 'HomeworkEdit',
  data() {
    const time = []
    for (let i = 0; i < 24; i++) {
      time.push((i < 10 ? '0' + i : i) + ':00')
      time.push((i < 10 ? '0' + i : i) + ':30')
    }
    time.push('23.59')
    return {
      needCheck: false,
      checkRate1: '',
      checkRate2: '',
      test: '',
      isClear: false,
      detail: "",
      time,
      homework: {
        name: '',
        intro: '',
        endTime: '',
        endTimeNext: '23:59',
        overtime: false,
        maxScore: '',
      }
    }
  },
  props: ['info'],
  created() {
    if (this.info.new === '0') {
      this.homework.name = this.info.homework.name
      this.homework.intro = this.info.homework.intro
      this.homework.endTime = this.info.homework.endTime
      this.homework.endTimeNext = this.info.homework.endTime.substr(11, 5)
      this.homework.overtime = this.info.homework.overtime
      this.homework.maxScore = this.info.homework.maxScore
    }
  },
  mounted() {

    this.editor = new E(this.$refs.editor)
    // 配置菜单栏
    this.editor.config.menus = [
      'head', // 标题
      'bold', // 粗体
      'fontSize', // 字号
      'fontName', // 字体
      'italic', // 斜体
      'underline', // 下划线
      'strikeThrough', // 删除线
      'foreColor', // 文字颜色
      'backColor', // 背景颜色
      'indent',//缩进
      'link', // 插入链接
      'list', // 列表
      'justify', // 对齐方式
      'quote', // 引用
      'undo', // 撤销
      'redo' // 重复
    ]

    // 创建富文本编辑器
    this.editor.create()
    this.editor.txt.html(this.info.homework.intro)
  },
  methods: {
    change(val) {
      console.log(val)
    },
    //格式化时间
    formatDate(date) {
      if (typeof date === 'string') {
        return date
      }

      // 获得时间
      let year = date.getFullYear();
      let month = date.getMonth() + 1;
      let day = date.getDate();
      if (month < 10) {
        month = "0" + month;
      }
      if (day < 10) {
        day = "0" + day;
      }
      return year + "/" + month + "/" + day;
    },

    //时间比较
    compare(new_date, new_date_next, now_date) {
      if (new_date.getFullYear() > now_date.getFullYear()) {
        return true;
      } else if (new_date.getFullYear() === now_date.getFullYear()) {
        if (new_date.getMonth() > now_date.getMonth()) {
          return true;
        } else if (new_date.getMonth() === now_date.getMonth()) {
          if (new_date.getDate() > now_date.getDate()) {
            return true;
          } else if (new_date.getDate() === now_date.getDate()) {
            return new_date_next > ((now_date.getHours() < 10 ? "0" + now_date.getHours()
                : now_date.getHours()) + ":" + (now_date.getMinutes() < 10 ? "0"
                + now_date.getMinutes() : now_date.getMinutes()));
          }
        }
      }
      return false;
    },
    homeworkSubmit() {
      this.homework.intro = this.editor.txt.html();
      let errorTip = ''
      if (this.homework.maxScore === '') {
        errorTip = '满分值不能为空'
      }
      if (this.homework.endTime === '' || this.homework.endTimeNext === '') {
        errorTip = '请选择日期'
      } else if (!this.compare(new Date(this.homework.endTime), this.homework.endTimeNext, new Date())) {
        errorTip = '请合理选择日期时间'
      }
      if (this.homework.name === '') {
        errorTip = '标题不能为空'
      }
      if (errorTip !== '') {
        this.$message({
          showClose: true,
          center: true,
          offset: this.$store.state.tip,
          message: errorTip
        })
        return
      }

      this.$axios.post('/teacher/homeworkEdit', {
            new: this.info.new,
            homeworkId: this.info.homework.id,
            courseId: localStorage.getItem('courseId'),
            name: this.homework.name,
            intro: this.homework.intro === '' ? '如题所述' : this.editor.txt.html(),
            overtime: this.homework.overtime,
            endTime: this.formatDate(new Date(this.homework.endTime)) + " " + this.homework.endTimeNext,
            maxScore: this.homework.maxScore,
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        this.$message({
          showClose: true,
          center: true,
          offset: this.$store.state.tip,
          message: resp.data
        })
        this.$router.go(0)
      })
    }
  }
}
</script>

<style scoped>
.w-e-toolbar {
  display: flex !important;
  padding: 0 6px !important;
  flex-wrap: wrap !important;
  position: static !important;
}

.w-e-text-container {
  border: 1px solid #ececec !important;
  border-radius: 4px !important;
  width: 1182px !important;
  height: 127px !important;
  position: static !important;
}

#sapp {
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}


.home_work {
  margin-top: 20px;
  padding: 20px 20px 10px;
  border: 1px solid #E2E6ED;
}

.display {
  display: inline-block;
}
</style>
