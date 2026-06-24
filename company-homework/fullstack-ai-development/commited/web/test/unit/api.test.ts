/**
 * @file 响应信封解包（app/utils/api.ts）的单元测试。
 */
import { describe, expect, it } from 'vitest'
import { isEnvelope, unwrapEnvelope } from '../../app/utils/api'

describe('isEnvelope', () => {
  it('含 code + data 的对象视为信封', () => {
    expect(isEnvelope({ code: 0, message: 'ok', data: { x: 1 } })).toBe(true)
  })
  it('缺字段/非对象不视为信封', () => {
    expect(isEnvelope({ code: 0 })).toBe(false)
    expect(isEnvelope({ data: 1 })).toBe(false)
    expect(isEnvelope(null)).toBe(false)
    expect(isEnvelope('blob')).toBe(false)
    expect(isEnvelope(undefined)).toBe(false)
  })
})

describe('unwrapEnvelope', () => {
  it('是信封时提取 data', () => {
    expect(unwrapEnvelope({ code: 0, message: 'ok', data: { name: '张三' } })).toEqual({ name: '张三' })
    expect(unwrapEnvelope({ code: 0, message: 'ok', data: [1, 2, 3] })).toEqual([1, 2, 3])
  })
  it('data 为 null/0/空数组等假值也应正确提取', () => {
    expect(unwrapEnvelope({ code: 0, message: 'ok', data: null })).toBe(null)
    expect(unwrapEnvelope({ code: 0, message: 'ok', data: 0 })).toBe(0)
  })
  it('非信封原样返回（兼容二进制/裸响应）', () => {
    expect(unwrapEnvelope('raw-string')).toBe('raw-string')
    const arr = [1, 2]
    expect(unwrapEnvelope(arr)).toBe(arr)
  })
})
