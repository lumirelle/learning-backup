package process

import (
	"testing"

	"orghr/internal/employee"
)

func TestPayloadStr(t *testing.T) {
	m := map[string]any{"a": "x", "b": 123}
	if payloadStr(m, "a") != "x" {
		t.Fatal("应取到字符串 x")
	}
	if payloadStr(m, "b") != "" {
		t.Fatal("非字符串值应返回空")
	}
	if payloadStr(m, "missing") != "" {
		t.Fatal("缺失键应返回空")
	}
}

func TestPayloadInt64(t *testing.T) {
	// JSON 反序列化的数字是 float64；也兼容 int64 与数字字符串。
	cases := []struct {
		v    any
		want int64
	}{
		{float64(42), 42},
		{int64(7), 7},
		{"99", 99},
		{"not-a-number", 0},
		{nil, 0},
		{true, 0},
	}
	for _, c := range cases {
		if got := payloadInt64(map[string]any{"k": c.v}, "k"); got != c.want {
			t.Fatalf("payloadInt64(%v) = %d，want %d", c.v, got, c.want)
		}
	}
	if payloadInt64(map[string]any{}, "missing") != 0 {
		t.Fatal("缺失键应返回 0")
	}
}

func TestTypeCode(t *testing.T) {
	want := map[string]string{
		"onboard": "ON", "offboard": "OFF", "transfer": "TR", "regularize": "RG", "unknown": "XX",
	}
	for in, exp := range want {
		if got := typeCode(in); got != exp {
			t.Fatalf("typeCode(%q) = %q，want %q", in, got, exp)
		}
	}
}

func TestApplyPlacement(t *testing.T) {
	e := &employee.Employee{DeptName: "研发部", JobLevel: "P4", OrgPath: "/hq/tech/rd"}
	// 只覆盖 payload 中提供的字段，未提供的保持不变。
	applyPlacement(e, map[string]any{
		"dept_name": "产品部",
		"dept_id":   float64(1001),
		"job_level": "P5",
		"org_path":  "/hq/tech/pd",
	})
	if e.DeptName != "产品部" || e.DeptID != 1001 || e.JobLevel != "P5" || e.OrgPath != "/hq/tech/pd" {
		t.Fatalf("placement 未正确应用: %+v", e)
	}

	// 空 payload 不应清空既有字段。
	e2 := &employee.Employee{DeptName: "研发部", JobLevel: "P4"}
	applyPlacement(e2, map[string]any{})
	if e2.DeptName != "研发部" || e2.JobLevel != "P4" {
		t.Fatalf("空 payload 不应改动字段: %+v", e2)
	}
}
