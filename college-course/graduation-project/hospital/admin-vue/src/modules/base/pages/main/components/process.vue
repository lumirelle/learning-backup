<script lang="ts" setup>
import { ContextMenu } from '@cool-vue/crud'
import { last } from 'lodash-es'
import { onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useBase } from '/$/base'
import { useCool } from '/@/cool'

defineOptions({
  name: 'app-process',
})

const { refs, setRefs, route, router, mitt } = useCool()
const { process, app } = useBase()
const { t } = useI18n()

// 刷新当前路由
function toRefresh() {
  mitt.emit('view.refresh')
}

// 回首页
function toHome() {
  router.push('/')
}

// 返回上一页
function toBack() {
  router.back()
}

// 设置全屏
function toFull() {
  app.setFull(!app.isFull)
}

// 跳转
function toPath() {
  const d = process.list.find(e => e.active)

  if (!d) {
    const next = last(process.list)
    router.push(next ? next.fullPath : '/')
  }
}

// 移动到
function scrollTo(left: number) {
  refs.scroller.wrapRef.scrollTo({
    left,
    behavior: 'smooth',
  })
}

// 调整滚动位置
function adScroll(index: number) {
  const el = refs[`item-${index}`]

  if (el) {
    scrollTo(el.offsetLeft - (refs.scroller.wrapRef.clientWidth + el.clientWidth) / 2)
  }
}

// 选择
function onTap(item: Process.Item, index: number) {
  adScroll(index)
  router.push(item.fullPath)
}

// 删除
function onDel(index: number) {
  process.remove(index)
  toPath()
}

// 右键菜单
function openCM(e: any, item: Process.Item) {
  ContextMenu.open(e, {
    list: [
      {
        label: t('关闭当前'),
        hidden: item.path !== route.path,
        callback(done) {
          done()

          process.close()
          toPath()
        },
      },
      {
        label: t('关闭其他'),
        callback(done) {
          done()

          process.set(process.list.filter(e => e.fullPath == item.fullPath))
          toPath()
        },
      },
      {
        label: t('关闭所有'),
        callback(done) {
          done()

          process.clear()
          toPath()
        },
      },
    ],
  })
}

watch(
  () => route.path,
  (val) => {
    adScroll(process.list.findIndex(e => e.fullPath === val) || 0)
  },
)

onMounted(() => {
  // 添加滚轮事件监听器
  refs.scroller.wrapRef?.addEventListener(
    'wheel',
    (event: WheelEvent) => {
      // 滚动的速度因子，可以根据需要调整
      const scrollSpeed = 2

      // 计算滚动的距离
      const distance = event.deltaY * scrollSpeed

      scrollTo(refs.scroller.wrapRef.scrollLeft + distance)
    },
    { passive: false },
  )
})
</script>

<template>
  <div class="app-process">
    <ul class="app-process__op">
      <li class="cl-comm__icon" @click="toBack">
        <cl-svg name="back" />
      </li>
      <li class="cl-comm__icon" @click="toRefresh">
        <cl-svg name="refresh" />
      </li>
      <li class="cl-comm__icon" @click="toHome">
        <cl-svg name="home" />
      </li>
    </ul>

    <div class="app-process__container">
      <el-scrollbar :ref="setRefs('scroller')" class="app-process__scroller">
        <div class="app-process__list">
          <div
            v-for="(item, index) in process.list"
            :key="index"
            :ref="setRefs(`item-${index}`)"
            class="app-process__item"
            :class="{ active: item.active }"
            :data-index="index"
            @click="onTap(item, Number(index))"
            @contextmenu.stop.prevent="openCM($event, item)"
          >
            <span class="label tracking-wider">
              {{ item.meta.label || item.name || item.path }}
            </span>

            <cl-svg class="close" name="close" @mousedown.stop="onDel(Number(index))" />
          </div>
        </div>
      </el-scrollbar>
    </div>

    <ul class="app-process__op">
      <li class="cl-comm__icon" @click="toFull">
        <cl-svg v-if="app.isFull" name="screen-normal" />
        <cl-svg v-else name="screen-full" />
      </li>
    </ul>
  </div>
</template>

<style lang="scss" scoped>
.app-process {
  display: flex;
  align-items: center;
  position: relative;
  padding: 5px 10px;
  user-select: none;
  background-color: var(--el-bg-color);
  margin-bottom: 10px;
  overflow: hidden;

  &__op {
    display: flex;
    align-items: center;
    list-style: none;

    .cl-comm__icon {
      margin-right: 5px;

      &:last-child {
        margin-right: 0;
      }
    }
  }

  &__container {
    height: 100%;
    flex: 1;
    position: relative;
    margin: 0 5px;
  }

  &__scroller {
    height: 40px;
    width: 100%;
    white-space: nowrap;
    position: absolute;
    left: 0;
    top: 0;
  }

  &__item {
    display: inline-flex;
    align-items: center;
    justify-content: space-between;
    height: 26px;
    padding: 0 8px;
    cursor: pointer;
    color: var(--el-text-color-regular);
    border-radius: var(--el-border-radius-base);
    margin-right: 5px;
    border: 1px solid var(--el-fill-color-dark);

    .close {
      width: 0;
      overflow: hidden;
      transition: width 0.2s ease-in-out;
      font-size: 14px;
      border-radius: 4px;
      opacity: 0;

      &:hover {
        background-color: rgba(0, 0, 0, 0.1);
      }
    }

    .label {
      font-size: 12px;
      line-height: 1;
    }

    &:last-child {
      margin-right: 0;
    }

    &:hover:not(.active) {
      background-color: var(--el-fill-color-light);
    }

    &.active {
      background-color: var(--el-color-primary);
      border-color: var(--el-color-primary);
      color: #fff;
    }

    &:hover,
    &.active {
      .close {
        margin-left: 10px;
        margin-right: -2px;
        width: 14px;
        opacity: 1;
      }
    }
  }
}
</style>
