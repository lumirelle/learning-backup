<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useCool } from '/@/cool'

const { service } = useCool()

const num = ref(0)

onMounted(() => {
  service.order.info.countPayed().then((res) => {
    num.value = res
  })
})

const rise = computed(() => {
  return 100
})
</script>

<template>
  <div class="count-paid">
    <div class="card">
      <div class="card__header">
        <span class="label">{{ $t('付款笔数') }}</span>
        <cl-svg name="order" class="icon" />
      </div>

      <div class="card__container">
        <cl-number :value="num" class="num" suffix="笔" />
      </div>

      <div class="card__footer">
        <span class="mr-2">{{ $t('转化率') }}</span>
        <span>{{ rise }}%</span>
      </div>
    </div>
  </div>
</template>
