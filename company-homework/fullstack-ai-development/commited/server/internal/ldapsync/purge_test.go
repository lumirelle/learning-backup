package ldapsync

import (
	"context"
	"testing"

	"orghr/internal/infra/idgen"
	"orghr/internal/testutil"
)

// 清理演示数据：演示 org/dept/emp + 其业务数据被软删/清掉，LDAP 来源与登录账号保留。
func TestPurgeDemo(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()

	// 演示员工（ext_source=''）+ 一份合同
	demoEmp := &empRow{ID: idgen.NextID(), EmployeeNo: "D001", Name: "演示员工", Status: "active"}
	if _, err := db.NewInsert().Model(demoEmp).Exec(ctx); err != nil {
		t.Fatalf("插演示员工: %v", err)
	}
	if _, err := db.NewInsert().Table("contracts").
		Model(&map[string]any{
			"id": idgen.NextID(), "employee_id": demoEmp.ID, "contract_no": "C1",
			"type": "fixed_term", "status": "active",
		}).Exec(ctx); err != nil {
		t.Fatalf("插合同: %v", err)
	}
	// LDAP 来源员工（应保留）
	ldapEmp := &empRow{ID: idgen.NextID(), EmployeeNo: "L001", Name: "LDAP员工", Status: "active", ExtSource: "ldap", ExtID: "wx1"}
	if _, err := db.NewInsert().Model(ldapEmp).Exec(ctx); err != nil {
		t.Fatalf("插 LDAP 员工: %v", err)
	}
	// 登录账号（演示，ext_source=''）——必须保留
	loginUser := &userRow{ID: idgen.NextID(), Username: "super_admin", Name: "超管", PasswordHash: "x", IsActive: true}
	if _, err := db.NewInsert().Model(loginUser).Exec(ctx); err != nil {
		t.Fatalf("插登录账号: %v", err)
	}

	rep, err := PurgeDemo(ctx, db, false)
	if err != nil {
		t.Fatalf("清理: %v", err)
	}
	if rep.Employees != 1 || rep.Contracts != 1 {
		t.Fatalf("应清 1 演示员工 + 1 合同，实际 %+v", rep)
	}

	// 演示员工没了，LDAP 员工还在
	demoLeft, _ := db.NewSelect().Table("employees").Where("ext_source = '' AND deleted_at IS NULL").Count(ctx)
	ldapLeft, _ := db.NewSelect().Table("employees").Where("ext_source = 'ldap' AND deleted_at IS NULL").Count(ctx)
	if demoLeft != 0 || ldapLeft != 1 {
		t.Fatalf("演示应清空、LDAP 应保留：demo=%d ldap=%d", demoLeft, ldapLeft)
	}
	// 登录账号必须保留（否则锁死后台）
	userLeft, _ := db.NewSelect().Table("users").Where("deleted_at IS NULL").Count(ctx)
	if userLeft != 1 {
		t.Fatalf("登录账号必须保留，实际剩 %d", userLeft)
	}
	// 合同被软删
	contractLeft, _ := db.NewSelect().Table("contracts").Where("deleted_at IS NULL").Count(ctx)
	if contractLeft != 0 {
		t.Fatal("演示合同应被软删")
	}
}

func TestPurgeDemoDryRun(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()

	demoEmp := &empRow{ID: idgen.NextID(), EmployeeNo: "D001", Name: "演示员工", Status: "active"}
	if _, err := db.NewInsert().Model(demoEmp).Exec(ctx); err != nil {
		t.Fatalf("插演示员工: %v", err)
	}

	rep, err := PurgeDemo(ctx, db, true)
	if err != nil {
		t.Fatalf("预演: %v", err)
	}
	if !rep.DryRun || rep.Employees != 1 {
		t.Fatalf("预演应报告将清 1 员工: %+v", rep)
	}
	left, _ := db.NewSelect().Table("employees").Where("deleted_at IS NULL").Count(ctx)
	if left != 1 {
		t.Fatal("预演不应真删")
	}
}
