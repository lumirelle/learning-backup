export const SUPPORT_LOCALES = [
  { code: 'en', language: 'en-US', name: 'English', file: 'en-US.json' },
  /**
   * For Chinese, it should be expressed with a script subtag instead of a region subtag. So we use `Hans` (for Simplified) and `Hant` (for Traditional) as recommended by BCP 47.
   *
   * @see https://en.wikipedia.org/wiki/IETF_language_tag#ISO_3166-1_and_UN_M.49
   */
  { code: 'zh', language: 'zh-Hans', name: '简体中文', file: 'zh-Hans.json' },
  // ...
]
