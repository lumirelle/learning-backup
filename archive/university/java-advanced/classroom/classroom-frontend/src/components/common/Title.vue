<template>
  <!--  导航区  -->
  <div id="tit" class="component-header">
    <div class="header-content">
      <!--  left  -->
      <a class="h-left">
        <img
            alt=""
            @click="todoAlert"
            src="https://www.ketangpai.com/images/common/logo_blue.png"
            style="max-height: 28px;"/>
      </a>
      <!--  center -->
      <el-menu mode="horizontal" router default-active="/courseManage" class="el-menu-demo">
        <el-menu-item index="/courseManage">我的课堂</el-menu-item>
        <el-menu-item index="/prepareLesson" v-if="identity === 1">备课区
        </el-menu-item>
      </el-menu>
      <!--  right  -->
      <div class="h-right">
        <!--    通知    -->
        <el-popover width="450" trigger="click" placement="bottom-end"
                    style="margin-right: 10px">
          <template #reference>
            <span>
              <span class="notice component-notice_count el-popover__reference"
                    aria-describedby="el-popover-3869" tabindex="0">
                <i class="icon-notifications_outline font_family"></i>
              </span>
            </span>
          </template>
          <div class="component-notice">
            <el-tabs tab-position="top">
              <el-tab-pane label="全部"></el-tab-pane>
              <el-tab-pane label="教学活动"></el-tab-pane>
              <el-tab-pane label="教务通知"></el-tab-pane>
              <el-tab-pane label="系统通知"></el-tab-pane>
            </el-tabs>
            <div class="handle flex-between">
              <div class="btn" @click="todoAlert"><i class="font_family icon-pichuli_outline"></i>一键标为已读
              </div>
              <div class="btn" @click="todoAlert"><i class="font_family icon-liulan"></i>查看全部
              </div>
            </div>
          </div>
        </el-popover>
        <!--     头像   -->
        <el-popover trigger="click">
          <template #reference>
            <span>
              <span class="userinfo el-popover__reference" aria-describedby="el-popover-2094"
                    tabindex="0">
                <img ref="avatarImgRef" class="avatar" alt=""
                     :src="user.address" :title="user.name">
              </span>
            </span>
          </template>
          <ul class="common_header-userinfo_drop">
            <li @click="todoAlert"><i class="font_family icon-set_outline"></i>个人设置</li>
            <li @click="logout"><i class="font_family icon-import_outline"></i>退出登录</li>
          </ul>
        </el-popover>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Title",
  data() {
    return {
      user: {},
      identity: "",
      command: 0
    };
  },
  // 钩子函数、初始化页面时使用
  mounted: function () {
    this.getUser();
  },
  methods: {
    todoAlert() {
      this.$message({
        center: true,
        message: "该功能暂未开放",
        type: "warning"
      });
    },
    logout() {
      this.$router.push("/login");
    },
    handleCommand(command) {
      if (command === "1") {
        this.$router.push('/login')
      } else {
        this.$message({
          center: true,
          message: "该功能暂未开放",
          type: "warning"
        });
      }
    },
    getUser() {
      this.$axios
      .post(
          "start/user",
          {
            token: this.$store.state.Authorization
          },
          {
            headers: {
              Authorization: this.$store.state.Authorization
            }
          }
      )
      .then(resp => {
        if (resp.status === 200) {
          this.user = resp.data.user;
          this.user.address = resp.data.address;
          this.identity = resp.data.user.account.startsWith("s") ? 0 : 1;
        }
      })
      .catch(resp => {
        console.log(resp);
      });
    }
  }
};
</script>

<style>
.component-header {
  height: 64px;
  width: 100%;
  margin-bottom: 12px;
  box-sizing: border-box;
  box-shadow: 0 0 10px #ccc;
}

.component-header .header-content {
  position: fixed;
  background-color: #fff;
  z-index: 999;
  top: 0;
  left: 0;
  height: 64px;
  width: 100%;
  padding: 0 28px;
  min-width: 900px;
  display: flex;
  flex-flow: row nowrap;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 0 0 #dfdfdf;
}

.component-header .header-content .h-left {
  cursor: pointer;
}

.component-header .header-content .el-menu-demo.el-menu.el-menu--horizontal {
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  border-bottom-width: 0;
}

.component-header .header-content .h-right {
  display: flex;
  align-items: center;
}

.component-header .header-content .el-menu-demo.el-menu.el-menu--horizontal > .el-menu-item.is-active {
  color: #4285f4;
}

.component-header .header-content .h-right .userinfo .avatar {
  display: block;
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.el-tabs__item {
  padding: 0 20px;
  height: 40px;
  box-sizing: border-box;
  line-height: 40px;
  display: inline-block;
  list-style: none;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  position: relative;
}

.icon-notifications_outline:before {
  content: "\e6b3";
}

.common_header-userinfo_drop li {
  display: flex;
  align-items: center;
  font-size: 12px;
  cursor: pointer;
  line-height: 40px;
  text-align: left;
  padding-left: 12px;
  padding-right: 20px;
  margin: 0 -12px;
}

.common_header-userinfo_drop li:hover {
  background: #e8f0ff;
}

.common_header-userinfo_drop li i {
  margin-right: 8px;
}

.el-menu--horizontal > .el-menu-item.is-active {
  border-bottom: 3px solid #4285f4;
  border-radius: 2px;
  color: #303133;
}
</style>
