/**
 * Middlewares are used to apply extra logic before route changes, to apply logic after route changes, please use router plugins instead.
 *
 * @see [save-user-source.client.ts](../plugins/save-user-source.client.ts)
 */
export default defineNuxtRouteMiddleware((to, from) => {
  // oxlint-disable-next-line no-console
  console.info('User is navigating to:', to.fullPath, 'from:', from.fullPath, '!')
})
