import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'

const STORAGE_KEY = 'ats-theme-mode'

function systemPrefersDark(): boolean {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

export function resolveDark(mode: ThemeMode): boolean {
  if (mode === 'dark')
    return true
  if (mode === 'light')
    return false
  return systemPrefersDark()
}

/** 同步到 <html data-theme>，供 tokens.css 与 color-scheme 使用 */
export function applyThemeToDocument(mode: ThemeMode) {
  const dark = resolveDark(mode)
  const root = document.documentElement
  root.dataset.theme = dark ? 'dark' : 'light'
  root.style.colorScheme = dark ? 'dark' : 'light'
  const meta = document.querySelector('meta[name="theme-color"]')
  meta?.setAttribute('content', dark ? '#0b0b12' : '#fafafa')
}

function readStoredMode(): ThemeMode {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw === 'light' || raw === 'dark' || raw === 'system')
      return raw
  }
  catch { /* private mode */ }
  return 'system'
}

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>(readStoredMode())
  const isDark = computed(() => resolveDark(mode.value))

  function setMode(next: ThemeMode) {
    mode.value = next
    try {
      localStorage.setItem(STORAGE_KEY, next)
    }
    catch { /* ignore */ }
    applyThemeToDocument(next)
  }

  function toggle() {
    setMode(isDark.value ? 'light' : 'dark')
  }

  function init() {
    applyThemeToDocument(mode.value)
    const mq = window.matchMedia('(prefers-color-scheme: dark)')
    const onChange = () => {
      if (mode.value === 'system')
        applyThemeToDocument('system')
    }
    mq.addEventListener('change', onChange)
    return () => mq.removeEventListener('change', onChange)
  }

  watch(mode, m => applyThemeToDocument(m))

  return { mode, isDark, setMode, toggle, init }
})
