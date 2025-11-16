<script lang="ts" setup>
import { useCrud, useTable, useUpsert } from '@cool-vue/crud'
import { useI18n } from 'vue-i18n'
import { useCool } from '/@/cool'

defineOptions({
  name: 'sys-role',
})

const { t } = useI18n()
const { service } = useCool()

// cl-crud
const Crud = useCrud({ service: service.base.sys.role }, (app) => {
  app.refresh()
})

// cl-upsert
const Upsert = useUpsert({
  dialog: {
    width: '800px',
  },

  items: [
    {
      prop: 'name',
      label: t('名称'),
      span: 12,
      required: true,
      component: {
        name: 'el-input',
      },
    },
    {
      prop: 'label',
      label: t('标识'),
      span: 12,
      required: true,
      component: {
        name: 'el-input',
      },
    },
    {
      prop: 'remark',
      label: t('备注'),
      span: 24,
      component: {
        name: 'el-input',
        props: {
          type: 'textarea',
          rows: 4,
        },
      },
    },
    {
      label: t('功能权限'),
      prop: 'menuIdList',
      value: [],
      component: {
        name: 'cl-menu-check',
      },
    },
    {
      label: t('数据权限'),
      prop: 'relevance',
      component: {
        name: 'slot-relevance',
      },
    },
  ],

  onSubmit(data, { next }) {
    next({
      ...data,
      departmentIdList: data.departmentIdList || [],
    })
  },
})

// cl-table
const Table = useTable({
  columns: [
    {
      type: 'selection',
      width: 60,
    },
    {
      prop: 'name',
      label: t('名称'),
      minWidth: 150,
    },
    {
      prop: 'label',
      label: t('标识'),
      minWidth: 120,
    },
    {
      prop: 'remark',
      label: t('备注'),
      showOverflowTooltip: true,
      minWidth: 150,
    },
    {
      prop: 'createTime',
      label: t('创建时间'),
      sortable: 'desc',
      minWidth: 170,
    },
    {
      prop: 'updateTime',
      label: t('更新时间'),
      sortable: 'custom',
      minWidth: 170,
    },
    {
      type: 'op',
    },
  ],
})
</script>

<template>
  <cl-crud ref="Crud">
    <cl-row>
      <cl-refresh-btn />
      <cl-add-btn />
      <cl-multi-delete-btn />
      <cl-flex1 />
      <cl-search-key :placeholder="$t('搜索名称')" />
    </cl-row>

    <cl-row>
      <cl-table ref="Table" />
    </cl-row>

    <cl-row>
      <cl-flex1 />
      <cl-pagination />
    </cl-row>

    <cl-upsert ref="Upsert">
      <template #slot-relevance="{ scope }">
        <div>
          <el-row>
            <cl-switch v-model="scope.relevance" />

            <span
              :style="{
                marginLeft: '10px',
                fontSize: '12px',
              }"
            >
              {{ t('是否关联上下级') }}
            </span>
          </el-row>

          <cl-dept-check v-model="scope.departmentIdList" :check-strictly="scope.relevance == 0" />
        </div>
      </template>
    </cl-upsert>
  </cl-crud>
</template>
