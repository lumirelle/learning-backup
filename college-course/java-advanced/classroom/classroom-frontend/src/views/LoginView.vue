<template>
  <div class="view-login">
    <div class="logo-box">
      <img src="/images/common/logo_blue.png" alt="">
    </div>
    <div class="login-content">
      <div class="left">
        <img src="/images/login/newbg.png" alt="">
      </div>
      <div class="right">
        <div class="right-content">
          <h2 class="title"><span>账号登录</span></h2>
          <ul class="tabs-nav">
            <li class="item activeLogin">
              账号登录
              <i class="line" style="background-color: rgb(66, 133, 244);"></i>
            </li>
            <li class="item" @click="todoAlert">
              手机短信登录
              <i class="line" style="background-color: rgb(66, 133, 244);"></i>
            </li>
            <li class="item" @click="todoAlert">
              微信登录
              <i class="line" style="background-color: rgb(66, 133, 244);"></i>
            </li>
          </ul>
          <div class="login-tab">
            <el-form
                ref="accountForm"
                :model="accountForm"
                :rules="rules"
                size="medium"
                status-icon>
              <el-form-item class="margin-bottom" prop="account">
                <el-input v-model="accountForm.account" autocomplete="on"
                          @keyup.enter.native="login()"
                          placeholder="请输入邮箱/手机号/账号"/>
              </el-form-item>
              <el-form-item class="margin-bottom" prop="password">
                <el-input v-model="accountForm.password" type="password" show-password
                          @keyup.enter.native="login()" placeholder="请输入密码"/>
              </el-form-item>
              <el-form-item class="margin-bottom">
                <div class="flex-between" style="width: 100%;">
                  <el-checkbox v-model="accountForm.checked">下次自动登录</el-checkbox>
                  <!--     TODO 忘记密码     -->
                  <el-link @click="todoAlert">忘记密码？</el-link>
                </div>
              </el-form-item>
            </el-form>
            <div class="bottom-box">
              <div class="margin-top">
                <el-button
                    type="primary"
                    size="medium"
                    @click="login()"
                    style="width: 100%">登录
                </el-button>
              </div>
              <div class="go-register text-right font14 flex-align flex-between">
                <p></p>
                <p> 还没有账号？
                  <router-link to="/register" style="cursor: pointer;">
                    <span class="font-color--main"
                          style="cursor: pointer;">去注册</span>
                  </router-link>
                </p>
              </div>
            </div>
            <div class="qr-box">
              <div class="qr-code" @click="todoAlert"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 模板获取mapMutations传参
import {mapMutations} from "vuex";

// export使键暴露出去，让别的类可以引用（router内引用）
export default {
  name: "Login",

  data() {
    return {
      accountForm: {
        account: "",
        password: "",
        checked: false
      },
      rules: {
        account: [{required: true, message: "账号不能为空"}],
        password: [{required: true, message: "密码不能为空"}],
      }
    };
  },

  created() {
    localStorage.clear();
    this.$store.commit("init");
  },

  methods: {
    // 将 mapMutations 中的 setToken 方法映射到 methods 中
    ...mapMutations(["setToken"]),
    todoAlert() {
      this.$message({
        center: true,
        message: "该功能暂未开放",
        type: "warning"
      });
    },
    // 发送登录请求，解析后台返回的json对象
    login() {
      //表单验证
      this.$refs['accountForm'].validate(valid => {
        if (valid) {
          // 向后端发动请求
          this.$axios.post(
              "/user/login",
              {
                account: this.accountForm.account,
                password: this.accountForm.password
              })
          .then(resp => {
            if (resp.status === 200) {
              if (resp.data === 0) {
                this.$message({
                  showClose: true,
                  center: true,
                  offset: this.$store.state.tip,
                  message: "用户不存在"
                });
              } else if (resp.data === 1) {
                this.$message({
                  showClose: true,
                  center: true,
                  offset: this.$store.state.tip,
                  message: "密码错误"
                });
              } else {
                this.setToken({Authorization: resp.data.token});
                this.$router.push("/home");
              }
            }
          })
          .catch(resp => {
            console.log(resp);
          });
        }
      });
    }
  }
};
</script>

<style scoped>
.view-login {
  background-size: 100%;
  background-repeat: no-repeat
}

.view-login .go-register {
  margin-top: 24px
}

.view-login .go-register a img {
  display: block
}

.tabs-nav li {
  position: relative
}

.tabs-nav li .line {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  display: none;
  width: 60px;
  height: 2px
}

.tabs-nav .activeLogin .line {
  display: block
}

.bottom-box .otherSchool {
  justify-content: center;
  margin-top: 14px;
  padding: 12px 20px;
  width: 100%;
  height: 48px;
  font-size: 14px;
  font-weight: 500;
  background: #fff;
  border-width: 1px;
  border-style: solid;
  border-radius: 4px;
  transition: .1s;
  cursor: pointer
}
</style>
<style src="@/css/login-register.css"/>
