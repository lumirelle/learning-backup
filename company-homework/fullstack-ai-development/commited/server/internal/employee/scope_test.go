package employee_test

import (
	"context"
	"sort"
	"testing"

	"orghr/internal/employee"
	"orghr/internal/infra/idgen"
	"orghr/internal/testutil"
)

// 构造一名员工（最小必填字段）。
func emp(no, name, orgPath, status string) *employee.Employee {
	return &employee.Employee{
		ID: idgen.NextID(), EmployeeNo: no, Name: name,
		OrgID: 1, OrgPath: orgPath, EmploymentStatus: status,
	}
}

func names(list []*employee.Employee) []string {
	out := make([]string, 0, len(list))
	for _, e := range list {
		out = append(out, e.Name)
	}
	sort.Strings(out)
	return out
}

// TestListScopeBoundary 验证数据权限按 materialized-path 的 `/` 边界限定子树，
// 不会把同前缀的兄弟节点（/hq/finance）误纳入 /hq/fin 的范围。
func TestListScopeBoundary(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	repo := employee.NewRepo(db)
	ctx := context.Background()

	seed := []*employee.Employee{
		emp("E1", "fin-self", "/hq/fin", "active"),            // 命中：scope 自身
		emp("E2", "fin-child", "/hq/fin/team1", "active"),     // 命中：后代
		emp("E3", "finance-sibling", "/hq/finance", "active"), // 不应命中：同前缀兄弟
		emp("E4", "hr", "/hq/hr", "active"),                   // 不应命中：他枝
	}
	if _, err := db.NewInsert().Model(&seed).Exec(ctx); err != nil {
		t.Fatalf("seed: %v", err)
	}

	list, total, err := repo.List(ctx, employee.ListFilter{OrgPath: "/hq/fin", Page: 1, Size: 20})
	if err != nil {
		t.Fatalf("list: %v", err)
	}
	got := names(list)
	want := []string{"fin-child", "fin-self"}
	if total != 2 || len(got) != 2 || got[0] != want[0] || got[1] != want[1] {
		t.Fatalf("scope=/hq/fin 应只命中 %v（total=2），实际 total=%d list=%v", want, total, got)
	}
}

// TestListScopeRootAndEmpty 验证根范围与无范围（管理员）的行为。
func TestListScopeRootAndEmpty(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	repo := employee.NewRepo(db)
	ctx := context.Background()

	seed := []*employee.Employee{
		emp("E1", "a", "/hq", "active"),
		emp("E2", "b", "/hq/tech/rd", "active"),
		emp("E3", "c", "/other", "active"),
	}
	if _, err := db.NewInsert().Model(&seed).Exec(ctx); err != nil {
		t.Fatalf("seed: %v", err)
	}

	// 空 scope（管理员）：全部可见。
	_, total, err := repo.List(ctx, employee.ListFilter{Page: 1, Size: 20})
	if err != nil || total != 3 {
		t.Fatalf("空 scope 应见 3 人，实际 total=%d err=%v", total, err)
	}

	// /hq 子树：a + b，不含 /other。
	_, total, err = repo.List(ctx, employee.ListFilter{OrgPath: "/hq", Page: 1, Size: 20})
	if err != nil || total != 2 {
		t.Fatalf("/hq 子树应见 2 人，实际 total=%d err=%v", total, err)
	}
}

// TestListFilters 验证关键字与状态筛选。
func TestListFilters(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	repo := employee.NewRepo(db)
	ctx := context.Background()

	seed := []*employee.Employee{
		emp("A001", "张三", "/hq", "active"),
		emp("A002", "李四", "/hq", "left"),
		emp("B001", "王五", "/hq", "probation"),
	}
	if _, err := db.NewInsert().Model(&seed).Exec(ctx); err != nil {
		t.Fatalf("seed: %v", err)
	}

	_, total, _ := repo.List(ctx, employee.ListFilter{Status: "active", Page: 1, Size: 20})
	if total != 1 {
		t.Fatalf("status=active 应 1 人，实际 %d", total)
	}
	_, total, _ = repo.List(ctx, employee.ListFilter{Keyword: "A00", Page: 1, Size: 20})
	if total != 2 {
		t.Fatalf("keyword=A00 应命中 2 人（工号），实际 %d", total)
	}
	_, total, _ = repo.List(ctx, employee.ListFilter{Keyword: "王", Page: 1, Size: 20})
	if total != 1 {
		t.Fatalf("keyword=王 应命中 1 人（姓名），实际 %d", total)
	}
}
