<script lang="ts" setup>
import { ElMessageBox } from 'element-plus'
import { isFunction, orderBy } from 'lodash-es'
import { computed, markRaw, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import AMenu from './amenu.vue'
import RouteNav from './route-nav.vue'
import { useBase } from '/$/base'
import { module, useCool } from '/@/cool'

defineOptions({
  name: 'app-topbar',
})

const { router, service, browser } = useCool()
const { user, app } = useBase()
const { t } = useI18n()

// 命令事件
async function onCommand(name: string) {
  switch (name) {
    case 'my':
      router.push('/my/info')
      break
    case 'exit':
      ElMessageBox.confirm(t('确定退出登录吗？'), t('提示'), {
        type: 'warning',
      })
        .then(async () => {
          await service.base.comm.logout()
          user.logout()
        })
        .catch(() => null)
      break
  }
}

// 工具栏
const toolbar = reactive({
  list: [] as any[],

  async init() {
    const arr = orderBy(
      module.list.filter(e => e.enable !== false && !!e.toolbar).map(e => e.toolbar),
      'order',
    )

    this.list = await Promise.all(
      arr
        .filter(e => e?.component)
        .map(async (e) => {
          if (e) {
            const c = await (isFunction(e.component) ? e.component() : e.component)

            return {
              ...e,
              component: markRaw(c.default || c),
            }
          }
        }),
    )
  },
})

// 工具栏组件
const toolbarComponents = computed(() => {
  return toolbar.list.filter((e) => {
    if (browser.isMini) {
      return e?.h5 ?? true
    }

    return e?.pc ?? true
  })
})

onMounted(() => {
  toolbar.init()
})
</script>

<template>
  <div class="app-topbar">
    <div class="cl-comm__icon mr-[10px]" @click="app.fold()">
      <cl-svg v-if="app.isFold" name="fold" />
      <cl-svg v-else name="expand" />
    </div>

    <!-- 路由导航 -->
    <a-menu v-if="app.info.menu.isGroup" />
    <route-nav v-else />

    <div class="flex1" />

    <!-- 工具栏 -->
    <ul class="app-topbar__tools">
      <li v-for="(item, index) in toolbarComponents" :key="index">
        <component :is="item.component" />
      </li>
    </ul>

    <!-- 用户信息 -->
    <template v-if="user.info">
      <el-dropdown
        hide-on-click
        popper-class="app-topbar__user-popper"
        :popper-options="{}"
        @command="onCommand"
      >
        <div class="app-topbar__user">
          <el-text class="mr-[10px]">
            {{ user.info.nickName }}
          </el-text>
          <cl-avatar :size="26" :src="user.info.headImg" />
        </div>

        <template #dropdown>
          <div class="user">
            <cl-avatar :size="34" :src="user.info.headImg" />

            <div class="det">
              <el-text size="small" tag="p">
                {{ user.info.nickName }}
              </el-text>
              <el-text size="small" type="info">
                {{ user.info.email }}
              </el-text>
            </div>
          </div>

          <el-dropdown-menu>
            <el-dropdown-item command="my">
              <cl-svg name="my" />
              <span>{{ t('个人中心') }}</span>
            </el-dropdown-item>
            <el-dropdown-item command="exit">
              <cl-svg name="exit" />
              <span>{{ t('退出登录') }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.app-topbar {
  display: flex;
  align-items: center;
  height: 46px;
  padding: 0 10px;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-extra-light);
  box-sizing: border-box;
  transition: height 0.2s ease-in-out;

  .flex1 {
    flex: 1;
  }

  &__tools {
    display: flex;
    margin-right: 10px;

    & > li {
      display: flex;
      justify-content: center;
      align-items: center;
      list-style: none;
      height: 45px;
      cursor: pointer;
      margin-left: 10px;
    }
  }

  &__user {
    display: flex;
    align-items: center;
    outline: none;
    cursor: pointer;
    white-space: nowrap;
    padding: 5px 5px 5px 10px;
    border-radius: 6px;

    &:hover {
      background-color: var(--el-fill-color-light);
    }
  }

  :deep(.cl-comm__icon) {
    &:hover {
      border-color: var(--el-color-primary);
      background-color: transparent;
    }
  }
}
</style>

<style lang="scss">
.app-topbar__user-popper {
  .el-dropdown-menu__item {
    padding: 6px 12px;
    font-size: 12px;
  }

  .user {
    display: flex;
    align-items: center;
    padding: 10px 10px;
    width: 200px;
    border-bottom: 1px solid var(--el-color-info-light-9);

    .det {
      margin-left: 10px;
      flex: 1;
      font-size: 12px;
    }
  }

  .cl-svg {
    margin-right: 8px;
    font-size: 16px;
  }
}
</style>
