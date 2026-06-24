import { fileURLToPath } from 'node:url'
import { $fetch, setup } from '@nuxt/test-utils/e2e'
import { describe, expect, it } from 'vitest'

await setup({
  rootDir: fileURLToPath(new URL('../../', import.meta.url)),
  dev: true,
})

describe('dev server', () => {
  it('/ -> should contain html doctype', async () => {
    // Fetch for the server-side rendered result
    const html = await $fetch<string>('/')
    expect(html.slice(0, 15)).toMatchInlineSnapshot(`"<!DOCTYPE html>"`)
  })
})
