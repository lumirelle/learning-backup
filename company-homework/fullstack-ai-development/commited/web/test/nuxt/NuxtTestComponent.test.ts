/**
 * @file A demo for testing Nuxt components in nuxt (vitest browser) environment
 */

import { mountSuspended } from '@nuxt/test-utils/runtime'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import NuxtTestComponent from '~/components/NuxtTestComponent.vue'

describe('component NuxtTestComponent', () => {
  it('mounts and renders via mount() properly', () => {
    const text = 'custom-text'
    const wrapper = mount(NuxtTestComponent, { propsData: { text } })
    expect(wrapper.text()).toContain(text)
  })

  it('mounts and renders via mountSuspended() properly', async () => {
    const text = 'custom-text'
    const component = await mountSuspended(NuxtTestComponent, { props: { text } })
    expect(component.html()).toContain(text)
  })
})
