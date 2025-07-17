<template>
  <div>
    <div v-if="info.identity === 1" style="width: 100%;height: auto;padding-top: 40px;">
      <div style="display: flex;align-items: center" v-if="inform">
        <el-button @click="edit = true;inform=false" type="primary">发布公告</el-button>
      </div>
      <InformEdit v-if="edit" :info="{user:info.user,informs,new: news}"
                  @close="edit = false;inform=true"></InformEdit>
    </div>
    <div v-if="this.informs.length === 0">
      <div style="width: 1224px;
                    height: auto;
                    background: #F1F3F4;
                    border-radius: 4px;
                    margin-top: 42px;
                    text-align: center;
                    padding: 35px 0;
                    margin-left: auto;
                    margin-right: auto;">
        <!--        教师端公告-->
        <div v-if="info.identity === 1">
          <span style=" font-size: 14px;
                        color: #707070;
                        font-weight: 400;
                        margin-bottom: 5px;">点击上方按钮，您可以发布新公告。<br/></span>
          <span style=" color: #A0A0A0;
                        font-size: 14px;
                        line-height: 20px;">
            <br>发布课程公告后，即刻推送到学生的“微信”
          </span>
        </div>
        <!--        学生端公告-->
        <div v-else>
          欢迎来到课堂！<br/><br/>
          您的老师暂未发布公告
        </div>
      </div>
    </div>
    <div v-else style="padding-bottom: 80px">
      <template v-for="(item,i) in this.informs">
        <InformCard v-if="item.top === '1'" @getInform="getInform" :key="i"
                    :info="{user:info.user,courseId: info.course.id,informs:item,identity:info.identity}"/>
        <InformCard v-else-if="item.top === '0'" @getInform="getInform" :key="i"
                    :info="{user:info.user,courseId: info.course.id,informs:item,identity:info.identity}"/>
      </template>

    </div>

  </div>
</template>

<script>
import InformEdit from "./InformEdit.vue";
import InformCard from "./InformCard.vue";

export default {
  name: "Inform",
  components: {InformCard, InformEdit},
  data() {
    return {
      edit: false,
      edit2: false,
      inform: true,
      informs: [],
      informed: [],
      news: '1'
    }
  },
  props: ['info'],
  mounted() {
    this.getInform()
  },
  methods: {
    getInform() {
      this.$axios.post('/start/inform', {
            courseId: this.info.course.id,
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          if (resp.data !== "") {
            this.informs = resp.data
          }
        }
      }).catch(reason => {
        console.log(reason)
      })
    }
  }
}
</script>

<style scoped>

</style>
