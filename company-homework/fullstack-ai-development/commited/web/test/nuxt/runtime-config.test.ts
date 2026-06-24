import { describe, expect, it } from 'vitest'

describe('runtime config', () => {
  it('should match the snapshot', () => {
    expect(useRuntimeConfig().app).toMatchInlineSnapshot(`
      {
        "baseURL": "/",
        "buildAssetsDir": "/_nuxt/",
        "buildId": "test",
        "cdnURL": "",
      }
    `)
  })
})
