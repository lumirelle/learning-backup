package ldapsync

import (
	"context"
	"testing"

	"github.com/uptrace/bun"

	"orghr/internal/infra/idgen"
	"orghr/internal/testutil"
)

// 同步集成测试：验证落库、幂等、ext_source 隔离、收敛软删除。测试库不可达时自动 Skip。

func countLDAP(t *testing.T, db *bun.DB, table string) int {
	t.Helper()
	n, err := db.NewSelect().Table(table).
		Where("ext_source = 'ldap' AND deleted_at IS NULL").Count(context.Background())
	if err != nil {
		t.Fatalf("count %s: %v", table, err)
	}
	return n
}

func TestApplySyncIdempotentAndIsolated(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()

	// 预置一条「自建」组织数据（ext_source 为空），同步绝不能动它。
	_, err := db.NewInsert().Model(&orgRow{
		ID: idgen.NextID(), Code: "self", Name: "自建集团", Type: "group", Path: "/self", Status: "active",
	}).Exec(ctx)
	if err != nil {
		t.Fatalf("插入自建组织: %v", err)
	}

	snap := sampleSnapshot()

	// 首次同步
	rep, err := Apply(ctx, db, snap, false)
	if err != nil {
		t.Fatalf("首次同步: %v", err)
	}
	if rep.Orgs.Created != 1 || rep.Depts.Created != 4 {
		t.Fatalf("首次应建 1 组织 + 4 部门，实际 %+v", rep.Depts)
	}
	// 两个用户（chen 有部门、li 无部门），各产生 employee + user
	if rep.Employees.Created != 2 || rep.Users.Created != 2 {
		t.Fatalf("首次应建 2 员工 + 2 账号，实际 emp=%+v user=%+v", rep.Employees, rep.Users)
	}
	if countLDAP(t, db, "departments") != 4 || countLDAP(t, db, "employees") != 2 {
		t.Fatal("落库行数不符")
	}

	// 自建组织未被波及
	selfN, _ := db.NewSelect().Table("organizations").Where("ext_source = '' AND deleted_at IS NULL").Count(ctx)
	if selfN != 1 {
		t.Fatal("自建组织被同步误伤")
	}

	// 二次同步（同快照）：全部命中，零新增、零软删
	rep2, err := Apply(ctx, db, snap, false)
	if err != nil {
		t.Fatalf("二次同步: %v", err)
	}
	if rep2.Depts.Created != 0 || rep2.Employees.Created != 0 || rep2.Users.Created != 0 {
		t.Fatalf("幂等失败，二次不应新增: dept=%+v emp=%+v", rep2.Depts, rep2.Employees)
	}
	if rep2.Depts.Deactivated != 0 || rep2.Employees.Deactivated != 0 {
		t.Fatalf("幂等失败，二次不应软删: %+v", rep2)
	}
	if countLDAP(t, db, "departments") != 4 {
		t.Fatal("二次同步后部门数变化")
	}
}

func TestApplyConvergesStale(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()

	full := sampleSnapshot()
	if _, err := Apply(ctx, db, full, false); err != nil {
		t.Fatalf("初始同步: %v", err)
	}
	empBefore := countLDAP(t, db, "employees")

	// 去掉用户 li 与「行政部」分组，再同步 → 应软删消失的部门/人员
	shrunk := &Snapshot{
		Groups: []Group{full.Groups[0], full.Groups[2]}, // 保留技术组链 + 自建分组，去掉行政部
		Users:  []User{full.Users[0]},                   // 仅保留 chen
	}
	rep, err := Apply(ctx, db, shrunk, false)
	if err != nil {
		t.Fatalf("收敛同步: %v", err)
	}
	if rep.Employees.Deactivated != 1 {
		t.Fatalf("应软删 1 名消失人员，实际 %+v", rep.Employees)
	}
	if rep.Depts.Deactivated != 1 {
		t.Fatalf("应软删 1 个消失部门（行政部），实际 %+v", rep.Depts)
	}
	if countLDAP(t, db, "employees") != empBefore-1 {
		t.Fatal("人员收敛数不符")
	}
}

// 模拟 SSO 先自动开通了同一人的账号（ext_source=”, wx_uid 已占用全局唯一索引），
// 再跑 LDAP 同步：应「认领」该账号而非新建，避免 uq_users_wx_uid 冲突。
func TestApplyAdoptsSSOProvisionedUser(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()

	// SSO 自动开通的账号：username=wx_chen、wx_uid=chen、ext_source=''、保留管理员位
	pre := &userRow{
		ID: idgen.NextID(), Username: "wx_chen", Name: "陈嘉生", PasswordHash: "",
		WxUID: "chen", OrgPath: "/sso-unassigned", IsActive: true,
	}
	if _, err := db.NewInsert().Model(pre).Exec(ctx); err != nil {
		t.Fatalf("预置 SSO 账号: %v", err)
	}
	// 顺带置 is_admin=true，验证认领不抹掉既有权限
	if _, err := db.NewUpdate().Table("users").Set("is_admin = true").Where("id = ?", pre.ID).Exec(ctx); err != nil {
		t.Fatalf("置管理员: %v", err)
	}

	rep, err := Apply(ctx, db, sampleSnapshot(), false)
	if err != nil {
		t.Fatalf("同步（应认领而非冲突）: %v", err)
	}
	// chen 被认领（更新），不应新建第二个 wx_uid=chen 的账号
	var n int
	n, _ = db.NewSelect().Table("users").Where("wx_uid = 'chen' AND deleted_at IS NULL").Count(ctx)
	if n != 1 {
		t.Fatalf("wx_uid=chen 应只有 1 行（认领），实际 %d", n)
	}
	// 认领后：ext_source 变 ldap、username 变 LDAP uid、is_admin 保留
	adopted := new(userRow)
	if err := db.NewSelect().Model(adopted).Where("id = ?", pre.ID).Scan(ctx); err != nil {
		t.Fatalf("读认领账号: %v", err)
	}
	if adopted.ExtSource != "ldap" || adopted.Username != "chen" {
		t.Fatalf("认领后应转 LDAP 托管且用 LDAP uid: %+v", adopted)
	}
	var isAdmin bool
	_ = db.NewSelect().Table("users").Column("is_admin").Where("id = ?", pre.ID).Scan(ctx, &isAdmin)
	if !isAdmin {
		t.Error("认领不应抹掉既有 is_admin 权限")
	}
	_ = rep
}

func TestApplyDryRunNoWrite(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()

	rep, err := Apply(ctx, db, sampleSnapshot(), true)
	if err != nil {
		t.Fatalf("dry-run: %v", err)
	}
	if !rep.DryRun || rep.Depts.Created != 4 {
		t.Fatalf("dry-run 应报告将建 4 部门: %+v", rep)
	}
	// 但库里什么都没有
	if countLDAP(t, db, "departments") != 0 || countLDAP(t, db, "employees") != 0 {
		t.Fatal("dry-run 不应落库")
	}
}
