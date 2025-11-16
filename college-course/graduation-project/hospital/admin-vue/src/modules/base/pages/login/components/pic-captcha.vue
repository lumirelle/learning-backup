<script lang="ts" setup>
import { Loading } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCool } from '/@/cool'

defineOptions({
  name: 'pic-captcha',
})

const emit = defineEmits(['update:modelValue', 'change'])

const { service } = useCool()
const { t } = useI18n()

// base64
const base64 = ref('')

// svg
const svg = ref('')

// 刷新
async function refresh() {
  svg.value = ''
  base64.value = ''

  await service.base.open
    .captcha({
      height: 45,
      width: 150,
      color: '#2c3142',
    })
    .then(({ captchaId, data }) => {
      if (data) {
        if (data.includes(';base64,')) {
          base64.value = data
        }
        else {
          svg.value = data
        }

        emit('update:modelValue', captchaId)
        emit('change', {
          base64,
          svg,
          captchaId,
        })
      }
      else {
        ElMessageBox.alert(t('验证码获取失败'), {
          title: t('提示'),
          type: 'error',
        })
      }
    })
    .catch((err) => {
      ElMessageBox.alert(err.message, {
        title: t('提示'),
        type: 'error',
      })
    })
}

onMounted(() => {
  refresh()
})

defineExpose({
  refresh,
})
</script>

<template>
  <div class="pic-captcha" @click="refresh">
    <div v-if="svg" class="svg" v-html="svg" />
    <img v-else-if="base64" class="base64" :src="base64" alt="">

    <template v-else>
      <el-icon class="is-loading" :size="18">
        <loading />
      </el-icon>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.pic-captcha {
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  height: 45px;
  width: 150px;
  position: relative;
  user-select: none;

  .svg {
    height: 100%;
    position: relative;
  }

  .base64 {
    height: 100%;
  }

  .is-loading {
    position: absolute;
    right: 15px;
  }
}
</style>
