export function revisePath(path: string) {
  if (!path) {
    return ''
  }

  return path.startsWith('/') ? path : `/${path}`
}

export * from './permission'
