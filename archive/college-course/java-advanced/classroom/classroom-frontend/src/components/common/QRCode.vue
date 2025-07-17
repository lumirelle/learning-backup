<template>
  <div class="component-qrcode">
    <div class="qrcode-box flex-between" :style="style">
      <div class="left" :style="style">
        <div><h1> 加课码: <span>{{ info.course.courseKey }}</span></h1>
          <h3>微信扫一扫加入课堂</h3>
          <div class="code-container">
            <img
                src=" https://openapiv5.ketangpai.com/QRCode/getCourse/ticketid/MDAwMDAwMDAwMLOGy5mGqclqhdtyoQ/code/MRFM77/secondDomain/www/expire/4798033073/sign/f4079ee6bffc015260c34fa377c1fa5a8dbf2153.html"
                alt="">
          </div>
        </div>
      </div>
      <div class="right" :style="style">
        <div class="header"><i @click="close" class="font_family icon-close_outline"></i></div>
        <div class="title-name"><h2>{{ info.course.name }}</h2>
          <h3 class="text_overflow2">{{ info.course.className }}</h3>
          <p class="count">{{ info.size }}</p>
          <h3>已加课人数</h3></div>
        <p class="but-box">
          <el-button @click="close" size="medium" type="primary" round>返回</el-button>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "QRCode",
  props: ['info'],
  emits: ['close'],
  data() {
    return {
      style: {
        minHeight: 'calc(100vh / ' + window.zoomRate + ')',
      }
    }
  },
  methods: {
    close() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.component-qrcode {
  width: 100%;
  height: 100%;
  color: #fff;
  overflow: hidden;
}

.component-qrcode .qrcode-box {
  border-radius: 0;
  background: url(@/assets/img/bg.736bc955.png) no-repeat 50%/cover;
  width: 100%;
  position: relative;
}

.component-qrcode .left {
  height: 100%;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.component-qrcode .left h1 {
  font-size: 40px;
  font-weight: 500;
  text-align: center;
  line-height: 40px;
}

.component-qrcode .left h3 {
  margin: 24px 0 32px;
  font-size: 24px;
  text-align: center;
  line-height: 32px;
}

.component-qrcode .left .code-container {
  width: 446px;
  height: 446px;
  padding: 16px;
  background-image: url(@/assets/img/bg2.f1d135dc.png);
  background-size: 100%;
  background-position: 50%;
  background-repeat: no-repeat;
  background-color: hsla(0, 0%, 100%, .4);
  border-radius: 8px;
}

.component-qrcode .left .code-container img {
  width: 100%;
  height: 100%;
}

.component-qrcode .right {
  height: 100%;
  width: 400px;
  text-align: center;
  background-color: hsla(0, 0%, 100%, .4);
}

.component-qrcode .right .header {
  padding: 16px 0;
  text-align: right;
}

.component-qrcode .right .header i {
  font-size: 38px;
  margin: 0 8px;
  cursor: pointer;
}

.icon-close_outline:before {
  content: "\e663";
}

.component-qrcode .right .title-name {
  padding: 40px 0;
}

.component-qrcode .right .title-name h2 {
  font-size: 30px;
  margin-bottom: 16px;
}

.component-qrcode .right .title-name h3 {
  font-size: 24px;
}

.component-qrcode .right .count {
  padding-top: 60px;
  font-size: 120px;
  font-weight: 700;
  color: #04f5b5;
}

.component-qrcode .right .but-box {
  position: absolute;
  width: 400px;
  text-align: center;
  bottom: 64px;
}

.component-qrcode .right .el-button {
  width: 200px;
  height: 56px;
  font-size: 22px;
  font-weight: 500;
  border-radius: 56px;
}
</style>
