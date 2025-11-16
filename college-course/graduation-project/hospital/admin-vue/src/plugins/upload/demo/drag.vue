<template>
  <div>
    <el-divider content-position="left">
      {{ $t('拖拽排序') }}
    </el-divider>
    <cl-upload multiple draggable />
  </div>

  <div>
    <el-divider content-position="left">
      {{ $t('拖拽上传') }}
    </el-divider>
    <cl-upload drag :size="[160, 300]" />
  </div>
</template>
