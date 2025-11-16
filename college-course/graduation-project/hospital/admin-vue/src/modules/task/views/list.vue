<script lang="ts" setup>
import { ContextMenu, useForm } from '@cool-vue/crud'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onActivated, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import TaskLogs from '../components/logs.vue'
import { useBrowser, useCool } from '/@/cool'

defineOptions({
  name: 'task-list',
})

const { service, refs, setRefs } = useCool()
const { browser } = useBrowser()
const Form = useForm()
const { t } = useI18n()

const list = ref<Eps.TaskInfoEntity[]>([])

// 刷新
function refresh() {
  service.task.info.page({ size: 100, page: 1 }).then((res) => {
    list.value = res.list.map((e) => {
      if (e.every) {
        e._every = Number.parseInt(String(e.every / 1000))
      }

      return e
    })
  })
}

// 启用任务
function start(item: Eps.TaskInfoEntity) {
  ElMessageBox.confirm(t('此操作将启用任务（{name}），是否继续？', { name: item.name }), '提示', {
    type: 'warning',
  })
    .then(() => {
      service.task.info
        .start({ id: item.id, type: item.type })
        .then(() => {
          refresh()
        })
        .catch((err) => {
          ElMessage.error(err.message)
        })
    })
    .catch(() => null)
}

// 停用任务
function stop(item: Eps.TaskInfoEntity) {
  ElMessageBox.confirm(t('此操作将停用任务（{name}），是否继续？', { name: item.name }), '提示', {
    type: 'warning',
  })
    .then(() => {
      service.task.info
        .stop({ id: item.id })
        .then(() => {
          refresh()
        })
        .catch((err) => {
          ElMessage.error(err.message)
        })
    })
    .catch(() => null)
}

// 删除任务
function remove(item: Eps.TaskInfoEntity) {
  ElMessageBox.confirm(t('此操作将删除任务（{name}），是否继续？', { name: item.name }), '提示', {
    type: 'warning',
  })
    .then(() => {
      service.task.info
        .delete({ ids: [item.id] })
        .then(() => {
          refresh()
        })
        .catch((err) => {
          ElMessage.error(err.message)
        })
    })
    .catch(() => null)
}

// 任务日志
function log(item: Eps.TaskInfoEntity) {
  refs.log.open(item)
}

// 新增、编辑
async function edit(item?: Eps.TaskInfoEntity) {
  if (item && !service.task.info._permission.update) {
    return false
  }

  Form.value?.open({
    title: t('编辑计划任务'),
    width: '600px',
    props: {
      labelWidth: '80px',
    },
    items: [
      {
        label: t('名称'),
        prop: 'name',
        component: {
          name: 'el-input',
          props: {
            placeholder: '请输入名称',
          },
        },
        required: true,
      },
      {
        label: t('类型'),
        prop: 'taskType',
        value: 0,
        component: {
          name: 'el-radio-group',
          options: [
            {
              label: 'cron',
              value: 0,
            },
            {
              label: t('时间间隔'),
              value: 1,
            },
          ],
        },
        required: true,
      },
      {
        label: 'cron',
        prop: 'cron',
        hidden: ({ scope }) => scope.taskType == 1,
        component: {
          name: 'el-input',
          props: {
            placeholder: '* * * * * *',
          },
        },
        required: true,
      },
      {
        label: t('间隔(秒)'),
        prop: 'every',
        hidden: ({ scope }) => scope.taskType == 0,
        hook: {
          bind(value) {
            return value / 1000
          },
          submit(value) {
            return value * 1000
          },
        },
        component: {
          name: 'el-input-number',
          props: {
            min: 1,
            max: 100000000,
          },
        },
        required: true,
      },
      {
        label: 'service',
        prop: 'service',
        component: {
          name: 'el-input',
          props: {
            placeholder: 'taskDemoService.test([1, 2])',
          },
        },
      },
      {
        label: t('开始时间'),
        prop: 'startDate',
        hidden: ({ scope }) => scope.taskType == 1,
        component: {
          name: 'el-date-picker',
          props: {
            'type': 'datetime',
            'value-format': 'YYYY-MM-DD HH:mm:ss',
          },
        },
      },
      {
        label: t('备注'),
        prop: 'remark',
        component: {
          name: 'el-input',
          props: {
            type: 'textarea',
            rows: 3,
          },
        },
      },
    ],
    form: {
      ...item,
    },
    on: {
      submit: (data, { close, done }) => {
        if (!data.limit) {
          data.limit = null
        }

        service.task.info[item?.id ? 'update' : 'add'](data)
          .then(() => {
            refresh()
            ElMessage.success(t('保存成功'))
            close()
          })
          .catch((err) => {
            ElMessage.error(err.message)
            done()
          })
      },
    },
  })
}

// 执行一次
function once(item: Eps.TaskInfoEntity) {
  service.task.info
    .once({ id: item.id })
    .then(() => {
      refresh()
    })
    .catch((err) => {
      ElMessage.error(err.message)
    })
}

// 右键菜单
function onContextMenu(e: any, item: Eps.TaskInfoEntity) {
  ContextMenu.open(e, {
    list: [
      item.status
        ? {
            label: t('暂停'),
            hidden: !service.task.info._permission.stop,
            callback(done) {
              stop(item)
              done()
            },
          }
        : {
            label: t('开始'),
            hidden: !service.task.info._permission.start,
            callback(done) {
              start(item)
              done()
            },
          },
      {
        label: t('立即执行'),
        hidden: !service.task.info._permission.once,
        callback(done) {
          once(item)
          done()
        },
      },
      {
        label: t('编辑'),
        hidden: !(service.task.info._permission.update && service.task.info._permission.info),
        callback(done) {
          edit(item)
          done()
        },
      },
      {
        label: t('删除'),
        hidden: !service.task.info._permission.delete,
        callback(done) {
          remove(item)
          done()
        },
      },
      {
        label: t('查看日志'),
        hidden: !service.task.info._permission.log,
        callback(done) {
          log(item)
          done()
        },
      },
    ],
  })
}

onActivated(() => {
  refresh()
})
</script>

<template>
  <div class="task-list" :class="{ 'is-mini': browser.isMini }">
    <div class="list">
      <div
        v-for="(item, index) in list"
        :key="index"
        class="item"
        @click="edit(item)"
        @contextmenu="
          e => {
            onContextMenu(e, item);
          }
        "
      >
        <p class="name">
          {{ item.name }}
        </p>
        <p class="row">
          <span>{{ $t('执行服务') }}</span>
          <span>{{ item.service }}</span>
        </p>
        <p class="row">
          <span>{{ $t('定时规则') }}</span>
          <span>{{
            item.taskType == 1 ? $t('间隔{every}秒执行', { every: item._every }) : item.cron
          }}</span>
        </p>

        <div class="status">
          <template v-if="item.status">
            <div
              v-permission="service.task.info.permission.stop"
              class="icon"
              @click.stop="stop(item)"
            >
              <cl-svg name="close-border" />
            </div>

            <el-tag disable-transitions effect="plain" type="success">
              {{ $t('进行中') }}
            </el-tag>
          </template>

          <template v-else>
            <div
              v-permission="service.task.info.permission.start"
              class="icon"
              @click.stop="start(item)"
            >
              <cl-svg name="play" />
            </div>

            <el-tag disable-transitions effect="plain" type="danger">
              {{ $t('已停止') }}
            </el-tag>
          </template>

          <div class="flex1" />

          <div v-permission="service.task.info.permission.log" class="icon" @click.stop="log(item)">
            <cl-svg name="order" />
          </div>

          <div
            v-permission="service.task.info.permission.delete"
            class="icon"
            @click.stop="remove(item)"
          >
            <cl-svg name="delete" />
          </div>
        </div>
      </div>

      <div v-permission="service.task.info.permission.add" class="item is-add" @click="edit()">
        <cl-svg name="plus" :size="36" />
        <p>{{ $t('添加计划任务') }}</p>
      </div>
    </div>

    <!-- 表单 -->
    <cl-form ref="Form" />

    <!-- 日志 -->
    <task-logs :ref="setRefs('log')" />
  </div>
</template>

<style lang="scss" scoped>
.task-list {
  height: 100%;

  .list {
    display: flex;
    flex-wrap: wrap;

    .item {
      background-color: var(--el-bg-color);
      padding: 15px 20px 0 20px;
      border-radius: 10px;
      margin: 0 10px 10px 0;
      height: 200px;
      width: 350px;
      cursor: pointer;
      box-sizing: border-box;

      .name {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 10px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }

      .row {
        margin-bottom: 10px;
        height: 40px;

        span {
          display: block;
          font-size: 12px;

          &:nth-child(1) {
            margin-bottom: 5px;
            color: var(--el-color-info);
          }
        }
      }

      .status {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-top: 15px;

        .flex1 {
          flex: 1;
        }

        .icon {
          font-size: 16px;
          cursor: pointer;
          border-radius: 6px;
          height: 28px;
          width: 28px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 10px;
          background-color: var(--el-border-color-lighter);
          color: var(--el-text-color-primary);

          &:hover {
            background-color: var(--el-border-color-light);
          }

          &:last-child {
            margin-right: 0;
          }
        }
      }

      &:hover {
        box-shadow: 0px 0px 10px 1px var(--el-color-info-light-9);
      }

      &.is-add {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: var(--el-color-info);

        p {
          font-size: 13px;
          margin: 20px 0;
        }
      }
    }
  }

  &.is-mini {
    .item {
      width: 100%;
      margin: 0 0 15px 0;
    }
  }
}
</style>
