<script lang="ts" setup>
import { useCrud, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import OrderSelect from '/$/order/components/order-info-select.vue'
import UserSelect from '/$/user/components/user-select.vue'
import { useCool } from '/@/cool'

defineOptions({
  name: 'feedback-complaint',
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

// cl-upsert
const Upsert = useUpsert({
  items: [
    () => {
      return {
        label: t('选择用户'),
        prop: 'complaintUserId',
        hidden: Upsert.value?.mode === 'update',
        component: { vm: UserSelect },
        span: 24,
        required: true,
      }
    },
    () => {
      return {
        label: t('订单ID'),
        prop: 'orderId',
        hidden: true,
        component: { vm: OrderSelect, props: { payOrderOnly: true } },
        span: 24,
        required: true,
      }
    },
    () => {
      return {
        label: t('类型'),
        prop: 'type',
        hidden: Upsert.value?.mode === 'update',
        component: {
          name: 'el-select',
          options: dict.get('complaint-type'),
          props: { clearable: true },
        },
        span: 12,
        required: true,
      }
    },
    () => {
      return {
        label: t('内容'),
        prop: 'content',
        hidden: Upsert.value?.mode === 'update',
        component: { name: 'el-input', props: { type: 'textarea', rows: 4 } },
        required: true,
      }
    },
    () => {
      return {
        label: t('状态'),
        prop: 'status',
        hidden: Upsert.value?.mode === 'add',
        component: { name: 'el-select', options: dict.get('complaint-status'), props: { clearable: true } },
        span: 12,
        required: true,
      }
    },
    () => {
      return {
        label: t('处理结果'),
        prop: 'handleResult',
        hidden: Upsert.value?.mode === 'add',
        component: {
          name: 'el-input',
          props: { type: 'textarea', rows: 4 },
        },
        required: true,
      }
    },
    () => {
      return {
        label: t('备注'),
        prop: 'remark',
        hidden: Upsert.value?.mode === 'update',
        component: { name: 'el-input', props: { type: 'textarea', rows: 4 } },
      }
    },
  ],
})

watch(
  () => Upsert.value?.form.complaintUserId,
  (val) => {
    Upsert.value?.setForm('orderId', undefined)
    if (val && Upsert.value?.mode !== 'update') {
      Upsert.value?.showItem('orderId')
    }
    else {
      Upsert.value?.hideItem('orderId')
    }
  },
)

// cl-table
const Table = useTable({
  contextMenu: [
    'refresh',
    (row) => {
      return {
        label: t('处理'),
        hidden: row.status === dict.getByLabel('complaint-status', '已解决'),
        type: 'primary',
        callback: (done) => {
          Upsert.value?.edit({
            ...row,
          })
          done()
        },
      }
    },
  ],
  columns: [
    { type: 'selection' },
    { label: t('用户ID（小程序用户）'), prop: 'complaintUserId', minWidth: 180 },
    { label: t('用户昵称'), prop: 'userNickName', minWidth: 140 },
    { label: t('类型'), prop: 'type', minWidth: 120, dict: dict.get('complaint-type') },
    {
      label: t('内容'),
      prop: 'content',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    { label: t('订单ID'), prop: 'orderId', minWidth: 140 },
    // 多张图片
    {
      label: t('图片'),
      prop: 'images',
      minWidth: 350,
    },
    {
      label: t('备注'),
      prop: 'remark',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    { label: t('处理人ID（后台用户）'), prop: 'handlerId', minWidth: 180 },
    { label: t('处理人昵称'), prop: 'handlerNickName', minWidth: 120 },
    { label: t('状态'), prop: 'status', minWidth: 120, dict: dict.get('complaint-status') },
    {
      label: t('处理结果'),
      prop: 'handleResult',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    {
      label: t('创建时间'),
      prop: 'createTime',
      minWidth: 170,
      sortable: 'desc',
      component: { name: 'cl-date-text' },
    },
    {
      label: t('更新时间'),
      prop: 'updateTime',
      minWidth: 170,
      sortable: 'custom',
      component: { name: 'cl-date-text' },
    },
    {
      type: 'op',
      buttons({ scope }) {
        return [
          {
            label: t('处理'),
            hidden: scope.row.status === dict.getByLabel('complaint-status', '已解决'),
            onClick({ scope }) {
              Upsert.value?.edit({
                ...scope.row,
              })
            },
          },
        ]
      },
    },
  ],
})

// cl-search
const Search = useSearch({
  resetBtn: true,
  items: [
    {
      label: t('用户ID（小程序用户）'),
      prop: 'complaintUserId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('订单ID'),
      prop: 'orderId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('类型'),
      prop: 'type',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('complaint-type') },
    },
    {
      label: t('处理人ID（后台用户）'),
      prop: 'handlerId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('状态'),
      prop: 'status',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('complaint-status') },
    },
  ],
})

// cl-crud
const Crud = useCrud(
  {
    service: service.feedback.complaint,
  },
  (app) => {
    app.refresh()
  },
)

// 刷新
function refresh(params?: any) {
  Crud.value?.refresh(params)
}
</script>

<template>
  <cl-crud ref="Crud">
    <cl-row>
      <!-- 刷新按钮 -->
      <cl-refresh-btn />
      <!-- 导出按钮 -->
      <cl-export-btn :columns="Table?.columns" />
      <cl-flex1 />
      <!-- 条件搜索 -->
      <cl-search ref="Search" />
    </cl-row>

    <cl-row>
      <!-- 数据表格 -->
      <cl-table ref="Table">
        <template #column-images="{ scope }">
          <div class="flex flex-wrap gap-2">
            <cl-image v-for="image in scope.row.images" :key="image" :src="image" :radius="8" />
          </div>
        </template>
      </cl-table>
    </cl-row>

    <cl-row>
      <cl-flex1 />
      <!-- 分页控件 -->
      <cl-pagination />
    </cl-row>

    <!-- 新增、编辑 -->
    <cl-upsert ref="Upsert" />
  </cl-crud>
</template>
