<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import { assign, isEmpty } from 'lodash-es'
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useBase } from '/$/base'
import { isDev } from '/@/config'
import { storage } from '/@/cool'

interface Item {
  userId: number
  avatar: string
  nickName: string
  username: string
  token: string
  refreshToken: string
  enable: boolean
}

const { t } = useI18n()
const { user } = useBase()

const list = ref<Item[]>([])

function refresh() {
  list.value = storage.get('devTools.account') || []
  save()
}

// 删除
function remove(index: number) {
  ElMessageBox.confirm(t('是否删除该账号？'), t('提示'), {
    type: 'warning',
  })
    .then(() => {
      list.value.splice(index, 1)
      update()
    })
    .catch(() => null)
}

// 添加、更新
function save() {
  if (!isDev) {
    return
  }

  if (!user.info) {
    return
  }

  const { nickName, username, id, headImg } = user.info!

  const item = list.value.find(e => e.userId == id)

  list.value.forEach((e) => {
    e.enable = false
  })

  const d = {
    token: user.token,
    refreshToken: storage.get('refreshToken')!,
    nickName: nickName!,
    username: username!,
    userId: id!,
    avatar: headImg!,
    enable: true,
  }

  if (item) {
    assign(item, d)
  }
  else {
    list.value.unshift(d)
  }

  update()
}

// 清除
function clear() {
  list.value.forEach((e) => {
    e.enable = false
  })
}

// 更新
function update() {
  storage.set('devTools.account', list.value)
}

// 切换
async function onChange(item: Item) {
  user.setToken({
    token: item.token,
    expire: 10,
    refreshToken: item.refreshToken,
    refreshExpire: 20,
  })

  await user.get()

  location.reload()
}

onMounted(() => {
  watch(
    () => user.info?.id,
    (val) => {
      refresh()

      if (!val) {
        clear()
      }
    },
    {
      immediate: true,
    },
  )
})
</script>

<template>
  <div class="dt-account">
    <div v-for="(item, index) in list" :key="item.userId" class="item flex items-center mb-[10px]">
      <cl-avatar :src="item.avatar" :size="28" />

      <el-text class="ml-[8px] mr-auto">
        {{ item.nickName }}（{{ item.username }}）
      </el-text>

      <el-switch v-model="item.enable" :disabled="item.enable" @change="onChange(item)" />

      <div class="cl-comm__icon remove" @click="remove(index)">
        <cl-svg name="close" />
      </div>
    </div>

    <el-empty v-if="isEmpty(list)" :image-size="80" />
  </div>
</template>

<style lang="scss" scoped>
.dt-account {
  .item {
    .remove {
      margin-left: 10px;
      border-radius: 30px;
      height: 20px;
      width: 20px;

      .cl-svg {
        font-size: 12px;
      }
    }
  }
}
</style>
