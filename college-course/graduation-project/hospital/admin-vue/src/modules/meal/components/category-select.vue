<script setup lang="ts">
import { computed, ref, useModel } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'meal-category-select',
})

const props = defineProps({
  modelValue: null,
  multiple: Boolean,
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

const value = useModel(props, 'modelValue')

const customService = computed(() => {
  return {
    page: (params: any) => {
      const queryParams = { ...params }
      queryParams.status = dict.getByLabel('base-status', '启用')
      return service.meal.category.page(queryParams)
    },
  }
})

const columns = ref([
  {
    prop: 'id',
    label: t('分类 ID'),
    minWidth: 100,
  },
  {
    prop: 'name',
    label: t('分类名称'),
    minWidth: 100,
  },
  {
    prop: 'sort',
    label: t('排序'),
    minWidth: 120,
  },
])

// 定义搜索项
const searchItems = ref([
  {
    label: t('分类 ID'),
    prop: 'id',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入分类 ID'),
      },
    },
  },
  {
    label: t('分类名称'),
    prop: 'name',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入分类名称'),
      },
    },
  },
])
</script>

<template>
  <cl-select-table
    v-model="value"
    :title="t('选择分类')"
    :service="customService"
    :columns="columns"
    :multiple="multiple"
    :search-items="searchItems"
    :dict="{ text: 'name', img: 'icon' }"
  />
</template>
