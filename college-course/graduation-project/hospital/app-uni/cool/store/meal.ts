import { defineStore } from 'pinia'
import { ref } from 'vue'

interface QueryParams {
  name: string
  categoryId: string
  hospitalId: string
  departmentId: string
  doctorId: string
  staffId: string
}

const useMealStore = defineStore('meal', () => {
  // 查询参数
  const queryParams = ref<QueryParams>({
    name: '',
    categoryId: '',
    hospitalId: '',
    departmentId: '',
    doctorId: '',
    staffId: '',
  })

  function setQueryParam(paramName: keyof QueryParams, value: string) {
    queryParams.value[paramName] = value
  }

  function getQueryParam(paramName: keyof QueryParams) {
    return queryParams.value[paramName]
  }

  function resetQueryParam(paramName: keyof QueryParams) {
    queryParams.value[paramName] = ''
  }

  function resetQueryParams() {
    queryParams.value = {
      name: '',
      categoryId: '',
      hospitalId: '',
      departmentId: '',
      doctorId: '',
      staffId: '',
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

export { useMealStore }
