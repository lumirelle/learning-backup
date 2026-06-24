package org_test

import (
	"context"
	"testing"

	"orghr/internal/common"
	"orghr/internal/employee"
	"orghr/internal/infra/idgen"
	"orghr/internal/org"
	"orghr/internal/testutil"
)

// TestMoveDeptRebasesSubtree 验证移动部门时，自身 path、子部门 path 与直属/后代员工
// 的 org_path 都按 materialized-path 前缀整体迁移（同一事务）。
func TestMoveDeptRebasesSubtree(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()
	svc := org.NewService(org.NewRepo(db))

	orgID := idgen.NextID()
	if _, err := db.NewInsert().Model(&org.Organization{ID: orgID, Code: "hq", Name: "集团", Path: "/hq", Status: "active"}).Exec(ctx); err != nil {
		t.Fatalf("seed org: %v", err)
	}
	// a(/hq/a) 、b(/hq/b) 为顶层部门；c(/hq/a/c) 是 a 的子部门。
	aID, bID, cID := idgen.NextID(), idgen.NextID(), idgen.NextID()
	depts := []*org.Department{
		{ID: aID, OrgID: orgID, Code: "a", Name: "A部", Path: "/hq/a", Status: "active"},
		{ID: bID, OrgID: orgID, Code: "b", Name: "B部", Path: "/hq/b", Status: "active"},
		{ID: cID, OrgID: orgID, ParentID: aID, Code: "c", Name: "C组", Path: "/hq/a/c", Status: "active"},
	}
	if _, err := db.NewInsert().Model(&depts).Exec(ctx); err != nil {
		t.Fatalf("seed depts: %v", err)
	}
	// c 部门下一名员工。
	empID := idgen.NextID()
	if _, err := db.NewInsert().Model(&employee.Employee{
		ID: empID, EmployeeNo: "M1", Name: "组员", OrgID: orgID, OrgPath: "/hq/a/c", DeptName: "C组", EmploymentStatus: "active",
	}).Exec(ctx); err != nil {
		t.Fatalf("seed emp: %v", err)
	}

	// 把 c 从 a 下移动到 b 下：/hq/a/c → /hq/b/c。
	if err := svc.MoveDept(ctx, cID, bID); err != nil {
		t.Fatalf("move: %v", err)
	}

	var cDept org.Department
	_ = db.NewSelect().Model(&cDept).Where("id = ?", cID).Scan(ctx)
	if cDept.Path != "/hq/b/c" || cDept.ParentID != bID {
		t.Fatalf("c 部门应迁移到 /hq/b/c parent=b，实际 path=%s parent=%d", cDept.Path, cDept.ParentID)
	}
	var emp employee.Employee
	_ = db.NewSelect().Model(&emp).Where("id = ?", empID).Scan(ctx)
	if emp.OrgPath != "/hq/b/c" {
		t.Fatalf("员工 org_path 应级联迁移到 /hq/b/c，实际 %s", emp.OrgPath)
	}
}

// TestMoveDeptCycleGuard 验证不能把部门移动到自身的子部门下（成环）。
func TestMoveDeptCycleGuard(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()
	svc := org.NewService(org.NewRepo(db))

	orgID := idgen.NextID()
	_, _ = db.NewInsert().Model(&org.Organization{ID: orgID, Code: "hq", Name: "集团", Path: "/hq", Status: "active"}).Exec(ctx)
	aID, cID := idgen.NextID(), idgen.NextID()
	depts := []*org.Department{
		{ID: aID, OrgID: orgID, Code: "a", Name: "A部", Path: "/hq/a", Status: "active"},
		{ID: cID, OrgID: orgID, ParentID: aID, Code: "c", Name: "C组", Path: "/hq/a/c", Status: "active"},
	}
	_, _ = db.NewInsert().Model(&depts).Exec(ctx)

	// 把 a 移动到其子部门 c 下 → 成环，应被拒（1002）。
	if err := svc.MoveDept(ctx, aID, cID); !isCode(err, 1002) {
		t.Fatalf("成环移动应被拒(1002)，实际 %v", err)
	}
	// a 仍保持原 path（事务回滚/未变更）。
	var aDept org.Department
	_ = db.NewSelect().Model(&aDept).Where("id = ?", aID).Scan(ctx)
	if aDept.Path != "/hq/a" {
		t.Fatalf("被拒移动后 a 应保持 /hq/a，实际 %s", aDept.Path)
	}
}

func isCode(err error, code int) bool {
	ae, ok := err.(*common.AppError)
	return ok && ae.Code == code
}
