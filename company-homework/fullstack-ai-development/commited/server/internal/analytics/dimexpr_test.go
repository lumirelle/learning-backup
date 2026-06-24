package analytics

import (
	"strings"
	"testing"
)

// dimExpr 是构造分组标签的纯函数，验证各维度落到预期的 SQL 片段，且默认回退到部门。
func TestDimExpr(t *testing.T) {
	cases := []struct {
		dim      string
		contains string
	}{
		{"level", "job_level"},
		{"gender", "case gender"},
		{"education", "education"},
		{"position", "position_name"},
		{"age", "age(birthday)"},
		{"dept", "dept_name"},
		{"unknown-dim", "dept_name"}, // 未知维度回退到部门
		{"", "dept_name"},
	}
	for _, c := range cases {
		got := dimExpr(c.dim)
		if !strings.Contains(got, c.contains) {
			t.Fatalf("dimExpr(%q) 应包含 %q，实际:\n%s", c.dim, c.contains, got)
		}
	}
}

func TestActiveStatuses(t *testing.T) {
	// 在职口径固定为 active + probation。
	if len(activeStatuses) != 2 || activeStatuses[0] != "active" || activeStatuses[1] != "probation" {
		t.Fatalf("activeStatuses 口径变化: %v", activeStatuses)
	}
}
