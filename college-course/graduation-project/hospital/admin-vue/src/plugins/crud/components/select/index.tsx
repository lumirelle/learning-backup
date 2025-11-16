import type { PropType, Ref } from 'vue'
import { useCrud, useForm } from '@cool-vue/crud'
import { cloneDeep, isEmpty, isFunction, isString } from 'lodash-es'
import {
  computed,
  defineComponent,
  onActivated,
  onMounted,

  ref,

  toValue,
  useModel,
} from 'vue'
import { useI18n } from 'vue-i18n'
import { CrudProps } from '../../comm'
import { useCool } from '/@/cool'
import { deepTree, parsePx } from '/@/cool/utils'

export default defineComponent({
  name: 'cl-select',

  props: {
    ...CrudProps,
    modelValue: [String, Number, Array],
    options: {
      type: [Array, Object] as PropType<any[] | Ref<any[]>>,
      default: () => [],
    },
    prop: String,
    labelKey: {
      type: String,
      default: 'label',
    },
    valueKey: {
      type: String,
      default: 'value',
    },
    width: [String, Number],

    // 是否树形
    tree: Boolean,
    // 是否返回选中层级下的所有值
    allLevelsId: Boolean,
    // 是否父子不互相关联
    checkStrictly: Boolean,
    // 默认展开所有
    defaultExpandAll: Boolean,
    // 顶级标签
    topLabel: String,
    // 是否选择当前表格数据，如果是，则不能选择自身及其下级
    current: Boolean,

    // 请求接口，如果是字符串，则使用 crud 下的方法
    api: [String, Function],

    // 值变化刷新
    refreshOnChange: {
      type: Boolean,
      default: true,
    },
    // 是否在激活时刷新
    refreshOnActivated: {
      type: Boolean,
      default: true,
    },
  },

  emits: ['update:modelValue', 'change'],

  setup(props, { emit, expose }) {
    const { refs, setRefs, route } = useCool()
    const { t } = useI18n()

    // cl-crud
    const Crud = useCrud()

    // cl-form
    const Form = useForm()

    // 是否用于搜索
    const isSearch = computed(() => !Form.value || Form.value?.name === 'search')

    // 选项
    const options = ref<Dict.Item[]>()

    // 选中值
    const value = useModel(props, 'modelValue')

    // 列表
    const list = computed(() => {
      let data: any[] = []

      if (props.current) {
        data = cloneDeep(toValue(Crud.value?.['cl-table']?.data))

        // 禁用自身及其下级
        function deep(d: any, f: boolean) {
          if (d.id && d.id == Form.value?.getForm('id')) {
            f = true
          }

          if (f) {
            d.disabled = true
          }

          if (d.children) {
            d.children.forEach((e: any) => {
              deep(e, f)
            })
          }
        }

        deep({ children: data }, false)
      }
      else {
        data = toValue(options.value || props.options) || []
      }

      // 如果存在顶级标签
      if (props.topLabel) {
        return [
          {
            [props.labelKey]: props.topLabel,
            [props.valueKey]: 0,
            children: data,
          },
        ]
      }

      return data
    })

    // 获取值
    function getValue(val: any): any | any[] {
      if (props.allLevelsId) {
        const ids: any[] = []

        // 获取所有的值
        const deep = (arr: Dict.Item[], f: boolean) => {
          arr.forEach((e) => {
            const f2 = e[props.valueKey] == val

            if (f || f2) {
              ids.push(e[props.valueKey])
            }

            if (e.children) {
              deep(e.children, f || f2)
            }
          })
        }

        deep(list.value, false)

        return isEmpty(ids) ? undefined : ids
      }
      else {
        return val === '' ? undefined : val
      }
    }

    // 值改变
    function onChange(val: any) {
      const v = getValue(val)

      emit('update:modelValue', v)
      emit('change', v)

      if (isSearch.value) {
        if (props.prop && props.refreshOnChange) {
          Crud.value?.refresh({ page: 1, [props.prop]: v })
        }
      }
    }

    // 聚焦
    function focus() {
      refs.select?.focus()
      refs.select?.$.proxy.$el?.querySelector('.el-select__wrapper')?.click()
    }

    // 获取选项数据
    function refresh() {
      let req: Promise<any> | null = null

      if (isString(props.api)) {
        req = Crud.value?.service[props.api]()
      }

      if (isFunction(props.api)) {
        req = props.api()
      }

      if (req) {
        req.then((res) => {
          options.value = deepTree(res)
        })
      }
    }

    onMounted(() => {
      if (props.api) {
        refresh()

        onActivated(() => {
          if (props.refreshOnActivated) {
            if (!Form.value || Form.value?.name == 'search') {
              refresh()
            }
          }
        })
      }
    })

    expose({
      focus,
    })

    return () => {
      // 样式
      const style = {
        width: parsePx(props.width!),
      }

      // 占位符
      const placeholder = isSearch.value ? t('全部') : t('请选择')

      // 树形下拉框
      const TreeSelect = (
        <el-tree-select
          v-model={value.value}
          clearable
          filterable
          placeholder={placeholder}
          disabled={props.disabled}
          data={list.value}
          checkStrictly={props.allLevelsId || props.checkStrictly}
          defaultExpandAll={props.defaultExpandAll}
          props={{
            label: props.labelKey,
            value: props.valueKey,
          }}
          style={style}
          onChange={onChange}
          ref={setRefs('select')}
        />
      )

      // 普通下拉框
      const Select = (
        <el-select
          v-model={value.value}
          clearable
          filterable
          disabled={props.disabled}
          placeholder={placeholder}
          style={style}
          onChange={onChange}
          ref={setRefs('select')}
        >
          {list.value?.map((e) => {
            return isString(e)
              ? (
                  <el-option label={e} value={e} />
                )
              : (
                  <el-option
                    disabled={e.disabled}
                    label={e[props.labelKey]}
                    value={e[props.valueKey]}
                  />
                )
          })}
        </el-select>
      )

      return props.tree ? TreeSelect : Select
    }
  },
})
