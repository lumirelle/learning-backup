<script lang="ts" setup>
import { ref } from 'vue'

const value = ref([])
</script>

<template>
  <cl-distpicker v-model="value" />
  <span :style="{ marginLeft: '20px' }">{{ value }}</span>
</template>
