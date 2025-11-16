<script setup lang="ts">
import { ref } from 'vue'
import Cool from '../static/cool.png'
import Echarts from '../static/echarts.png'
import ElementPlus from '../static/element-plus.png'
import Lodash from '../static/lodash.png'
import Pinia from '../static/pinia.png'
import Tailwindcss from '../static/tailwindcss.png'
import Vite from '../static/vite.png'
import Vue from '../static/vue.png'

const list = ref([
  {
    label: 'cool',
    icon: Cool,
    link: 'https://cool-js.com/',
  },
  {
    label: 'vue',
    icon: Cool,
    link: 'https://vue.cool-admin.com/',
  },
  {
    label: 'node',
    icon: Cool,
    link: 'https://node.cool-admin.com/',
  },
  {
    label: 'vue',
    icon: Vue,
    link: 'https://cn.vuejs.org/',
  },
  {
    label: 'vue-router',
    icon: Vue,
    link: 'https://router.vuejs.org/zh/',
  },
  {
    label: 'pinia',
    link: 'https://pinia.vuejs.org/',
    icon: Pinia,
  },
  {
    label: 'vite',
    link: 'https://vite.dev/',
    icon: Vite,
  },
  {
    label: 'element-plus',
    link: 'https://element-plus.org/zh-CN/component/overview.html',
    icon: ElementPlus,
  },
  {
    label: 'tailwindcss',
    link: 'https://tailwindui.com/components',
    icon: Tailwindcss,
  },
  {
    label: 'echarts',
    link: 'https://echarts.apache.org/examples/zh/index.html',
    icon: Echarts,
  },
  {
    label: 'lodash',
    link: 'https://lodash.com',
    icon: Lodash,
  },
])

function toLink(url?: string) {
  if (url) {
    window.open(url, '_blank')
  }
}
</script>

<template>
  <div class="dt-doc">
    <div class="list">
      <div
        v-for="(item, index) in list"
        :key="index"
        class="item"
        :class="{
          'is-left': item.icon == list[index - 1]?.icon,
          'is-right': item.icon == list[index + 1]?.icon,
        }"
        @click="toLink(item.link)"
      >
        <img v-if="item.icon" :src="item.icon">
        <span>{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dt-doc {
  .list {
    display: flex;
    flex-wrap: wrap;

    .item {
      display: inline-flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 10px 20px;
      background-color: var(--el-fill-color-light);
      border-radius: 8px;
      cursor: pointer;
      margin: 0 5px 5px 0;
      height: 72px;
      transition: all 0.3s;

      img {
        height: 28px;
        margin-bottom: 5px;
      }

      span {
        font-size: 12px;

        &:nth-child(1) {
          font-size: 18px;
        }
      }

      &:hover {
        background-color: var(--el-fill-color-dark);
      }

      &.is-left {
        border-radius: 0 8px 8px 0;
      }

      &.is-right {
        border-radius: 8px 0 0 8px;
        margin-right: 0;
      }

      &.is-left.is-right {
        border-radius: 0;
      }
    }
  }
}
</style>
