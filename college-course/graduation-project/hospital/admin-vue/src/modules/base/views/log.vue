<script lang="ts" setup>
import { useCrud, useTable } from '@cool-vue/crud'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCool } from '/@/cool'

defineOptions({
  name: 'sys-log',
})

const { service } = useCool()
const { t } = useI18n()

// 天数
const day = ref(1)

// cl-crud
const Crud = useCrud({ service: service.base.sys.log }, (app) => {
  app.refresh()
})

// cl-table
const Table = useTable({
  contextMenu: ['refresh'],
  columns: [
    {
      type: 'index',
      label: '#',
      width: 60,
    },
    {
      prop: 'userId',
      label: t('用户ID'),
      minWidth: 100,
    },
    {
      prop: 'name',
      label: t('用户昵称'),
      minWidth: 120,
    },
    {
      prop: 'action',
      label: t('请求地址'),
      minWidth: 200,
      showOverflowTooltip: true,
    },
    {
      prop: 'params',
      label: t('参数'),
      minWidth: 200,
      component: {
        name: 'cl-code-json',
        props: {
          popover: true,
        },
      },
    },
    {
      prop: 'ip',
      label: 'ip',
      minWidth: 150,
      dict: [],
      dictColor: true,
      formatter(row) {
        return row.ip.split(',')
      },
    },
    {
      prop: 'createTime',
      label: t('请求时间'),
      minWidth: 170,
      sortable: 'desc',
    },
  ],
})

// 保存天数
function saveDay() {
  service.base.sys.log
    .setKeep({ value: day.value })
    .then(() => {
      ElMessage.success(t('保存成功'))
    })
    .catch((err) => {
      ElMessage.error(err.message)
    })
}

// 清空日志
function clear() {
  ElMessageBox.confirm(t('是否要清空日志？'), t('提示'), {
    type: 'warning',
  })
    .then(() => {
      service.base.sys.log
        .clear()
        .then(() => {
          ElMessage.success(t('清空成功'))
          Crud.value?.refresh()
        })
        .catch((err) => {
          ElMessage.error(err.message)
        })
    })
    .catch(() => null)
}

onMounted(() => {
  // 获取天数
  service.base.sys.log.getKeep().then((res) => {
    day.value = Number(res)
  })
})
</script>

<template>
  <cl-crud ref="Crud">
    <cl-row>
      <cl-refresh-btn />

      <el-button v-permission="service.base.sys.log.permission.clear" type="danger" @click="clear">
        {{ $t('清空') }}
      </el-button>

      <cl-filter :label="$t('日志保存天数')">
        <el-input-number
          v-model="day"
          controls-position="right"
          :max="10000"
          :min="1"
          @change="saveDay"
        />
      </cl-filter>

      <cl-flex1 />
      <cl-search-key :placeholder="$t('搜索请求地址、用户昵称、ip')" />
    </cl-row>

    <cl-row>
      <cl-table ref="Table" />
    </cl-row>

    <cl-row>
      <cl-flex1 />
      <cl-pagination />
    </cl-row>
  </cl-crud>
</template>
