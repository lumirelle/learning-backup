import { renderNode, useCrud } from '@cool-vue/crud'
import { ElMessage } from 'element-plus'
import { cloneDeep } from 'lodash-es'
import { nextTick, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRefs } from '/@/cool'

/**
 * 行编辑
 * @returns
 */
export function rowEdit() {
  return ({ exposed }) => {
    const Crud = useCrud()
    const { t } = useI18n()
    const { refs, setRefs } = useRefs()

    // 配置
    const { columns } = exposed.config as ClTable.Config

    // 可编辑列
    const list = columns.filter(e => e.edit?.enable === true || e.edit === true)

    // 操作列
    const op = columns.find(e => e.type === 'op')

    // 编辑信息
    const edit = reactive({})

    // 列点击
    exposed.config.on.onCellClick = (row: any, column: any) => {
      const item = list.find(e => e.prop == column.property)

      if (item) {
        edit[row.id] = {
          data: cloneDeep(row),
          loading: false,
          [`show_${column.property}`]: true,
        }

        nextTick(() => {
          refs[`${row.id}_${column.property}`]?.focus()
        })
      }
    }

    // 设置值的格式化
    list.forEach((e) => {
      e._formatter = e.formatter

      e.formatter = (row, column, value) => {
        const id = `${row.id}_${e.prop}`

        if (edit[row.id] && edit[row.id][`show_${column.property}`]) {
          if (e.edit?.component) {
            return renderNode(e.edit.component, {
              prop: e.prop,
              scope: row,
              ref: setRefs(id),
            })
          }

          return (
            <el-input ref={setRefs(id)} v-model={row[e.prop]} clearable placeholder={t('请输入')} />
          )
        }
        else {
          return value
        }
      }
    })

    if (op) {
      if (!op._buttons) {
        op._buttons = op.buttons || ['edit', 'delete']
      }

      // 操作按钮
      op.buttons = ({ scope }) => {
        const ed = edit[scope.row.id]

        if (ed) {
          return [
            {
              label: t('保存'),
              type: 'success',
              props: {
                loading: ed.loading,
              },
              async onClick() {
                ed.loading = true

                await Crud.value?.service
                  .update(scope.row)
                  .then(() => {
                    ElMessage.success(t('操作成功'))
                    delete edit[scope.row.id]
                  })
                  .catch((err) => {
                    ElMessage.error(err.message)
                    edit[scope.row.id].loading = false
                  })
              },
            },
            {
              label: t('取消'),
              onClick() {
                if (ed) {
                  for (const i in ed.data) {
                    scope.row[i] = ed.data[i]
                  }

                  delete edit[scope.row.id]
                }
              },
            },
          ]
        }
        else {
          return op._buttons
        }
      }
    }
  }
}
