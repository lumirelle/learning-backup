<template>
  <!--    作业卡片-->
  <div id="homeworkCard" style="border: 1px solid #E2E6ED;
                border-radius: 8px;
                background: #FFF;
                margin-top: 20px;
                z-index: 6">
    <el-card v-if="!edit" :body-style="{width:'100%',height:'100%',padding:'0'}"
             style="height: 100%;padding: 0 20px 0 40px">
      <div style="display: flex;flex-direction: column">
        <div style="display:flex;
                            justify-content: space-between;
                            color: #AAA;
                            padding: 21px 0 10px;
                            line-height: 30px;">
          <span style="color: #5F6368;">
            <span style="color: #5F6368;
                                    background: #F1F3F4;
                                    padding: 3px 5px;
                                    margin: 0 11px 0 -1px;
                                    border-radius: 2px;">{{ info.homework.type === '0' ? '个人作业' : '小组作业' }}</span>
            {{ info.homework.startTime }}
          </span>
          <div v-if="info.identity === 1">
            <el-dropdown @command="handleCommand" trigger="click">
              <div id="rowDots"></div>
              <el-dropdown-menu slot="dropdown" placement="bottom-end" style="margin: -26px 0 0 -20px;width: 120px;">
                <el-dropdown-item :command="1">编辑</el-dropdown-item>
                <el-dropdown-item :command="2">保存到...</el-dropdown-item>
                <el-dropdown-item :command="3">删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
        <div style="display: flex;justify-content: space-between">
          <div style="margin:11px 0 0 -1px">
            <span @click="jump(0)" class="homeworkName">{{ info.homework.name }}</span>
            <br/><span v-html='info.homework.intro'></span>
          </div>
          <div>
            <div v-if="info.identity === 1" style="margin:8px 8px 0 0;display: flex;text-align: center">
              <span @click="jump('0')" style="width: 75px;
                                          cursor: pointer;
                                          margin:-1px 0 0 15px;
                                          padding: 0 10px;">
                <span style="font-size: 30px;
                                            display: block;
                                            line-height: 48px;
                                            height: 50px;">{{ this.number }}</span>已批</span>
              <span @click="jump('0')" style="width: 75px;
                                        cursor: pointer;
                                        margin:-1px 0 0 15px;
                                        padding: 0 10px;">
                <span style="font-size: 30px;
                                            display: block;
                                            line-height: 48px;
                                            height: 50px;">{{ this.submitNum - this.number }}</span>未批</span>
              <span @click="jump('0')" style="width: 75px;
                                            cursor: pointer;
                                            margin:-1px 0 0 15px;
                                            padding: 0 10px;">
                <span style="font-size: 30px;
                                            display: block;
                                            line-height: 48px;
                                            height: 50px;">{{ this.unSubmitNum }}</span>未交</span>
            </div>
            <div v-else>
              <div id="homeworkInput" @click="jump('0')"
                   :style="{'background':info.homework ? '#32BAF0' : 'white','color':info.homework ? 'white' : '#32BAF0'}"
                   style="margin: 35px 38px -30px 0">
                {{ submit ? "已提交" : "上传作业" }}
              </div>
            </div>
          </div>
        </div>
        <span style="height: 20px;line-height: 20px;margin: 18px 0 18px -1px;color: #A0A0A0;">
          <img v-if="info.identity === 0"
               src="https://www.ketangpai.com/Public/Home/img/recruitment/ic_card_time@2x.png" alt=""
               style="width: 14px;height: 14px;line-height: 14px;"/>
          截止日期：{{ info.homework.endTime }}
          <span @click="jump('1')" class="discuss">0条评论</span>
        </span>
      </div>
    </el-card>
    <HomeworkEdit v-else :info="{homework:info.homework,new:'0'}" @close="edit = false"></HomeworkEdit>
  </div>
</template>

<script>
import HomeworkEdit from "./HomeworkEdit.vue";

export default {
  name: "HomeworkCard",
  components: {HomeworkEdit},
  data() {
    return {
      edit: false,
      number: '',
      submitNum: '',
      unSubmitNum: '',
      submit: false,
    }
  },
  props: ['info'],
  mounted() {
    if (this.info.identity === 1) {
      this.getHomeworkData()
    } else if (this.info.identity === 0) {
      this.getHomeworkDataByStudent()
    }

  },
  methods: {
    jump(homeworkView) {
      localStorage.setItem('homeworkView', homeworkView)
      localStorage.setItem('homeworkId', this.info.homework.id)
      this.$router.push('/homeworkInfo')
    },
    getHomeworkData() {
      this.$axios.post('/teacher/homeworkShow', {
        homeworkId: this.info.homework.id,
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          this.number = resp.data.number
          this.submitNum = resp.data.studentHomework.length
          this.unSubmitNum = resp.data.unHandIn.length
          if (this.info.identity === 0) {
            for (let n = 0; n < resp.data.studentHomework.length; n++) {
              if (this.info.user.id === resp.data.studentHomework[n].id) {
                this.submit = true
              }
            }
          }

        }
      }).catch(reason => {
        console.log(reason)
      })
    },
    getHomeworkDataByStudent() {
      this.$axios.post('/student/homeworkShow', {
            homeworkId: this.info.homework.id,
            studentId: this.info.user.account,
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {

        if (resp.status === 200) {
          if (resp.data.files.length !== 0) {
            this.submit = true
          }

        }
      }).catch(reason => {
        console.log(reason)
      })
    },
    handleCommand(command) {
      switch (parseInt(command)) {
        case 1:
          this.edit = true
          break
        case 2:
          this.$message({
            showClose: true,
            center: true,
            offset: this.$store.state.tip,
            message: '敬请期待'
          })
          break
        case 3:
          this.$confirm('', '确认要删除该次作业？', {lockScroll: false}).then(() => {
            this.$axios.post('/teacher/homeworkDelete', {
                  homeworkId: this.info.homework.id,
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
          })
      }
    }
  }
}
</script>

<style scoped>
#rowDots {
  display: block;
  margin-top: -11px;
  width: 34px;
  height: 34px;
  background: url('https://www.ketangpai.com/Public/Home/img/opt1.png') left center no-repeat;
  border-radius: 3px;
  cursor: pointer;
}

#rowDots:hover {
  background: url('https://www.ketangpai.com/Public/Home/img/opt1.png') right center no-repeat;
}

#homeworkCard .homeworkName {
  display: inline-block;
  margin: -8px 0 5px 0;
  color: #3B3D45;
  font-size: 20px;
  font-weight: 400;
  line-height: 30px;
  padding-bottom: 5px;
  word-break: break-all;
  word-wrap: break-word;
  cursor: pointer;
}

#homeworkCard:hover .homeworkName {
  color: blue;
}

#homeworkCard .homeworkName:hover {
  text-decoration: underline;
}

#homeworkCard .discuss {
  margin-left: 11px;
  cursor: pointer;
}

#homeworkCard .discuss:hover {
  color: #32BAF0;
}

#homeworkInput {
  width: 80px;
  height: 30px;
  line-height: 30px;
  color: #32BAF0;
  border: 1px solid #32BAF0;
  float: right;
  text-align: center;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

#homeworkInput:hover {
  background: #32BAF0;
}
</style>
