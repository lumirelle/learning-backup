import { colorThemeDark, colorThemeLight } from '../config/app'

declare module 'nuxt/schema' {
  interface AppConfig {
    colorThemeLight: string
    colorThemeDark: string
  }
}

/**
 * XXX(Lumirelle): In Nitro 3 / Nuxt 5, `app.config.ts`, `defineAppConfig`, and `useAppConfig` seem no longer supported. Maybe we should prepare to migrate to a new way?
 */
export default import.meta.client
  ? defineAppConfig({
      colorThemeLight,
      colorThemeDark,
    })
  : {}
