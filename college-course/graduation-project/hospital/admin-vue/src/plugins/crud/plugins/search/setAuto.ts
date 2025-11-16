import { useCrud } from '@cool-vue/crud'
import { isArray, isEmpty, isString } from 'lodash-es'
import { useI18n } from 'vue-i18n'
import { useDict } from '/$/dict'

/**
 * 设置自动读取
 * @param options.hideLabel 是否隐藏标签
 * @param options.customComponent 自定义组件
 * @param options.ignoreFields 忽略的字段 prop
 * @returns
 */
export function setAuto(
  options: {
    hideLabel?: boolean
    customComponent?(field: ClCrud.Field): Render.Component | null
    ignoreFields?: string[]
  } = {
    hideLabel: true,
  },
) {
  function getComponent(field: ClCrud.Field) {
    if (options.customComponent) {
      const c = options.customComponent(field)

      if (c) {
        return c
      }
    }

    const { dict } = useDict()
    const { t } = useI18n()

    if (field.dict) {
      let options = [] as any

      if (isArray(field.dict)) {
        options = field.dict.map((e, i) => {
          return {
            label: t(e),
            value: i,
          }
        })
      }

      if (isString(field.dict)) {
        options = dict.get(field.dict)
      }

      return {
        name: 'cl-select',
        props: {
          options,
          placeholder: t('搜索{name}', { name: t(field.comment) }),
        },
        style: {
          width: '150px',
        },
      }
    }
    else {
      return {
        name: 'el-input',
        props: {
          placeholder: t('搜索{name}', { name: t(field.comment) }),
          clearable: true,
        },
        style: {
          width: '150px',
        },
      }
    }
  }

  return ({ exposed }) => {
    const { t } = useI18n()
    const Crud = useCrud()
    const { items, inline } = exposed.config as ClSearch.Config

    if (isEmpty(items) || !items) {
      const arr = [] as ClForm.Item[]

      if (Crud.value?.service?.search) {
        const { fieldEq, fieldLike, keyWordLikeFields } = Crud.value?.service.search
        const fields = [fieldEq, fieldLike].flat().filter(Boolean)

        // 精确搜索
        if (!isEmpty(fields)) {
          fields.forEach((e) => {
            if (options.ignoreFields?.includes(e.propertyName)) {
              return
            }

            arr.push({
              prop: e.propertyName,
              label: options.hideLabel ? undefined : t(e.comment),
              component: getComponent(e),
            })
          })
        }

        // 关键字搜索
        if (!isEmpty(keyWordLikeFields)) {
          const placeholder = keyWordLikeFields.map(e => t(e.comment)).join('、')

          arr.push({
            prop: 'keyWord',
            label: inline ? undefined : t('关键字'),
            component: {
              name: 'el-input',
              props: {
                placeholder: t('搜索{name}', { name: placeholder }),
                clearable: true,
              },
              style: {
                width: '200px',
              },
            },
          })
        }
      }

      items?.splice(0, 999, ...arr)
    }
  }
}
