<template>
  <div class="h-right">
    <!--    通知    -->
    <el-popover width="450" trigger="click" placement="bottom-end"
                style="margin-right: 10px">
      <template #reference>
            <span>
              <span class="notice component-notice_count el-popover__reference" tabindex="0">
                <i class="icon-notifications_outline font_family" style="color: black"></i>
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
              <span class="userinfo el-popover__reference" tabindex="0">
                <img ref="avatarImgRef" class="avatar" alt=""
                     :src="info.user.address" :title="info.user.name">
              </span>
            </span>
      </template>
      <ul class="common_header-userinfo_drop">
        <li @click="todoAlert"><i class="font_family icon-set_outline"></i>个人设置</li>
        <li @click="handleCommand('1')"><i class="font_family icon-import_outline"></i>退出登录</li>
      </ul>
    </el-popover>
  </div>
</template>

<script>
export default {
  name: "AccountTools",
  props: ['info'],
  data() {
    return {}
  },
  methods: {
    todoAlert() {
      this.$message({
        center: true,
        message: '该功能暂未开放',
        type: 'warning'
      })
    },
    handleCommand(command) {
      if (command === "1") {
        this.$router.push('/login')
      } else {
        this.todoAlert()
      }
    },
  }
}
</script>

<style scoped>
.el-menu-demo.el-menu.el-menu--horizontal {
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  border-bottom-width: 0;
}

.h-right {
  display: flex;
  align-items: center;
}

.el-menu-demo.el-menu.el-menu--horizontal > .el-menu-item.is-active {
  color: #4285f4;
}

.userinfo .avatar {
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

.icon-notifications_outline:before {
  content: "\e6b3";
}

.font_family {
  font-size: 24px;
  vertical-align: middle;
}
</style>
