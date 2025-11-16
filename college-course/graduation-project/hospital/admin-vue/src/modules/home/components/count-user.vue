<script lang="ts" setup>
import { TopRight } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useCool } from '/@/cool'

const totalUsers = ref(0)
const totalUsersToday = ref(0)

const { service } = useCool()

onMounted(() => {
  service.user.info.count().then((res) => {
    totalUsers.value = res
  })
  service.user.info.countToday().then((res) => {
    totalUsersToday.value = res
  })
})

const rise = computed(() => {
  const result = totalUsersToday.value === totalUsers.value ? 100.00 : (totalUsersToday.value / (totalUsers.value - totalUsersToday.value)) * 100
  return result.toFixed(2)
})
</script>

<template>
  <div class="count-sales">
    <div class="card">
      <div class="card__header">
        <span class="label">{{ $t('总用户数') }}</span>
        <cl-svg name="team" class="icon" />
      </div>

      <div class="card__container">
        <cl-number :value="totalUsers" class="num" />

        <div class="rise">
          <el-icon>
            <top-right />
          </el-icon>

          <span>+{{ rise }}%</span>
        </div>
      </div>

      <div class="card__footer">
        <span class="mr-2">{{ $t('日增用户数') }}</span>
        <span>{{ totalUsersToday }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.count-sales {
  .fall,
  .rise {
    display: inline-flex;
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
</style>
