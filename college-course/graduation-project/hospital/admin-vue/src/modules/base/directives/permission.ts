import { checkPerm } from '../utils/permission'

function change(el: HTMLElement, binding: { value: any }) {
  el.style.display = checkPerm(binding.value) ? el.getAttribute('_display') || '' : 'none'
}

export default {
  created(el: HTMLElement, binding: { value: any }) {
    el.setAttribute('_display', el.style.display || '')
    change(el, binding)
  },
  updated: change,
}
