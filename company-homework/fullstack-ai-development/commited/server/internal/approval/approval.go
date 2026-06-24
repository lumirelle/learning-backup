// Package approval 是通用串行多级审批引擎（实例 + 步骤）。
// 所有方法接收 bun.IDB，便于在业务事务内被调用。
package approval

import (
	"context"
	"time"

	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/infra/idgen"
)

// Instance 审批实例。
type Instance struct {
	bun.BaseModel `bun:"table:approval_instances,alias:ai"`

	ID          int64     `bun:"id,pk" json:"id,string"`
	BizType     string    `bun:"biz_type" json:"biz_type"`
	BizID       int64     `bun:"biz_id" json:"biz_id,string"`
	Status      string    `bun:"status" json:"status"`
	CurrentStep int       `bun:"current_step" json:"current_step"`
	TotalSteps  int       `bun:"total_steps" json:"total_steps"`
	SubmittedBy int64     `bun:"submitted_by" json:"submitted_by,string"`
	CreatedAt   time.Time `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt   time.Time `bun:"updated_at,nullzero,default:now()" json:"updated_at"`
	Steps       []*Step   `bun:"-" json:"steps,omitempty"`
}

// Step 审批步骤。
type Step struct {
	bun.BaseModel `bun:"table:approval_steps,alias:asp"`

	ID         int64      `bun:"id,pk" json:"id,string"`
	InstanceID int64      `bun:"instance_id" json:"instance_id,string"`
	StepNo     int        `bun:"step_no" json:"step_no"`
	ApproverID int64      `bun:"approver_id" json:"approver_id,string"`
	Action     string     `bun:"action" json:"action"`
	Comment    string     `bun:"comment" json:"comment"`
	ActedAt    *time.Time `bun:"acted_at,nullzero" json:"acted_at,omitempty"`
	CreatedAt  time.Time  `bun:"created_at,nullzero,default:now()" json:"created_at"`
}

// Engine 审批引擎（无状态）。
type Engine struct{}

// New 构造引擎。
func New() *Engine { return &Engine{} }

// Create 按审批人顺序创建实例与步骤。
func (e *Engine) Create(ctx context.Context, idb bun.IDB, bizType string, bizID, submittedBy int64, approverIDs []int64) (*Instance, error) {
	if len(approverIDs) == 0 {
		return nil, common.NewError(400, 1002, "至少需要一个审批人")
	}
	inst := &Instance{
		ID: idgen.NextID(), BizType: bizType, BizID: bizID, Status: "pending",
		CurrentStep: 1, TotalSteps: len(approverIDs), SubmittedBy: submittedBy,
	}
	if _, err := idb.NewInsert().Model(inst).Exec(ctx); err != nil {
		return nil, err
	}
	steps := make([]*Step, 0, len(approverIDs))
	for i, aid := range approverIDs {
		steps = append(steps, &Step{ID: idgen.NextID(), InstanceID: inst.ID, StepNo: i + 1, ApproverID: aid, Action: "pending"})
	}
	if _, err := idb.NewInsert().Model(&steps).Exec(ctx); err != nil {
		return nil, err
	}
	inst.Steps = steps
	return inst, nil
}

// Get 读取实例（含步骤）。
func (e *Engine) Get(ctx context.Context, idb bun.IDB, instanceID int64) (*Instance, error) {
	inst := new(Instance)
	if err := idb.NewSelect().Model(inst).Where("id = ?", instanceID).Limit(1).Scan(ctx); err != nil {
		return nil, common.ErrNotFound
	}
	steps, err := e.Steps(ctx, idb, instanceID)
	if err != nil {
		return nil, err
	}
	inst.Steps = steps
	return inst, nil
}

// Steps 读取实例的步骤（按顺序）。
func (e *Engine) Steps(ctx context.Context, idb bun.IDB, instanceID int64) ([]*Step, error) {
	var l []*Step
	err := idb.NewSelect().Model(&l).Where("instance_id = ?", instanceID).Order("step_no").Scan(ctx)
	return l, err
}

// Approve 当前审批人通过当前步骤；返回 done=true 表示全部通过。
// isAdmin 为 true 时可代任意步骤审批（越权审批由上层决定是否允许）。
func (e *Engine) Approve(ctx context.Context, idb bun.IDB, instanceID, actorID int64, isAdmin bool, comment string) (done bool, inst *Instance, err error) {
	inst = new(Instance)
	if err = idb.NewSelect().Model(inst).Where("id = ?", instanceID).Limit(1).Scan(ctx); err != nil {
		return false, nil, common.ErrNotFound
	}
	if inst.Status != "pending" {
		return false, inst, common.NewError(409, 1203, "审批已结束")
	}
	step := new(Step)
	if err = idb.NewSelect().Model(step).
		Where("instance_id = ? AND step_no = ?", instanceID, inst.CurrentStep).Limit(1).Scan(ctx); err != nil {
		return false, inst, common.ErrNotFound
	}
	if !isAdmin && step.ApproverID != actorID {
		return false, inst, common.NewError(403, 1202, "非当前审批人")
	}
	now := time.Now()
	step.Action = "approved"
	step.Comment = comment
	step.ActedAt = &now
	if _, err = idb.NewUpdate().Model(step).Column("action", "comment", "acted_at").WherePK().Exec(ctx); err != nil {
		return false, inst, err
	}
	if inst.CurrentStep >= inst.TotalSteps {
		inst.Status = "approved"
		inst.UpdatedAt = now
		_, err = idb.NewUpdate().Model(inst).Column("status", "updated_at").WherePK().Exec(ctx)
		return true, inst, err
	}
	inst.CurrentStep++
	inst.UpdatedAt = now
	_, err = idb.NewUpdate().Model(inst).Column("current_step", "updated_at").WherePK().Exec(ctx)
	return false, inst, err
}

// Reject 驳回当前步骤，整单置为 rejected。
func (e *Engine) Reject(ctx context.Context, idb bun.IDB, instanceID, actorID int64, isAdmin bool, comment string) (*Instance, error) {
	inst := new(Instance)
	if err := idb.NewSelect().Model(inst).Where("id = ?", instanceID).Limit(1).Scan(ctx); err != nil {
		return nil, common.ErrNotFound
	}
	if inst.Status != "pending" {
		return inst, common.NewError(409, 1203, "审批已结束")
	}
	step := new(Step)
	if err := idb.NewSelect().Model(step).
		Where("instance_id = ? AND step_no = ?", instanceID, inst.CurrentStep).Limit(1).Scan(ctx); err != nil {
		return inst, common.ErrNotFound
	}
	if !isAdmin && step.ApproverID != actorID {
		return inst, common.NewError(403, 1202, "非当前审批人")
	}
	now := time.Now()
	step.Action = "rejected"
	step.Comment = comment
	step.ActedAt = &now
	if _, err := idb.NewUpdate().Model(step).Column("action", "comment", "acted_at").WherePK().Exec(ctx); err != nil {
		return inst, err
	}
	inst.Status = "rejected"
	inst.UpdatedAt = now
	_, err := idb.NewUpdate().Model(inst).Column("status", "updated_at").WherePK().Exec(ctx)
	return inst, err
}

// TodoInstanceIDs 返回「当前步骤待 approverID 审批」的实例 id。
func (e *Engine) TodoInstanceIDs(ctx context.Context, idb bun.IDB, approverID int64) ([]int64, error) {
	var ids []int64
	err := idb.NewSelect().
		ColumnExpr("ai.id").
		TableExpr("approval_instances AS ai").
		Join("JOIN approval_steps AS s ON s.instance_id = ai.id AND s.step_no = ai.current_step").
		Where("ai.status = ?", "pending").
		Where("s.approver_id = ? AND s.action = ?", approverID, "pending").
		Scan(ctx, &ids)
	return ids, err
}
