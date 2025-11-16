<script lang="ts" setup>
import { useCrud, useTable } from '@cool-vue/crud'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onActivated } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCool } from '/@/cool'

defineOptions({
  name: 'recycle-data',
})

const { service } = useCool()

const { t } = useI18n()

// cl-table
const Table = useTable({
  contextMenu: [],
  columns: [
    {
      type: 'selection',
    },
    { label: t('操作人'), prop: 'userName', minWidth: 120 },
    {
      label: t('被删除的数据'),
      prop: 'data',
      minWidth: 200,
      component: {
        name: 'cl-code-json',
        props: {
          popover: true,
        },
      },
    },
    {
      label: t('请求的接口'),
      prop: 'url',
      showOverflowTooltip: true,
      minWidth: 150,
    },
    {
      label: t('请求参数'),
      prop: 'params',
      minWidth: 150,
      component: {
        name: 'cl-code-json',
        props: {
          popover: true,
        },
      },
    },
    { label: t('删除条数'), prop: 'count', minWidth: 120, sortable: 'custom' },
    {
      label: t('创建时间'),
      prop: 'createTime',
      minWidth: 170,
      sortable: 'desc',
    },
    {
      type: 'op',
      width: 120,
      buttons: [
        {
          label: t('恢复'),
          hidden: !service.recycle.data._permission.restore,
          type: 'success',
          onClick({ scope }) {
            restore(scope.row.id)
          },
        },
      ],
    },
  ],
})

// cl-crud
const Crud = useCrud({
  service: service.recycle.data,
})

// 刷新
function refresh(params?: any) {
  Crud.value?.refresh(params)
}

// 数据恢复
function restore(id?: string) {
  const ids = id ? [id] : Table.value?.selection.map(e => e.id)

  ElMessageBox.confirm(t('此操作将恢复被删除的数据，是否继续？'), t('提示'), {
    type: 'warning',
  })
    .then(() => {
      service.recycle.data
        .restore({
          ids,
        })
        .then(() => {
          ElMessage.success(t('数据恢复成功'))
          refresh()
        })
        .catch((err) => {
          ElMessage.error(err.message)
        })
    })
    .catch(() => null)
}

onActivated(() => {
  refresh()
})
</script>

<template>
  <cl-crud ref="Crud">
    <cl-row>
      <!-- 刷新按钮 -->
      <cl-refresh-btn />
      <el-button
        v-permission="service.recycle.data.permission.restore"
        type="success"
        :disabled="Table?.selection.length == 0"
        @click="restore()"
      >
        {{ $t('批量恢复') }}
      </el-button>

      <cl-flex1 />
      <!-- 关键字搜索 -->
      <cl-search-key />
    </cl-row>

    <cl-row>
      <!-- 数据表格 -->
      <cl-table ref="Table" />
    </cl-row>

    <cl-row>
      <cl-flex1 />
      <!-- 分页控件 -->
      <cl-pagination />
    </cl-row>
  </cl-crud>
</template>
