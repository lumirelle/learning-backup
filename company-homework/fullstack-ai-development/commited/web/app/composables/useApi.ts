// XXX(Lumirelle): Better type...
// XXX(Lumirelle): We should manually import this macro, otherwise it will cause an error,
// because auto-imports will rename it by `as __$__createUseFetch`
import { createUseFetch } from '#app/composables/fetch'

export const useApi = createUseFetch(options => ({
  ...options,
  $fetch: useNuxtApp().$api,
}))
