<script lang="ts" setup>
import { useCrud, useTable, useUpsert } from '@cool-vue/crud'
import { reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDict } from '../index'
import { Plugins } from '/#/crud'
import { useCool } from '/@/cool'
import { useViewGroup } from '/@/plugins/view'

defineOptions({
  name: 'dict-list',
})

const { service } = useCool()
const { dict } = useDict()
const { t } = useI18n()

/**
 * 字典信息样式选项
 */
const options = reactive({
  type: [
    {
      label: t('Primary'),
      value: 'primary',
      type: 'primary',
    },
    {
      label: t('Success'),
      value: 'success',
      type: 'success',
    },
    {
      label: t('Info'),
      value: 'info',
      type: 'info',
    },
    {
      label: t('Warning'),
      value: 'warning',
      type: 'warning',
    },
    {
      label: t('Danger'),
      value: 'danger',
      type: 'danger',
    },
  ],
})

const { ViewGroup } = useViewGroup({
  label: t('类型'),
  title: t('字典列表'),
  service: service.dict.type,
  onSelect(item) {
    refresh({
      typeId: item.id,
      page: 1,
      prop: 'orderNum',
      order: 'asc',
    })
  },
  onEdit() {
    return {
      width: '500px',
      props: {
        labelWidth: '60px',
      },
      items: [
        {
          label: t('名称'),
          prop: 'name',
          component: {
            name: 'el-input',
            props: {
              maxlength: 20,
            },
          },
          required: true,
        },
        {
          label: 'Key',
          prop: 'key',
          component: {
            name: 'el-input',
            props: {
              maxlength: 20,
            },
          },
          required: true,
        },
      ],
    }
  },
})

// cl-upsert
const Upsert = useUpsert({
  dialog: {
    width: '600px',
  },
  props: {
    labelWidth: '100px',
  },
  items: [
    {
      label: t('上级节点'),
      prop: 'parentId',
      component: {
        name: 'cl-select',
        props: {
          labelKey: 'name',
          valueKey: 'id',
          checkStrictly: true,
          tree: true,
          current: true,
          defaultExpandAll: true,
        },
      },
    },
    {
      label: t('名称'),
      prop: 'name',
      required: true,
      component: { name: 'el-input' },
    },
    {
      label: t('值'),
      prop: 'value',
      component: { name: 'slot-value' },
    },
    {
      label: t('样式'),
      prop: 'type',
      value: 'primary',
      component: { name: 'el-select', options: options.type },
    },
    {
      label: t('排序'),
      prop: 'orderNum',
      value: 1,
      component: { name: 'el-input-number', props: { min: 1 } },
    },
    {
      label: t('备注'),
      prop: 'remark',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
    },
  ],
  onSubmit(data, { next }) {
    next({
      ...data,
      typeId: ViewGroup.value?.selected?.id,
    })
  },
  plugins: [Plugins.Form.setFocus('name')],
})

// cl-table
const Table = useTable({
  contextMenu: [
    'refresh',
    (row) => {
      return {
        label: t('新增'),
        hidden: !service.dict.info._permission?.add,
        callback(done) {
          append(row)
          done()
        },
      }
    },
    'edit',
    'delete',
    'order-asc',
    'order-desc',
  ],
  columns: [
    {
      type: 'selection',
    },
    { label: t('名称'), prop: 'name', align: 'left', minWidth: 200 },
    { label: t('ID'), prop: 'id', minWidth: 120 },
    {
      label: t('值'),
      prop: 'value',
      minWidth: 200,
      showOverflowTooltip: true,
    },
    {
      label: t('样式'),
      prop: 'type',
      dict: options.type,
      minWidth: 170,
    },
    {
      label: t('备注'),
      prop: 'remark',
      showOverflowTooltip: true,
      minWidth: 170,
    },
    {
      label: t('排序'),
      prop: 'orderNum',
      sortable: 'asc',
      width: 100,
      fixed: 'right',
    },
    {
      label: t('创建时间'),
      prop: 'createTime',
      sortable: 'custom',
      minWidth: 170,
    },
    {
      label: t('更新时间'),
      prop: 'updateTime',
      sortable: 'custom',
      minWidth: 170,
    },
    {
      type: 'op',
      width: 250,
      buttons: [
        {
          label: t('新增'),
          type: 'success',
          hidden: !service.dict.info._permission.add,
          onClick({ scope }) {
            append(scope.row)
          },
        },
        'edit',
        'delete',
      ],
    },
  ],
  plugins: [Plugins.Table.toTree()],
})

// cl-crud
const Crud = useCrud({
  service: service.dict.info,
  onRefresh(params, { render }) {
    service.dict.info.list(params).then((res) => {
      render(res)

      // 刷新字典
      dict.refresh([ViewGroup.value?.selected?.key])
    })
  },
})

// 刷新
function refresh(params?: any) {
  Crud.value?.refresh(params)
}

// 追加子集
function append(row: any) {
  Crud.value?.rowAppend({
    parentId: row.id,
    orderNum: 1,
  })
}

// 文件选择
function onFileConfirm(selection: any[]) {
  Upsert.value?.setForm('value', selection[0]?.url)
}
</script>

<template>
  <cl-view-group ref="ViewGroup">
    <template #item-name="{ item }">
      {{ item.name }} - {{ item.key }}
    </template>

    <template #right>
      <cl-crud ref="Crud">
        <cl-row>
          <!-- 刷新按钮 -->
          <cl-refresh-btn />
          <!-- 新增按钮 -->
          <cl-add-btn />
          <!-- 批量删除 -->
          <cl-multi-delete-btn />
          <cl-flex1 />
          <!-- 关键字搜索 -->
          <cl-search-key :placeholder="$t('搜索名称')" />
        </cl-row>

        <cl-row>
          <!-- 数据表格 -->
          <cl-table ref="Table" />
        </cl-row>

        <cl-row>
          <cl-flex1 />
        </cl-row>

        <!-- 新增、编辑 -->
        <cl-upsert ref="Upsert">
          <template #slot-value="{ scope }">
            <div>
              <el-input
                v-model="scope.value"
                :placeholder="$t('请填写值')"
                clearable
                type="textarea"
                :rows="4"
                class="mb-2"
              />

              <cl-upload-space :text="$t('使用文件')" :limit="1" @confirm="onFileConfirm" />
            </div>
          </template>
        </cl-upsert>
      </cl-crud>
    </template>
  </cl-view-group>
</template>
