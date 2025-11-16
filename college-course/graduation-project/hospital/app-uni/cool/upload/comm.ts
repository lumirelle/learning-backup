import { has, isArray, isObject } from 'lodash-es'

type Size = number | number[] | { h?: number, w?: number, m?: string }

function parse(rules: string[], { url, size }: { url: string, size: Size }) {
  if (!url) {
    return ''
  }

  if (url.startsWith('blob:') || url.includes('file://')) {
    return url
  }

  let h = 0
  let w = 0

  if (isArray(size)) {
    h = size[0]
    w = size[1]
  }
  else if (isObject(size) && has(size, 'h')) {
    h = size.h!
    w = size.w!

    if (size.m) {
      rules.push(`m_${size.m}`)
    }
  }
  else {
    h = w = Number(size)
  }

  url += url.includes('?') ? '&' : '?'

  if (h) {
    rules.push(`h_${h}`)
  }

  if (w) {
    rules.push(`w_${w}`)
  }

  return `${url}${rules.join(',')}`
}

function videoPoster(url: string, size: Size) {
  return parse(['x-oss-process=video/snapshot,t_1000,f_jpg,m_fast'], { url, size })
}

function resizeImage(url: string, size: Size) {
  return parse(['x-oss-process=image/resize'], { url, size })
}

export { resizeImage, videoPoster }
