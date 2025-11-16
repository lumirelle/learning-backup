import { defineComponent } from 'vue'
import { CrudProps } from '../../comm'

export default defineComponent({
  name: 'cl-render',

  props: {
    ...CrudProps,
    modelValue: null,
    placeholder: String,
  },

  setup(props, { slots }) {
    return () => {
      return slots.default?.(props)
    }
  },
})
