declare namespace ClViewGroup {
  interface Item {
    id: any
    name: string
    children: Item[]
    [key: string]: any
  }

  type M<T> = T & Item

  interface Ref<T = Item> {
    selected: M<T> | undefined
    isExpand: boolean
    select(data?: any): void
    expand(value?: boolean): void
    edit(item?: M<T>): void
    remove(item: M<T>): void
    refresh(params?: any): void
  }

  interface Node {
    id: number
    text: string
    checked: boolean
    indeterminate: boolean
    data: any
    expanded: boolean
    visible: boolean
    isCurrent: boolean
    store: any
    isLeaf: boolean
    level: number
    loaded: boolean
    childNodes: Node[]
    loading: boolean
    [key: string]: any
  }

  interface Options<T = Item> {
    label: string
    title: string
    leftWidth: string
    data: {
      [key: string]: any
    }
    tree: {
      lazy?: boolean
      props?: {
        label?: string | ((node: Node, data: any) => string)
        children?: any
        disabled?: string | ((node: Node, data: any) => string)
        isLeaf?: string | ((node: Node, data: any) => string)
        class?: any
      }
      onLoad?(node: Node, resolve: (data: any) => void): void
    }
    service: any
    enableContextMenu?: boolean
    enableAdd?: boolean
    enableRefresh?: boolean
    enableKeySearch?: boolean
    custom?: boolean
    onSelect?(item: M<T>): void
    onEdit?(item?: M<T>): DeepPartial<ClForm.Options>
    onContextMenu?(item: M<T>): ClContextMenu.Options
    onData?(list: M<T>[]): any[]
    onDelete?(item: M<T>, { next }: { next(params: any): void }): Promise<void> | void
  }
}
