<script lang="ts" setup>
import { ContextMenu, useForm } from '@cool-vue/crud'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assign, isEmpty, merge } from 'lodash-es'
import { inject, nextTick, onMounted, reactive, ref, useSlots } from 'vue'
import { useI18n } from 'vue-i18n'
import { useBrowser, useCool } from '/@/cool'
import { deepTree } from '/@/cool/utils'

defineOptions({
  name: 'cl-view-group',
})

const { browser, onScreenChange } = useBrowser()
const slots = useSlots()
const Form = useForm()
const { refs, setRefs } = useCool()
const { t } = useI18n()

// 配置
const config = reactive(
  assign(
    {
      label: t('组'),
      title: t('列表'),
      leftWidth: '300px',
      data: {},
      service: {},
      enableContextMenu: true,
      enableRefresh: true,
      enableKeySearch: true,
      enableAdd: true,
      custom: false,
    },
    inject('useViewGroup__options'),
  ),
) as ClViewGroup.Options

// 左侧内容是否自定义
const isCustom = !!slots.left

if (isEmpty(config.service) && !isCustom) {
  console.error('[cl-view-group] service is required')
}

// 加载中
const loading = ref(false)

// 搜索关键字
const keyWord = ref('')

// 列表
const list = ref<ClViewGroup.Item[]>([])

// 是否展开
const isExpand = ref(true)

// 选中值
const selected = ref<ClViewGroup.Item>()

// 树配置
const tree = reactive(
  merge(
    {
      visible: !!config.tree,
      props: {
        label: 'name',
        children: 'children',
        disabled: 'disabled',
        isLeaf: 'isLeaf',
        class: '',
      },
      defaultExpandedKeys: [] as number[],
    },
    config.tree,
  ),
)

// 收起、展开
function expand(value?: boolean) {
  isExpand.value = value === undefined ? !isExpand.value : value
}

// 设置选中值
function select(data?: ClViewGroup.Item) {
  if (!data) {
    data = list.value[0]
  }

  selected.value = data

  nextTick(() => {
    if (data) {
      if (browser.isMini) {
        expand(false)
      }

      if (config.onSelect) {
        config.onSelect(data)
      }
    }
  })
}

// 编辑
function edit(item?: ClViewGroup.Item) {
  Form.value?.open({
    title: (item ? t('编辑') : t('添加')) + config.label,
    form: {
      ...item,
    },
    on: {
      submit(data, { close, done }) {
        config.service[item ? 'update' : 'add'](data)
          .then(() => {
            ElMessage.success(t('保存成功'))

            if (item) {
              assign(item, data)
            }

            refresh()
            close()
          })
          .catch((err) => {
            ElMessage.error(err.message)
            done()
          })
      },
    },
    ...(config.onEdit?.(item) as any),
  })
}

// 删除
function remove(item: ClViewGroup.Item) {
  ElMessageBox.confirm(t('此操作将会删除选择的数据，是否继续？'), t('提示'), {
    type: 'warning',
  })
    .then(() => {
      function next(params: any) {
        config.service
          .delete(params)
          .then(async () => {
            ElMessage.success(t('删除成功'))

            // 刷新列表
            await refresh()

            // 删除当前
            if (selected.value?.id == item.id) {
              select()
            }
          })
          .catch((err) => {
            ElMessage.error(err.message)
          })
      }

      // 删除事件
      if (config.onDelete) {
        config.onDelete(item, { next })
      }
      else {
        next({ ids: [item.id] })
      }
    })
    .catch(() => null)
}

// 请求参数
const reqParams = {
  order: 'createTime',
  sort: 'asc',
  page: 1,
  size: 50, // 每页条数
}

// 是否加载完
const loaded = ref(false)

// 刷新
async function refresh(params?: any) {
  if (isCustom) {
    return false
  }

  assign(reqParams, params)

  loading.value = true

  const data = {
    ...reqParams,
    ...config.data,
    keyWord: keyWord.value,
  }

  let req: Promise<void>

  if (tree.visible) {
    // 树形数据
    req = config.service.list(data).then((res) => {
      list.value = deepTree(res)
    })
  }
  else {
    // 列表数据
    req = config.service.page(data).then((res) => {
      const arr = config.onData?.(res.list) || res.list

      if (reqParams.page == 1) {
        list.value = arr
      }
      else {
        list.value.push(...arr)
      }

      loaded.value = res.pagination.total <= list.value.length
    })
  }

  await req
    .then(() => {
      const item = selected.value || list.value[0]

      if (item) {
        if (tree.visible) {
          const node = refs.tree.getNode(item)
          node.expand()
        }

        select(item)
      }
    })
    .catch((err) => {
      ElMessage.error(err.message)
    })

  loading.value = false
}

// 加载更多
function onMore() {
  refresh({
    page: reqParams.page + 1,
  })
}

// 右键菜单
function onContextMenu(e: any, item: ClViewGroup.Item) {
  if (!config.enableContextMenu) {
    return false
  }

  ContextMenu.open(e, {
    hover: {
      target: 'item',
    },
    list: [
      {
        label: t('编辑'),
        hidden: !config.service._permission?.update,
        callback(done) {
          done()
          edit(item)
        },
      },
      {
        label: t('删除'),
        hidden: !config.service._permission?.delete,
        callback(done) {
          done()
          remove(item)
        },
      },
    ],
    ...(config.onContextMenu && config.onContextMenu(item)),
  })
}

// 监听屏幕变化
onScreenChange(() => {
  expand(!browser.isMini)
})

onMounted(() => {
  refresh()
})

defineExpose({
  list,
  selected,
  isExpand,
  expand,
  select,
  browser,
  edit,
  remove,
  refresh,
})
</script>

<template>
  <div class="cl-view-group" :class="[isExpand ? 'is-expand' : 'is-collapse']">
    <div class="cl-view-group__wrap">
      <!-- 左侧 -->
      <div class="cl-view-group__left">
        <slot name="left">
          <div class="scope">
            <div class="head">
              <el-text class="label">
                {{ config.label }}
              </el-text>

              <el-tooltip v-if="config.enableRefresh" :content="$t('刷新')">
                <div class="icon" @click="refresh()">
                  <cl-svg name="refresh" />
                </div>
              </el-tooltip>

              <el-tooltip v-if="config.enableAdd" :content="$t('添加')">
                <div v-permission="config.service.permission.add" class="icon" @click="edit()">
                  <cl-svg name="plus-border" />
                </div>
              </el-tooltip>

              <slot name="left-op" />
            </div>

            <div v-if="config.enableKeySearch" class="search">
              <el-input
                v-model="keyWord"
                :placeholder="$t('搜索关键字')"
                clearable
                :prefix-icon="Search"
                @change="
                  refresh({
                    page: 1,
                  })
                "
              />
            </div>

            <div v-loading="loading" class="data">
              <el-scrollbar>
                <!-- 树类型 -->
                <template v-if="tree.visible">
                  <el-tree
                    :ref="setRefs('tree')"
                    class="tree"
                    highlight-current
                    auto-expand-parent
                    :expand-on-click-node="false"
                    :lazy="tree.lazy"
                    :data="list"
                    :props="tree.props"
                    :load="tree.onLoad"
                    @node-click="select"
                  >
                    <template #default="{ data }">
                      <div class="item">
                        <component :is="data.icon" v-if="data.icon" />

                        <slot name="item-name" :item="data" :selected="selected">
                          <el-text truncated>
                            {{ data[tree.props.label] }}
                            {{
                              isEmpty(data[tree.props.children])
                                ? ``
                                : `（${data[tree.props.children]?.length}）`
                            }}
                          </el-text>
                        </slot>
                      </div>
                    </template>
                  </el-tree>
                </template>

                <!-- 列表类型 -->
                <template v-else>
                  <ul
                    v-infinite-scroll="onMore"
                    class="list"
                    :infinite-scroll-immediate="false"
                    :infinite-scroll-disabled="loaded"
                  >
                    <li
                      v-for="(item, index) in list"
                      :key="index"
                      @click="select(item)"
                      @contextmenu="
                        e => {
                          onContextMenu(e, item);
                        }
                      "
                    >
                      <slot name="item" :item="item" :selected="selected" :index="index">
                        <div
                          class="item"
                          :class="{
                            'is-active': selected?.id == item.id,
                          }"
                        >
                          <slot name="item-name" :item="item" :selected="selected" :index="index">
                            <span class="text-ellipsis overflow-hidden mr-2">
                              {{ item.name }}
                            </span>
                          </slot>

                          <cl-svg v-show="selected?.id == item.id" name="right" class="ml-auto" />
                        </div>
                      </slot>
                    </li>

                    <el-empty v-if="list.length == 0" :image-size="80" />
                  </ul>
                </template>
              </el-scrollbar>
            </div>
          </div>
        </slot>

        <!-- 收起按钮 -->
        <div v-if="browser.isMini" class="collapse-btn" @click="expand(false)">
          <cl-svg name="right" />
        </div>
      </div>

      <!-- 右侧 -->
      <div class="cl-view-group__right">
        <div class="head">
          <div
            class="icon is-bg absolute left-[10px]"
            :class="{ 'is-fold': !isExpand }"
            @click="expand()"
          >
            <cl-svg name="back" />
          </div>

          <slot name="title" :selected="selected">
            <span class="title"> {{ config.title }}（{{ selected?.name || $t('未选择') }}） </span>
          </slot>

          <div class="absolute right-[10px]">
            <slot name="right-op" />
          </div>
        </div>

        <div v-if="selected || config.custom" class="content">
          <slot name="right" />
        </div>

        <el-empty v-else :image-size="80" />
      </div>
    </div>

    <cl-form ref="Form" />
  </div>
</template>

<style lang="scss" scoped>
.cl-view-group {
  height: 100%;
  width: 100%;

  $left-width: v-bind('config.leftWidth');
  $bg: var(--el-bg-color);

  :deep(.icon) {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    height: 26px;
    width: 26px;
    font-size: 16px;
    border-radius: 6px;
    color: var(--el-text-color-regular);

    &:hover {
      background-color: var(--el-fill-color-light);
      color: var(--el-text-color-primary);
    }

    &.is-bg {
      background-color: var(--el-fill-color-lighter);
    }

    &.is-fold {
      transform: rotate(180deg);
    }
  }

  &__wrap {
    display: flex;
    height: 100%;
    width: 100%;
    position: relative;
  }

  &__left {
    position: relative;
    height: 100%;
    width: $left-width;
    background-color: $bg;

    .collapse-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      position: absolute;
      right: 20px;
      bottom: 30px;
      height: 40px;
      width: 40px;
      border-radius: 100%;
      background-color: var(--el-color-primary);
      box-shadow: 0 0 10px 1px rgba(0, 0, 0, 0.3);

      .cl-svg {
        color: #fff;
        font-size: 18px;
      }
    }

    .scope {
      display: flex;
      flex-direction: column;
      height: 100%;
      width: 100%;
      box-sizing: border-box;
      white-space: nowrap;

      .head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 40px;
        font-size: 14px;
        padding: 0 10px;
        border-bottom: 1px solid var(--el-border-color-extra-light);

        .label {
          flex: 1;
        }

        .icon {
          margin-left: 5px;
        }
      }

      .search {
        padding: 10px 10px 0 10px;

        :deep(.el-input) {
          .el-input__wrapper {
            border-radius: 6px;
          }
        }
      }

      .data {
        flex: 1;
        overflow: hidden;
        box-sizing: border-box;
        margin-top: 10px;

        :deep(.el-tree-node__content) {
          height: 36px;
          margin: 0 5px;
        }

        .tree {
          .item {
            display: flex;
            align-items: center;
            flex: 1;
            overflow: hidden;
          }
        }

        .list {
          height: 100%;

          li {
            .item {
              display: flex;
              align-items: center;
              list-style: none;
              box-sizing: border-box;
              padding: 10px 12px;
              margin: 0 10px;
              cursor: pointer;
              font-size: 14px;
              margin-bottom: 10px;
              border-radius: 6px;
              color: var(--el-text-color-regular);
              position: relative;
              background-color: var(--el-fill-color-lighter);

              &.is-active {
                background-color: var(--el-color-primary);
                color: #fff;
              }

              &:hover:not(.is-active) {
                background-color: var(--el-fill-color-light);
              }
            }
          }

          &::after {
            display: block;
            content: '';
            height: 1px;
          }
        }
      }
    }
  }

  &__right {
    display: flex;
    flex-direction: column;
    position: absolute;
    right: 0;
    top: 0;
    height: 100%;
    width: 100%;
    transition: width 0.3s;
    background-color: $bg;

    .head {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 40px;
      position: relative;
      font-size: 14px;
      border-bottom: 1px solid var(--el-border-color-extra-light);

      .title {
        white-space: nowrap;
        overflow: hidden;
      }
    }

    .content {
      height: calc(100% - 40px);
    }
  }

  &.is-expand {
    .cl-view-group__right {
      width: calc(100% - $left-width);
      border-left: 1px solid var(--el-border-color-extra-light);
    }
  }

  @media only screen and (max-width: 768px) {
    .cl-view-group__left {
      overflow: hidden;
      transition: width 0.2s;
      width: 0;
      z-index: 9;
    }

    .cl-view-group__right {
      width: 100% !important;
    }

    &.is-expand {
      .cl-view-group__left {
        width: 100%;
      }
    }
  }
}
</style>
