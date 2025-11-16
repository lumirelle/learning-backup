<script lang="ts" setup>
import { Check } from '@element-plus/icons-vue'
import { useDark } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import { assign } from 'lodash-es'
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTheme } from '../hooks'
import { useBase } from '/$/base'

defineOptions({
  name: 'cl-theme',
})

const { menu } = useBase()
const { t } = useI18n()
const { theme, setTheme, changeDark, themes } = useTheme()

// 是否暗黑模式
const isDark = useDark()

// 表单
const form = reactive<{ color: string, theme: Theme }>({
  color: theme?.color || '',
  theme,
})

// 抽屉
const visible = ref(false)

// 打开
function open() {
  visible.value = true
}

// 清除暗黑模式
function clearDark() {
  isDark.value = false
}

// 设置颜色
function setColor(color: any) {
  setTheme({ color })
  clearDark()
}

// 设置暗黑模式
function setDark(el: any) {
  changeDark(el.srcElement, !isDark.value, () => {
    isDark.value = !isDark.value
    setTheme({ color: form.color, dark: isDark.value })
  })
}

// 设置推荐
function setComd(item: any) {
  assign(form.theme, item)
  form.color = item.color
  setTheme(item)
  ElMessage.success(`${t('切换主题')}: ${item.label}`)
}

// 设置分组
function setGroup(val: any) {
  setTheme({ isGroup: val })
  menu.setMenu()
}

// 设置转场动画
function setTransition(val: any) {
  setTheme({ transition: val })
}
</script>

<template>
  <div class="cl-comm__icon" @click="open">
    <cl-svg name="theme" />
  </div>

  <div class="cl-comm__icon ml-[10px]" @click="setDark">
    <cl-svg :name="isDark ? 'light' : 'dark'" />
  </div>

  <el-drawer
    v-model="visible"
    :title="$t('设置主题')"
    size="350px"
    modal-class="drawer-theme"
    append-to-body
  >
    <div class="cl-theme__drawer">
      <el-form label-position="top">
        <el-form-item :label="$t('推荐')">
          <ul class="cl-theme__comd">
            <li v-for="(item, name) in themes" :key="name" @click="setComd(item)">
              <div
                class="w"
                :style="{
                  backgroundColor: item.color,
                }"
              >
                <check v-show="item.color == form.theme.color" />
              </div>

              <span>{{ item.label }}</span>
            </li>
          </ul>
        </el-form-item>

        <el-form-item :label="$t('自定义主色')">
          <el-color-picker v-model="form.color" @change="setColor" />
          <el-text size="small" class="ml-[10px]">
            {{ form.color }}
          </el-text>
        </el-form-item>

        <el-form-item :label="$t('菜单分组显示')">
          <el-switch v-model="form.theme.isGroup" @change="setGroup" />
        </el-form-item>

        <el-form-item :label="$t('转场动画')">
          <el-switch
            v-model="form.theme.transition"
            active-value="slide"
            inactive-value="none"
            @change="setTransition"
          />
        </el-form-item>
      </el-form>
    </div>
  </el-drawer>
</template>

<style lang="scss">
.cl-theme {
  &-dark {
    width: 45px;
    margin-left: 10px;
  }

  &__comd {
    display: flex;
    flex-wrap: wrap;

    li {
      display: inline-flex;
      align-items: center;
      list-style: none;
      margin-right: 15px;
      cursor: pointer;

      .w {
        height: 20px;
        width: 20px;
        border-radius: 4px;
        margin: 5px 10px 5px 0;
        text-align: center;
        color: #fff;
        line-height: 20px;
        padding: 2px;
        box-sizing: border-box;

        .el-icon {
          height: 100%;
          width: 100%;
        }
      }

      &:hover {
        opacity: 0.7;
      }
    }
  }
}

.drawer-theme {
  .el-drawer__header {
    padding: 20px 20px;
    margin-bottom: 0;
  }

  .el-drawer__title {
    font-size: 16px;
  }

  .el-color-picker {
    &__trigger {
      padding: 0;
      border: 0;
      height: 20px;
      width: 20px;
    }

    &__color {
      border: 0;

      &-inner {
        border-radius: 4px;
      }
    }
  }
}
</style>
