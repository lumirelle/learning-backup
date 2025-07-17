<template>
  <div>
    <!--顶端-->
    <div id="title">
      <div id="goBack" ref="CourseInfoLP.vue">
        <!--        返回上一页-->
        <i @click="$router.go(-1)" class="el-icon-back"></i>
        <!--        跳转到课程信息页-->
        <a @click="$router.push('/courseInfo')" id="familiar_keTangPai">{{ course.name }}</a>
      </div>

      <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal">
        <el-menu-item @click="choosePage('0')" index="1">成员</el-menu-item>
        <el-menu-item @click="choosePage('1')" index="2">学生分组</el-menu-item>
        <el-menu-item @click="choosePage('2')" index="3">成绩管理</el-menu-item>
      </el-menu>

      <div id="data">
        <AccountTools :info="{user}"></AccountTools>
      </div>
    </div>

    <div id="pageContent">
      <!--成员页面-->
      <div id="stuPage" v-if="page === '0'">
        <!--            写死了-->
        <div id="stuPage_top">
          <div id="stuPage_top_first">
            <a id="importMember" @click="importMem(1)">导入成员</a>
            <a id="downloadMemberInfo"><i class="el-icon-upload"></i>下载成员信息</a>
            <a id="memberWithdrawalRecord"><i class="el-icon-s-order"></i>成员退课记录</a>
          </div>
          <div id="stuPage_top_second">
            <label>
              <input type="text" placeholder="姓名、学号">
            </label>
            <i class="el-icon-search" id="search1"></i>
          </div>
        </div>
        <div id="stuPage_content">
          <!--        左部导航栏-->
          <div id="stuPage_left">
            <a id="techNumber" @click="chooseShow(0)">教学团队({{ teacher.length }})</a>
            <a id="stuNumber" @click="chooseShow(1)">全部学生(学生{{ student.length }})</a>
          </div>
          <!--          右部成员信息-->
          <div id="stuPage_right">
            <!--            教师团队-->
            <div id="tech_show" v-if="chooseShowNum === 0">
              <div id="tech_show_title">
                <a>教学团队(老师{{ teacher.length }})</a>
                <a v-if="identity === '1'" id="add_teach" @click="addTeacher(1)">
                  <i class="el-icon-s-custom"></i>添加助教/老师
                </a>
              </div>
              <div id="tech_show_content">
                <ul id="teach_ul">
                  <li v-for="(item,i) in this.teacher" :key="i">
                    <el-avatar :style="{'background':'url(' + item.address + ') no-repeat center'}"></el-avatar>
                    <a>{{ item.name }}</a>
                    <a>{{ item.phone + "    (" + item.role + ")" }}</a>
                    <span class="private_community">
                      <i class="el-icon-chat-dot-round"></i>
                      <el-dropdown trigger="click" v-if="identity === '1'">
                        <span class="el-dropdown-link">
                          <i class="el-icon-more"></i>
                        </span>
                        <el-dropdown-menu style="width: 100px">
                          <a><el-dropdown-item>私信</el-dropdown-item></a>
                          <a @click="delInfo(item)"><el-dropdown-item>删除</el-dropdown-item></a>
                        </el-dropdown-menu>
                      </el-dropdown>
                    </span>
                  </li>
                </ul>
              </div>
            </div>
            <!--    全部学生信息  -->
            <div id="stu_show" v-if="chooseShowNum === 1">
              <div id="stu_show_title">
                <div id="stu_show_title_first">
                  <a>全部学生(学生{{ student.length }})</a>
                </div>
                <div v-if="identity === '1'" id="stu_show_title_second">
                  <a>
                    <label><input type="checkBox" style="width: 15px;height: 15px"></label>人数限制
                  </a>
                  <a>
                    <el-switch
                        v-model="value"
                        active-color="rgb(50,186,240)"
                        inactive-color="rgb(204,204,204)">
                    </el-switch>
                    不允许退课
                  </a>
                </div>
              </div>
              <div id="stu_show_content">
                <div id="stu_div">
                  <label><input type="checkBox" style="width: 15px;height: 15px"></label>
                  <span>本页全选 已经选(<object>0</object>)人</span>
                  <a>删除成员</a>
                  <a>群发私信</a>
                </div>
                <ul id="stu_ul">
                  <li v-for="(item,i) in this.student" :key="i">
                    <a id="stu_ul_input"><label><input type="checkBox"></label></a><!--input-->
                    <a id="stu_ul_avatar">
                      <el-avatar :style="{'background':'url(' + item.address + ') no-repeat center'}"></el-avatar>
                    </a>
                    <a id="stu_ul_schoolNum">{{ item.schoolNum }}</a>
                    <a id="stu_ul_name">{{ item.name }}</a>
                    <a id="stu_ul_siXin"><img src="https://www.ketangpai.com/Public/Home/img/sixin.png" alt=""></a>
                    <a id="stu_ul_more">
                      <el-dropdown v-if="identity === '1'" trigger="click">
                        <span class="el-dropdown-link">
                          <i class="el-icon-more"></i>
                        </span>
                        <el-dropdown-menu style="width: 100px">
                          <el-dropdown-item>私信</el-dropdown-item>
                          <a @click="delInfo(item)">
                            <el-dropdown-item>删除</el-dropdown-item>
                          </a>
                        </el-dropdown-menu>
                      </el-dropdown>
                    </a>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!--学生分组页面  死页面-->
      <div id="stuSortPage" v-if="page === '1'">
        <!--        固定分组栏-->
        <div id="stuSortPage_first">
          <div id="stuSortPage_first_firstDiv">
            <span>固定分组</span>
            <span>课程固定分组</span>
            <span><a>*本课程主要应用与成绩统计,考勤等活动的默认分组</a></span>
          </div>
          <div id="stuSortPage_first_twoDiv">
            <div>
              <span>组数</span>
              <span>0</span>
            </div>
            <div>
              <span>未进组人数</span>
              <span>20</span>
            </div>
          </div>
          <div id="stuSortPage_first_threeDiv">
            <a>查看详情<i class="el-icon-arrow-right"></i></a>
          </div>
        </div>
        <div id="stuSortPage_second">
          <span>任务分组</span>
          <a><i class="el-icon-plus"></i>新建任务分组</a>
        </div>
      </div>
      <!--成绩管理页面-->
      <div id="GradeManPage" v-if="page === '2'">
        <el-tabs style="padding: 10px">
          <el-tab-pane label="总成绩">
            <div class="gradeMan_sumGrade">
              <div id="gradeMan_sum_first">
                <span style="color: rgb(255,103,30)"><i class="el-icon-question"></i>查看帮助视频</span>
                <span style="color: rgb(77,144,254)"><i class="el-icon-setting"></i>成绩加权设置</span>
              </div>
              <div id="gradeMan_sum_second">
                <span>请点击右上角按钮进行成绩加权设置吧</span>
                <span>(作业、测试、考勤、表现均可进行加权计算，一键生成期末成绩，还可灵活控制各模块内的具体占比。)</span>
                <span>如何使用成绩模块？点击<a href="#">播放视频</a>，查看使用详情</span>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="作业成绩">
            <div class="gradeMan_sumGrade">
              <div id="gradeMan_workGrade_first">
                <div id="add_work">
                  <div id="add_work_content">
                    <a><i class="el-icon-plus"></i>手动添加作业成绩</a>
                    <a><i class="el-icon-download"></i>下载作业报表</a>
                  </div>
                  <div id="gradeMan_workGrade_search">
                    <label>
                      <input type="text" placeholder="姓名、学号">
                    </label>
                    <i class="el-icon-search" id="search2"></i>
                  </div>
                </div>
                <div id="grade_setting">
                  <a>
                    <el-switch v-model="value1" active-color="rgb(50,142,235)"
                               inactive-color="rgb(204,204,204)"></el-switch>
                    等级评分模式</a>
                  <a><i class="el-icon-edit-outline"></i>等级制分值设置</a>
                </div>
              </div>
              <div id="gradeMan_workGrade_second">
                <table id="gradeMan_workGrade_table">
                  <tr>
                    <td class="gradeMan_workGrade_table_firstTd">全部成员</td>
                    <td class="gradeMan_workGrade_table_secondTd">范例_第一次作业</td>
                  </tr>
                  <tr v-for="(item,i) in this.student" :key="i">
                    <td class="gradeMan_workGrade_table_firstTd">{{ item.schoolNum + "  " + item.name }}</td>
                    <td class="gradeMan_workGrade_table_secondTd">{{ item.school }}</td>
                  </tr>
                </table>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="测试成绩">
            <div id="test_grade" class="gradeMan_sumGrade">
              <div id="test_grade_first">
                <div id="downloadAndSearch">
                  <a><i class="el-icon-download"></i>下载测评报表</a>
                  <a>
                    <label>
                      <input style="width: 176px;height: 30px;border: 1px solid rgb(200,200,200)" placeholder="姓名、学号">
                    </label>
                    <i class="el-icon-search" id="downloadAndSearch_search"></i>
                  </a>
                </div>
                <div id="test_grade_sort">
                  <a style="margin-right: 10px">
                    <el-switch v-model="value1" active-color="rgb(50,142,235)"
                               inactive-color="rgb(204,204,204)"></el-switch>
                    等级评分模式</a>
                  <a><i class="el-icon-edit-outline"></i>等级制分值设置</a>
                </div>
              </div>
              <div id="test_grade_second">

              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="考勤统计">
            <div class="gradeMan_sumGrade">
              <span>考勤统计</span>
            </div>
          </el-tab-pane>
          <el-tab-pane label="表现统计">
            <div class="gradeMan_sumGrade">
            <span>表现统计</span>
            </div>
          </el-tab-pane>
          <el-tab-pane label="平时成绩">
            <div id="usual_grade" class="gradeMan_sumGrade">
              <div id="usual_grade_first"><a><i class="el-icon-plus"></i>手动添加平时成绩</a></div>
              <div id="usual_grade_second">
                <p style="text-align: center">暂无平时成绩记录</p>
                <p>您可以在互动分析,课件学习管理,话题参与统计三个模块下,将统计结果生成平时成绩,具体方式如下:</p>
                <p>点击<a href="#">课堂互动</a>下得某个课件互动或者试题互动,切换到互动分析</p>
                <p>点击<a href="#">课堂互动</a>下得某个课件互动,切换到学习管理</p>
                <p>点击<a href="#">话题</a>下得某个话题,切换到话题参与统计</p>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="期末成绩">
            <div id="final_grade" class="gradeMan_sumGrade">
              <div id="final_grade_first">
                <a><i class="el-icon-plus"></i>手动添加课程</a>
              </div>
              <div id="final_grade_second">
                您可以在<a>作业</a>或<a>测试</a>模块下,选择某个作业或测试,生成期末成绩.生成期末成绩后,原作业或测试成绩默认不参与总成绩统计,您可以在总成绩的加权设置里,对作业或测试的权重进行更改占比操作,来决定是否统计原作业或测试.
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <!--导入成员页面-->
      <div id="importMemberPage" v-if="importMember === 1">
        <div id="importMemberPage_content">
          <div id="pop_title">
            <div><h1>导入成员</h1><i class="el-icon-close" @click="importMem(0)"></i></div>
            <h3>从课堂导入</h3>
          </div>
          <div id="pop_cont">
            <a>
              <label>
                <input><i class="el-icon-search"></i>
              </label>
            </a>
            <a>没有其他课程可供导入</a>
          </div>
          <div id="pop_end">
            <a @click="importMem(0)">取消</a>
            <a @click="importMem(0)">确定</a>
          </div>
        </div>
      </div>
      <!--        添加老师/主教页面-->
      <div id="add_Teacher" v-if="addTeach === 1 && identity === '1'">
        <div id="add_Teacher_content">
          <div id="add_Teacher_cont">
            <h3>添加老师/助教</h3>
            <p>邀请老师列表（请使用回车将老师隔开）</p>
            <textarea v-model="teacherInfo"
                      style="width: 480px;height: 122px;border: 1px solid rgb(204,204,204)"></textarea>
            <div>
              <a @click="addTeacher(0)">取消</a>
              <a @click="addTeacher2(0);submitTeacher()">确定</a></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import AccountTools from "@/components/tools/AccountTools.vue";

export default {
  name: "MemberManage",

  components: {AccountTools},//组件

  data() {
    return {
      activeIndex: (parseInt(localStorage.getItem('view')) + 1) + '',
      page: localStorage.getItem('view'),//页面，获得本地的view值
      chooseShowNum: 0,//显示教师团队 或 全部学生
      value: false,//switch开关1
      value1: false,//switch开关2
      importMember: 0,//导入成员页面
      addTeach: 0,// 添加助教/老师
      teacherInfo: "",//助教账号

      user: {},//用户
      identity: '',//判断老师和学生
      course: {},
      teacher: {},//老师信息
      student: {},//学生信息
    }
  },

  mounted() {
    this.getUser()
  },

  methods: {
    addTeacher(c) {//开关添加教师页面并清空上次输入数据
      this.addTeach = c;
      this.teacherInfo = ''
    },
    addTeacher2(c) {//开关添加教师页面并清空上次输入数据
      this.addTeach = c;
    },

    importMem(c) {//开关 导入成员页面
      this.importMember = c;
    },

    choosePage(c) {//选择页面
      this.page = c;
    },

    chooseShow(c) {//教学团队 或 全部学生
      this.chooseShowNum = c;
    },

    submitTeacher() {//提交 助教/老师信息
      this.$axios.post('/teacher/addTeacher', {
        courseId: this.course.id,
        teacherInfo: this.teacherInfo,
      }, {
        headers: {'Authorization': this.$store.state.Authorization}
      }).then(resp => {
        alert(resp.data)
      }).catch(resp => {
        console.log(resp)
      })
      this.teacherInfo = ''
    },

    delInfo(c) {// 删除 信息
      this.$confirm('该学生将无法参与该班级\n' +
          '学生可通过邀请码重新加入该班级', '确定要删除”' + c.name + '“码？').then(() => {
        this.$axios.post("/teacher/deleteUser", {
          courseId: this.course.id,
          account: c.account,
        }, {
          headers: {'Authorization': this.$store.state.Authorization}
        }).then(resp => {
          this.$message({
            showClose: true,
            center: true,
            offset: this.$store.state.tip,
            message: resp.data
          })
          this.$router.go(0)
        }).catch(resp => {
          console.log(resp.data);
        })
      }).catch(() => {
        this.$message({
          center: true,
          message: '已取消删除'
        })
      })
    },
    getUser() {
      this.$axios.post('/start/user', {
        token: this.$store.state.Authorization
      }, {
        headers: {'Authorization': this.$store.state.Authorization}
      }).then(resp => {
        if (resp.status === 200) {//status=200 代表成功
          this.user = resp.data.user
          this.user.address = resp.data.address
          this.identity = resp.data.user.account.startsWith('s') ? '0' : '1'
          this.getCourse()
        }
      }).catch(resp => {
        console.log(resp)
      })
    },

    getCourse() {
      this.$axios.post('/start/courseInfo',
          {courseId: localStorage.getItem('courseId')},//17
          {headers: {'Authorization': this.$store.state.Authorization}}
      ).then(resp => {
        if (resp.status === 200) {//status=200 代表成功
          this.course = resp.data.course
          this.teacher = resp.data.teacher
          this.student = resp.data.student
          if (this.identity === 1) {
            for (let i = 0; i < resp.data.tc.length; i++) {
              if (this.user.id === resp.data.tc[i].teacher) {
                if (resp.data.tc[i].role === '学生') {
                  this.identity = '0'
                }
              }
            }
          }
        }
      })
    }
  }
}
</script>

<style scoped>
#title {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  /*background-color: #e1f3d8;*/
  align-items: center;
  border-bottom: 2px solid rgb(248, 248, 248);
}

#goBack {
  margin-left: 30px;
  /*width: 130px;*/
  height: 32px;
  /*background-color: #ebb563;*/
  display: flex;
  align-items: center;
  cursor: pointer;
}

#goBack i {
  font-size: 25px;
}

#familiar_keTangPai {
  /*width: 80px;*/
  height: 32px;
  background-color: rgb(44, 88, 171);
  padding: 0 10px;
  margin-left: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  border-radius: 16px;
}

.el-menu--horizontal > .el-menu-item.is-active {
  border-bottom: 4px solid #2c58ab;
  color: #303133;
}

.el-menu-item {
  font-size: 14px;
  color: #303133;
  padding: 0 20px;
  cursor: pointer;
  transition: border-color .3s, background-color .3s, color .3s;
  box-sizing: border-box;
  border-left: 2px solid rgba(0, 0, 0, 0);
  border-right: 2px solid rgba(0, 0, 0, 0);
}

.el-menu.el-menu--horizontal {
  border-bottom: solid 0 #e6e6e6;
}


#data {
  width: 200px;
  height: 60px;
  /*background-color: #ebb563;*/
  margin-right: 20px;
}

#pageContent {
  display: flex;
  align-items: center;
  justify-content: center;
}

#stuPage {
  margin-top: 40px;
  width: 1224px;
  display: flex;
  flex-wrap: wrap;
}

#stuPage_top {
  width: 1224px;
  height: 52px;
  border-bottom: 1px solid rgb(226, 230, 237);
  margin-bottom: 27px;
  /*background-color: #409eff;*/
  display: flex;
  justify-content: space-between;
}

#stuPage_top_first {
  display: flex;
  /*background-color: #1da3c5;*/
}

#stuPage_top_first a {
  margin-right: 20px;
}

#importMember {
  width: 104px;
  height: 32px;
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgb(50, 186, 240);
  color: white;
  cursor: pointer;
}

#importMember:hover {
  background-color: rgb(77, 144, 254);
}

#importMemberPage {
  position: relative;
  /*display: none;*/
}


#importMemberPage_content {
  width: 810px;
  height: 525px;
  position: absolute;
  background-color: whitesmoke;
  top: -400px;
  left: -1000px;
}

#pop_title {
  width: 745px;
  height: 100px;
  padding: 0 30px 0 33px;
  border: 1px solid rgb(220, 220, 220);
  /*background: #ebb563;*/
}

#pop_title div {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

#pop_title div i {
  font-size: 25px;
  cursor: pointer;
}

#pop_cont {
  width: 808px;
  height: 337px;
  /*background-color: red;*/
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

#pop_cont a {
  width: 808px;
  display: flex;
  justify-content: center;
}

#pop_end {
  width: 808px;
  height: 38px;
  padding: 24px 0 22px 0;
  border-top: 1px solid rgb(220, 220, 220);
  background-color: rgb(246, 246, 246);
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

#pop_end a {
  width: 94px;
  height: 38px;
  margin-right: 38px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgb(154, 189, 244);
  cursor: pointer;
}

/**/
#add_Teacher {
  position: relative;
  /*display: none;*/
}


#add_Teacher_content {
  width: 500px;
  height: 284px;
  padding: 15px 15px;
  position: absolute;
  background-color: whitesmoke;
  top: -300px;
  left: -900px;
}

#add_Teacher_cont {
  width: 480px;
  height: 263px;
  padding: 10px;
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
}

#add_Teacher_cont h3 {
  width: 480px;
}

#add_Teacher_cont p {
  width: 480px;
}

#add_Teacher_cont div {
  width: 480px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

#add_Teacher_cont div a {
  width: 90px;
  height: 32px;
  border: 1px solid rgb(204, 204, 204);
  background-color: rgb(77, 144, 254);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  border-radius: 5px;
  cursor: pointer;
}

#downloadMemberInfo {
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

#memberWithdrawalRecord {
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

#stuPage_top_second {
  width: 173px;
  height: 32px;
  display: flex;
  align-items: center;
  background-color: rgb(241, 243, 244);
  border-radius: 5px;
}

#stuPage_top_second label input {
  background-color: rgb(241, 243, 244);
  margin-left: 5px;
  width: 150px;
}

#search1 {
  cursor: pointer;
}

#stuPage_content {
  display: flex;
  border: 1px solid rgb(226, 230, 237);
  margin-bottom: 60px;
  border-radius: 5px;
}

#stuPage_left {
  width: 286px;
  height: 600px;
  font-size: 16px;
}

#stuPage_left a {
  width: 238px;
  height: 55px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  cursor: pointer;
}

#stuPage_left a:hover {
  background-color: rgb(228, 237, 253);
}


#stuPage_right {
  width: 935px;
  border: 1px solid rgb(226, 230, 237);
  /*background: #cccccc;*/
}

#tech_show {
  width: 937px;
  height: 55px;
  background-color: rgb(241, 243, 244);
}

#tech_show_title {
  width: 896px;
  height: 55px;
  padding: 0 12px 0 25px;
  /*background-color: #5daf34;*/
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  color: black;
}

#add_teach {
  width: 172px;
  height: 32px;
  background-color: rgb(154, 189, 244);
  font-size: 16px;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 5px;
}

#teach_ul {
  display: flex;
  flex-wrap: wrap;
  font-size: 16px;
}

#teach_ul li {
  width: 896px;
  height: 60px;
  padding: 0 14px 0 25px;
  /*background-color: #e1f3d8;*/
  display: flex;
  align-items: center;
  justify-content: space-between;
}

#teach_ul li:hover {
  background-color: rgb(241, 243, 244);
}

#teach_ul li a {
  width: 250px;
  height: 50px;
  /*background-color:cornflowerblue;*/
  display: flex;
  align-items: center;
  justify-content: flex-start;
  color: black;
}

.private_community {
  width: 250px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  /*background-color: #409eff;*/
}

.private_community i {
  font-size: 20px;
  /*background-color: #ebb563;*/
  margin: 0 20px 0 10px;
  cursor: pointer;
}

#stu_show_title {
  width: 900px;
  height: 55px;
  font-size: 16px;
  padding: 0 12px 0 25px;
  background: rgb(241, 243, 244);
  border-bottom: 1px solid rgb(226, 230, 237);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#stu_show_title div {
  height: 55px;
  /*background-color: #ebb563;*/
}

#stu_show_title_first {
  display: flex;
  align-items: center;
  justify-content: center;
}

#stu_show_title_first a {
  color: black;
}

#stu_show_title_second {
  display: flex;
}

#stu_show_title_second a {
  font-size: 14px;
  color: black;
  margin-left: 30px;
  display: flex;
  align-items: center;
}

#stu_div {
  width: 899px;
  height: 55px;
  padding: 0 12px 0 25px;
  border-bottom: 1px solid rgb(226, 230, 237);
  display: flex;
  align-items: center;
}

#stu_div a {
  width: 100px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgb(221, 221, 221);
  margin-left: 10px;
  font-size: 12px;
  color: white;
  border-radius: 5px;
  cursor: pointer;
}

#stu_ul {
  display: flex;
  flex-wrap: wrap;
  font-size: 16px;
}

#stu_ul li {
  width: 896px;
  height: 60px;
  padding: 0 14px 0 25px;
  /*background-color: #e1f3d8;*/
  display: flex;
  align-items: center;
  justify-content: flex-start;
  flex-wrap: nowrap;
}

#stu_ul li:hover {
  background-color: rgb(241, 243, 244);
}

#stu_ul li a {
  height: 50px;
  margin-right: 10px;
  /*background-color:cornflowerblue;*/
  display: flex;
  align-items: center;
  justify-content: flex-start;
  color: black;
}

#stu_ul_input input {
  height: 15px;
  width: 15px;
}

#stu_ul_avatar {
  width: 50px;
}

#stu_ul_schoolNum {
  width: 150px;
}

#stu_ul_name {
  width: 600px;
}

#stu_ul_siXin {
  cursor: pointer;
  margin-right: 10px;
}

#stu_ul_more {
  margin-left: 20px;
  margin-right: 20px;
  cursor: pointer;
}

/*学生分组*/
#stuSortPage {
  width: 1200px;
  height: 280px;
  /*background-color: #ebb563;*/
}

#stuSortPage_first {
  width: 1140px;
  height: 100px;
  padding: 30px;
  margin: 40px 0;
  border-radius: 5px;
  border: 1px solid rgb(233, 233, 233);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#stuSortPage_first_firstDiv {
  width: 320px;
  height: 100px;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

#stuSortPage_first_twoDiv {
  width: 320px;
  height: 100px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  /*background-color: #ebb563;*/
}

#stuSortPage_first_twoDiv div {
  width: 100px;
  height: 30px;
  margin-left: 30px;
  /*background: #5daf34;*/
}

#stuSortPage_first_threeDiv a {
  color: rgb(50, 142, 235);
}

#stuSortPage_second {
  width: 1200px;
  height: 40px;
  /*background-color: #409eff;*/
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
}

#stuSortPage_second a {
  width: 160px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border-radius: 5px;
  background-color: rgb(50, 142, 235);
  cursor: pointer;
}

/*成绩管理*/
#GradeManPage {
  width: 980px;
  /*height: 600px;*/
  border: 1px solid rgb(200, 200, 200);
  margin: 50px 0;
}

.gradeMan_sumGrade {
  height: 600px;
}

#gradeMan_sum_first {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  /*background-color: #e1f3d8;*/
}

#gradeMan_sum_first span {
  width: 121px;
  height: 40px;
  margin: 20px 20px 0 0;
  font-size: 16px;
  cursor: pointer;
  /*background-color: #ebb563;*/
}

#gradeMan_sum_second {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}

#gradeMan_sum_second span {
  width: 980px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

#gradeMan_sum_second span a {
  color: rgb(77, 144, 254);
}

#gradeMan_workGrade_first {
  width: 938px;
  height: 68px;
  padding: 25px 20px;
  /*background-color: #67c23a;*/
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}


#add_work {
  width: 938px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  /*background-color: #ebb563;*/
}

#add_work_content a {
  margin-right: 20px;
  cursor: pointer;
}


#grade_setting a {
  margin-right: 20px;
  cursor: pointer;
}

#gradeMan_workGrade_second {
  width: 938px;
  border: 1px solid rgb(220, 220, 220);
  margin: 0 20px 60px 20px;
  /*background-color: #409eff;*/
}

#gradeMan_workGrade_table {
  width: 938px;
  /*background-color: #ebb563;*/
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
}

#gradeMan_workGrade_table tr {
  width: 888px;
  padding: 0 24px;
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
  background-color: rgb(246, 246, 246);
  border-bottom: 1px solid rgb(220, 220, 220);
}

.gradeMan_workGrade_table_firstTd {
  width: 212px;
  height: 58px;
  border-right: 1px solid rgb(220, 220, 220);
  display: flex;
  align-items: center;
}

.gradeMan_workGrade_table_secondTd {
  width: 655px;
  height: 58px;
  padding-left: 20px;
  display: flex;
  align-items: center;
}

#gradeMan_workGrade_search {
  width: 173px;
  height: 32px;
  display: flex;
  align-items: center;
  background-color: rgb(241, 243, 244);
  border-radius: 5px;
}

#gradeMan_workGrade_search label input {
  background-color: rgb(241, 243, 244);

}

#search2 {
  cursor: pointer;
}

/*测试成绩*/
#test_grade {
  width: 980px;
  /*background-color: #ebb563;*/
}

#test_grade_first {
  width: 938px;
  height: 67px;
  padding: 25px 20px;
  /*background-color: #67c23a;*/
  display: flex;
  flex-wrap: wrap;
}

#downloadAndSearch {
  width: 938px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#downloadAndSearch_search {
  width: 25px;
  height: 25px;
  margin-left: -25px;
  cursor: pointer;
}

#test_grade_second {
  width: 938px;
  height: 200px;
  padding: 25px 20px;
  /*background-color: #4d90fe;*/
}

/*平时成绩*/
#usual_grade {
  width: 980px;
  height: 517px;
}

#usual_grade_first {
  width: 938px;
  height: 30px;
  padding: 25px 20px;
  /*background-color: #ebb563;*/
}

#usual_grade_second {
  width: 838px;
  height: 150px;
  padding: 0 70px;
  /*background-color: #1da3c5;*/
}

#usual_grade_second p {
  color: rgb(128, 128, 128);
  height: 30px;
}

#usual_grade_second p a {
  color: rgb(58, 178, 243);
}

/*期末成绩*/
#final_grade {
  width: 980px;
  height: 517px;
  /*background-color: #ebb563;*/
}

#final_grade_first {
  width: 938px;
  height: 30px;
  padding: 25px 20px;
  /*background-color: #3a8ee6;*/
}

#final_grade_second {
  width: 838px;
  height: 40px;
  padding: 0 70px;
  /*background-color: rosybrown;*/
}

#final_grade_second a {
  color: rgb(58, 195, 248);
}
</style>
