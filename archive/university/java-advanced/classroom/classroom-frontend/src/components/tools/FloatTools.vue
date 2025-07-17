<template>
  <!--  浮动工具-->
  <div class="component-global_handle">
    <ul>
      <li @click="todoAlert"><i class="el-icon-document font_family"></i><span class="name">待办</span></li>
      <li @click="todoAlert"><i class="icon-chat_outline font_family"></i><span class="name">私信</span></li>
      <li @click="todoAlert"><i class="icon-telephone_directory_outline font_family"></i><span class="name">好友</span>
      </li>
      <li @click="todoAlert"><i class="icon-listen_outline font_family"></i><span class="name">客服</span></li>
      <li @click="todoAlert"><i class="icon-qr_outline font_family"></i><span class="name">公众号</span></li>
      <li @click="todoAlert"><i class="el-icon-edit-outline font_family"></i><span class="name">反馈</span></li>
    </ul>
  </div>
</template>

<script>
export default {
  name: "FloatTools",
  methods: {
    todoAlert() {
      this.$message({
        center: true,
        message: "该功能暂未开放",
        type: "warning"
      });
    }
  }
}
</script>

<style scoped>
.component-global_handle {
  position: fixed;
  z-index: 2;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background: #fff;
  border-radius: 8px 0 0 8px;
  box-shadow: 0 0 18px 0 rgba(0,0,0,.1);
}

.component-global_handle ul {
  padding: 10px 0;
}

.component-global_handle ul li {
  padding: 0 10px;
  cursor: pointer;
  margin-bottom: 12px;
  color: #ff6000;
  text-align: center;
}

.component-global_handle ul li .font_family {
  display: block;
  font-size: 30px;
}

.component-global_handle ul li .name {
  font-size: 12px;
  line-height: 16px;
}
</style>
