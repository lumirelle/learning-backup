<template>
  <div class="view-login view-regist">
    <div class="logo-box">
      <img src="/images/common/logo_blue.png" alt="">
    </div>
    <div class="login-content">
      <div class="left">
        <img src="/images/login/bg.png" alt="">
      </div>
      <div class="right">
        <div class="right-content"><h2 class="title"><span>注册账号</span></h2>
          <el-form
              ref="registerForm"
              :model="registerForm"
              :rules="rules"
              size="medium"
              status-icon>
            <el-form-item class="margin-bottom" prop="account">
              <el-input v-model="registerForm.account" autocomplete="on"
                        placeholder="请输入邮箱/手机号"/>
            </el-form-item>
            <el-form-item class="margin-bottom" prop="password">
              <el-input v-model="registerForm.password" type="password" show-password
                        placeholder="请输入密码"/>
            </el-form-item>
            <el-form-item class="margin-bottom" prop="checkPassword">
              <el-input v-model="registerForm.checkPassword" type="password" show-password
                        placeholder="请再次输入密码确认"/>
            </el-form-item>
            <el-form-item class="margin-bottom">
              <p class="font-bold font16">选择身份</p>
              <div class="role-box">
                <div ref="teacherRole"
                     @click="changeRole('老师')"
                     class="item flex-align active">
                  <img src="@/assets/img/teacher.svg" class="icon" alt="">
                  <span class="name">老师</span>
                </div>
                <div ref="studentRole"
                     @click="changeRole('学生')"
                     class="item flex-align">
                  <img src="@/assets/img/student.svg" class="icon" alt="">
                  <span class="name">学生</span>
                </div>
              </div>
            </el-form-item>
            <el-form-item class="margin-bottom" prop="name">
              <el-input v-model="registerForm.name" placeholder="请输入姓名"/>
            </el-form-item>
            <el-form-item class="margin-bottom" prop="school">
              <el-input v-model="registerForm.school" placeholder="请输入学校/机构"/>
            </el-form-item>
            <el-form-item v-if="chooseStudent" class="margin-bottom" prop="schoolNum">
              <el-input v-model="registerForm.schoolNum" placeholder="请输入学号"/>
            </el-form-item>
            <el-form-item class="margin-bottom" prop="answer">
              <div class="flex-between">
                <el-input v-model="registerForm.answer" type="text"
                          placeholder="请输入验证码"/>
                <ValidCode :info="{code}" @validCode="validCode()"></ValidCode>
              </div>
            </el-form-item>
          </el-form>
          <div class="bottom-box">
            <div class="margin-top">
              <el-button v-if="chooseStudent" @click="studentRegister" type="primary" size="medium"
                         style="width: 100%;">注册
              </el-button>
              <el-button v-else @click="teacherRegister" type="primary" size="medium"
                         style="width: 100%;">注册
              </el-button>
            </div>
            <div class="margin-top text-right font14">
              已有账号？
              <router-link to="/login">
                 <span @click="this.$router.push('/login')" class="font-color--main"
                       style="cursor: pointer;">去登录</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ValidCode from "../components/common/ValidCode.vue";

export default {
  name: 'Register',
  components: {ValidCode},

  data() {
    return {
      chooseStudent: false,
      registerForm: {
        account: '',
        password: '',
        checkPassword: '',
        name: '',
        schoolNum: '',
        school: '',
        answer: '',
      },
      code: '',
      answerPlus: '',
      rules: {
        account: [
          {required: true, message: '邮箱/手机不能为空'},
          {
            validator: (rule, value, callback) => {
              const phoneReg = /^1[(34578)][0-9]\d{8}$/
              const mailboxReg = /^([a-zA-Z]|[0-9])(\w|\\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
              if (phoneReg.test(value) || mailboxReg.test(value)) {
                callback();
              } else {
                callback(new Error('邮箱/手机格式不对'));
              }
            }, trigger: 'change'
          }
        ],
        password: [
          {required: true, message: '密码不能为空'},
          {
            validator: (rule, value, callback) => {
              if (value.length < 6 || value.length > 12) {
                callback(new Error('密码太弱，请注意您的账户安全'))
              } else {
                callback();
              }
            },
            trigger: 'change'
          }
        ],
        checkPassword: [
          {required: true, message: '再次输入密码不能为空'},
          {
            validator: (rule, value, callback) => {
              if (value !== this.registerForm.password) {
                callback(new Error('两次密码不一致'));
              } else {
                callback();
              }
            }, trigger: 'change'
          }
        ],
        name: [
          {required: true, message: '姓名不能为空'}
        ],
        school: [
          {required: true, message: '学校不能为空'}
        ],
        schoolNum: [
          {required: true, message: '学号不能为空'}
        ],
        answer: [
          {required: true, message: '验证码不能为空'}
        ]
      },
    }
  },

  methods: {
    // 验证码
    validCode() {
      this.$axios.get('/user/validCode').then(resp => {
        if (resp.status === 200) {
          this.code = resp.data.code
          this.answerPlus = resp.data.answerPlus
        }
      }).catch(reason => {
        console.log(reason)
      })
    },
    // 用户切换选择的身份
    changeRole(role) {
      this.$refs['studentRole'].classList.toggle('active')
      this.$refs['teacherRole'].classList.toggle('active')
      this.$data['chooseStudent'] = (role === '学生')
    },
    teacherRegister() {
      this.$refs['registerForm'].validate((valid) => {
        if (valid) {
          this.$axios.post('/user/register/teacher', {
            account: this.registerForm.account,
            password: this.registerForm.password,
            name: this.registerForm.name,
            school: this.registerForm.school,
            answer: this.registerForm.answer,
            answerPlus: this.answerPlus
          }).then(resp => {
            if (resp.status === 200) {
              this.$message({
                showClose: true,
                center: true,
                offset: this.$store.state.tip,
                message: resp.data
              })
              if (resp.data === '注册成功') {
                this.$router.push('/login');
              }
            }
          })
        }
      });
    },
    studentRegister() {
      //表单验证
      this.$refs['registerForm'].validate((valid) => {
        if (valid) {
          this.$axios.post('/user/register/student', {
            account: this.registerForm.account,
            password: this.registerForm.password,
            name: this.registerForm.name,
            school: this.registerForm.school,
            schoolNum: this.registerForm.schoolNum,
            answer: this.registerForm.answer,
            answerPlus: this.answerPlus
          }).then(resp => {
            // 访问成功
            if (resp.status === 200) {
              this.$message({
                showClose: true,
                center: true,
                offset: this.$store.state.tip,
                message: resp.data
              })
              // 注册成功
              if (resp.data === '注册成功') {
                this.$router.push('/login');
              }
            }
          })
        }
      })
    }
  },
}
</script>

<style scoped>
.view-regist {
  display: flex;
  flex-direction: column;
  background-size: 100%;
  background-repeat: no-repeat;
  padding-top: 20px
}

.view-regist .login-content {
  flex: 1;
  display: flex;
  align-items: center
}

.view-regist .login-content .right {
  margin: 12px 0
}

.view-regist .login-content .right .verify-img {
  cursor: pointer;
  display: block;
  border-radius: 8px;
  margin-left: 16px;
  width: 154px;
  height: 48px
}

.view-regist .login-content .right .bottom-box {
  padding-bottom: 12px;
  margin-top: 12px
}
</style>
<style src="@/css/login-register.css"/>
