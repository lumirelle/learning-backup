package common

import "github.com/uptrace/bun"

// Subtree 给查询追加「materialized-path 子树」范围约束：命中 scope 自身及其后代，
// 且严格按 `/` 边界匹配，避免 `LIKE scope%` 这种裸前缀误伤同前缀的兄弟节点
// （例如 scope=/hq/fin 不应匹配 /hq/finance）。scope 为空表示不限范围（管理员）。
//
// col 为路径列名（如 "org_path" / "d.path"）。OR 条件已显式加括号，可安全与其它
// .Where(...) 以 AND 组合。
func Subtree(q *bun.SelectQuery, col, scope string) *bun.SelectQuery {
	if scope == "" {
		return q
	}
	return q.Where("("+col+" = ? OR "+col+" LIKE ?)", scope, scope+"/%")
}

// SubtreeExpr 返回子树范围的 SQL 片段与参数，供无法直接用 *bun.SelectQuery 的场景
// （如子查询字符串拼接）复用同一套 `/` 边界语义。scope 为空返回恒真条件。
func SubtreeExpr(col, scope string) (string, []any) {
	if scope == "" {
		return "TRUE", nil
	}
	return "(" + col + " = ? OR " + col + " LIKE ?)", []any{scope, scope + "/%"}
}
