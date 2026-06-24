package process_test

import (
	"context"
	"testing"

	"orghr/internal/approval"
	"orghr/internal/common"
	"orghr/internal/employee"
	"orghr/internal/infra/idgen"
	"orghr/internal/middleware"
	"orghr/internal/process"
	"orghr/internal/testutil"
)

// TestTransferEffectAtomic 验证调动流程：发起 → 非审批人被拒 → 审批人通过即在同一事务内
// 落地（员工主数据 + 任职履历 + 人事动态），且终态后不可重复审批。
func TestTransferEffectAtomic(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()

	empSvc := employee.NewService(employee.NewRepo(db))
	svc := process.NewService(db, approval.New(), empSvc)

	// 一名研发部员工。
	e := &employee.Employee{
		ID: idgen.NextID(), EmployeeNo: "T001", Name: "调动测试", OrgID: 1,
		OrgPath: "/hq/tech/rd", DeptName: "研发部", JobLevel: "P4", EmploymentStatus: "active",
	}
	if _, err := db.NewInsert().Model(e).Exec(ctx); err != nil {
		t.Fatalf("seed emp: %v", err)
	}

	const applicant int64 = 100
	const approver int64 = 200
	actorApplicant := &middleware.Principal{UserID: applicant, OrgPath: "/hq/tech/rd"}
	actorApprover := &middleware.Principal{UserID: approver, OrgPath: "/hq/tech"}
	actorStranger := &middleware.Principal{UserID: 999}

	p, err := svc.Create(ctx, process.CreateInput{
		Type:       "transfer",
		EmployeeID: e.ID,
		Payload: map[string]any{
			"dept_name": "产品部", "job_level": "P5", "org_path": "/hq/tech/pd", "reason": "业务调整",
		},
		ApproverIDs: []int64{approver},
	}, actorApplicant)
	if err != nil {
		t.Fatalf("create: %v", err)
	}
	if p.Status != "pending" {
		t.Fatalf("新建流程应 pending，实际 %s", p.Status)
	}

	// 非审批人审批应被拒（1202），且不产生任何副作用。
	if _, err := svc.Approve(ctx, p.ID, actorStranger, ""); !isCode(err, 1202) {
		t.Fatalf("非审批人审批应 1202，实际 %v", err)
	}
	cur, _ := empSvc.GetTx(ctx, db, e.ID)
	if cur.DeptName != "研发部" {
		t.Fatalf("被拒审批不应改动员工，实际部门=%s", cur.DeptName)
	}

	// 审批人通过 → 落地。
	done, err := svc.Approve(ctx, p.ID, actorApprover, "同意")
	if err != nil {
		t.Fatalf("approve: %v", err)
	}
	if done.Status != "effective" || done.EffectiveDate == nil {
		t.Fatalf("通过后应 effective 且有生效日期，实际 status=%s date=%v", done.Status, done.EffectiveDate)
	}

	// 1) 员工主数据已更新。
	cur, _ = empSvc.GetTx(ctx, db, e.ID)
	if cur.DeptName != "产品部" || cur.JobLevel != "P5" || cur.OrgPath != "/hq/tech/pd" {
		t.Fatalf("员工未按 payload 更新: %+v", cur)
	}

	// 2) 任职履历新增一条 transfer。
	jhCount, _ := db.NewSelect().Table("employee_job_history").
		Where("employee_id = ? AND change_type = ?", e.ID, "transfer").Count(ctx)
	if jhCount != 1 {
		t.Fatalf("应新增 1 条调动履历，实际 %d", jhCount)
	}

	// 3) 人事动态新增一条 transfer 事件。
	evCount, _ := db.NewSelect().Table("hr_events").
		Where("employee_id = ? AND event_type = ?", e.ID, "transfer").Count(ctx)
	if evCount != 1 {
		t.Fatalf("应新增 1 条调动事件，实际 %d", evCount)
	}

	// 终态后再次审批应被拒（1201）。
	if _, err := svc.Approve(ctx, p.ID, actorApprover, ""); !isCode(err, 1201) {
		t.Fatalf("终态流程再审批应 1201，实际 %v", err)
	}
}

// TestOnboardCreatesEmployee 验证入职流程通过后会创建员工并写动态。
func TestOnboardCreatesEmployee(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()

	empSvc := employee.NewService(employee.NewRepo(db))
	svc := process.NewService(db, approval.New(), empSvc)

	const approver int64 = 200
	p, err := svc.Create(ctx, process.CreateInput{
		Type: "onboard",
		Payload: map[string]any{
			"employee_no": "NEW001", "name": "新人", "org_path": "/hq/hr", "dept_name": "人力资源部",
		},
		ApproverIDs: []int64{approver},
	}, &middleware.Principal{UserID: 100, OrgPath: "/hq"})
	if err != nil {
		t.Fatalf("create: %v", err)
	}

	done, err := svc.Approve(ctx, p.ID, &middleware.Principal{UserID: approver}, "")
	if err != nil {
		t.Fatalf("approve: %v", err)
	}
	if done.EmployeeID == 0 {
		t.Fatalf("入职通过后流程应回填 employee_id")
	}
	cnt, _ := db.NewSelect().Table("employees").Where("employee_no = ?", "NEW001").Count(ctx)
	if cnt != 1 {
		t.Fatalf("应创建 1 名员工，实际 %d", cnt)
	}
}

// TestRejectNoEffect 验证驳回不产生任何业务副作用。
func TestRejectNoEffect(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()

	empSvc := employee.NewService(employee.NewRepo(db))
	svc := process.NewService(db, approval.New(), empSvc)

	e := &employee.Employee{ID: idgen.NextID(), EmployeeNo: "R1", Name: "驳回测试", OrgID: 1, OrgPath: "/hq", DeptName: "研发部", EmploymentStatus: "active"}
	_, _ = db.NewInsert().Model(e).Exec(ctx)

	p, _ := svc.Create(ctx, process.CreateInput{
		Type: "transfer", EmployeeID: e.ID,
		Payload:     map[string]any{"dept_name": "产品部"},
		ApproverIDs: []int64{200},
	}, &middleware.Principal{UserID: 100, OrgPath: "/hq"})

	got, err := svc.Reject(ctx, p.ID, &middleware.Principal{UserID: 200}, "不通过")
	if err != nil {
		t.Fatalf("reject: %v", err)
	}
	if got.Status != "rejected" {
		t.Fatalf("应 rejected，实际 %s", got.Status)
	}
	cur, _ := empSvc.GetTx(ctx, db, e.ID)
	if cur.DeptName != "研发部" {
		t.Fatalf("驳回不应改动员工，实际部门=%s", cur.DeptName)
	}
}

func isCode(err error, code int) bool {
	ae, ok := err.(*common.AppError)
	return ok && ae.Code == code
}
