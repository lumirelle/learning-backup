// A demo for testing Nuxt components just under the same folder

import { unindent } from '@antfu/utils'
import { mountSuspended } from '@nuxt/test-utils/runtime'
import { describe, expect, it } from 'vitest'
import { AppHeader } from '#components'

describe('components/AppHeader.vue', () => {
  it('should render', async () => {
    const wrapper = await mountSuspended(AppHeader)
    expect(wrapper.html()).toMatchInlineSnapshot(
      unindent`
      "<header text-2xl="" font-300="" flex="" cursor-default="" justify-center="">
        <h1 opacity0="" absolute=""> Nuxt 4 and Vite 8 </h1>
        <div flex="~ col" children:mx-auto=""><img i-public-nuxt="" h-18="" w-18="" inline-block="" alt="Nuxt Logo"><span text-green-400="" mt--2="">Nuxt 4</span></div>
        <div text="3xl gray-400" m="x-4 y-auto" i-carbon-add="" transform="" transition-all-500="" hover:rotate-135=""></div>
        <div flex="~ col" children:mx-auto=""><img i-public-vite="" p-2="" h-18="" w-18="" inline-block="" alt="Vite Logo"><span text-purple-500="" mt--2="">Vite 8</span></div>
      </header>"
    `,
    )
  })
})
