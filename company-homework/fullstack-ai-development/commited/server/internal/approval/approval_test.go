package approval_test

import (
	"context"
	"testing"

	"orghr/internal/approval"
	"orghr/internal/common"
	"orghr/internal/testutil"
)

func TestSerialApprovalFlow(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	eng := approval.New()
	ctx := context.Background()

	const (
		submitter int64 = 100
		appr1     int64 = 201
		appr2     int64 = 202
		stranger  int64 = 999
	)

	inst, err := eng.Create(ctx, db, "process", 555, submitter, []int64{appr1, appr2})
	if err != nil {
		t.Fatalf("create: %v", err)
	}
	if inst.TotalSteps != 2 || inst.CurrentStep != 1 || inst.Status != "pending" {
		t.Fatalf("初始实例异常: %+v", inst)
	}

	// 非当前审批人不可审批（1202）。
	if _, _, err := eng.Approve(ctx, db, inst.ID, stranger, false, ""); !isCode(err, 1202) {
		t.Fatalf("陌生人审批应被拒(1202)，实际 err=%v", err)
	}
	// 第二级审批人不能越过第一级（当前步骤是 1，审批人应为 appr1）。
	if _, _, err := eng.Approve(ctx, db, inst.ID, appr2, false, ""); !isCode(err, 1202) {
		t.Fatalf("越级审批应被拒(1202)，实际 err=%v", err)
	}

	// 第一级通过 → 未结束，推进到第二级。
	done, _, err := eng.Approve(ctx, db, inst.ID, appr1, false, "ok1")
	if err != nil || done {
		t.Fatalf("第一级通过后不应 done，err=%v done=%v", err, done)
	}
	got, _ := eng.Get(ctx, db, inst.ID)
	if got.CurrentStep != 2 || got.Status != "pending" {
		t.Fatalf("应推进到第二级 pending，实际 step=%d status=%s", got.CurrentStep, got.Status)
	}

	// 第二级通过 → 整单 approved。
	done, _, err = eng.Approve(ctx, db, inst.ID, appr2, false, "ok2")
	if err != nil || !done {
		t.Fatalf("第二级通过应 done，err=%v done=%v", err, done)
	}
	got, _ = eng.Get(ctx, db, inst.ID)
	if got.Status != "approved" {
		t.Fatalf("应为 approved，实际 %s", got.Status)
	}

	// 终态后再审批应被拒（1203）。
	if _, _, err := eng.Approve(ctx, db, inst.ID, appr2, false, ""); !isCode(err, 1203) {
		t.Fatalf("终态审批应被拒(1203)，实际 err=%v", err)
	}
}

func TestRejectEndsInstance(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	eng := approval.New()
	ctx := context.Background()

	inst, err := eng.Create(ctx, db, "process", 1, 100, []int64{201, 202})
	if err != nil {
		t.Fatalf("create: %v", err)
	}
	got, err := eng.Reject(ctx, db, inst.ID, 201, false, "不同意")
	if err != nil {
		t.Fatalf("reject: %v", err)
	}
	if got.Status != "rejected" {
		t.Fatalf("应 rejected，实际 %s", got.Status)
	}
	// 驳回后不可再操作。
	if _, _, err := eng.Approve(ctx, db, inst.ID, 201, false, ""); !isCode(err, 1203) {
		t.Fatalf("驳回后审批应被拒(1203)，实际 err=%v", err)
	}
}

func TestAdminCanApproveAnyStep(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	eng := approval.New()
	ctx := context.Background()

	inst, _ := eng.Create(ctx, db, "process", 1, 100, []int64{201, 202})
	// isAdmin=true 可代任意步骤（即便 actor 非当前审批人）。
	if _, _, err := eng.Approve(ctx, db, inst.ID, 999, true, "admin"); err != nil {
		t.Fatalf("管理员审批不应被拒，实际 err=%v", err)
	}
}

func TestTodoInstanceIDs(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	eng := approval.New()
	ctx := context.Background()

	// appr1 是 inst1 的当前审批人；inst2 的当前审批人是 appr2。
	inst1, _ := eng.Create(ctx, db, "process", 1, 100, []int64{201})
	_, _ = eng.Create(ctx, db, "process", 2, 100, []int64{202})

	ids, err := eng.TodoInstanceIDs(ctx, db, 201)
	if err != nil {
		t.Fatalf("todo: %v", err)
	}
	if len(ids) != 1 || ids[0] != inst1.ID {
		t.Fatalf("appr=201 的待办应只含 inst1，实际 %v", ids)
	}
	// 通过后该实例不再出现在 201 的待办里。
	if _, _, err := eng.Approve(ctx, db, inst1.ID, 201, false, ""); err != nil {
		t.Fatalf("approve: %v", err)
	}
	ids, _ = eng.TodoInstanceIDs(ctx, db, 201)
	if len(ids) != 0 {
		t.Fatalf("通过后 201 应无待办，实际 %v", ids)
	}
}

func isCode(err error, code int) bool {
	ae, ok := err.(*common.AppError)
	return ok && ae.Code == code
}
