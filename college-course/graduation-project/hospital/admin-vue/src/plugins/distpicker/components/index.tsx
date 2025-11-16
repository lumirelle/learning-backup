import type { PropType } from 'vue'
import { defineComponent } from 'vue'
import data from '../data/pca.json'
import { useCool } from '/@/cool'

export default defineComponent({
  name: 'cl-distpicker',

  props: {
    props: Object,
    type: {
      type: String as PropType<'pc' | 'pca'>,
      default: 'pca',
    },
  },

  setup(props, { expose }) {
    const { refs, setRefs } = useCool()

    function focus() {
      refs.cascader?.togglePopperVisible()
    }

    expose({
      focus,
    })

    return () => {
      return (
        <el-cascader
          ref={setRefs('cascader')}
          clearable
          options={data.map((e) => {
            if (props.type === 'pc') {
              return {
                ...e,
                children: e.children.map((a) => {
                  return {
                    ...a,
                    children: undefined,
                  }
                }),
              }
            }

            return e
          })}
          props={{ label: 'name', value: 'name', ...props.props }}
        />
      )
    }
  },
})
