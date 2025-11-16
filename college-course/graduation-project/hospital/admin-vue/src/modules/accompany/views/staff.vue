<script lang="ts" setup>
import { useCrud, useForm, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import UserSelect from '/$/user/components/user-select.vue'
import { useCool } from '/@/cool'
import { phoneValidator } from '/@/cool/utils'

defineOptions({
  name: 'accompany-staff',
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

// cl-upsert
const Upsert = useUpsert({
  items: [
    () => {
      return {
        label: t('选择关联用户'),
        prop: 'staffUserId',
        hidden: Upsert.value?.mode === 'update',
        component: {
          vm: UserSelect,
          props: {
            role: dict.getByLabel('user-role', '陪诊人员'),
          },
        },
        required: true,
        span: 24,
      }
    },
    {
      label: t('姓名'),
      prop: 'name',
      component: { name: 'el-input', props: { clearable: true } },
      required: true,
      span: 12,
    },
    {
      label: t('电话'),
      prop: 'phone',
      component: { name: 'el-input', props: { clearable: true, maxlength: 11 } },
      rules: { required: true, trigger: 'blur', validator: phoneValidator },
      span: 12,
    },
    {
      label: t('性别'),
      prop: 'gender',
      value: dict.getByLabel('gender', '男'),
      component: { name: 'el-radio-group', options: dict.get('gender') },
      required: true,
      span: 12,
    },
    {
      label: t('生日'),
      prop: 'birthday',
      component: {
        name: 'el-date-picker',
        props: { type: 'date', valueFormat: 'YYYY-MM-DD HH:mm:ss' },
      },
      required: true,
      span: 24,
    },
    () => {
      return {
        label: t('状态'),
        prop: 'status',
        value: dict.getByLabel('acc-staff-status', '正常'),
        hidden: Upsert.value?.mode === 'add',
        component: {
          name: 'el-radio-group',
          options: dict.get('acc-staff-status'),
        },
        required: true,
        span: 24,
      }
    },
    {
      label: t('简介'),
      prop: 'introduction',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
      required: true,
      span: 24,
    },
    {
      label: t('备注'),
      prop: 'remark',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
      span: 24,
    },
  ],
})

// 审核表单
const Review = useForm()

// 打开审核表单
function openReview(row: any) {
  Review.value?.open({
    title: '资质审核',
    width: '600px',
    items: [
      {
        label: '陪诊员姓名',
        prop: 'name',
        component: {
          name: 'el-input',
          props: {
            disabled: true,
          },
        },
      },
      {
        label: '当前级别',
        prop: 'oldLevel',
        component: {
          name: 'el-input',
          props: {
            disabled: true,
          },
        },
        hook: {
          bind: (value) => {
            return dict.get('acc-staff-level').value.find((e: any) => e.value === value)?.label || value
          },
        },
      },
      {
        label: '审核结果',
        prop: 'level',
        value: row.level,
        component: {
          name: 'el-radio-group',
          options: dict.get('acc-staff-level'),
        },
        required: true,
      },
      {
        label: '审核意见',
        prop: 'remark',
        component: {
          name: 'el-input',
          props: {
            type: 'textarea',
            rows: 4,
          },
        },
        required: true,
      },
    ],
    form: {
      staffId: row.id,
      name: row.name,
      oldLevel: row.level,
      oldLevelValue: row.level,
    },
    on: {
      submit: async (data, { done, close }) => {
        try {
          await service.accompany.staff.doreview({
            staffId: data.staffId,
            oldLevel: data.oldLevelValue,
            level: data.level,
            remark: data.remark,
          })

          ElMessage.success('审核完成')
          refresh()
          close()
        }
        catch (err: any) {
          ElMessage.error(err?.message || '审核失败')
          done()
        }
      },
    },
  })
}

// cl-table
const Table = useTable({
  columns: [
    { type: 'selection' },
    { label: t('陪诊员 ID'), prop: 'id', minWidth: 120 },
    { label: t('姓名'), prop: 'name', minWidth: 140 },
    { label: t('性别'), prop: 'gender', minWidth: 120, dict: dict.get('gender') },
    {
      label: t('年龄'),
      prop: 'birthday',
      minWidth: 140,
      sortable: 'custom',
      formatter: (row: any) => {
        return row.birthday ? dayjs().diff(dayjs(row.birthday), 'year') : ''
      },
    },
    { label: t('电话'), prop: 'phone', minWidth: 140 },
    { label: t('级别'), prop: 'level', dict: dict.get('acc-staff-level'), minWidth: 120 },
    { label: t('状态'), prop: 'status', dict: dict.get('acc-staff-status'), minWidth: 120 },
    {
      label: t('简介'),
      prop: 'introduction',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    { label: t('账户 ID'), prop: 'staffUserId', minWidth: 140 },
    { label: t('账户昵称'), prop: 'nickName', minWidth: 140 },
    {
      label: t('备注'),
      prop: 'remark',
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
      buttons: [
        'edit',
        {
          label: '审核',
          type: 'primary',
          onClick: ({ scope }) => {
            openReview(scope.row)
          },
        },
        'delete',
      ],
      width: 260,
    },
  ],
})

// cl-search
const Search = useSearch({
  resetBtn: true,
  items: [
    {
      label: t('陪诊员 ID'),
      prop: 'id',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('姓名'),
      prop: 'name',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('性别'),
      prop: 'gender',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('gender') },
    },
    {
      label: t('电话'),
      prop: 'phone',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('级别'),
      prop: 'level',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('acc-staff-level') },
    },
    {
      label: t('状态'),
      prop: 'status',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('acc-staff-status') },
    },
    // {
    //   label: t('账户 ID'),
    //   prop: 'staffUserId',
    //   component: { name: 'el-input', props: { clearable: true } },
    // },
  ],
})

// cl-crud
const Crud = useCrud(
  {
    service: service.accompany.staff,
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
      <!-- 新增按钮 -->
      <cl-add-btn />
      <!-- 删除按钮 -->
      <cl-multi-delete-btn />
      <!-- 导出按钮 -->
      <cl-export-btn :columns="Table?.columns" />
    </cl-row>

    <cl-row>
      <!-- 条件搜索 -->
      <cl-search ref="Search" />
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

    <!-- 新增、编辑 -->
    <cl-upsert ref="Upsert" />

    <!-- 审核表单 -->
    <cl-form ref="Review" />
  </cl-crud>
</template>

<style lang="scss" scoped>
.cl-crud {
  height: 100%;
}
</style>
