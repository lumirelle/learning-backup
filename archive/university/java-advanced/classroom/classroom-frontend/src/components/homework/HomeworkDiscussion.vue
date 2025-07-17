<template>
  <div id="viewer-discuss" class="homework-cont gWidth">
    <div class="homework-list clearfix editor-page">
      <div class="homework-box" style="padding: 0 20px;">
        <div class="announce-cont-box">
          <div class="title clearfix"><h2 title="范例_第一次作业">{{ homework.name }}</h2></div>
          <div class="time">
            <div class="lefttime fl">
              截至：<span class="dates">{{ homework.endTime }}</span>
            </div>
            <div class="fr" style="display:block;">
              <div class="dldiscusschart fl">下载讨论内容</div>
              <div class="getWordCloud fl">展示词云</div>
            </div>
          </div>
          <div class="announce-cont">
            <div class="word">
              <div class="p special" v-html="homework.intro"></div>
            </div>
            <div class="annex">
              <ul class="clearfix">

              </ul>
            </div>
          </div>
        </div>
        <div class="comment-box">
          <div class="input-click clearfix" v-if="icon">
            <div class="img">
              <img v-bind:src="info.user.address">
              <span></span>
            </div>
            <p @click="icon2 = true;icon = false;">添加评论 </p>
          </div>

          <div class="add-comment" v-if="icon2">
            <div class="input-comment clearfix">
              <div class="img fl">
                <img v-bind:src="info.user.address">
                <span></span>
              </div>
              <div class="input fr">
                <textarea autofocus="autofocus" class="comment-txt" placeholder="添加评论"
                          style="resize: none; height: 30px;" v-model="word"></textarea>
                <p class="rr"></p>
                <div class="annex-box"></div>
              </div>
            </div>
            <div class="opt-cont">

              <div class="opt-btn fr">
                <a class="cancel" @click="icon=true;icon2=false;cancels()">取消</a>
                <a class="sure" @click="icon=true;icon2=false;setComment()">确定</a>
              </div>
            </div>
          </div>

          <ul class="comment-list">
            <li v-for="item in comments">
              <div class="img fl">
                <img v-bind:src="item.image" alt="">
                <span></span>
              </div>
              <div class="com-det fl">
                <div class="title clearfix">
                  <div class="name fl">
                    <span>{{ item.name }}</span>
                    <span class="time">{{ item.time }}</span>
                  </div>
                  <div class="r-d fr">
                    <a class="reply" @click="reply(item.name)" title="回复"></a>
                    <a class="del" title="删除" @click="deleted(item.id)"
                       v-if="((item.account !== accounts) && (item.account === info.user.account)) || (info.identity===1) "></a>
                  </div>
                </div>
                <p class="cont">{{ item.word }}<br></p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>


</template>

<script>
export default {
  name: "HomeworkDiscussion",
  data() {
    return {
      icon: true,
      icon2: false,
      homework: {},
      homeworkId: localStorage.getItem('homeworkId'),
      word: '',
      comment: {},
      accounts: '',
      // comment:{},
      comments: [],
    }
  },
  props: ['info'],
  mounted() {
    this.getTeacher()
    this.getHomeworkByStudent()
    this.getComment()

  },
  methods: {
    getTeacher() {
      this.$axios.post('/start/master', {
        master: this.info.course.master,
      }, {
        headers: {'Authorization': this.$store.state.Authorization}
      }).then(resp => {
        this.accounts = resp.data
      })
    },
    //格式化时间
    formatDate(date) {
      if (typeof date === 'string') {
        return date
      }

      let year = date.getFullYear();
      let month = date.getMonth() + 1;
      let day = date.getDate();
      let hour = date.getHours();
      let minutes = date.getMinutes()

      month = month < 10 ? "0" + month : month
      day = day < 10 ? "0" + day : day
      hour = hour < 10 ? "0" + hour : hour
      minutes = minutes < 10 ? "0" + minutes : minutes

      return year + "年" + month + "月" + day + "日" + " " + hour + ":" + minutes;
    },
    reply(name) {
      this.word = this.word !== '' ? '' : "@" + name + " "
      this.icon2 = true;
      this.icon = false;
    },
    cancels() {
      this.word = ''
    },
    deleted(id) {
      this.$axios.post('/start/deleteComments', {
        id: id,
      }, {
        headers: {'Authorization': this.$store.state.Authorization}
      }).then(resp => {
        this.$message({
          showClose: true,
          center: true,
          offset: this.$store.state.tip,
          message: resp.data
        })
        this.comments = resp.data
        this.getComment()
      })
    },
    getComment() {
      this.$axios.post('/start/comments', {
        homeworkId: this.homeworkId,
      }, {
        headers: {'Authorization': this.$store.state.Authorization}
      }).then(resp => {
        this.comments = resp.data
      })
    },
    setComment() {
      if (this.word !== "") {
        this.$axios.post('/start/discuss', {
          account: this.info.user.account,
          name: this.info.user.name,
          time: this.formatDate(new Date()),
          image: this.info.user.address,
          word: this.word,
          homeworkid: this.homeworkId
        }, {
          headers: {'Authorization': this.$store.state.Authorization}
        }).then(resp => {
          this.$message({
            showClose: true,
            center: true,
            offset: this.$store.state.tip,
            message: resp.data
          })
          this.getComment()
        })

      }

    },
    getHomeworkByStudent() {
      this.$axios.post('/start/homework',
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
    }
  }
}
</script>

<style scoped>

.homework-cont, .send-box {
  margin-top: 20px;
}

.gWidth {
  width: 810px;
  margin-left: auto;
  margin-right: auto;
}

.homework-box {
  padding: 0 20px 0 40px;
  border: 1px solid #E2E6ED;
  border-radius: 8px;
  background: #FFF;
  margin-bottom: 20px;
}

.announce-cont-box {
  padding: 0 20px 10px 30px;
  position: relative;
}

.clearfix :after {
  content: "";
  display: table;
  clear: both;
}

.announce-cont-box .title h2 {
  padding-top: 10px;
  float: left;
  height: 50px;
  line-height: 50px;
  font-size: 18px;
  font-weight: 400;
  width: 100%;
}

.dates {
  height: 34px;
  width: 100px;
}

.announce-cont-box .time {
  height: 34px;
  padding-top: 7px;
  line-height: 34px;
  font-size: 12px;
  color: #A0A0A0;
  margin-bottom: 6px;
}

.fl {
  float: left;
}

.fr {
  float: right;
}

.dldiscusschart {
  color: #666;
  padding-left: 20px;
  background: url(https://www.ketangpai.com/Public/Home/img/downg.png) left no-repeat;
  cursor: pointer;
  user-select: none;
}

.dldiscusschart, .downcharts i {
  padding-right: 10px;
}

.getWordCloud {
  display: inline-block;
  background: #357ae8;
  border: 1px solid #357ae8;
  color: #fff;
  margin: 7px 10px;
  height: 20px;
  line-height: 20px;
  font-size: 12px;
  padding-left: 5px;
  padding-right: 5px;
  cursor: pointer;
  border-radius: 3px;
}

.word .p {
  color: #707070;
  overflow: hidden;
  line-height: 1.8;
}

.annex {
  max-height: 110px;
  margin-top: 10px;
  overflow: hidden;
}

.input-click {
  border-top: 1px solid #dcdcdc;
  padding: 20px 24px;
  cursor: text;
}

.img {
  width: 37px;
  height: 37px;
  position: relative;
  float: left;
  margin-right: 18px;
  margin-top: 10px;
  margin-left: 0;
}

.comment-box .img img {
  width: 37px;
  height: 37px;
  border-radius: 37px;
  background-repeat: no-repeat;
  /* background-position: center; */
}

.input-click p {
  line-height: 38px;
  float: left;
  color: grey;
}

.comment-list > li {
  zoom: 1;
  padding: 10px 24px;
  border-top: 1px solid #dcdcdc;
  list-style: none;
}

.com-det {
  width: calc(100% - 75px);
}

.com-det .name {
  color: #2d2d2d;
  line-height: 32px;
}

.com-det .name span {
  float: left;
  margin-right: 18px;
}

.com-det .name span.time {
  color: #a9a9a9;
  font-size: 12px;
}

.com-det .cont {
  line-height: 32px;
}

.reply {
  background: url(https://www.ketangpai.com/Public/Home/img/reply.png) left top no-repeat;
}

.com-det .r-d a {
  width: 32px;
  height: 32px;
  display: inline-block;
  border-radius: 3px;
}

.del {
  background: url(https://www.ketangpai.com/Public/Home/img/del2.png) left top no-repeat;
  margin-left: 20px;
}

.r-d {
  height: 32px;
  display: none;
}

.comment-list > li:hover .r-d {
  display: block;
}

.add-comment {
  background: #FFF;
  border-top: 1px solid #dcdcdc;
}

.input-comment {
  padding: 30px 34px 0;
}

.input {
  width: calc(100% - 75px);
  position: relative;
}

.comment-txt {
  width: calc(100% - 75px);
  padding: 0 20px;
  border-bottom: 1px solid #ececec;
  line-height: 30px;
  height: 30px;
  word-break: break-all;
  display: block;
  overflow: hidden;
  margin-top: 6px;
  font-size: 14px;
}

.annex-box {
  background: #f6f6f6;
  position: relative;
  border: 0 !important;
  margin: 0 !important;
  padding-bottom: 5px;
}

.opt-cont {
  height: 64px;
  padding: 26px 33px 0 79px;
}

.opt-btn {
  height: 38px;
  text-align: right;
  font-size: 0;
}

.opt-btn a {
  display: inline-block;
  width: 84px;
  height: 38px;
  line-height: 38px;
  text-align: center;
  font-size: 16px;
  border-radius: 3px;
}

input, select, textarea {
  outline: none;
  border: none;
  background: none;
  cursor: text;
}

.cancel {
  color: #818181;
  margin-right: 15px;
  background: #FFF;
}

.sure {
  color: #FFF;
  background: #9abdf4;
  cursor: default;
}

.comment-txt:focus {
  border-bottom: 2px solid #4d90fe;
}
</style>
