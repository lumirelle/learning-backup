import type { PropType } from 'vue'
import { cloneDeep, isArray, isString } from 'lodash-es'
import { defineComponent, h, ref, toValue } from 'vue'
import { CrudProps } from '../../comm'
import { deepFind } from '/$/dict/utils'

export default defineComponent({
  name: 'cl-dict',

  props: {
    ...CrudProps,
    // 绑定值
    modelValue: [String, Array, Number],
    // 选项列表
    options: {
      type: Array as PropType<DictOptions[]>,
      default: () => [],
    },
    // 格式化返回
    formatter: Function as PropType<(arr: DictOptions[]) => any>,
    // 颜色组
    color: {
      type: Array as PropType<string[]>,
      default: () => [],
    },
    // 分割符号
    separator: {
      type: String,
      default: ',',
    },
    // 展示所有层级
    allLevels: Boolean,
    // 超出几个隐藏
    hideOver: {
      type: Number,
      default: 5,
    },
    // 纯文字显示
    text: Boolean,
  },

  setup(props) {
    // 选项列表
    const list: DictOptions = cloneDeep(toValue(props.options || []))

    // 设置颜色
    if (props.color) {
      list.forEach((e, i) => {
        if (!e.color) {
          e.color = props.color[i]
        }
      })
    }

    // 是否展开
    const isExpand = ref(false)

    return () => {
      const value = props.modelValue

      // 绑定值
      let values: any[] = []

      // 格式化值
      if (isArray(value)) {
        values = value
      }
      else if (isString(value)) {
        if (props.separator) {
          values = value.split(props.separator)
        }
        else {
          values = [value]
        }
      }
      else {
        values = [value]
      }

      // 数据
      const data = values
        .filter(e => e !== undefined && e !== null && e !== '')
        .map((v) => {
          let d = deepFind(v, list, { allLevels: props.allLevels })

          if (!d) {
            d = {
              label: v,
              value: v,
            }
          }

          return {
            ...d,
            children: [],
          }
        })

      // 是否隐藏部分
      const isHide = data.length > props.hideOver && !isExpand.value

      // 自定义返回
      if (props.formatter) {
        return props.formatter(data)
      }

      // 文字返回
      if (props.text) {
        return data.map(e => e.label).join(props.separator)
      }

      // el-tag 渲染
      return [
        data
          .filter((_, i) => {
            return !isHide || i < props.hideOver
          })
          .map((e) => {
            return h(
              <el-tag style={{ margin: '2px', border: e.color ? '0' : null }} />,
              {
                type: e.type,
                closable: e.closable,
                hit: e.hit,
                color: e.color,
                size: e.size,
                effect: e.effect || 'dark',
                round: e.round,
                disableTransitions: true,
              },
              {
                default: () => e.label,
              },
            )
          }),
        isHide
        && h(
          <el-link
            style={{ marginLeft: '5px', userSelect: 'none', cursor: 'pointer' }}
            onClick={() => {
              isExpand.value = true
            }}
          />,
          {},
          { default: () => '...' },
        ),
      ]
    }
  },
})
