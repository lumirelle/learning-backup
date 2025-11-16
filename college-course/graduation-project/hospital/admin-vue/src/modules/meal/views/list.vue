<script lang="ts" setup>
import { useCrud, useSearch, useTable, useUpsert } from '@cool-vue/crud'
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import CategorySelect from '../components/category-select.vue'
import StaffSelect from '/$/accompany/components/staff-select.vue'
import { useDict } from '/$/dict'
import DepartmentSelect from '/$/hospital/components/department-select.vue'
import DoctorSelect from '/$/hospital/components/doctor-select.vue'
import HospitalSelect from '/$/hospital/components/hospital-select.vue'
import { useCool } from '/@/cool'

defineOptions({
  name: 'meal-list',
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

// cl-upsert
const Upsert = useUpsert({
  items: [
    () => {
      return {
        label: t('选择分类'),
        prop: 'categoryId',
        hidden: Upsert?.value.mode === 'update',
        component: { vm: CategorySelect },
        span: 12,
        required: true,
      }
    },
    {
      label: t('名称'),
      prop: 'name',
      component: { name: 'el-input', props: { clearable: true } },
      span: 12,
      required: true,
    },
    {
      label: t('价格（元）'),
      prop: 'price',
      hook: 'number',
      component: { name: 'el-input-number', props: { min: 0, placeholder: '初级<=100，中级<=1000' } },
      span: 12,
      required: true,
    },
    {
      label: t('状态'),
      prop: 'status',
      value: dict.getByLabel('base-status', '启用'),
      component: { name: 'el-radio-group', options: dict.get('base-status') },
      span: 12,
      required: true,
    },
    () => {
      return {
        label: t('选择医院'),
        prop: 'hospitalId',
        hidden: Upsert?.value.mode === 'update',
        component: { vm: HospitalSelect },
        span: 24,
        required: true,
      }
    },
    () => {
      return {
        label: t('选择科室'),
        prop: 'departmentId',
        hidden: true,
        component: { vm: DepartmentSelect },
        span: 24,
        required: true,
      }
    },
    () => {
      return {
        label: t('选择医生'),
        prop: 'doctorId',
        hidden: true,
        component: { vm: DoctorSelect },
        span: 24,
        required: true,
      }
    },
    () => {
      return {
        label: t('选择陪诊员'),
        prop: 'staffId',
        hidden: Upsert?.value.mode === 'update',
        component: { vm: StaffSelect },
        span: 24,
        required: true,
      }
    },
    {
      label: t('服务范围'),
      prop: 'serviceArea',
      hook: {
        bind: (value) => {
          try {
            return value ? JSON.parse(value)?.sort() : []
          }
          catch {
            return []
          }
        },
      },
      value: [],
      component: {
        name: 'el-checkbox-group',
        options: dict.get('meal-service-area'),
      },
      span: 12,
      required: true,
    },
    {
      label: t('简介'),
      prop: 'intro',
      component: {
        name: 'el-input',
        props: { type: 'textarea', rows: 4 },
      },
    },
    { label: t('封面图'), prop: 'cover', component: { name: 'cl-upload' }, required: true },
  ],
  onSubmit: (form, { next }) => {
    next({
      ...form,
      serviceArea: JSON.stringify(form.serviceArea || []).replace(/,/g, ', '),
    })
  },
})

// 监听医院数据改变
watch(
  () => Upsert.value?.form.hospitalId,
  (val) => {
    // 清空科室和医生选择
    Upsert.value?.setForm('departmentId', undefined)
    Upsert.value?.setForm('doctorId', undefined)
    // 显示/隐藏科室选择
    if (val && Upsert.value?.mode !== 'update') {
      Upsert.value?.showItem('departmentId')
    }
    else {
      Upsert.value?.hideItem('departmentId')
      Upsert.value?.hideItem('doctorId')
    }
  },
)

// 监听科室数据改变
watch(
  () => Upsert.value?.form.departmentId,
  (val) => {
    Upsert.value?.setForm('doctorId', undefined)
    // 显示/隐藏医生选择
    if (val && Upsert.value?.mode !== 'update') {
      Upsert.value?.showItem('doctorId')
    }
    else {
      Upsert.value?.hideItem('doctorId')
    }
  },
)

// cl-table
const Table = useTable({
  columns: [
    { type: 'selection' },
    { label: t('套餐 ID'), prop: 'id', minWidth: 140 },
    { label: t('名称'), prop: 'name', minWidth: 140 },
    { label: t('分类名称'), prop: 'categoryName', minWidth: 120 },
    { label: t('价格（元）'), prop: 'price', minWidth: 140, sortable: 'custom' },
    { label: t('陪诊员 ID'), prop: 'staffId', minWidth: 140 },
    { label: t('陪诊员'), prop: 'staffName', minWidth: 140 },
    { label: t('医院 ID'), prop: 'hospitalId', minWidth: 140 },
    { label: t('医院'), prop: 'hospitalName', minWidth: 140 },
    { label: t('科室 ID'), prop: 'departmentId', minWidth: 140 },
    { label: t('科室'), prop: 'departmentName', minWidth: 140 },
    { label: t('医生 ID'), prop: 'doctorId', minWidth: 140 },
    { label: t('医生'), prop: 'doctorName', minWidth: 140 },
    {
      label: t('服务范围'),
      prop: 'serviceArea',
      minWidth: 120,
    },
    {
      label: t('简介'),
      prop: 'intro',
      showOverflowTooltip: true,
      minWidth: 200,
    },
    {
      label: t('状态'),
      prop: 'status',
      minWidth: 100,
      component: { name: 'cl-switch' },
    },
    {
      label: t('封面图'),
      prop: 'cover',
      minWidth: 100,
      component: { name: 'cl-image', props: { size: 60 } },
    },
    {
      label: t('服务次数'),
      prop: 'serviceCount',
      minWidth: 140,
      sortable: 'custom',
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
    { type: 'op', buttons: ['edit', 'delete'] },
  ],
})

// cl-search
const Search = useSearch({
  resetBtn: true,
  items: [
    {
      label: t('套餐 ID'),
      prop: 'id',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('名称'),
      prop: 'name',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('医院 ID'),
      prop: 'hospitalId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('科室 ID'),
      prop: 'departmentId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('医生 ID'),
      prop: 'doctorId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('陪诊员 ID'),
      prop: 'staffId',
      component: { name: 'el-input', props: { clearable: true } },
    },
    {
      label: t('状态'),
      prop: 'status',
      component: { name: 'el-select', props: { clearable: true }, options: dict.get('base-status') },
    },
    {
      label: t('服务范围'),
      prop: 'serviceArea',
      component: { name: 'el-checkbox-group', options: dict.get('meal-service-area') },
    },
  ],
  onSearch: (form, { next }) => {
    next({
      ...form,
      serviceArea: JSON.stringify(form.serviceArea?.sort() || []).replace(/,/g, ', '),
    })
  },
})

// cl-crud
const Crud = useCrud(
  {
    service: service.meal.info,
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
      <cl-flex1 />
      <!-- 条件搜索 -->
      <cl-search ref="Search" />
    </cl-row>

    <cl-row>
      <!-- 数据表格 -->
      <cl-table ref="Table">
        <template #column-serviceArea="{ scope }">
          <el-tag
            v-for="area in JSON.parse(scope.row.serviceArea || '[]')"
            :key="area"
            :type="dict.get('meal-service-area').value.find(item => item.value === area)?.type || 'primary'"
            class="m-2"
          >
            {{ dict.get('meal-service-area').value.find(item => item.value === area)?.label || area }}
          </el-tag>
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

<style lang="scss" scoped>
.cl-crud {
  height: 100%;
}
</style>
