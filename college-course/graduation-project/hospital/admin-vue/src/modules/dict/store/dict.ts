import { assign, isArray, orderBy } from 'lodash-es'
import { defineStore } from 'pinia'
import { computed, reactive, toRaw } from 'vue'
import { deepFind, isEmpty } from '../utils'
import { isDev } from '/@/config'
import { service } from '/@/cool'
import { deepTree } from '/@/cool/utils'

const useDictStore = defineStore('dict', () => {
  // 对象数据
  const data = reactive<Dict.Data>({})

  // 获取
  function get(name: Dict.Key, sort?: 'desc' | 'asc') {
    return computed(() => orderBy(data[name] || [], 'orderNum', sort))
  }

  // 查找
  function find(name: Dict.Key, value: any | any[]) {
    const arr = isArray(value) ? value : [value]
    return arr.filter(e => e !== undefined).map(v => deepFind(v, get(name).value))
  }

  // 通过 label 获取值，没有则返回 0
  function getByLabel(name: Dict.Key, label: string) {
    return get(name).value.find(item => item.label === label)?.value || 0
  }

  // 刷新
  async function refresh(types?: Dict.Key[]) {
    return service.dict.info
      .data({
        types: types?.filter(e => !isEmpty(e)),
      })
      .then((res: Dict.Data) => {
        const d = {}

        for (const [i, arr] of Object.entries(res)) {
          arr.forEach((e) => {
            e.label = e.name

            if (isEmpty(e.value)) {
              e.value = e.id
            }
          })

          d[i] = deepTree(arr, 'desc')
        }

        assign(data, d)

        if (isDev) {
          console.group('字典数据')
          console.log(toRaw(data))
          console.groupEnd()
        }

        return data
      })
  }

  return {
    data,
    get,
    getByLabel,
    find,
    refresh,
  }
})

export { useDictStore }
