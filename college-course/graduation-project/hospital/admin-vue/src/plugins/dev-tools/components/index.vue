<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Logo from '../static/logo.png'
import { getIcon } from '../utils'
import ToolAccount from './account.vue'

import ToolDict from './dict.vue'
import ToolDoc from './doc.vue'
import ToolEps from './eps.vue'
import ToolProxy from './proxy.vue'
import { checkPerm, useBase } from '/$/base'
import { isDev } from '/@/config'
import { service, storage } from '/@/cool'

const { t } = useI18n()
const { user } = useBase()

const components = {
  eps: ToolEps,
  dict: ToolDict,
  proxy: ToolProxy,
  doc: ToolDoc,
  account: ToolAccount,
}

const tab = reactive({
  active: storage.get('devTools.tab') || 'eps',

  list: [
    {
      label: 'Eps',
      value: 'eps',
      icon: 'icon-shandian_2',
    },
    {
      label: t('代理请求'),
      value: 'proxy',
      icon: 'icon-jingji',
    },
    {
      label: t('字典'),
      value: 'dict',
      icon: 'icon-dingdanliebiao',
      isLogin: true,
      permission: {
        and: [service.dict.info.permission.list, service.dict.type.permission.list],
      },
    },
    {
      label: t('账号'),
      value: 'account',
      icon: 'icon-geren',
    },
    {
      label: t('文档'),
      value: 'doc',
      icon: 'icon-shuben',
    },
  ],

  change(item: any) {
    if (item.link) {
      window.open(item.link, '_blank')
      return
    }

    tab.active = item.value

    // 记录
    storage.set('devTools.tab', item.value)
  },
})

const tabList = computed(() => {
  return tab.list.filter((e) => {
    let f = true

    if (e.isLogin) {
      f = !!user.token
    }

    if (f) {
      if (e.permission) {
        f = checkPerm(e.permission)
      }
    }

    if (!f) {
      if (tab.active == e.value) {
        tab.active = 'eps'
      }
    }

    return f
  })
})

const info = computed(() => tab.list.find(e => e.value == tab.active))

const visible = ref(false)

function open() {
  if (visible.value) {
    close()
  }
  else {
    visible.value = true
  }
}

function close() {
  visible.value = false
}

onMounted(() => {
  getIcon()
  // open();
})
</script>

<template>
  <teleport to="body">
    <div
      class="dev-tools"
      :class="{
        'is-open': visible,
      }"
    >
      <div class="content">
        <el-scrollbar>
          <div v-if="info" class="inner">
            <component :is="components[tab.active]" />
          </div>
        </el-scrollbar>
      </div>

      <div class="menu">
        <div class="icon">
          <img :src="Logo">
        </div>

        <el-tooltip
          v-for="item in tabList"
          :key="item.value"
          :content="item.label"
          placement="left"
        >
          <div
            class="item"
            :class="{
              'is-active': item.value == tab.active,
            }"
            @click="tab.change(item)"
          >
            <i class="iconfont" :class="item.icon" />
          </div>
        </el-tooltip>
      </div>
    </div>

    <div
      v-if="isDev"
      class="dev-tools__close hidden-sm-and-down"
      :class="{
        'is-hide': !visible,
      }"
      @click="open"
    >
      <i class="iconfont icon-xia" />
    </div>
  </teleport>
</template>

<style lang="scss">
.dev-tools {
  position: fixed;
  bottom: 10px;
  right: 10px;
  background-color: var(--el-bg-color);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-light);
  transition: all 0.3s;
  height: 360px;
  width: 600px;
  max-width: 100%;
  display: flex;
  color: var(--el-text-color-primary);
  z-index: 9;
  opacity: 0;
  transform: translate(100%, 100%);

  &.is-open {
    transform: translate(0, 0);
    opacity: 1;
  }

  .menu {
    border-left: 1px solid var(--el-border-color-extra-light);
    padding: 8px;
    width: 50px;

    .icon {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 34px;
      width: 34px;
      background-color: #2c3142;
      border-radius: 8px;
      margin-bottom: 8px;

      img {
        height: 18px;
        width: 18px;
      }
    }

    .item {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 34px;
      width: 34px;
      cursor: pointer;
      border-radius: 8px;
      margin-top: 5px;

      i {
        font-size: 18px;
        color: var(--el-text-color-secondary);
      }

      &:hover {
        background-color: var(--el-fill-color-light);

        i {
          color: var(--el-text-color-primary);
        }
      }

      &.is-active {
        background-color: var(--el-fill-color-light);

        i {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .content {
    position: relative;
    flex: 1;

    .inner {
      padding: 10px;
    }
  }

  &__close {
    position: fixed;
    right: 20px;
    bottom: 20px;
    z-index: 10;
    border-radius: 20px;
    background-color: #2c3142;
    color: #fff;
    cursor: pointer;
    height: 30px;
    width: 30px;
    display: flex;
    align-items: center;
    justify-content: center;

    --rotate: -45deg;

    i {
      display: inline-block;
      font-size: 12px;
      transform: rotate(var(--rotate));
      transition: transform 0.3s;
    }

    &:hover {
      --rotate: -45deg;

      i {
        transform: rotate(var(--rotate)) translateY(5px);
      }
    }

    &.is-hide {
      --rotate: 135deg;
    }
  }
}
</style>
