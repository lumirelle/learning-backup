import { assign } from 'lodash-es'
import { defineComponent, h, reactive, ref, resolveComponent, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { isComponent } from '/@/cool/utils'

export default defineComponent({
  name: 'cl-editor',

  props: {
    name: {
      type: String,
      required: true,
    },
  },

  setup(props, { slots, expose }) {
    const Editor = ref()
    const ex = reactive({})
    const { t } = useI18n()

    watch(Editor, (v) => {
      if (v) {
        assign(ex, v)
      }
    })

    expose(ex)

    return () => {
      return isComponent(props.name)
        ? (
            h(
              resolveComponent(props.name),
              {
                ...props,
                ref: Editor,
              },
              slots,
            )
          )
        : (
            <el-input type="textarea" rows={4} placeholder={t('请输入')} {...props} />
          )
    }
  },
})
