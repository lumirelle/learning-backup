<script lang="ts" setup>
import { ref } from 'vue'

const wang = ref(
  '<p><span style="font-size: 22px;"><em>富文本编</em></span><span style="color: rgb(216, 68, 147); font-size: 22px;"><em>辑器</em></span></p>',
)
</script>

<template>
  <cl-editor-preview v-model="wang" name="wang" :text="$t('查看内容')" />
</template>
