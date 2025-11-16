<template>
  <div id="app">
    <router-view :style="style"/>
  </div>
</template>

<script>
export default {
  name: "App",
  data() {
    return {
      style: {
        minHeight: 'calc(100vh / ' + window.zoomRate + ')',
      }
    };
  },
};
</script>

<style>
#app {
  font-size: 14px;
  font-family: "PingFang SC", tahoma, arial, "helvetica neue",
  "hiragino sans gb", "microsoft yahei ui", "microsoft yahei", simsun,
  sans-serif;
  color: #2d2d2d;
  background: white;
}

*, :after, :before {
  box-sizing: border-box;
}

body::-webkit-scrollbar, div::-webkit-scrollbar, html::-webkit-scrollbar, section::-webkit-scrollbar, textarea::-webkit-scrollbar, ul::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

body::-webkit-scrollbar-thumb, div::-webkit-scrollbar-thumb, html::-webkit-scrollbar-thumb, textarea::-webkit-scrollbar-thumb, ul::-webkit-scrollbar-thumb {
  border-radius: 2px;
  background: #9e9e9e;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between
}

.flex-align {
  display: flex;
  align-items: center;
}

.text-right {
  text-align: right;
}

.el-popover {
  position: absolute;
  background: #fff;
  min-width: 150px;
  border: 1px solid #ebeef5;
  padding: 12px;
  z-index: 2000;
  color: #606266;
  line-height: 1.4;
  text-align: justify;
  font-size: 14px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1);
  word-break: break-all;
}

.component-header .header-content .h-right .font_family {
  font-size: 24px;
  vertical-align: middle;
}

@font-face {
  font-family: font_family;
  src: url(/src/fonts/iconfont.fc45beb7.woff2) format("woff2"), url(/src/fonts/iconfont.c53a3b4f.woff) format("woff"), url(/src/fonts/iconfont.e77da190.ttf) format("truetype")
}

.font_family {
  font-family: font_family !important;
  font-size: 16px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale
}

.el-tabs__item.is-active {
  color: #4285f4;
}

.el-tabs__active-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 2px;
  background-color: #4285f4;
  z-index: 1;
  transition: transform .3s cubic-bezier(.645, .045, .355, 1);
  list-style: none;
}

.el-tabs__nav-wrap:after {
  content: "";
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 2px;
  background: transparent;
  z-index: 1;
}

.component-notice {
  padding: 0 24px;
}

.component-notice .handle .btn i {
  margin-right: 5px;
}

.icon-pichuli_outline:before {
  content: "\e6cd";
}

.icon-liulan:before {
  content: "\e71c";
}

.icon-set_outline:before {
  content: "\e709";
}

.icon-import_outline:before {
  content: "\e729";
}

.icon-chat_outline:before {
  content: "\e673";
}

.icon-telephone_directory_outline:before {
  content: "\e6e5";
}

.icon-listen_outline:before {
  content: "\e732";
}

.icon-qr_outline:before {
  content: "\e6c1";
}

.icon-ic_header_hamberg:before {
  content: "\e690";
}

.el-icon-plus:before {
  content: "\e6d9";
}

.icon-qr2_outline:before {
  content: "\e708";
}

.el-button [class*=el-icon-] + span {
  margin-left: 5px;
}

[class*=" el-icon-"], [class^=el-icon-] {
  font-family: element-icons !important;
  speak: none;
  font-style: normal;
  font-weight: 400;
  font-variant: normal;
  text-transform: none;
  line-height: 1;
  vertical-align: baseline;
  display: inline-block;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.component-notice .handle {
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  color: #4285f4;
  line-height: 20px;
}

.component-notice .handle .btn {
  cursor: pointer;
  width: 50%;
  height: 50px;
  line-height: 50px;
}

.text_overflow1, .text_overflow2 {
  display: -webkit-box;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
  white-space: normal;
  -webkit-box-orient: vertical;
}

.mgl-24 {
  margin-left: 24px;
}

.el-input--medium .el-input__inner {
  height: 36px;
  line-height: 36px;
}

.el-input--suffix .el-input__inner {
  padding-right: 30px;
}
</style>
