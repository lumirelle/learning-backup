/**
 * @file A demo for testing Nuxt in an end-to-end server environment
 */

import { fileURLToPath } from 'node:url'
import { $fetch, setup } from '@nuxt/test-utils/e2e'
import { describe, expect, it } from 'vitest'

/**
 * Setup test environment
 *
 * @see [Setup Vitest](https://github.com/nuxt/test-utils/blob/main/src/e2e/setup/vitest.ts#L3)
 */
await setup({
  rootDir: fileURLToPath(new URL('../../', import.meta.url)),
})

describe('server', () => {
  // TODO(Lumirelle): It seems `node <output>/server/index.mjs` does not work as expected, we need to investigate it later
  it.todo('/ -> should contain html doctype', async () => {
    // Fetch for the server-side rendered result
    const html = await $fetch<string>('/')
    expect(html.slice(0, 15)).toMatchInlineSnapshot(`"<!DOCTYPE html>"`)
  })
})
