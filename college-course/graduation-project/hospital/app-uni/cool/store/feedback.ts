import { defineStore } from 'pinia'
import { ref } from 'vue'

interface QueryParams {
  orderId: string | null
}

const useFeedbackStore = defineStore('feedback', () => {
  // 查询参数
  const queryParams = ref<QueryParams>({
    orderId: null,
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
      orderId: null,
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

export { useFeedbackStore }
