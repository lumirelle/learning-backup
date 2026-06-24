package contract_test

import (
	"context"
	"testing"
	"time"

	"orghr/internal/common"
	"orghr/internal/contract"
	"orghr/internal/employee"
	"orghr/internal/infra/idgen"
	"orghr/internal/testutil"
)

func ptrDate(s string) *time.Time {
	t, _ := time.Parse("2006-01-02", s)
	return &t
}

func TestContractRenew(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()
	svc := contract.NewService(db)

	empID := idgen.NextID()
	if _, err := db.NewInsert().Model(&employee.Employee{
		ID: empID, EmployeeNo: "C1", Name: "合同员工", OrgID: 1, OrgPath: "/hq", EmploymentStatus: "active",
	}).Exec(ctx); err != nil {
		t.Fatalf("seed emp: %v", err)
	}

	old, err := svc.Create(ctx, &contract.Contract{
		EmployeeID: empID, StartDate: ptrDate("2024-01-01"), EndDate: ptrDate("2025-01-01"), SalaryBand: "B3",
	})
	if err != nil {
		t.Fatalf("create: %v", err)
	}
	if old.Status != "active" || old.ContractNo == "" {
		t.Fatalf("新合同应 active 且有编号: %+v", old)
	}

	fresh, err := svc.Renew(ctx, old.ID, ptrDate("2026-01-01"), "")
	if err != nil {
		t.Fatalf("renew: %v", err)
	}
	// 新合同关联上一份、继承薪资带、起始日 = 原到期次日。
	if fresh.PrevContractID != old.ID {
		t.Fatalf("新合同应链接上一份 %d，实际 %d", old.ID, fresh.PrevContractID)
	}
	if fresh.SalaryBand != "B3" {
		t.Fatalf("应继承薪资带 B3，实际 %s", fresh.SalaryBand)
	}
	if fresh.StartDate == nil || fresh.StartDate.Format("2006-01-02") != "2025-01-02" {
		t.Fatalf("新合同起始日应为原到期次日 2025-01-02，实际 %v", fresh.StartDate)
	}
	// 原合同应置为 renewed。
	reloaded, _ := svc.Get(ctx, old.ID)
	if reloaded.Status != "renewed" {
		t.Fatalf("原合同应 renewed，实际 %s", reloaded.Status)
	}
	// 已续签合同不可再续签。
	if _, err := svc.Renew(ctx, old.ID, ptrDate("2027-01-01"), ""); !isCode(err, 1201) {
		t.Fatalf("非在用合同续签应 1201，实际 %v", err)
	}
}

func TestContractTerminate(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()
	svc := contract.NewService(db)

	empID := idgen.NextID()
	_, _ = db.NewInsert().Model(&employee.Employee{ID: empID, EmployeeNo: "C2", Name: "x", OrgID: 1, OrgPath: "/hq", EmploymentStatus: "active"}).Exec(ctx)
	c, _ := svc.Create(ctx, &contract.Contract{EmployeeID: empID, StartDate: ptrDate("2024-01-01"), EndDate: ptrDate("2025-01-01")})

	got, err := svc.Terminate(ctx, c.ID)
	if err != nil {
		t.Fatalf("terminate: %v", err)
	}
	if got.Status != "terminated" {
		t.Fatalf("应 terminated，实际 %s", got.Status)
	}
	if _, err := svc.Terminate(ctx, c.ID); !isCode(err, 1201) {
		t.Fatalf("非在用合同终止应 1201，实际 %v", err)
	}
}

func TestContractCreateValidation(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()
	svc := contract.NewService(db)

	empID := idgen.NextID()
	_, _ = db.NewInsert().Model(&employee.Employee{ID: empID, EmployeeNo: "C3", Name: "x", OrgID: 1, OrgPath: "/hq", EmploymentStatus: "active"}).Exec(ctx)

	// 缺员工。
	if _, err := svc.Create(ctx, &contract.Contract{StartDate: ptrDate("2024-01-01")}); !isCode(err, 1002) {
		t.Fatalf("缺员工应 1002，实际 %v", err)
	}
	// 终止日早于起始日。
	if _, err := svc.Create(ctx, &contract.Contract{EmployeeID: empID, StartDate: ptrDate("2025-01-01"), EndDate: ptrDate("2024-01-01")}); !isCode(err, 1303) {
		t.Fatalf("期限非法应 1303，实际 %v", err)
	}
}

func TestContractExpiringReminder(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()
	svc := contract.NewService(db)

	empID := idgen.NextID()
	_, _ = db.NewInsert().Model(&employee.Employee{ID: empID, EmployeeNo: "C4", Name: "x", OrgID: 1, OrgPath: "/hq", EmploymentStatus: "active"}).Exec(ctx)

	soon := time.Now().AddDate(0, 0, 10).Format("2006-01-02")
	far := time.Now().AddDate(0, 0, 200).Format("2006-01-02")
	_, _ = svc.Create(ctx, &contract.Contract{EmployeeID: empID, StartDate: ptrDate("2024-01-01"), EndDate: ptrDate(soon)})
	_, _ = svc.Create(ctx, &contract.Contract{EmployeeID: empID, StartDate: ptrDate("2024-01-01"), EndDate: ptrDate(far)})

	list, err := svc.Reminders(ctx, 30, "")
	if err != nil {
		t.Fatalf("reminders: %v", err)
	}
	if len(list) != 1 {
		t.Fatalf("30 天内到期应只命中 1 份，实际 %d", len(list))
	}
	if list[0].DaysLeft == nil || *list[0].DaysLeft < 0 || *list[0].DaysLeft > 30 {
		t.Fatalf("剩余天数应在 [0,30]，实际 %v", list[0].DaysLeft)
	}
}

func isCode(err error, code int) bool {
	ae, ok := err.(*common.AppError)
	return ok && ae.Code == code
}
