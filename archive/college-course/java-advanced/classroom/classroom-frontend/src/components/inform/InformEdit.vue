<template>
  <div id="sapp">
    <!--发布公告点击页面-->
    <div class="publishWord home_work">
      <el-form ref="ruleForm" style="width: 1182px">
        <el-form-item prop="homeworkName" style="width: 1184px">
          <el-input placeholder="公告名称" v-model="informs.name"></el-input>
        </el-form-item>
        <div ref="editor">
        </div>
        <el-form-item>
          <el-button @click="$router.push('future')" type="primary" icon="el-icon-download"
                     style="margin-top: 85px;float: left;background: #32BAF0;color: #fff;">导入公告
          </el-button>
          <el-button type="primary" @click="informSubmit"
                     style="margin-top: 85px;
                                  float: right;
                                  color: #FFF;
                                  background: rgba(50,186,240,.7);
                                  cursor: default;">确定
          </el-button>
          <el-button @click="informs = {};$emit('close')"
                     style="margin-top: 85px;
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
  name: 'InformEdit',
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
      informs: {
        name: '',
        intro: '',
        time: '',
        top: '',
      }
    }
  },
  props: ['info'],
  created() {
    if (this.info.new === '0') {
      this.informs.name = this.info.informs.name
      this.informs.intro = this.info.informs.intro
      this.informs.time = this.info.informs.time
      this.informs.top = this.info.informs.top

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
    this.editor.txt.html(this.info.informs.intro)

  },
  methods: {
    informSubmit() {
      this.informs.intro = this.editor.txt.html();
      let errorTip = ''
      if (this.informs.intro === '') {
        errorTip = '公告内容不能为空'
      }

      if (this.informs.name === '') {
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

      this.$axios.post('/teacher/informEdit', {
            new: this.info.new,
            informId: this.info.informs.id,
            user: this.info.user.id,
            courseId: localStorage.getItem('courseId'),
            name: this.informs.name,
            intro: this.informs.intro,
            top: this.info.informs.top,

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
  height: 187px !important;
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
