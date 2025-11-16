<template>
  <!--    公告卡牌-->
  <div id="informCard" style="border: 1px solid #E2E6ED;
                border-radius: 8px;
                background: #FFF;
                margin-top: 20px;
                z-index: 6">
    <el-card v-if="!edit" :body-style="{width:'100%',height:'100%',padding:'0'}" style="height: 100%;padding: 0 20px 0 40px">
      <div style="display: flex;flex-direction: column">

        <div style="display:flex;
                            justify-content: space-between;
                            color: #AAA;
                            padding: 21px 0 10px;
                            height: 25px;
                            ">
          <h3 style="
                    font-size: 20px;
                    line-height: 30px;" @click="jump(1)" class="informName">
            <b class="toptips" v-if="this.info.informs.top === '1'">置顶</b>
            {{ info.informs.name }}
          </h3>
          <!--  教师端编辑-->
          <div v-if="info.identity !== 0">
            <el-dropdown @command="handleCommand" trigger="click">
              <div id="rowDots"></div>
              <el-dropdown-menu slot="dropdown" placement="bottom-end" style="margin: -26px 0 0 -20px;width: 120px;">
                <el-dropdown-item :command="1" v-if="this.info.informs.top==='0'">置顶</el-dropdown-item>
                <el-dropdown-item :command="1" v-if="this.info.informs.top==='1'">取消置顶</el-dropdown-item>
                <el-dropdown-item :command="2">编辑</el-dropdown-item>
                <el-dropdown-item :command="3">删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>

        <div style="display: flex;justify-content: space-between">
          <div style="margin:0 0 0 -1px;font-size: 12px;color: #A0A0A0;">
            <span>发布人: {{ this.teacher.name }}  发布时间: {{ info.informs.time }}</span>
          </div>
        </div>
        <div style="margin-top: 10px;
                    color:#707070;
                    overflow: hidden;
                    line-height: 1.8;
                    display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 1;
                    ">
          <span v-html="ToText(info.informs.intro)"></span>
        </div>
        <span style="height: 20px;line-height: 20px;margin: 18px 0 18px -1px;color: #A0A0A0;">
          <img v-if="info.identity === 0"
               src="https://www.ketangpai.com/Public/Home/img/recruitment/ic_card_time@2x.png" alt=""
               style="width: 14px;height: 14px;line-height: 14px;"/>
          0人已读
          <span class="discuss">0条评论</span>
        </span>
      </div>
    </el-card>
    <InformEdit v-else :info="{user:info.user,informs:info.informs,new:'0'}" @close="edit = false"></InformEdit>
  </div>
</template>

<script>
import InformEdit from "./InformEdit.vue";

export default {
  name: "InformCard",
  components: {InformEdit},
  data() {
    return {
      edit: false,
      number: '',
      submitNum: '',
      unSubmitNum: '',
      submit: false,

      teacher: {},
      address: {}
    }
  },
  props: ['info'],
  mounted() {
    this.getUser()
  },
  methods: {

    //获取发布人
    getUser() {
      this.$axios.post('/start/issuer', {
            userId: this.info.informs.user,
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          this.teacher = resp.data.user
          this.address = resp.data.address
        }
      }).catch(reason => {
        console.log(reason)
      })
    },
    //转化文本
    ToText(HTML) {
      return HTML.replace(/<(style|script|iframe)[^>]*?>[\s\S]+?<\/\1\s*>/gi, '')
          .replace(/<[^>]+?>/g, '').replace(/\s+/g, '').replace(/ /g, '').replace(/>/g, '')
    },
    jump() {
      localStorage.setItem('InformId', this.info.informs.id)
      this.$router.push('/informInfo')
    },

    SetTop() {
      this.$axios.post('/teacher/setTop', {
            id: this.info.informs.id,
            top: this.info.informs.top,
            course: this.info.courseId
          },
          {
            headers: {'Authorization': this.$store.state.Authorization}
          }).then(resp => {
        if (resp.status === 200) {
          this.$message({
            showClose: true,
            center: true,
            offset: this.$store.state.tip,
            message: resp.data

          })
          this.$emit('getInform')
          // this.$router.go(0)
        }
      })
    },

    handleCommand(command) {
      switch (parseInt(command)) {
        case 1:
          this.SetTop()
          break
        case 2:
          this.edit = true
          break
        case 3:
          this.$confirm('', '确认要删除该公告？', {lockScroll: false}).then(() => {
            this.$axios.post('/teacher/informDelete', {
                  informId: this.info.informs.id,
                },
                {
                  headers: {'Authorization': this.$store.state.Authorization}
                }).then(resp => {
              this.$message({
                showClose: true,
                center: true,
                offset: this.$store.state.tip,
                message: resp.data
              })
              this.$emit('getInform')
            })
          })
      }
    }
  }
}
</script>

<style scoped>
#rowDots {
  display: block;
  margin-top: -11px;
  width: 34px;
  height: 34px;
  background: url('https://www.ketangpai.com/Public/Home/img/opt1.png') left center no-repeat;
  border-radius: 3px;
  cursor: pointer;
}

#rowDots:hover {
  background: url('https://www.ketangpai.com/Public/Home/img/opt1.png') right center no-repeat;
}

#informCard .informName {
  display: inline-block;
  margin: -8px 0 5px 0;
  color: #3B3D45;
  font-size: 20px;
  font-weight: 400;
  line-height: 30px;
  padding-bottom: 5px;
  word-break: break-all;
  word-wrap: break-word;
  cursor: pointer;
}


#informCard .informName:hover {
  text-decoration: underline;
}

#informCard .discuss {
  margin-left: 11px;
  cursor: pointer;
}

#informCard .discuss:hover {
  color: #32BAF0;
}

/*#homeworkInput {*/
/*    width: 80px;*/
/*    height: 30px;*/
/*    line-height: 30px;*/
/*    color: #32BAF0;*/
/*    border: 1px solid #32BAF0;*/
/*    float: right;*/
/*    text-align: center;*/
/*    border-radius: 4px;*/
/*    font-size: 14px;*/
/*    cursor: pointer;*/
/*}*/

/*#homeworkInput:hover {*/
/*    background: #32BAF0;*/
/*}*/
.toptips {
  display: inline-block;
  vertical-align: middle;
  height: 20px;
  line-height: 20px;
  padding: 0 5px;
  color: #fff;
  font-size: 12px;
  font-weight: 400;
  background-color: #2C57AB;
  border-radius: 4px;
  margin-right: 5px;
}

.informName {
  color: #2F2F2F;

}

#informCard .informName:hover {
  color: #4D90FE;
}
</style>
