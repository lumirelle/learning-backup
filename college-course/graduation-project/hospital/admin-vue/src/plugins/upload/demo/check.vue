<script lang="ts" setup>
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

function onBeforeUpload(file: any) {
  return new Promise((resolve) => {
    if (file.size > 100000) {
      ElMessage.warning(t('文件不能大于100k'))
    }
    else {
      resolve(true)
    }
  })
}
</script>

<template>
  <cl-upload :before-upload="onBeforeUpload" />
</template>
