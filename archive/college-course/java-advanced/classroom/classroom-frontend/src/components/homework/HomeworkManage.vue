<template>
  <div id="app">
    <div class="header">

      <div class="header-right">
        <span><i class="iconfont iconxiaoxi1" style="font-size: 32px;color: #5F6368;"></i></span>
        <span><img src="" style="width:30px;height:30px" alt=""></span>
      </div>
    </div>

    <div class="new-page">
      <div class="head-title">
        <div class="homework-title">
          <h2 style="float:left;margin-top: -8px;">{{ homework.name }}</h2>
          <div class="generate-final-grade" style="font-size:14px">生成期末考成绩</div>
        </div>
        <div class="togsh">
          <p class="fl">截至&nbsp;&nbsp;{{ homework.endTime }}</p>
          <div style="float:right;margin-top:-8px">
            <span class="check"><i class="el-icon-s-tools"></i>&nbsp;&nbsp;查重设置</span>
            <span>
                <el-select class="select" v-model="hide" style="background: #357ae8;color: white">
                  <el-option
                      v-for="item in options1"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value">
                  </el-option>
                </el-select>
              </span>
            <span class="togsh-input">
                <el-input
                    class="input"
                    style="width:200px"
                    placeholder="学号、姓名"
                    suffix-icon="el-icon-search"
                >
                </el-input>
              </span>
          </div>

        </div>
      </div>
      <div class="reviewwrap">
        <div class="select-group">
          <ul class="typehead" style="margin-top: 20px">
            <li class="body_next_1" datatype="text">
              <div id="studyNumTitle" class="dt"><i class="downdot"></i></div>
            </li>
            <li class="body_next_6" datatype="text">
              <div id="" class="dt">学号<i class="downdot"></i></div>
            </li>
            <li class="body_next_3" id="li-results" datatype="num">
              <div id="results" class="dt">姓名<i class="downdot"></i></div>
            </li>
            <li class="body_next_4" id="li-similarity" datatype="Float">
              <div id="similarity" class="dt">成绩<i class="downdot"></i></div>
            </li>
            <li class="body_next_4" id="li-submitState" datatype="num">
              <div id="submitState" class="dt">提交时间<i class="downdot"></i></div>
            </li>
            <li class="body_next_4" id="li-homeworkNumber" datatype="num">
              <div id="homeworkNumber" class="dt">提交状态<i class="downdot"></i></div>
            </li>
          </ul>
        </div>

        <div class="glist">
          <div class="head">
            <span style="margin-left: 20px;"><el-checkbox v-model="selectedAll" @click="selectAll()"></el-checkbox></span>
            <span style="margin-top: -22px">
              <el-select :disabled="!selectedAll" placeholder="批量给分" class="giveGrade" size="small" v-model="number">
                <el-option
                    v-for="item2 in options2"
                    :key="item2.value"
                    :label="item2.label"
                    :value="item2.value">
                </el-option>
              </el-select>
            </span>
            <span style="margin: -4px 0 0 30px"><el-button type="primary" class="backwork"
                                                           style="height:32px;text-align: center">打回作业</el-button></span>
            <span style="margin:-28px 0 0 30px">
              <el-select :disabled="!selectedAll" placeholder="下载" class="download" size="small" v-model="save">
                <el-option
                    v-for="item3 in options3"
                    :key="item3.value"
                    :label="item3.label"
                    :value="item3.value">
                </el-option>
              </el-select>
            </span>
          </div>
          <div class="body">
            <div class="homeworkmanage" v-for="(item4,index) in studentHomework" :key="index">
              <span class="body_1"><el-checkbox v-model="item4.select"
                                                @click="selectStudent(index)"></el-checkbox></span>
              <span class="body_6">{{ item4.account }}</span>
              <span class="body_3">{{ item4.name }}</span>
              <span class="body_4" v-if="item4.score!==null">
              {{ item4.score }}/{{ homework.maxScore }}
              </span>
              <span class="body_4" v-else-if="item4.review==='0'">
                未批/{{ homework.maxScore }}
              </span>
              <span class="body_4" v-else-if="item4.review==='1'">
                已批/{{ homework.maxScore }}
              </span>
              <span class="body_4 ">{{ item4.time }}</span>
              <span class="body_4 body_9" @click="readHomework('1',item4)">进入批阅</span>
            </div>
            <div class="homeworkmanage" v-for="(item5,index2) in unHandIn" :key="'info2-'+index2">
              <span class="body_1"><el-checkbox v-model="item5.select" @click="selectStudent(index2)"></el-checkbox></span>
              <span class="body_6">{{ item5.account }}</span>
              <span class="body_3">{{ item5.name }}</span>
              <span class="body_4" style="color: red">未交</span>
              <span class="body_4">--</span>
              <span class="body_4" @click="readHomework('0',item5)">催交</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeworkManage',
  data() {
    return {
      options1: [{
        value: '对学生隐藏成绩',
        label: '对学生隐藏成绩'
      }, {
        value: '对学生显示成绩',
        label: '对学生显示成绩'
      }],
      options2: [{
        value: '统一给分',
        label: '统一给分'
      }, {
        value: '区间给分',
        label: '区间给分'
      }],
      options3: [{
        value: '下载作业文档',
        label: '下载作业文档'
      }, {
        value: '下载选中的报表',
        label: '下载选中的报表'
      }, {
        value: '下载全班的报表',
        label: '下载全班的报表'
      }],
      selectedAll: false,
      hide: '对学生隐藏成绩',
      number: '',
      save: '',
      studentHomework: [{
        select: false,
        account: '',
        phone: '',
        mailbox: '',
        name: '',
        school: '',
        time: '',
        file: '',
        score: '',
        review: '',
      }],
      homework: {
        id: '',
        course: '',
        name: '',
        intro: '',
        startTime: '',
        endTime: '',
        maxScore: '',
        file: '',
        type: '',
        overtime: '',
      },
      unHandIn: [{
        id: '',
        account: '',
        phone: '',
        mailbox: '',
        name: '',
        school: '',
      }]
    }
  },
  methods: {

    selectAll() {

    },
    selectStudent(i) {
      if (this.studentHomework[i].select === true) {
        this.studentHomework[i].select = false
      } else {
        this.studentHomework[i].select = false;
      }
    },
    readHomework(check, item) {
      if (check === '1') {
        localStorage.setItem('studentId', item.account)
        localStorage.setItem('homeworkId', item.homework)
        localStorage.setItem('name', item.name)
        localStorage.setItem('answer', item.answer)
        localStorage.setItem('score', item.score)
        this.$router.push('/homeworkCheck')
      } else {
        this.$message({
          showClose: true,
          center: true,
          offset: this.$store.state.tip,
          message: '敬请期待'
        })
      }
    }
  },
  created() {
    const _this = this
    this.$axios.post("/teacher/homeworkShow",
        {homeworkId: localStorage.getItem('homeworkId')},
        {headers: {'Authorization': this.$store.state.Authorization}}
    ).then(function (rep) {
      _this.studentHomework = rep.data.studentHomework;
      _this.homework = rep.data.homework;
      _this.unHandIn = rep.data.unHandIn;
    })

    if (this.selectedAll === true) {
      this.studentHomework.select = true;
    } else {
      for (const element of this.studentHomework) {
        element.select = false;
      }
    }
  }
}


</script>

<style scoped>

#menu li {
  width: 140px;
  height: 64px;
  margin: 3px;
  background: red;
  list-style: none;
  float: left;
}

.header {
  box-shadow: 0px 1px 3px 0px rgba(0, 0, 0, 0.04);
  background: none repeat scroll 0% 0% #fff;
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  height: 45px;
}

.header-right {
  position: relative;
  right: 100px;

}

.header-center span {
  height: 74px;
  padding-left: 50px;
  padding-right: 50px;
  font-size: 16px;
  font-weight: 500;
  color: rgba(59, 61, 69, 1);
  padding-bottom: 20px;
  cursor: pointer;

}

.header-center span:hover {
  border-bottom: 4px solid #2C58AB;
}

.head-title {
  padding: 39px 30px 27px;
  height: 80px;
  position: relative;
  border-bottom: 1px solid #c8c8c8;
}

.homework-title {
  display: inline-block;
  width: 100%;
}

.head-title h2 {
  font-size: 18px;
  color: #2d2d2d;
  font-weight: 400;
  display: inline-block;
}

.homework-title .generate-final-grade {
  height: 26px;
  line-height: 26px;
  display: block;
  text-align: center;
  background-color: #fff;
  color: #818181;
  cursor: pointer;
  -webkit-user-select: none;
  border-radius: 3px;
  padding: 0 10px;
  float: right;
  border: 1px solid #ccc;
}

.togsh {
  padding-top: 25px;

}

.head-title .togsh p {
  padding: 5px 20px;
  margin-right: 10px;
  background: rgba(0, 0, 0, .1);
  font-size: 14px;
  margin-top: -5px;
}

.new-page {
  background: #FFF;
  border: 1px solid #c8c8c8;
  width: 1020px;
  margin: 40px auto;
}

.check {
  height: 30px;
  line-height: 30px;
  margin-right: 15px;
  color: #999;
  padding-left: 20px;
  cursor: pointer;
  user-select: none;
  font-size: 14px;
}

.select /deep/ .el-input .el-select__caret {
  color: white;
  position: relative;
  top: 5px;
}

.select /deep/ .el-input__inner {
  background-color: #357ae8;
  color: white;
  height: 30px;
  width: 150px;
}

.togsh-input {
  margin-left: 15px;
}

.input /deep/ .el-input__inner {
  width: 200px;
  height: 32px;
}

.classify dt {
  color: #5A5A5A;
  line-height: 46px;
  height: 46px;
  width: 150px;
  border-right: 1px solid #DCDCDC;
  background: #F2F2F2;
  text-indent: 30px;
  font-size: 12px;
  margin-right: 36px;
  border-bottom: 1px dashed #eaeaea;
  margin-bottom: -1px;
}

.classify li.cur {
  background: #357AE8;
  border-color: #357AE8;
  border-radius: 2px;
  cursor: default;
  color: #FFF;
}

.classify li {
  width: 48px;
  height: 20px;
  line-height: 20px;
  margin-right: 30px;
  border: 1px solid transparent;
  cursor: pointer;
  font-size: 12px;
  text-align: center;
}

.classify el-checkbox {
  padding-left: 25px;
  font-size: 12px;
  color: #5a5a5a;
  border-radius: 3px;
  height: 33px;
  line-height: 33px;
  width: 83px;
  cursor: pointer;
}

.classify div.input-dd {
  width: 118px;
  height: 27px;
  font-size: 12px;
}

.classify .curBtn {
  color: #818181;
  height: 33px;
  line-height: 33px;
  width: 60px;
  outline: 0;
  border: none;
  cursor: pointer;
  font-size: 12px;
  position: relative;
  bottom: 65px;
  left: 344px;
}

input.input-sm {
  width: 48px;
  height: 20px;
  line-height: 20px;
  border: 1px solid #d2d2d2;

}

span.sm-line {
  width: 8px;
  overflow: hidden;
  line-height: .8;
  position: relative;
  bottom: 35px;
  left: 198px;
  z-index: 1;

}

li {
  list-style: none;
}

.select-group {
  padding: 20px 0;
  position: relative;
  margin: 0 30px;
}

.typehead {
  height: 40px;
  border: 1px solid #DCDCDC;
  background-color: #F6F6F6;
}

.typehead li {
  display: inline-block;
  float: left;
  vertical-align: middle;
}

.typehead li div {
  display: block;
  width: 100px;
  line-height: 38px;
  border: 1px solid transparent;
  margin-left: -1px;
  color: #595959;
  font-size: 12px;
}

.dt {
  cursor: pointer;
}

.dt:hover {
  color: #4d90fe;
}

.glist {
  margin: 0 30px 60px;
}

.head {
  display: flex;
  align-items: center;
  height: 60px;
  border: 1px solid #DCDCDC;
  background: #F6F6F6;
  font-weight: 400;

  margin-bottom: -1px;
}

.giveGrade {
  width: 100px;
  height: 32px;
  position: relative;
  bottom: -9px;
  margin-left: 30px;
}

.backwork /deep/ .el-button {
  height: 32px;
  width: 76px;
  position: relative;
  top: 12px;


}

.backwork /deep/ span {
  position: relative;
  bottom: 3px;
  right: 4px;
  font-size: 12px;
}

.download {
  width: 140px;
  position: relative;
  top: 10px;
  margin-top: 5px;
  font-size: 12px;
}

.homeworkmanage {
  height: 62px;
  width: 958px;
  border: 1px solid #DCDCDC;
  font-size: 14px;
  color: #595959;
}

.homeworkmanage:hover {
  background: #dcdcdc;
}

.body_1 {
  margin-left: 20px;
  position: relative;
  top: 22px;
  width: 2%;
  display: block;
  float: left;
  overflow: hidden
}

.body_next_1 {
  margin-left: 20px;
  position: relative;
  width: 2%;
  display: block;
  float: left;
  overflow: hidden
}

.body_6 {
  position: relative;
  margin-left: 50px;
  top: 22px;
  width: 9%;
  display: block;
  float: left;
  overflow: hidden;
  color: #2D2D2D;
  font-weight: 700;
}

.body_next_6 {
  position: relative;
  margin-left: 50px;
  width: 9%;
  display: block;
  float: left;
  overflow: hidden;
  color: #2D2D2D;
  font-weight: 700;
}

.body_3 {
  position: relative;
  margin-left: 50px;
  top: 21px;
  /*width: 80px;*/
  font-weight: 700;
  color: #2D2D2D;
  width: 10%;
  display: block;
  float: left;
  overflow: hidden

}

.body_next_3 {
  position: relative;
  margin-left: 50px;
  /*width: 80px;*/
  font-weight: 700;
  color: #2D2D2D;
  width: 10%;
  display: block;
  float: left;
  overflow: hidden

}

.body_4 {
  margin-left: 50px;
  position: relative;
  top: 22px;
  width: 15%;
  display: block;
  float: left;
  overflow: hidden;
  text-align: center;

}

.body_next_4 {
  margin-left: 50px;
  position: relative;
  width: 15%;
  display: block;
  float: left;
  overflow: hidden;
  text-align: center;

}

.body_9 {
  cursor: pointer;
  font-size: 16px;
  color: #4D90FE;
  width: 10%;
  display: block;
  float: left;
  overflow: hidden
}

.marktime /deep/ .el-button {
  border: 0px solid white;
  padding: 0;
  background: white;
}

.inputCase /deep/ .el-input__inner {
  height: 26px;
  border-top: none;
  border-left: none;
  border-right: none;
}
</style>
