import { reactive } from 'vue'

export function useRefs() {
  const refs = reactive<{ [key: string]: any }>({})
  function setRefs(name: string) {
    return (el: any) => {
      refs[name] = el
    }
  }

  return { refs, setRefs }
}
