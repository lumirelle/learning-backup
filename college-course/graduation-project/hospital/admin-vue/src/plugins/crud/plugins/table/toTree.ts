import { useCrud } from '@cool-vue/crud'
import { isEmpty } from 'lodash-es'
import { deepTree } from '/@/cool/utils'

interface Item extends Eps.BaseSysMenuEntity {
  children?: Item[]
  _children?: Item[]
  hasChildren?: boolean
}

/**
 * 树形表格
 * @param options.lazy 是否懒加载，数据过多时开启
 * @param options.onRefresh 刷新方法
 * @returns
 */
export function toTree(
  options: { lazy?: boolean, onRefresh?(params: any): Promise<any[]> } = {},
) {
  return ({ exposed }) => {
    const Crud = useCrud()

    // 设置刷新方法
    if (Crud.value) {
      // 原 cl-crud 的 onRefresh
      const onRefresh = Crud.value.config.onRefresh

      // 重写 onRefresh
      Crud.value.config.onRefresh = async (
        params: Parameters<ClCrud.Config['onRefresh']>[0],
        { render, next, done }: Parameters<ClCrud.Config['onRefresh']>[1],
      ) => {
        const req: Promise<any[]> = new Promise((resolve) => {
          if (onRefresh) {
            const _next = async (params?: any) => {
              const res = await next(params)
              resolve(res.list)
            }

            const _render = (list: any[]) => {
              resolve(list)
            }

            onRefresh(params, { render: _render, next: _next, done })
          }
          else {
            resolve(
              options.onRefresh
                ? options.onRefresh(params)
                : Crud.value?.service[Crud.value.dict.api.list](params),
            )
          }
        })

        const list = await req

        render(onData(list, params.sort))
      }
    }

    // 数据处理
    const onData = (list: Item[], sort: 'desc' | 'asc') => {
      const data = deepTree(list, sort)

      // 递归处理
      const deep = (arr: Item[]) => {
        arr.forEach((e) => {
          const nodes: { [key: number]: Item[] }
            = exposed.Table.value?.store.states.lazyTreeNodeMap.value || {}

          if (nodes[e.id!]) {
            nodes[e.id!] = e.children || []
          }

          if (!isEmpty(e.children)) {
            e.hasChildren = true
            e._children = e.children

            if (options.lazy) {
              delete e.children
            }

            deep(e._children || [])
          }
        })
      }

      deep(data)

      return data
    }

    // 层级参数
    exposed.config.props.lazy = true
    exposed.config.props['row-key'] = 'id'
    exposed.config.props['tree-props'] = {
      children: 'children',
      hasChildren: 'hasChildren',
    }

    // 层级事件
    exposed.config.on.load = (row: Item, treeNode: unknown, resolve: (data: Item[]) => void) => {
      resolve(row._children || [])
    }

    // 行点击
    exposed.config.on.onRowClick = (row: Item) => {
      if (row._children) {
        exposed.Table.value?.store.loadOrToggle(row)
      }
    }
  }
}
