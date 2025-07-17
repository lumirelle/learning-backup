<template>
  <div>
    <div class="kt_header">
      <div class="left_header">
        <i style="font-size: 25px;margin:24px 10px 0 20px;cursor: pointer" @click="$router.go(-1)"
           class="el-icon-back"></i>
        <p style="margin-top: 26px;font-size: 20px">{{ name }}</p>
      </div>

      <div class="mi_header">
        <a style="cursor: pointer" class="share"></a>
        <a style="cursor: pointer" class="chat"></a>
        <a style="cursor: pointer" class="load"></a>
      </div>
      <div class="right_header">
        <div class="backHomework">打回作业</div>
        <div class="scoring">
          <label>成绩：<input type="text" v-model="score" placeholder=""/></label>
        </div>
        <div class="action">
          <el-button type="primary" @click="homeworkCheck">录入</el-button>
        </div>
      </div>
    </div>

    <div id="liuyan" class="">
      <ul>
        <li>
          <span class="fl">{{ name }}留言：</span>
          <p>{{ answer }}</p>
        </li>
      </ul>
    </div>

    <div style="width:65%;margin-top:103px;overflow:hidden;min-height:460px;
        margin-left:auto;margin-right:auto;border:solid 1px #c9c9c9; box-shadow: 0 0 2px 2px #d4d4d4;font-size: 22px;"
         v-for="f in fileLists">
      <div style="padding-left: 200px;padding-top:100px;display: inline-flex;">
        <a v-bind:href="f.fileurl" target="_blank">
          <img src="https://www.ketangpai.com/Public/Common/img/fileicon/file_ext_big_rar.png" class="show_rar_img">
        </a>
        <div class="item-download attach_file_download show_rar_title" style="margin-left:5px;margin-top: 46px;">
          <a target="_blank" v-bind:href="f.fileurl"
             style="color: #0000EE;text-decoration:underline;">{{ f.filename }}</a>
        </div>
        <div style="margin-top: 46px;margin-left: 10px">&nbsp;&nbsp;{{ f.fileSize }}KB</div>
      </div>
      <!--            <span v-for="f in fileLists">{{f.filename}}</span>-->
    </div>

  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      id: localStorage.getItem('studentId'),
      homeworkId: localStorage.getItem('homeworkId'),
      name: localStorage.getItem('name'),
      answer: localStorage.getItem('answer'),
      score: localStorage.getItem('score'),
      fileLists: []
    }
  },
  mounted() {
    if (this.score === 'null') {
      this.score = ''
    }

    this.getHomeworkData()
  },
  methods: {
    getHomeworkData() {
      // this.$axios.post('/teacher/homeworkShow',
      //     {homeworkId:this.homeworkId},
      //     {
      //         headers: {'Authorization': this.$store.state.Authorization}
      //     }).then(resp => {
      //     if (resp.status === 200){
      //         for (let i = 0;i < resp.data.studentHomework.length;i++){
      //             if (this.homeworkId === resp.data.studentHomework[i].homework){
      //                 this.id = resp.data.studentHomework[i].account
      //                 alert(resp.data.studentHomework[i].account)
      //                 this.name = resp.data.studentHomework[i].name
      //                 this.answer = resp.data.studentHomework[i].answer
      //             }
      //         }
      //     }
      // }).catch(reason => {
      //     console.log(reason)
      // })
      this.$axios.post('/student/homeworkShow',
          {
            homeworkId: this.homeworkId,
            studentId: this.id
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          if (resp.data.files.length !== 0) {
            this.fileLists = resp.data.files
          } else {
            this.fileLists = []
          }
        }
      }).catch(reason => {
        console.log(reason)
      })
    },
    homeworkCheck() {
      this.$axios.post('/teacher/homeworkCheck', {
        studentId: this.id,
        homeworkId: this.homeworkId,
        score: this.score
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
        }
      }).catch(reason => {
        console.log(reason)
      })
    }
  }
}
</script>

<style>
.kt_header {
  height: 73px;
  background: #333;
  position: fixed;
  width: 100%;
  left: 0;
  top: 0;
}

.left_header {
  display: flex;
  height: 73px;
  margin-left: 34px;
  color: #FFFFFF;
}

.mi_header a {
  float: left;
  width: 24px;
  height: 24px;
  margin-right: 38px;
  margin-top: 3px;
  background-repeat: no-repeat;
  background-size: cover;
}

.mi_header {
  height: 34px;
  position: absolute;
  top: 50%;
  margin-top: -16px;
  left: 50%;
  margin-left: -102px;
}

.share {
  background-image: url("https://www.ketangpai.com/Public/Home/img/navpizhu/share.png");

}

.chat {
  background-image: url("https://www.ketangpai.com/Public/Home/img/navpizhu/sixin.png");

}

.load {
  background-image: url("https://www.ketangpai.com/Public/Home/img/navpizhu/xiazai.png");
}

.right_header {
  width: 600px;
  height: 58px;
  position: absolute;
  right: 0;
  top: 15px;
}

.backHomework {
  width: 120px;
  font-size: 14px;
  color: #fff;
  text-align: center;
  border-radius: 20px;
  display: inline-block;
  float: left;
  margin-right: 20px;
  border: 1px solid #fff;
  background: 0 0;
  cursor: pointer;
  height: 39px;
  line-height: 39px;
}

.scoring {
  float: left;
  width: 132px;
  height: 39px;
  margin-left: 10px;
  background-color: rgba(250, 250, 250, .9);
  position: relative;
}

.scoring label {
  line-height: 39px;
  font-size: 14px;
  color: #595959;
  padding: 0 4px 0 18px;
}

.scoring input {
  width: 50px;
  font-size: 14px;
  color: #000;
  font-weight: 400;
  background-color: rgba(250, 250, 250, .9);
  border: 0;
}

.action {
  float: left;
  margin-left: 27px;
}

.lef_icon {
  border-radius: 3px 0 0 3px;
  width: 42px;
  height: 39px;
  line-height: 39px;
  text-align: center;
  background: rgba(250, 250, 250, .9);
  color: #333;
  font-weight: 700;
  float: left;
}

.right_icon {
  border-left: 1px solid rgba(0, 0, 0, .3);
  border-radius: 0 3px 3px 0;
  width: 42px;
  height: 39px;
  line-height: 39px;
  text-align: center;
  background: rgba(250, 250, 250, .9);
  color: #333;
  font-weight: 700;
  float: left;
}

#liuyan {
  width: 65%;
  margin: 113px auto 20px;
  padding: 18px 0;
  overflow: hidden;
  background-color: #F0E5B8;
}

.fl {
  float: left;
}

#liuyan li p, #liuyan li span {
  font-size: 14px;
}

#liuyan li p {
  padding-left: 100px;
}

#liuyan li {
  padding: 0 18px;
}
</style>

