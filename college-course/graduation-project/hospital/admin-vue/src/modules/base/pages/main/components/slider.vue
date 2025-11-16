<script lang="ts" setup>
import { ref } from 'vue'
import BMenu from './bmenu'
import { useBase } from '/$/base'
import { useBrowser } from '/@/cool'

defineOptions({
  name: 'app-slider',
})

const { browser } = useBrowser()
const { app } = useBase()

const keyWord = ref('')
</script>

<template>
  <div
    class="app-slider"
    :class="{
      'is-collapse': app.isFold,
    }"
  >
    <div class="app-slider__logo">
      <img src="/logo.png">
      <span v-if="!app.isFold || browser.isMini">{{ app.info.name }}</span>
    </div>

    <div class="app-slider__search">
      <el-input
        v-model="keyWord"
        :placeholder="$t('搜索关键字')"
        clearable
        @focus="app.fold(false)"
      >
        <template #prefix>
          <cl-svg name="search" :size="16" />
        </template>
      </el-input>
    </div>

    <div class="app-slider__container">
      <el-scrollbar>
        <b-menu :key-word="keyWord" />
      </el-scrollbar>
    </div>
  </div>
</template>

<style lang="scss">
.app-slider {
  $slider-menu-height: 50px;
  --slider-bg-color: #2c3147;
  --slider-text-color: #e5eaf3;

  height: 100%;
  background-color: var(--slider-bg-color);
  border-right: 1px solid var(--el-border-color-extra-light);

  &__logo {
    display: flex;
    align-items: center;
    height: 66px;
    padding: 0 21px;
    user-select: none;

    img {
      height: 24px;
      width: 24px;
    }

    span {
      color: #fff;
      font-weight: bold;
      font-size: 20px;
      margin-left: 10px;
      white-space: nowrap;
      letter-spacing: 1px;
    }
  }

  &__search {
    margin: 0 10px 10px 10px;
    overflow: hidden;
    border-radius: 6px;

    .el-input__wrapper {
      background-color: rgba(200, 200, 200, 0.1);
      box-shadow: none;
      height: 36px;
      padding: 0 14px;

      .el-input__inner {
        color: var(--slider-text-color);
      }
    }
  }

  &__container {
    height: calc(100% - 112px);
  }

  &__menu {
    user-select: none;

    .b-menu__badge {
      display: flex;
      align-items: center;
      justify-content: center;
      height: $slider-menu-height;
      font-size: 10px;
      height: 14px;
      min-width: 14px;
      padding: 0 3px;
      border-radius: 4px;
      background-color: rgba(255, 255, 255, 0.2);
      font-weight: bold;
      color: #fff;
      transition: background-color 0.3s;
    }

    .el-menu {
      width: 100%;
      border-right: 0;
      background-color: transparent;

      &--popup {
        border-radius: 6px;
        padding: 5px;

        &-container {
          padding: 0;
        }

        .el-menu-item,
        .el-sub-menu__title {
          height: $slider-menu-height;
          border-radius: 6px;

          &:hover {
            background-color: var(--el-fill-color-light);
          }
        }
      }

      &:not(&--popup) {
        --el-menu-base-level-padding: 23px;

        .el-menu-item,
        .el-sub-menu__title {
          height: $slider-menu-height;
          color: var(--slider-text-color);

          .cl-svg {
            flex-shrink: 0;
          }

          &.is-active,
          &:hover {
            background-color: rgba(0, 0, 0, 0.25);
            color: #fff;
          }

          &.is-active {
            background-color: var(--el-color-primary);
          }
        }
      }
    }
  }

  &.is-collapse {
    .app-slider__search {
      .el-input__inner {
        opacity: 0;
      }
    }

    .app-slider__menu {
      .el-sub-menu {
        &.is-active {
          background-color: rgba(0, 0, 0, 0.25);
        }
      }
    }
  }
}
</style>
