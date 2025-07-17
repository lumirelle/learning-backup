<template>
  <div class="component-no_data">
    <div class="img-box">
      <img src="@/assets/img/img_empty.84d0931c.png" alt="">
    </div>
    暂无数据
  </div>
</template>


<script>
export default {
  name: "NoData",
}
</script>

<style scoped>
.component-no_data, .component-no_data .img-box {
  padding-bottom: 12px;
}

.component-no_data {
  height: -webkit-max-content;
  height: -moz-max-content;
  height: max-content;
  padding-top: 100px;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
  color: #74787c;
  line-height: 24px;
  margin-bottom: 30px;
}

.component-no_data, .component-no_data .img-box {
  padding-bottom: 12px;
}
</style>
