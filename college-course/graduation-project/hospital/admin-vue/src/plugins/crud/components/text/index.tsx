import type { PropType } from 'vue'
import { computed, defineComponent, useModel } from 'vue'

export default defineComponent({
  name: 'cl-text',

  props: {
    modelValue: null,
    formatter: Function as PropType<(value: any, scope: any) => string>,
    // 继承 el-text https://element-plus.org/zh-CN/component/text.html#attributes
  },

  setup(props) {
    const value = useModel(props, 'modelValue')

    const text = computed(() => {
      if (props.formatter) {
        return props.formatter(value.value, props)
      }

      return value.value
    })

    return () => {
      return <el-text>{text.value}</el-text>
    }
  },
})
