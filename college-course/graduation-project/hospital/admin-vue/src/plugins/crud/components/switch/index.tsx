import { useCrud } from '@cool-vue/crud'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isBoolean, isFunction } from 'lodash-es'
import { defineComponent, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { CrudProps } from '../../comm'

export default defineComponent({
  name: 'cl-switch',

  props: {
    ...CrudProps,
    modelValue: [Number, String, Boolean],
    activeValue: {
      type: [Number, String, Boolean],
      default: 1,
    },
    inactiveValue: {
      type: [Number, String, Boolean],
      default: 0,
    },
    api: Function,
    isCheck: Boolean,
  },

  emits: ['update:modelValue', 'change'],

  setup(props, { emit }) {
    const { t } = useI18n()

    // cl-crud
    const Crud = useCrud()

    // 状态
    const status = ref<boolean | number | string>()

    // 选中值类型
    const activeValue = ref()
    const inactiveValue = ref()

    // 监听值
    watch(
      () => props.modelValue,
      (val) => {
        status.value = val

        if (val !== undefined) {
          if (isBoolean(val)) {
            activeValue.value = true
            inactiveValue.value = false

            return true
          }
        }

        activeValue.value = props.activeValue
        inactiveValue.value = props.inactiveValue
      },
      {
        immediate: true,
      },
    )

    // 监听改变
    function onChange(val: boolean | string | number) {
      const next = () => {
        if (props.column && props.scope) {
          if (val !== undefined) {
            if (status.value === activeValue.value || status.value === inactiveValue.value) {
              const params = {
                id: props.scope.id,
                [props.column.property]: val,
              }

              const req: Promise<any> = isFunction(props.api)
                ? props.api(params)
                : Crud.value?.service.update(params)

              if (req) {
                req
                  .then(() => {
                    emit('update:modelValue', val)
                    emit('change', val)
                    ElMessage.success(t('更新成功'))
                  })
                  .catch((err) => {
                    ElMessage.error(err.message)
                  })
              }
            }
          }
        }
        else {
          emit('update:modelValue', val)
          emit('change', val)
        }
      }

      if (props.isCheck) {
        ElMessageBox.confirm(val ? t('确定要开启吗？') : t('确定要关闭吗？'), t('提示'), {
          type: 'warning',
        })
          .then(() => {
            next()
          })
          .catch(() => null)
      }
      else {
        next()
      }
    }

    // 点击事件, 阻止冒泡
    function onClick(event: MouseEvent) {
      event.stopPropagation()
    }

    return () => {
      return (
        <el-switch
          model-value={status.value}
          active-value={activeValue.value}
          inactive-value={inactiveValue.value}
          disabled={props.disabled}
          onChange={onChange}
          onClick={onClick}
        />
      )
    }
  },
})
