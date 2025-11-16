<script lang="ts" setup>
import CategoryRatio from '../components/category-ratio.vue'
import CountEffect from '../components/count-effect.vue'
import CountPaid from '../components/count-paid.vue'
import CountUser from '../components/count-user.vue'
import CountViews from '../components/count-views.vue'
import HotGoods from '../components/hot-goods.vue'
import TabChart from '../components/tab-chart.vue'

defineOptions({
  name: 'home',
})
</script>

<template>
  <el-scrollbar>
    <div class="demo-home">
      <el-row :gutter="10">
        <el-col :lg="6" :md="12" :xs="24">
          <count-user />
        </el-col>
        <el-col :lg="6" :md="12" :xs="24">
          <count-views />
        </el-col>
        <el-col :lg="6" :md="12" :xs="24">
          <count-paid />
        </el-col>
        <el-col :lg="6" :md="12" :xs="24">
          <count-effect />
        </el-col>
      </el-row>

      <el-row :gutter="10">
        <el-col :lg="24" :xs="24">
          <tab-chart />
        </el-col>
      </el-row>

      <el-row :gutter="10">
        <el-col :lg="14" :sm="24">
          <hot-goods />
        </el-col>
        <el-col :lg="10" :sm="24">
          <category-ratio />
        </el-col>
      </el-row>
    </div>
  </el-scrollbar>
</template>

<style lang="scss">
.demo-home {
  overflow-x: hidden;

  .card {
    border-radius: 10px;
    margin-bottom: 10px;
    border: 1px solid var(--el-border-color-extra-light);
    background-color: var(--el-bg-color);
    color: var(--el-text-color-primary);
    user-select: none;

    &__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 20px;
      min-height: 50px;

      .label {
        font-size: 15px;
      }

      .icon {
        font-size: 30px;
        background-color: var(--el-fill-color-light);
        padding: 5px;
        border-radius: 6px;
      }
    }

    &__container {
      padding: 0 20px;
      min-height: 50px;

      .num {
        font-size: 32px;
      }
    }

    &__footer {
      display: flex;
      align-items: center;
      height: 50px;
      margin: 0 5px;
      padding: 0 15px;
      box-sizing: border-box;
      font-size: 12px;

      .label {
        margin-right: 10px;
      }
    }
  }
}
</style>
