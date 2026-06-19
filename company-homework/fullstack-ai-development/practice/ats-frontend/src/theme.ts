import type { GlobalThemeOverrides } from 'naive-ui'

/**
 * Naive UI 主题覆盖（浅色 / 深色）
 * 语义色与 src/styles/tokens.css 对齐；组件级 token 随 data-theme 切换
 */
export const lightThemeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#10b981',
    primaryColorHover: '#34d399',
    primaryColorPressed: '#059669',
    primaryColorSuppl: '#10b981',

    infoColor: '#3b82f6',
    successColor: '#10b981',
    warningColor: '#f59e0b',
    errorColor: '#ef4444',

    bodyColor: '#fafafa',
    cardColor: '#ffffff',
    modalColor: '#ffffff',
    popoverColor: '#ffffff',

    textColorBase: '#18181b',
    textColor1: '#18181b',
    textColor2: '#27272a',
    textColor3: '#52525b',
    textColorDisabled: '#a1a1aa',

    borderColor: '#e4e4e7',
    dividerColor: '#ececef',

    fontFamily:
      'Inter, -apple-system, BlinkMacSystemFont, \'PingFang SC\', \'Microsoft YaHei\', \'Noto Sans SC\', sans-serif',
    fontFamilyMono:
      '\'JetBrains Mono\', \'SF Mono\', Consolas, \'Liberation Mono\', monospace',

    fontSize: '14px',
    fontSizeMedium: '14px',
    fontSizeLarge: '16px',
    fontSizeSmall: '13px',

    borderRadius: '8px',
    borderRadiusSmall: '6px',

    cubicBezierEaseInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
    cubicBezierEaseOut: 'cubic-bezier(0.16, 1, 0.3, 1)',
  },
  Button: {
    fontWeight: '500',
    textColorPrimary: '#ffffff',
    heightMedium: '36px',
  },
  Card: {
    borderRadius: '12px',
    paddingMedium: '20px',
  },
  Input: {
    heightMedium: '36px',
    borderRadius: '8px',
  },
  DataTable: {
    thColor: '#fafafa',
    thTextColor: '#71717a',
    thFontWeight: '600',
    tdColorHover: '#fafafa',
  },
  Tag: {
    borderRadius: '6px',
  },
}

export const darkThemeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#34d399',
    primaryColorHover: '#6ee7b7',
    primaryColorPressed: '#10b981',
    primaryColorSuppl: '#34d399',

    infoColor: '#60a5fa',
    successColor: '#34d399',
    warningColor: '#fbbf24',
    errorColor: '#f87171',

    bodyColor: '#0f0f14',
    cardColor: '#1f1f28',
    modalColor: '#1f1f28',
    popoverColor: '#27272f',
    tableColor: '#1f1f28',

    textColorBase: '#fafafa',
    textColor1: '#f4f4f5',
    textColor2: '#e4e4e7',
    textColor3: '#a1a1aa',
    textColorDisabled: '#71717a',

    borderColor: '#3f3f46',
    dividerColor: '#27272f',

    fontFamily:
      'Inter, -apple-system, BlinkMacSystemFont, \'PingFang SC\', \'Microsoft YaHei\', \'Noto Sans SC\', sans-serif',
    fontFamilyMono:
      '\'JetBrains Mono\', \'SF Mono\', Consolas, \'Liberation Mono\', monospace',

    fontSize: '14px',
    fontSizeMedium: '14px',
    fontSizeLarge: '16px',
    fontSizeSmall: '13px',

    borderRadius: '8px',
    borderRadiusSmall: '6px',

    cubicBezierEaseInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
    cubicBezierEaseOut: 'cubic-bezier(0.16, 1, 0.3, 1)',
  },
  Button: {
    fontWeight: '500',
    textColorPrimary: '#0f0f14',
    heightMedium: '36px',
  },
  Card: {
    borderRadius: '12px',
    paddingMedium: '20px',
  },
  Input: {
    heightMedium: '36px',
    borderRadius: '8px',
    color: '#fafafa',
    colorDisabled: '#71717a',
  },
  DataTable: {
    thColor: '#1f1f28',
    thTextColor: '#a1a1aa',
    thFontWeight: '600',
    tdColor: '#1f1f28',
    tdColorHover: '#27272f',
  },
  Tag: {
    borderRadius: '6px',
  },
  Drawer: {
    color: '#1f1f28',
  },
}

/** @deprecated 使用 lightThemeOverrides */
export const themeOverrides = lightThemeOverrides
