<template>
  <div>
    <!--    作业页面-->
    <div v-if="info.identity === 1" style="width: 100%;height: auto;padding-top: 40px;">
      <div style="display: flex;align-items: center">
        <el-button @click="edit = true" type="primary">发布个人作业</el-button>
        <el-button @click="todoAlert" plain style="color: blue">发布小组作业</el-button>
        <div @click="$router.push('future')" style="padding-left: 26px;
                            background: url('https://www.ketangpai.com/Public/Home/img/taskAachievementsL.png') left no-repeat;
                            background-size: 20px;
                            color: #5F6368;
                            font-size: 14px;
                            cursor: pointer;
                            margin:0 25px 0 680px;">作业成绩管理
        </div>
        <div @click="$router.push('future')" style="color: #5F6368;
                            font-size: 14px;
                            cursor: pointer;
                            line-height: 35px;
                            background: url('https://www.ketangpai.com/Public/Home/img/down-url.png') left no-repeat;
                            padding-left: 27px;">下载所有作业
        </div>
      </div>
      <HomeworkEdit v-if="edit" :info="{homework:{},new:'1'}" @close="edit = false"></HomeworkEdit>
    </div>
    <!--        未发布作业时-->
    <div v-if="info.homework.length === 0">
      <div style="width: 1224px;
                    height: auto;
                    background: #F1F3F4;
                    border-radius: 4px;
                    margin-top: 42px;
                    text-align: center;
                    padding: 35px 0;
                    margin-left: auto;
                    margin-right: auto;">
        <!--        老师页面-->
        <div v-if="info.identity === 1">
          <span style=" font-size: 14px;
                        color: #707070;
                        font-weight: 400;
                        margin-bottom: 5px;">欢迎开课！<br/></span>
          <span style=" color: #A0A0A0;
                        font-size: 14px;
                        line-height: 24px;">快速发布、收集学生作业，支持50多种文档在线批阅。
            <br>多维度（全班查重、作业字数等）统计学生作业情况。（点击<span style="color: #32BAF0;cursor: pointer">播放视频</span>，查看使用详情）
          </span>
        </div>
        <!--        学生页面-->
        <div v-else>欢迎来到课堂！<br/><br/>
          您的老师暂未发布作业
        </div>
      </div>
    </div>

    <div v-else style="padding-bottom: 80px">
      <HomeworkCard v-for="(item,i) in info.homework" :key="i"
                    :info="{user:info.user,homework:item,identity:info.identity}"></HomeworkCard>
    </div>
  </div>
</template>

<script>
import HomeworkEdit from "./HomeworkEdit.vue";
import HomeworkCard from "./HomeworkCard.vue";

export default {
  name: "Homework",
  components: {HomeworkCard, HomeworkEdit},
  data() {
    return {
      edit: false,
    }
  },
  props: ['info'],
  methods: {
    todoAlert() {
      this.$message({
        center: true,
        message: '暂未开放',
        type: 'warning'
      })
    }
  }
}
</script>

<style scoped>

</style>
