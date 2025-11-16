<script lang="ts" setup>
import { ref } from 'vue'

const value = ref('')
</script>

<template>
  <cl-upload v-model="value" />
</template>
