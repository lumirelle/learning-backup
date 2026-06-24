export default defineNuxtRouteMiddleware(() => {
  // Just abort all navigation
  return showError(createError({ statusCode: 500, statusMessage: 'Navigation aborted by middleware!' }))
})
