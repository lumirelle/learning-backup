<script lang="ts" setup>
import { BottomRight, TopRight } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { service } from '/@/cool'

const num = ref(0)

onMounted(async () => {
  const res = await service.order.info.sumAmount()
  num.value = res
})
</script>

<template>
  <div class="count-effect">
    <div class="card">
      <div class="card__header">
        <span class="label">{{ $t('总销售额') }}</span>

        <cl-svg name="amount" class="icon" />
      </div>

      <div class="card__container">
        <cl-number :value="num" class="num" type="amount" suffix="元" />
      </div>

      <div class="card__footer">
        <ul class="cop">
          <li>
            <span>{{ $t('周同比') }}</span>

            <div class="fall">
              <el-icon>
                <bottom-right />
              </el-icon>

              <span>-4%</span>
            </div>
          </li>

          <li>
            <span>{{ $t('日同比') }}</span>

            <div class="rise">
              <el-icon>
                <top-right />
              </el-icon>

              <span>+7%</span>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.count-effect {
  .cop {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;

    li {
      display: flex;
      list-style: none;
      flex: 1;

      .fall,
      .rise {
        display: flex;
        align-items: center;
        margin-left: 10px;
      }

      .fall {
        color: var(--el-color-success);
      }

      .rise {
        color: var(--el-color-danger);
      }
    }
  }
}
</style>
