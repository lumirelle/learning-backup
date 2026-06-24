// Package process 实现入离调转/转正的统一人事流程单：
// 发起 → 审批（通用审批引擎）→ 通过即在一个事务内「生效」，
// 同步更新 员工主数据 + 任职履历 + 人事动态，形成端到端闭环。
package process

import (
	"context"
	"fmt"
	"strconv"
	"time"

	"github.com/uptrace/bun"

	"orghr/internal/approval"
	"orghr/internal/common"
	"orghr/internal/employee"
	"orghr/internal/infra/idgen"
	"orghr/internal/middleware"
)

var validTypes = map[string]bool{"onboard": true, "offboard": true, "transfer": true, "regularize": true}

// HrProcess 映射 hr_processes 表。
type HrProcess struct {
	bun.BaseModel `bun:"table:hr_processes,alias:hp"`

	ID            int64              `bun:"id,pk" json:"id,string"`
	ProcessNo     string             `bun:"process_no" json:"process_no"`
	Type          string             `bun:"type" json:"type"`
	EmployeeID    int64              `bun:"employee_id,nullzero" json:"employee_id,string,omitempty"`
	ApplicantID   int64              `bun:"applicant_id" json:"applicant_id,string"`
	OrgID         int64              `bun:"org_id" json:"org_id,string"`
	OrgPath       string             `bun:"org_path" json:"org_path"`
	Payload       map[string]any     `bun:"payload,type:jsonb" json:"payload"`
	Status        string             `bun:"status" json:"status"`
	EffectiveDate *time.Time         `bun:"effective_date,nullzero" json:"effective_date,omitempty"`
	Result        map[string]any     `bun:"result,type:jsonb" json:"result,omitempty"`
	ApprovalID    int64              `bun:"approval_id,nullzero" json:"approval_id,string,omitempty"`
	CreatedAt     time.Time          `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt     time.Time          `bun:"updated_at,nullzero,default:now()" json:"updated_at"`
	Approval      *approval.Instance `bun:"-" json:"approval,omitempty"`
}

// CreateInput 发起流程的入参。
type CreateInput struct {
	Type        string
	EmployeeID  int64
	Payload     map[string]any
	ApproverIDs []int64
}

// Service 人事流程业务。
type Service struct {
	db   *bun.DB
	appr *approval.Engine
	emp  *employee.Service
}

// NewService 构造 service。
func NewService(db *bun.DB, appr *approval.Engine, emp *employee.Service) *Service {
	return &Service{db: db, appr: appr, emp: emp}
}

// Create 发起并提交流程（创建流程单 + 审批实例，状态 pending）。
func (s *Service) Create(ctx context.Context, in CreateInput, actor *middleware.Principal) (*HrProcess, error) {
	if !validTypes[in.Type] {
		return nil, common.NewError(400, 1002, "未知流程类型")
	}
	if in.Type != "onboard" && in.EmployeeID == 0 {
		return nil, common.NewError(400, 1002, "缺少员工")
	}
	if len(in.ApproverIDs) == 0 {
		return nil, common.NewError(400, 1002, "至少需要一个审批人")
	}
	p := &HrProcess{
		ID: idgen.NextID(), Type: in.Type, EmployeeID: in.EmployeeID,
		ApplicantID: actor.UserID, OrgPath: actor.OrgPath, Payload: in.Payload, Status: "pending",
	}
	p.ProcessNo = fmt.Sprintf("HP-%s-%d", typeCode(in.Type), p.ID)

	err := s.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		if _, e := tx.NewInsert().Model(p).Exec(ctx); e != nil {
			return e
		}
		inst, e := s.appr.Create(ctx, tx, "process", p.ID, actor.UserID, in.ApproverIDs)
		if e != nil {
			return e
		}
		p.ApprovalID = inst.ID
		p.Approval = inst
		_, e = tx.NewUpdate().Model(p).Column("approval_id").WherePK().Exec(ctx)
		return e
	})
	if err != nil {
		return nil, err
	}
	return p, nil
}

// Approve 审批通过当前步骤；最后一步通过则在同一事务内生效。
func (s *Service) Approve(ctx context.Context, processID int64, actor *middleware.Principal, comment string) (*HrProcess, error) {
	var out *HrProcess
	err := s.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		p := new(HrProcess)
		if e := tx.NewSelect().Model(p).Where("id = ?", processID).Limit(1).Scan(ctx); e != nil {
			return common.ErrNotFound
		}
		if p.Status != "pending" {
			return common.NewError(409, 1201, "流程状态不允许此操作")
		}
		done, _, e := s.appr.Approve(ctx, tx, p.ApprovalID, actor.UserID, actor.IsAdmin, comment)
		if e != nil {
			return e
		}
		if done {
			if e := s.applyEffect(ctx, tx, p, actor); e != nil {
				return e
			}
			now := time.Now()
			p.Status = "effective"
			p.EffectiveDate = &now
			p.UpdatedAt = now
			if _, e := tx.NewUpdate().Model(p).Column("status", "effective_date", "employee_id", "updated_at").WherePK().Exec(ctx); e != nil {
				return e
			}
		}
		out = p
		return nil
	})
	if err != nil {
		return nil, err
	}
	return s.Get(ctx, out.ID)
}

// Reject 驳回流程。
func (s *Service) Reject(ctx context.Context, processID int64, actor *middleware.Principal, comment string) (*HrProcess, error) {
	err := s.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		p := new(HrProcess)
		if e := tx.NewSelect().Model(p).Where("id = ?", processID).Limit(1).Scan(ctx); e != nil {
			return common.ErrNotFound
		}
		if p.Status != "pending" {
			return common.NewError(409, 1201, "流程状态不允许此操作")
		}
		if _, e := s.appr.Reject(ctx, tx, p.ApprovalID, actor.UserID, actor.IsAdmin, comment); e != nil {
			return e
		}
		p.Status = "rejected"
		p.UpdatedAt = time.Now()
		_, e := tx.NewUpdate().Model(p).Column("status", "updated_at").WherePK().Exec(ctx)
		return e
	})
	if err != nil {
		return nil, err
	}
	return s.Get(ctx, processID)
}

// applyEffect 将通过的流程落地到员工主数据 + 履历 + 动态（同一事务内）。
func (s *Service) applyEffect(ctx context.Context, tx bun.Tx, p *HrProcess, actor *middleware.Principal) error {
	now := time.Now()
	switch p.Type {
	case "transfer":
		emp, err := s.emp.GetTx(ctx, tx, p.EmployeeID)
		if err != nil {
			return common.NewError(400, 1003, "员工不存在")
		}
		from := employee.Snapshot(emp)
		applyPlacement(emp, p.Payload)
		if err := s.emp.SaveTx(ctx, tx, emp); err != nil {
			return err
		}
		if err := s.emp.AddJobHistory(ctx, tx, &employee.JobHistory{
			EmployeeID: emp.ID, ChangeType: "transfer", FromSnapshot: from, ToSnapshot: employee.Snapshot(emp),
			ProcessID: p.ID, EffectiveDate: &now, Remark: payloadStr(p.Payload, "reason"),
		}); err != nil {
			return err
		}
		return s.emp.AddEvent(ctx, tx, &employee.Event{
			EmployeeID: emp.ID, EventType: "transfer", Title: emp.Name + " 岗位调动至 " + emp.DeptName,
			OrgPath: emp.OrgPath, ActorID: actor.UserID, OccurredAt: now,
		})

	case "regularize":
		emp, err := s.emp.GetTx(ctx, tx, p.EmployeeID)
		if err != nil {
			return common.NewError(400, 1003, "员工不存在")
		}
		from := employee.Snapshot(emp)
		emp.EmploymentStatus = "active"
		emp.RegularAt = &now
		if err := s.emp.SaveTx(ctx, tx, emp); err != nil {
			return err
		}
		if err := s.emp.AddJobHistory(ctx, tx, &employee.JobHistory{
			EmployeeID: emp.ID, ChangeType: "regularize", FromSnapshot: from, ToSnapshot: employee.Snapshot(emp),
			ProcessID: p.ID, EffectiveDate: &now,
		}); err != nil {
			return err
		}
		return s.emp.AddEvent(ctx, tx, &employee.Event{
			EmployeeID: emp.ID, EventType: "regularize", Title: emp.Name + " 转正",
			OrgPath: emp.OrgPath, ActorID: actor.UserID, OccurredAt: now,
		})

	case "offboard":
		emp, err := s.emp.GetTx(ctx, tx, p.EmployeeID)
		if err != nil {
			return common.NewError(400, 1003, "员工不存在")
		}
		from := employee.Snapshot(emp)
		emp.EmploymentStatus = "left"
		emp.LeftAt = &now
		if err := s.emp.SaveTx(ctx, tx, emp); err != nil {
			return err
		}
		if err := s.emp.AddJobHistory(ctx, tx, &employee.JobHistory{
			EmployeeID: emp.ID, ChangeType: "leave", FromSnapshot: from, ToSnapshot: employee.Snapshot(emp),
			ProcessID: p.ID, EffectiveDate: &now, Remark: payloadStr(p.Payload, "reason"),
		}); err != nil {
			return err
		}
		return s.emp.AddEvent(ctx, tx, &employee.Event{
			EmployeeID: emp.ID, EventType: "leave", Title: emp.Name + " 离职",
			OrgPath: emp.OrgPath, ActorID: actor.UserID, OccurredAt: now,
		})

	case "onboard":
		emp := &employee.Employee{
			EmployeeNo:       payloadStr(p.Payload, "employee_no"),
			Name:             payloadStr(p.Payload, "name"),
			Gender:           payloadStr(p.Payload, "gender"),
			Phone:            payloadStr(p.Payload, "phone"),
			WorkEmail:        payloadStr(p.Payload, "work_email"),
			Education:        payloadStr(p.Payload, "education"),
			OrgID:            payloadInt64(p.Payload, "org_id"),
			OrgPath:          payloadStr(p.Payload, "org_path"),
			DeptID:           payloadInt64(p.Payload, "dept_id"),
			DeptName:         payloadStr(p.Payload, "dept_name"),
			PositionID:       payloadInt64(p.Payload, "position_id"),
			PositionName:     payloadStr(p.Payload, "position_name"),
			JobLevel:         payloadStr(p.Payload, "job_level"),
			EmploymentType:   "full_time",
			EmploymentStatus: "probation",
			HiredAt:          &now,
		}
		emp.ID = idgen.NextID()
		if emp.EmployeeNo == "" {
			emp.EmployeeNo = fmt.Sprintf("EMP%d", emp.ID%1000000)
		}
		if err := s.emp.CreateTx(ctx, tx, emp); err != nil {
			return common.NewError(409, 1302, "工号重复或入职信息无效")
		}
		p.EmployeeID = emp.ID // 回填，供外层更新 employee_id
		return s.emp.AddEvent(ctx, tx, &employee.Event{
			EmployeeID: emp.ID, EventType: "onboard", Title: emp.Name + " 入职 " + emp.DeptName,
			OrgPath: emp.OrgPath, ActorID: actor.UserID, OccurredAt: now,
		})
	}
	return common.NewError(400, 1002, "未知流程类型")
}

// Get 读取流程详情（含审批轨迹）。
func (s *Service) Get(ctx context.Context, id int64) (*HrProcess, error) {
	p := new(HrProcess)
	if err := s.db.NewSelect().Model(p).Where("id = ?", id).Limit(1).Scan(ctx); err != nil {
		return nil, common.ErrNotFound
	}
	if p.ApprovalID != 0 {
		if inst, err := s.appr.Get(ctx, s.db, p.ApprovalID); err == nil {
			p.Approval = inst
		}
	}
	return p, nil
}

// ListFilter 流程列表筛选。
type ListFilter struct {
	Type   string
	Status string
	Scope  string // 非空时按 org_path 子树做数据权限限制
	Page   int
	Size   int
}

// List 分页查询流程。
func (s *Service) List(ctx context.Context, f ListFilter) ([]*HrProcess, int, error) {
	var list []*HrProcess
	q := s.db.NewSelect().Model(&list)
	if f.Type != "" {
		q = q.Where("type = ?", f.Type)
	}
	if f.Status != "" {
		q = q.Where("status = ?", f.Status)
	}
	q = common.Subtree(q, "org_path", f.Scope)
	total, err := q.Order("id DESC").Limit(f.Size).Offset((f.Page - 1) * f.Size).ScanAndCount(ctx)
	return list, total, err
}

// Todo 返回待当前用户审批的流程。
func (s *Service) Todo(ctx context.Context, me int64) ([]*HrProcess, error) {
	ids, err := s.appr.TodoInstanceIDs(ctx, s.db, me)
	if err != nil || len(ids) == 0 {
		return []*HrProcess{}, err
	}
	var list []*HrProcess
	err = s.db.NewSelect().Model(&list).Where("approval_id IN (?)", bun.In(ids)).Order("id DESC").Scan(ctx)
	return list, err
}

// Mine 返回当前用户发起的流程。
func (s *Service) Mine(ctx context.Context, me int64) ([]*HrProcess, error) {
	var list []*HrProcess
	err := s.db.NewSelect().Model(&list).Where("applicant_id = ?", me).Order("id DESC").Scan(ctx)
	return list, err
}

// ---- helpers ----

func applyPlacement(e *employee.Employee, payload map[string]any) {
	if v := payloadStr(payload, "dept_name"); v != "" {
		e.DeptName = v
	}
	if v := payloadInt64(payload, "dept_id"); v != 0 {
		e.DeptID = v
	}
	if v := payloadStr(payload, "position_name"); v != "" {
		e.PositionName = v
	}
	if v := payloadInt64(payload, "position_id"); v != 0 {
		e.PositionID = v
	}
	if v := payloadStr(payload, "job_level"); v != "" {
		e.JobLevel = v
	}
	if v := payloadInt64(payload, "manager_id"); v != 0 {
		e.ManagerID = v
	}
	if v := payloadStr(payload, "org_path"); v != "" {
		e.OrgPath = v
	}
}

func payloadStr(m map[string]any, key string) string {
	if v, ok := m[key]; ok {
		if s, ok := v.(string); ok {
			return s
		}
	}
	return ""
}

func payloadInt64(m map[string]any, key string) int64 {
	v, ok := m[key]
	if !ok {
		return 0
	}
	switch t := v.(type) {
	case float64:
		return int64(t)
	case int64:
		return t
	case string:
		n, _ := strconv.ParseInt(t, 10, 64)
		return n
	}
	return 0
}

func typeCode(t string) string {
	switch t {
	case "onboard":
		return "ON"
	case "offboard":
		return "OFF"
	case "transfer":
		return "TR"
	case "regularize":
		return "RG"
	}
	return "XX"
}
