export function deepFind(value: any, list: any[], options?: { allLevels: boolean }) {
  const { allLevels = true } = options || {}

  function deep(arr: any[], name: string[]): any | undefined {
    for (const e of arr) {
      if (e.value === value) {
        if (allLevels) {
          return {
            ...e,
            label: [...name, e.label].join(' / '),
          }
        }
        else {
          return e
        }
      }
      else if (e.children) {
        const d = deep(e.children, [...name, e.label])

        if (d !== undefined) {
          return d
        }
      }
    }
    return undefined
  }

  return deep(list, [])
}

export function isEmpty(val: any) {
  return val === '' || val === null || val === undefined
}
