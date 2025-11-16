<script lang="ts" setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  assign,
  debounce,
  isEmpty,
  isFunction,
  isObject,
  isString,
  merge,
  orderBy,
  startsWith,
} from 'lodash-es'
import { marked } from 'marked'
import { computed, markRaw, nextTick, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { usePlugin } from '../hooks'
import { module, useCool } from '/@/cool'

defineOptions({
  name: 'helper-plugins-vue',
})

interface Plugin {
  name?: string
  label?: string
  version?: string
  author?: string
  logo?: string
  doc?: string
  readme?: string
  demo?: string
  isVue?: boolean
  status?: number
  config: {
    [key: string]: any
  }
  [key: string]: any
}

const { router, service, refs, setRefs, mitt } = useCool()
const helper = module.get('helper')
const { t } = useI18n()
const { install } = usePlugin()

// 选项卡
const tab = reactive({
  active: 'installed',
  p: '',

  list: [
    {
      label: t('已安装'),
      value: 'installed',
    },
    {
      label: t('全部插件'),
      value: 'shop',
    },
    {
      label: t('插件开发'),
      value: 'dev',
    },
  ],

  change(val: string) {
    tab.active = val
  },

  onChange(val: string) {
    if (val == 'dev') {
      tab.active = tab.p || 'installed'

      nextTick(() => {
        window.open('https://cool-js.com/plugin/create')
      })
    }
    else {
      tab.p = val
      plugin.refresh()
    }
  },
})

// 是否已安装插件
const isInstalled = computed(() => tab.active == 'installed')

// 插件
const plugin = reactive({
  // 已安装
  installed: [] as Plugin[],
  // 插件市场
  shop: [] as Plugin[],
  // 详情
  data: null as Plugin | null,

  // 初始化
  init() {
    plugin.refresh()

    // 移除事件
    mitt.off('plugin.refresh')

    // 监听刷新事件
    mitt.on('plugin.refresh', () => {
      tab.change('installed')
      plugin.refresh()
    })
  },

  // 卸载
  remove(item: Eps.PluginInfoEntity, index: number) {
    ElMessageBox.confirm(t(`确定要卸载插件【{name}】吗？`, { name: item.name }), t('提示'), {
      type: 'warning',
    })
      .then(() => {
        service.plugin.info
          .delete({
            ids: [item.id],
          })
          .then(() => {
            plugin.installed.splice(index, 1)
            ElMessage.success(t('卸载成功'))
          })
          .catch((err) => {
            ElMessage.error(err.message)
          })
      })
      .catch(() => null)
  },

  // 文档
  async doc(item: Plugin) {
    if (item.readme) {
      plugin.data = item
      refs.editorPreview.open(await marked.parse(item.readme!))
    }
    else if (item.doc) {
      window.open(item.doc)
    }
  },

  // 启用/禁用
  onEnable(item: Plugin) {
    if (item.isVue) {
      service.request({
        url: '/__cool_updatePlugin',
        method: 'POST',
        data: {
          name: item.name,
          data: item.config,
          enable: item.status == 1,
        },
        proxy: false,
      })
    }
    else {
      service.plugin.info
        .update({
          id: item.id,
          status: item.status ? 1 : 0,
        })
        .then(() => {
          ElMessage.success(item.status ? t('启用成功') : t('禁用成功'))
        })
        .catch((err) => {
          ElMessage.error(err.message)
        })
    }
  },

  // 分页列表
  keyWord: '',

  params: {
    page: 1,
    size: 24,
    // type: 68,
    isMy: false,
  },

  loaded: false,
  loading: false,

  async refresh(params?: any) {
    if (tab.active == 'shop') {
      assign(plugin.params, params || {})

      const data = {
        keyWord: plugin.keyWord || undefined,
        ...plugin.params,
      }

      if ((plugin.loaded || plugin.loading) && data.page != 1) {
        return
      }

      plugin.loading = true

      await service
        .request({
          url: `${helper.options.api}/app/plugin/info/page`,
          method: 'POST',
          data,
        })
        .then((res: ClCrud.Response['page']) => {
          const list = res.list.map((e) => {
            return {
              ...e,
              label: e.name,
              name: '',
              logo: e.avatarUrl,
              author: e.nickName,
              doc: `https://cool-js.com/plugin/${e.id}`,
            }
          })

          if (data.page == 1) {
            plugin.shop = list
          }
          else {
            plugin.shop.push(...list)
          }

          plugin.loaded = plugin.shop.length >= res.pagination.total
          plugin.params.page = res.pagination.page
        })
        .catch((err) => {
          ElMessage.error(err.message)
        })

      plugin.loading = false
    }
    else {
      service.plugin.info.page({ size: 1000 }).then((res) => {
        plugin.installed = res.list
          .concat(
            orderBy(
              module.list.filter(e => e.type == 'plugins' && e.label),
              'order',
              'desc',
            ),
          )
          .map((e) => {
            if (e.author == 'COOL') {
              e.logo = 'https://cool-js.com/logo.png'
            }

            const isVue = e.type == 'plugins'

            if (isVue) {
              return {
                ...e,
                label: e.label || e.name,
                status: e.enable === false ? 0 : 1,
                config: e.options || {},
                children: e.views,
                isVue,
              }
            }
            else {
              return {
                ...e,
                logo: `data:image/jpg;base64,${e.logo}`,
                label: e.name!,
                name: e.keyName,
                config: e.config || {},
              }
            }
          })
          .filter((e) => {
            return plugin.keyWord
              ? e.label.toLocaleLowerCase().includes(plugin.keyWord.toLocaleLowerCase())
              : true
          })
      })
    }
  },

  onScroll: debounce(({ scrollTop }: { scrollTop: number }) => {
    const { clientHeight, scrollHeight } = refs.scrollbar.wrapRef

    if (scrollTop + clientHeight >= scrollHeight) {
      if (tab.active == 'shop') {
        plugin.refresh({
          page: plugin.params.page + 1,
        })
      }
    }
  }, 100),
})

// 插件配置
const config = reactive({
  insert(content: string) {
    const input = refs.configInput.ref

    // 获取当前光标
    const cursor = input.selectionStart

    // 插入文案
    input.value = input.value.slice(0, cursor) + (content || '') + input.value.slice(cursor)
  },

  formatter() {
    try {
      refs.config?.setForm('config', JSON.stringify(JSON.parse(refs.config.form.config), null, 4))

      return true
    }
    catch (err) {
      ElMessage.error(`${t('参数格式错误')}：${err}`)
      return false
    }
  },

  onFileConfirm(list: Eps.SpaceInfoEntity[]) {
    const urls = list.map(e => e.url)
    const content = urls.length > 1 ? JSON.stringify(urls) : urls[0]

    config.insert(content!)
  },

  open(item: Plugin) {
    refs.config?.open({
      title: t('设置'),

      form: {
        id: item.id,
        config: item.config,
      },

      items: [
        {
          label: t('参数'),
          prop: 'config',
          component: {
            name: 'el-input',
            props: {
              type: 'textarea',
              rows: 15,
            },
            ref: setRefs('configInput'),
          },
        },
        {
          component: {
            name: 'slot-tools',
          },
        },
      ],

      on: {
        open(data) {
          if (!data.config) {
            data.config = {}
          }

          if (isString(data.config)) {
            try {
              data.config = JSON.parse(data.config)
            }
            catch (err) {
              data.config = {}
            }
          }

          if (isObject(data.config)) {
            data.config = JSON.stringify(data.config)
          }

          config.formatter()
        },
        submit(data, { done, close }) {
          if (!config.formatter()) {
            done()
            return
          }

          service.plugin.info
            .update(data)
            .then(() => {
              ElMessage.success(t('修改成功'))
              merge(item, data)
              close()
            })
            .catch((err) => {
              ElMessage.error(err.message)
              done()
            })
        },
      },
    } as ClForm.Options)
  },

  close() {
    refs.config?.close()
  },
})

// 插件示例
const demo = reactive({
  visible: false,
  title: '',
  active: 0,
  tabs: [] as any[],

  async open(item: Plugin) {
    if (isString(item.demo)) {
      if (startsWith(item.demo, 'http')) {
        window.open(item.demo)
      }
      else {
        router.push(item.demo)
      }
    }
    else {
      demo.visible = true
      demo.active = 0
      demo.tabs = []
      demo.title = item.label!

      demo.tabs = await Promise.all(
        (item.demo || []).map(async (e: any) => {
          if (e) {
            const c = await (isFunction(e.component) ? e.component() : e.component)

            return {
              ...e,
              component: markRaw(c.default),
            }
          }
        }),
      )
    }
  },

  close() {
    demo.visible = false
  },
})

// 图片预览
const pic = reactive({
  visible: false,
  show: false,
  index: 0,
  list: [] as string[],

  preview(urls: string[]) {
    pic.visible = true
    pic.list = urls
  },

  open(num: number) {
    pic.show = true
    pic.index = num
  },

  close() {
    pic.visible = false
  },

  onClose() {
    pic.show = false
  },

  prev() {
    document.querySelector('.plugins__pics .list')?.scrollBy({
      left: -500,
      behavior: 'smooth',
    })
  },

  next() {
    document.querySelector('.plugins__pics .list')?.scrollBy({
      left: 500,
      behavior: 'smooth',
    })
  },
})

// 上传
function onBeforeUpload(file: File) {
  install(file)
  return false
}

// 鼠标移动
function onMousemove(e: MouseEvent) {
  if (!isInstalled.value) {
    return false
  }

  const list = document.querySelectorAll('.plugins .list .item')

  list.forEach((item) => {
    const rect = item.getBoundingClientRect()
    const x = e.clientX - rect.left - 250
    const y = e.clientY - rect.top - 250

    if (e.type !== 'scroll') {
      requestAnimationFrame(() => {
        (item as HTMLElement).style.setProperty('--x', `${x}px`);
        (item as HTMLElement).style.setProperty('--y', `${y}px`)
      })
    }
  })
}

onMounted(() => {
  plugin.init()
})
</script>

<template>
  <div class="plugins">
    <div class="plugins__header">
      <cl-select-button v-model="tab.active" :options="tab.list" @change="tab.onChange" />

      <el-input
        v-model="plugin.keyWord"
        class="search"
        :placeholder="$t('搜索插件名称')"
        clearable
        @change="
          plugin.refresh({
            page: 1,
          })
        "
      >
        <template #prefix>
          <cl-svg name="search" :size="16" />
        </template>
      </el-input>
    </div>

    <div v-loading="plugin.loading" class="plugins__container">
      <el-scrollbar :ref="setRefs('scrollbar')" @scroll="plugin.onScroll">
        <div
          class="list"
          :class="{
            'is-installed': isInstalled,
          }"
          @mousemove="onMousemove"
        >
          <el-row :gutter="10">
            <el-col v-if="isInstalled" :xs="24" :sm="12" :md="8" :lg="6">
              <div class="item">
                <cl-upload :before-upload="onBeforeUpload" accept=".cool">
                  <div class="item-inner is-add">
                    <cl-svg name="plus" />
                  </div>
                </cl-upload>
              </div>
            </el-col>

            <el-col
              v-for="(item, index) in plugin[tab.active]"
              :key="index"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
            >
              <div class="item">
                <div class="item-inner">
                  <div v-if="isInstalled && !item.isVue" class="op">
                    <div class="cl-comm__icon is-delete" @click="plugin.remove(item, index)">
                      <cl-svg name="delete" />
                    </div>

                    <div class="cl-comm__icon is-set" @click="config.open(item)">
                      <cl-svg name="set" />
                    </div>
                  </div>

                  <div class="c">
                    <cl-image class="mr-[15px]" :radius="6" :src="item.logo" :size="40" />

                    <div class="det">
                      <div class="tag">
                        <el-tag v-if="item.name" disable-transitions size="small" effect="dark">
                          {{ item.name }}
                        </el-tag>
                        <el-tag disable-transitions size="small" effect="plain">
                          v{{ item.version || '1.0.0' }}
                        </el-tag>
                        <el-tag
                          v-if="isInstalled"
                          disable-transitions
                          size="small"
                          effect="plain"
                          :type="item.isVue ? 'success' : 'info'"
                        >
                          {{ item.isVue ? 'Vue' : $t('后端') }}
                        </el-tag>
                      </div>

                      <p class="title line-clamp-1">
                        {{ item.label || $t('未知') }}
                      </p>

                      <p class="desc line-clamp-2">
                        {{ item.description || $t('暂无描述') }}
                      </p>

                      <div class="author">
                        <span>{{ item.author || 'Ta' }}：</span>
                        <span>{{ item.updateTime || '2025-01-01' }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="f">
                    <cl-switch
                      v-if="isInstalled && (item.isVue ? item.enable !== undefined : true)"
                      v-model="item.status"
                      @change="plugin.onEnable(item)"
                    />

                    <span class="mr-auto" />

                    <el-button v-if="item.demo" type="success" @click="demo.open(item)">
                      {{ $t('示例') }}
                    </el-button>

                    <el-button
                      v-if="!isEmpty(item.pics)"
                      type="success"
                      @click="pic.preview(item.pics)"
                    >
                      {{ $t('预览') }}
                    </el-button>

                    <el-button v-if="item.doc || item.readme" @click="plugin.doc(item)">
                      {{ $t('文档') }}
                    </el-button>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <el-empty v-if="isEmpty(plugin[tab.active])" :image-size="80" />
      </el-scrollbar>
    </div>

    <!-- 示例代码 -->
    <cl-dialog v-model="demo.visible" :title="demo.title" width="60%">
      <el-tabs v-model="demo.active" type="card">
        <el-tab-pane
          v-for="(item, index) in demo.tabs"
          :key="index"
          :label="item.name"
          :name="index"
          :lazy="index != 0"
        >
          <component :is="item.component" />
        </el-tab-pane>
      </el-tabs>
    </cl-dialog>

    <!-- 图片预览 -->
    <cl-dialog
      v-model="pic.visible"
      :title="$t('图片预览')"
      width="80%"
      padding="0"
      hide-header
      transparent
    >
      <div class="plugins__pics">
        <div class="close" @click="pic.close">
          <cl-svg name="close" />
        </div>

        <div class="list">
          <div v-for="(item, index) in pic.list" :key="index" class="item" @click="pic.open(index)">
            <img :src="item">
          </div>
        </div>

        <div class="op">
          <div class="prev" @click="pic.prev">
            <cl-svg name="left" />
          </div>

          <div class="next" @click="pic.next">
            <cl-svg name="right" />
          </div>
        </div>
      </div>

      <el-image-viewer
        v-if="pic.visible && pic.show"
        :url-list="pic.list"
        infinite
        :initial-index="pic.index"
        teleported
        @close="pic.onClose"
      />
    </cl-dialog>

    <!-- 详情预览 -->
    <cl-editor-preview
      :ref="setRefs('editorPreview')"
      name="wang"
      :show-btn="false"
      :title="`${plugin.data?.name}（v${plugin.data?.version}）${$t('说明文档')}`"
    >
      <template #prepend>
        <div class="flex align-center mb-[10px] select-none">
          <span class="mr-[20px]">{{ $t('作者') }}：{{ plugin.data?.author }}</span>
          <span>{{ $t('更新时间') }}：{{ plugin.data?.updateTime }}</span>
        </div>
      </template>
    </cl-editor-preview>

    <!-- 插件配置 -->
    <cl-form :ref="setRefs('config')">
      <template #slot-tools>
        <div class="flex align-center">
          <el-button type="success" @click="config.formatter">
            {{ $t('格式化') }}
          </el-button>
          <cl-upload-space
            :show-list="false"
            :text="$t('插入文件链接')"
            @confirm="config.onFileConfirm"
          />
        </div>
      </template>
    </cl-form>
  </div>
</template>

<style lang="scss" scoped>
.plugins {
  height: 100%;
  background-color: var(--el-bg-color);

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px;

    .search {
      width: 240px;

      :deep(.el-input__wrapper) {
        background-color: var(--el-fill-color-light);
        box-shadow: none;
        border-radius: 8px;
        height: 39px;
      }
    }
  }

  &__container {
    height: calc(100% - 59px);

    .list {
      padding: 0 10px;

      .item {
        border-radius: 10px;
        margin-bottom: 10px;
        height: 200px;
        width: 100%;
        box-sizing: border-box;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        border: 1px solid var(--el-border-color-light);

        &::before {
          content: '';
          position: absolute;
          inset: 0;
          height: 500px;
          width: 500px;
          background: radial-gradient(closest-side circle, var(--el-text-color-regular), transparent);
          transform: translate(var(--x, 1000px), var(--y, 1000px));
        }

        &-inner {
          position: absolute;
          inset: 1px;
          border-radius: 8px;
          background-color: var(--el-bg-color);

          &.is-add {
            display: flex;
            align-items: center;
            justify-content: center;

            .cl-svg {
              font-size: 40px;
              color: var(--el-text-color-placeholder);
            }
          }

          .op {
            display: flex;
            position: absolute;
            top: 12px;
            right: 12px;
            z-index: 2;

            .cl-comm__icon {
              margin-left: 5px;

              &.is-delete {
                &:hover {
                  .cl-svg {
                    color: var(--el-color-danger);
                  }
                }
              }
            }
          }

          .c {
            display: flex;
            box-sizing: border-box;
            padding: 15px;
            position: relative;
            height: calc(100% - 43px);

            .set {
              position: absolute;
              right: 10px;
              top: 10px;
              font-size: 18px;
              color: var(--el-color-info);
            }

            .det {
              display: flex;
              flex-direction: column;
              flex: 1;

              .tag {
                display: flex;
                flex-wrap: wrap;
                margin-bottom: 10px;

                .el-tag {
                  margin-right: 5px;
                }

                .el-skeleton__item {
                  height: 20px;
                  width: 50px;
                  margin-right: 5px;
                  border-radius: 4px;
                }
              }

              .title {
                font-size: 14px;
                font-weight: bold;
                margin-bottom: 5px;
              }

              .desc {
                font-size: 12px;
                color: var(--el-text-color-regular);
                margin-bottom: 14px;
                flex: 1;
              }

              .author {
                font-size: 12px;
                color: var(--el-text-color-secondary);
              }
            }
          }

          .f {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 15px;
            height: 28px;

            .el-button {
              font-size: 12px;
              height: 28px;
              margin-left: 8px;
              border-radius: 6px;
            }
          }
        }
      }

      &:not(.is-installed) {
        .item {
          transition: border-color 0.3s;

          &:hover {
            border-color: var(--el-color-primary);
          }
        }
      }
    }
  }

  &__pics {
    padding: 10px;
    position: relative;

    .close {
      position: absolute;
      right: 20px;
      top: 20px;
      border: 1px solid var(--el-border-color-light);

      .cl-svg {
        font-size: 20px;
      }
    }

    .list {
      display: flex;
      overflow: auto hidden;
      user-select: none;
      border-radius: 10px;

      &::-webkit-scrollbar {
        display: none;
      }

      .item {
        display: flex;
        align-items: center;
        position: relative;
        transition: transform 0.3s;
        cursor: pointer;
        margin: 0 15px 0 0;
        flex-shrink: 0;

        img {
          height: 60vh;
          border-radius: 12px;
        }

        &:hover {
          img {
            border-color: var(--el-border-color-dark);
          }
        }
      }
    }

    .op {
      display: flex;
      justify-content: flex-end;
      padding: 20px 0 0 0;

      .prev,
      .next {
        margin: 0 10px;
      }
    }

    .close,
    .op .prev,
    .op .next {
      height: 36px;
      width: 36px;
      border-radius: 36px;
      background-color: var(--el-bg-color);
      cursor: pointer;
      z-index: 2;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      transition: all 0.2s;

      .cl-svg {
        color: var(--el-text-color-regular);
      }

      &:hover {
        transform: scale(1.1);

        .cl-svg {
          color: var(--el-text-color-primary);
        }
      }
    }
  }
}
</style>
