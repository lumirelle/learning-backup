/**
 * @file 人事枚举/格式化工具（app/utils/hr.ts）的单元测试——纯函数，无需 Nuxt 环境。
 */
import { describe, expect, it } from 'vitest'
import {
  contractStatusMap,
  employmentStatusMap,
  fmtDate,
  fmtDateTime,
  genderMap,
  processStatusMap,
  toneClass,
} from '../../app/utils/hr'

describe('toneClass', () => {
  it('已知色调映射到对应的 UnoCSS class（语义色板：green→emerald / red→rose / blue→primary）', () => {
    expect(toneClass('green')).toContain('text-emerald-700')
    expect(toneClass('red')).toContain('text-rose-700')
    expect(toneClass('blue')).toContain('text-primary-700')
  })
  it('未知/空色调回退到 gray', () => {
    expect(toneClass('not-a-tone')).toBe(toneClass('gray'))
    expect(toneClass('')).toBe(toneClass('gray'))
  })
})

describe('fmtDate', () => {
  it('空值显示破折号', () => {
    expect(fmtDate()).toBe('—')
    expect(fmtDate(null)).toBe('—')
    expect(fmtDate('')).toBe('—')
  })
  it('截断 RFC3339 到 YYYY-MM-DD', () => {
    expect(fmtDate('2024-03-01T08:30:00Z')).toBe('2024-03-01')
    expect(fmtDate('2024-03-01')).toBe('2024-03-01')
  })
})

describe('fmtDateTime', () => {
  it('空值显示破折号', () => {
    expect(fmtDateTime(null)).toBe('—')
  })
  it('转为 YYYY-MM-DD HH:mm（T 替换为空格）', () => {
    expect(fmtDateTime('2024-03-01T08:30:45Z')).toBe('2024-03-01 08:30')
  })
})

describe('枚举映射完整性', () => {
  it('在职状态四种齐全且带语义色', () => {
    for (const k of ['probation', 'active', 'leaving', 'left']) {
      expect(employmentStatusMap[k]).toBeTruthy()
      expect(employmentStatusMap[k]!.label).toBeTruthy()
      expect(employmentStatusMap[k]!.tone).toBeTruthy()
    }
  })
  it('流程状态含待审批/已通过/已驳回/已生效', () => {
    expect(processStatusMap.pending!.label).toBe('待审批')
    expect(processStatusMap.effective!.label).toBe('已生效')
    expect(processStatusMap.rejected!.label).toBe('已驳回')
  })
  it('合同状态含在用/已续签/已终止', () => {
    expect(contractStatusMap.active!.label).toBe('在用')
    expect(contractStatusMap.renewed!.label).toBe('已续签')
    expect(contractStatusMap.terminated!.label).toBe('已终止')
  })
  it('性别映射', () => {
    expect(genderMap.male).toBe('男')
    expect(genderMap.female).toBe('女')
  })
})
