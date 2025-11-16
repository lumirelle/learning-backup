<script setup lang="ts">
import { computed, ref, useModel } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'
import { useCool } from '/@/cool'

defineOptions({
  name: 'user-select',
})

const props = defineProps({
  modelValue: null,
  multiple: Boolean,
  role: Number,
})

const { service } = useCool()
const { t } = useI18n()
const { dict } = useDict()

const value = useModel(props, 'modelValue')

const customService = computed(() => {
  return {
    page: (params: any) => {
      const queryParams = { ...params }
      if (props.role !== undefined) {
        queryParams.role = props.role
      }
      queryParams.status = dict.getByLabel('user-status', '启用')
      return service.user.info.page(queryParams)
    },
  }
})

const columns = ref([
  {
    prop: 'id',
    label: t('用户 ID'),
    minWidth: 100,
  },
  {
    prop: 'unionid',
    label: t('唯一登录 ID'),
    minWidth: 100,
  },
  {
    prop: 'nickName',
    label: t('昵称'),
    minWidth: 150,
  },
  {
    prop: 'avatarUrl',
    label: t('头像'),
    component: {
      name: 'cl-avatar',
    },
    minWidth: 100,
  },
  {
    prop: 'phone',
    label: t('手机号'),
    minWidth: 120,
  },
  {
    label: t('角色'),
    prop: 'role',
    minWidth: 100,
    dict: dict.get('user-role'),
  },
])

// 定义搜索项
const searchItems = ref([
  {
    label: t('用户 ID'),
    prop: 'id',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入用户 ID'),
      },
    },
  },
  {
    label: t('唯一登录 ID'),
    prop: 'unionid',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入唯一登录 ID'),
      },
    },
  },
  {
    label: t('昵称'),
    prop: 'nickName',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入昵称'),
      },
    },
  },
  {
    label: t('手机号'),
    prop: 'phone',
    component: {
      name: 'el-input',
      props: {
        clearable: true,
        placeholder: t('请输入手机号'),
      },
    },
  },
])
</script>

<template>
  <cl-select-table
    v-model="value"
    :title="t('选择用户')"
    :service="customService"
    :columns="columns"
    :multiple="multiple"
    :search-items="searchItems"
  />
</template>
