import { defineComponent, useModel } from 'vue'
import './index.scss'

export default defineComponent({
  name: 'cl-select-button',

  props: {
    modelValue: [String, Number, Array],
    options: Array,
    prop: String,
    small: Boolean,
  },

  setup(props) {
    const value = useModel(props, 'modelValue')

    return () => {
      return (
        <el-segmented
          class={[
            'cl-select-button',
            {
              'is-small': props.small,
            },
          ]}
          v-model={value.value}
          options={props.options}
        />
      )
    }
  },
})
