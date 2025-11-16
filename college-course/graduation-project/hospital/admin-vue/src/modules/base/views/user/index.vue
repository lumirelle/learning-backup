<script lang="ts" setup>
import { useCrud, useTable, useUpsert } from '@cool-vue/crud'
import { useI18n } from 'vue-i18n'
import DeptList from './components/dept-list.vue'
import UserMove from './components/user-move.vue'
import { Plugins } from '/#/crud'
import { useCool } from '/@/cool'
import { useViewGroup } from '/@/plugins/view'

defineOptions({
  name: 'sys-user',
})

const { service, refs, setRefs } = useCool()
const { t } = useI18n()

const { ViewGroup } = useViewGroup({
  title: t('用户列表'),
})

// cl-crud
const Crud = useCrud({
  service: service.base.sys.user,
})

// cl-table
const Table = useTable({
  columns: [
    {
      type: 'selection',
      width: 60,
    },
    {
      prop: 'headImg',
      label: t('头像'),
      component: {
        name: 'cl-avatar',
        props: {
          size: 32,
        },
      },
    },
    {
      prop: 'username',
      label: t('用户名'),
      minWidth: 150,
    },
    {
      prop: 'name',
      label: t('姓名'),
      minWidth: 120,
    },
    {
      prop: 'nickName',
      label: t('昵称'),
      minWidth: 120,
    },
    {
      prop: 'departmentName',
      label: t('部门名称'),
      minWidth: 120,
    },
    {
      prop: 'roleName',
      label: t('角色'),
      headerAlign: 'center',
      minWidth: 160,
      dict: [],
      formatter(row) {
        return row.roleName?.split(',')
      },
    },
    {
      prop: 'status',
      label: t('状态'),
      minWidth: 100,
      component: {
        name: 'cl-switch',
      },
    },
    {
      prop: 'phone',
      label: t('手机号码'),
      minWidth: 120,
    },
    {
      prop: 'remark',
      label: t('备注'),
      minWidth: 200,
      showOverflowTooltip: true,
    },
    {
      prop: 'createTime',
      label: t('创建时间'),
      sortable: 'desc',
      minWidth: 170,
    },
    {
      type: 'op',
      buttons: ['slot-btn', 'edit', 'delete'],
      width: 270,
    },
  ],
})

// cl-upsert
const Upsert = useUpsert({
  dialog: {
    width: '800px',
  },

  items: [
    {
      prop: 'headImg',
      label: t('头像'),
      component: {
        name: 'cl-upload',
        props: {
          text: t('选择头像'),
        },
      },
    },
    {
      prop: 'name',
      label: t('姓名'),
      span: 12,
      required: true,
      component: {
        name: 'el-input',
      },
    },
    {
      prop: 'nickName',
      label: t('昵称'),
      required: true,
      span: 12,
      component: {
        name: 'el-input',
      },
    },
    {
      prop: 'username',
      label: t('用户名'),
      required: true,
      span: 12,
      component: {
        name: 'el-input',
      },
    },
    () => {
      return {
        prop: 'password',
        label: t('密码'),
        span: 12,
        required: Upsert.value?.mode === 'add',
        component: {
          name: 'el-input',
          props: {
            type: 'password',
            showPassword: true,
            autocomplete: 'new-password',
          },
        },
        rules: [
          {
            min: 6,
            max: 16,
            message: t('密码长度在 6 到 16 个字符'),
          },
        ],
      }
    },
    {
      prop: 'roleIdList',
      label: t('角色'),
      value: [],
      required: true,
      component: {
        name: 'el-select',
        options: [],
        props: {
          'multiple': true,
          'multiple-limit': 3,
        },
      },
    },
    {
      prop: 'phone',
      label: t('手机号码'),
      span: 12,
      component: {
        name: 'el-input',
        props: {
          maxlength: 11,
        },
      },
    },
    {
      prop: 'email',
      label: t('邮箱'),
      span: 12,
      component: {
        name: 'el-input',
      },
    },
    {
      prop: 'remark',
      label: t('备注'),
      component: {
        name: 'el-input',
        props: {
          type: 'textarea',
          rows: 4,
        },
      },
    },
    {
      prop: 'status',
      label: t('状态'),
      value: 1,
      component: {
        name: 'el-radio-group',
        options: [
          {
            label: t('启用'),
            value: 1,
          },
          {
            label: t('禁用'),
            value: 0,
          },
        ],
      },
    },
  ],

  onSubmit(data, { next }) {
    next({
      departmentId: ViewGroup.value?.selected?.id,
      ...data,
    })
  },

  async onOpen() {
    // 设置权限列表
    service.base.sys.role.list().then((res) => {
      Upsert.value?.setOptions(
        'roleIdList',
        res.map((e) => {
          return {
            label: e.name || '',
            value: e.id,
          }
        }),
      )
    })
  },

  plugins: [Plugins.Form.setFocus('name')],
})

// 刷新列表
function refresh(params?: any) {
  Crud.value?.refresh(params)
}

// 新增成员
function onUserAdd({ id }: Eps.BaseSysDepartmentEntity) {
  Crud.value?.rowAppend({
    departmentId: id,
  })
}

// 移动成员
async function toMove(item?: Eps.BaseSysDepartmentEntity) {
  let ids: number[] = []

  if (item) {
    ids = [item.id!]
  }
  else {
    ids = Table.value?.selection.map(e => e.id) || []
  }

  refs.userMove.open(ids)
}
</script>

<template>
  <cl-view-group ref="ViewGroup">
    <template #left>
      <!-- 部门列表 -->
      <dept-list @refresh="refresh" @user-add="onUserAdd" />
    </template>

    <template #right>
      <cl-crud ref="Crud">
        <cl-row>
          <!-- 刷新按钮 -->
          <cl-refresh-btn />
          <!-- 新增按钮 -->
          <cl-add-btn />
          <!-- 批量删除按钮 -->
          <cl-multi-delete-btn />
          <!-- 用户转移 -->
          <el-button
            v-permission="service.base.sys.user.permission.move"
            type="success"
            :disabled="Table?.selection.length == 0"
            @click="toMove()"
          >
            {{ $t('转移') }}
          </el-button>
          <cl-flex1 />
          <cl-search-key :placeholder="$t('搜索用户名、姓名')" />
        </cl-row>

        <cl-row>
          <cl-table ref="Table">
            <!-- 单个转移 -->
            <template #slot-btn="{ scope }">
              <el-button
                v-permission="service.base.sys.user.permission.move"
                text
                @click="toMove(scope.row)"
              >
                {{ $t('转移') }}
              </el-button>
            </template>
          </cl-table>
        </cl-row>

        <cl-row>
          <cl-flex1 />
          <cl-pagination />
        </cl-row>

        <!-- 新增、编辑 -->
        <cl-upsert ref="Upsert" />

        <!-- 移动 -->
        <user-move :ref="setRefs('userMove')" />
      </cl-crud>
    </template>
  </cl-view-group>
</template>
