<template>
  <div style="padding: 40px 0 60px 0;
            display: flex;
            flex-direction: column;
            align-items: center">
    <div style="    border-radius: 8px;
            border: 1px solid rgba(218,220,224,1);
            width: 1224px;
            height: 60px;
            padding: 24px;
            margin-bottom: 24px;
            box-sizing: border-box;">
    </div>
    <div style="width: 1224px">
      <span style="display:block;margin-bottom: 10px;font-size: 26px;">{{ homework.name }}</span>
      <p style="color: #707070;
                    overflow: hidden;
                    line-height: 1.8;
                    font-size: 14px;
                    margin-bottom: 30px;
                    clear: both;" v-html="homework.intro"></p>
      <span style="
                    font-size: 12px;
                    padding: 0 5px;
                    line-height: 20px;
                    margin-right: 10px;
                    margin-bottom: 40px;
                    background-color: #f1f3f4;
                    color: #5F6368;
                    border-radius: 2px;
                    float: left;">截止日期:{{ homework.endTime }}
        <span style="margin-left: 10px;">{{ homework.type === '0' ? '个人作业' : '小组作业' }}</span>
      </span>
    </div>
    <div style="width: 1224px;display: flex;justify-content: space-between">
      <el-button :disabled="fileListNum === 0 && fileLists.length === 0" @click="setFlag" v-if="flag" style="width: 102px;
                        background: #32BAF0;
                        color: #fff;" type="primary">
        {{ submit ? '更新提交' : '提交' }}
      </el-button>
      <el-button :disabled="fileLists.length === 0 && fileListNum === 0" @click="homeworkSubmit" v-if="!flag"
                 style="width: 102px;
                        background:#32BAF0;
                        color: #fff;" type="primary">
        {{ submit ? '更新提交' : '提交' }}
      </el-button>
      <span>{{ submit ? '已完成' : '未完成' }}</span>
    </div>


    <div style="width: 1224px;
        margin-top: 10px;
            border: 1px solid #E2E6ED;
            background: #FFF;
            border-radius: 8px;">
      <div class="all" v-for="files in fileLists">
        <div class="file">
          <div class="file-icon fl">
            <a v-bind:href="files.fileurl" class="fileext" title="Interact.xml">
              <img src="https://www.ketangpai.com/Public/Common/img/fileicon/file_ext_big_others.png" alt="">
            </a>
          </div>
          <div class="file-cont fl">
            <h3 class="file-name">
              <a v-bind:href="files.fileurl" class="fileext" title="Interact.xml">
                {{ files.filename }}
              </a>
            </h3>
            <div class="opt clearfix">
              <p class="file-size fl" style="line-height: 14px;">{{ files.fileSize }}k<a @click="deleteFile(files)" v-if="!flag" class="cancel hide">删除</a></p>
              <div class="state fl">
                <a v-bind:href="files.fileurl" class="download">下载</a>
              </div>

            </div>
          </div>
        </div>
      </div>
      <div style="padding: 25px 25px 0;" v-if="!flag || this.fileLists.length === 0">
        <el-upload class="upload-demo"
                   ref="upload"
                   drag
                   action="http://localhost:8888/api/v1/upload"
                   multiple
                   :headers="{'Authorization': this.$store.state.Authorization}"
                   :auto-upload="false"
                   :limit="5"
                   :on-change="onchange"
                   :on-remove="handleRemove"
                   :data="{homeworkIds:this.homeworkId,studentIds:this.info.user.account}">
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em>
            <div class="el-upload__tip" slot="tip">只能上传不超过500kb</div>
          </div>
        </el-upload>
      </div>
    </div>

    <div class="work-message clearfix" id="mess2">
      <span class="s1">作业留言：</span>
      <span class="s2" v-if="flag">{{ answer }}</span>
      <input class="s3" placeholder="留言" v-model="answer" v-if="!flag || this.fileLists.length === 0">
    </div>

  </div>
</template>

<script>
export default {
  name: "HomeworkSubmit",
  data() {
    return {
      homeworkId: localStorage.getItem('homeworkId'),
      homework: {},
      submit: false,
      answer: '',
      files: '',
      urlfile: '',
      fileLists: [],
      flag: true,
      fileListNum: 0,
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
    setFlag() {
      this.flag = false
    },
    getHomeworkData() {

      // this.$axios.post('/teacher/homeworkShow',
      //     {homeworkId:this.homeworkId},
      //     {
      //         headers: {'Authorization': this.$store.state.Authorization}
      //     }).then(resp => {
      //     if (resp.status === 200){
      //         this.homework = resp.data.homework
      //         for (let i = 0;i < resp.data.studentHomework.length;i++){
      //             if (this.info.user.id === resp.data.studentHomework[i].id){
      //                 this.submit = true
      //                 this.answer = resp.data.studentHomework[i].answer
      //             }
      //         }
      //     }
      // }).catch(reason => {
      //     console.log(reason)
      // })
    },
    getHomeworkDataByStudent() {
      this.$axios.post('/student/homeworkShow',
          {
            homeworkId: this.homeworkId,
            studentId: this.info.user.account,
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          if (resp.data.files.length !== 0) {
            this.submit = true
            this.fileLists = resp.data.files

            this.answer = resp.data.sw.answer
          } else {
            this.fileLists = []
          }

        }
      }).catch(reason => {
        console.log(reason)
      })
      this.$axios.post('/student/homework',
          {
            homeworkId: this.homeworkId,
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          this.homework = resp.data
        }
      }).catch(reason => {
        console.log(reason)
      })
    },
    homeworkSubmit() {
      let url = ''

      if (this.info.identity === 0 && ((this.info.user.account.startsWith('s') ? 0 : 1) === 0)) {
        url = '/student/homeworkSubmit'
      } else if (this.info.identity === 0 && ((this.info.user.account.startsWith('s') ? 0 : 1) === 1)) {
        url = '/teacher/homeworkSubmit'
      }
      this.$axios.post(url, {
            homeworkId: this.homeworkId,
            answer: this.answer,
            token: this.$store.state.Authorization
          },
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
          // this.$router.go(0)
        }
      })
      this.submitUpload()
      this.$router.go(0)
    },
    deleteFile(file) {
      if (this.fileLists.length > 1) {
        this.$axios.post("/v1/delete", {
              homeworkId: this.homeworkId,
              studentId: this.info.user.account,
              name: file.filename,
              id: file.id,
            },
            {
              headers: {'Authorization': this.$store.state.Authorization}
            }).then(() => {
          this.getHomeworkDataByStudent()
        }).catch(reason => {
          console.log(reason)
        })
      } else {
        this.fileLists = []
      }
    },
    handleRemove(file, fileList) {
      this.$message({
        center: true,
        message: '功能暂未开放',
        type: 'warning'
      })
    },
    submitUpload() {
      this.$refs.upload.submit();
    },
    onchange(file, fileList) {
      this.fileListNum = fileList.length
    }
  }
}
</script>

<style scoped>
.btn-upload {
  top: 70px;
  right: 40px;
  position: fixed;
  z-index: 100;
  border-radius: 30px;
  box-shadow: 0 2px 12px 0 rgba(91, 156, 255, 0.9)
}

.el-upload {
  margin: 5px;
}

.file {
  padding: 25px 25px 0;
  height: 64px;
}

.file .file-icon {
  margin-right: 20px;
}

.file .file-icon, .file .file-icon img {
  width: 55px;
  height: 55px;
}

.fl {
  float: left;
}

a {
  text-decoration: none;
  outline: none;
  blr: expression(this.onFocus=this.blur ());
  cursor: pointer;
}

.file .file-cont {
  margin-right: 30px;
}

.file .file-name {
  line-height: 30px;
  font-weight: normal;
  font-size: 14px;
  max-width: 600px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file .file-cont .opt {
  margin-top: 5px;
}

.file .file-size {
  font-size: 14px;
  color: #ababab;
  margin-right: 14px;
}

.mywork-page .file .download {
  line-height: 14px;
}

.all :hover a {
  color: #32BAF0;
}


.file .state a {
  font-size: 14px;
  margin-right: 11px;
}

.file .cancel {
  width: 30px;
  height: 14px;
  margin-top: 0;
  color: #32BAF0;
  margin-left: 20px;
}

.work-message {
  border: 1px solid #dcdcdc;
  border-radius: 0 0 8px 8px;
  line-height: 30px;
  padding: 24px 0;
  font-size: 0;
  cursor: text;
  margin-top: 10px;
  width: 1224px;

}

.s1 {
  font-size: 14px;
  width: 120px;
  padding-right: 35px;
  text-align: right;
  color: #010000;
  float: left;
}

.s2 {
  margin-top: 1px;
  float: left;
  font-size: 14px;
  color: #818181;
  width: 685px;
  word-break: break-all;
  word-wrap: break-word;
}

.s3 {
  margin-top: 10px;
  float: left;
  font-size: 14px;
  color: #818181;
  width: 685px;
  word-break: break-all;
  word-wrap: break-word;
}
</style>
