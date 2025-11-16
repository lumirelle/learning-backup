import { defineStore } from 'pinia'
import { ref } from 'vue'

interface QueryParams {
  status: string | null
}

const useOrderStore = defineStore('order', () => {
  // 查询参数
  const queryParams = ref<QueryParams>({
    status: null,
  })

  function setQueryParam(paramName: keyof QueryParams, value: any) {
    queryParams.value[paramName] = value
  }

  function getQueryParam(paramName: keyof QueryParams) {
    return queryParams.value[paramName]
  }

  function resetQueryParam(paramName: keyof QueryParams) {
    queryParams.value[paramName] = null
  }

  function resetQueryParams() {
    queryParams.value = {
      status: null,
    }
  }

  return {
    queryParams,
    setQueryParam,
    getQueryParam,
    resetQueryParam,
    resetQueryParams,
  }
})

export { useOrderStore }
