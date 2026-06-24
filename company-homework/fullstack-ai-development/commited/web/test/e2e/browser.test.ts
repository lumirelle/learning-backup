/**
 * @file A demo for testing Nuxt in an end-to-end browser environment
 */

import { fileURLToPath } from 'node:url'
import { createPage, setup } from '@nuxt/test-utils/e2e'
import { describe, expect, it } from 'vitest'

/**
 * Setup test environment
 *
 * @see [Setup Vitest](https://github.com/nuxt/test-utils/blob/main/src/e2e/setup/vitest.ts#L3)
 */
await setup({
  rootDir: fileURLToPath(new URL('../../', import.meta.url)),
  browser: true,
})

describe('browser', () => {
  // TODO(Lumirelle): Skip browser test because playwright cannot be launched with bun.
  // See: https://github.com/oven-sh/bun/issues/15679
  it.todo('/ -> should contain html doctype', async () => {
    // Render page in headless browser and wait until hydration is complete
    const page = await createPage('/')
    const html = await page.content()
    expect(html).toContain('<!DOCTYPE html>')
    await page.close()
  })
})
