/**
 * 数字模糊化 helper —— 用于登录 / 注册页等公开"水位"展示。
 *
 * <h3>规则（与后端 PublicStatsVO 对齐）</h3>
 * <ul>
 *   <li>n &lt; 5：返回 fewLabel（默认"多人"），避免在数据稀疏时暴露具体到个位的人数</li>
 *   <li>n &gt; 999：返回类似 "1.2k+" 的简写</li>
 *   <li>否则：返回真实数字</li>
 * </ul>
 *
 * @example
 * formatCount(0)    // "多人"
 * formatCount(3)    // "多人"
 * formatCount(8)    // "8"
 * formatCount(120)  // "120"
 * formatCount(1234) // "1.2k+"
 * formatCount(2)    // "多人"
 * formatCount(999)  // "999"
 * formatCount(1000) // "999"   ← 注意：1000 不会进 k+，刚好等于 999 不算 ">999"
 *
 * @param n 真实数字（来自后端聚合 API）
 * @param fewLabel 当 n &lt; 5 时展示的标签（候选人 / 部门场景可不同）
 */
export function formatCount(n: number, fewLabel = '多人'): string {
  if (n < 5) return fewLabel
  if (n > 999) {
    // 1234 → "1.2k+"，1000 不进入此分支（刚好不 > 999）
    const k = n / 1000
    // 保留 1 位小数，去掉多余 .0（5000 → "5k+" 而非 "5.0k+"）
    const rounded = Math.floor(k * 10) / 10
    return rounded % 1 === 0 ? `${rounded.toFixed(0)}k+` : `${rounded.toFixed(1)}k+`
  }
  return String(n)
}
