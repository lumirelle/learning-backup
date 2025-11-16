<script setup lang="ts">
import { ref } from 'vue'
import { config } from '/@/config'
import { storage } from '/@/cool'

const list = ref(config.i18n.languages)

function setLang(value: string) {
  storage.set('locale', value)
  location.reload()
}
</script>

<template>
  <div class="i18n-switch">
    <el-dropdown trigger="click">
      <div class="cl-comm__icon">
        <cl-svg name="i18n-lang" />
      </div>

      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item v-for="item in list" :key="item.value" @click="setLang(item.value)">
            <div class="i18n-switch__item" :class="{ active: config.i18n.locale === item.value }">
              <span class="i18n-switch__item-label">{{ item.label }}</span>
              <span class="i18n-switch__item-dot" />
            </div>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<style lang="scss" scoped>
.i18n-switch {
  &__item {
    width: 120px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    &-label {
      font-size: 12px;
    }

    &.active {
      .i18n-switch__item-label {
        color: var(--el-color-primary);
      }

      .i18n-switch__item-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background-color: var(--el-color-primary);
      }
    }

    &:hover {
      color: var(--el-color-primary);
    }
  }
}
</style>
