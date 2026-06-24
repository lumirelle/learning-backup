package employee

import (
	"context"
	"time"

	"github.com/uptrace/bun"

	"orghr/internal/infra/idgen"
)

// JobHistory 映射 employee_job_history（任职履历快照）。
type JobHistory struct {
	bun.BaseModel `bun:"table:employee_job_history,alias:jh"`

	ID            int64          `bun:"id,pk" json:"id,string"`
	EmployeeID    int64          `bun:"employee_id" json:"employee_id,string"`
	ChangeType    string         `bun:"change_type" json:"change_type"`
	FromSnapshot  map[string]any `bun:"from_snapshot,type:jsonb" json:"from"`
	ToSnapshot    map[string]any `bun:"to_snapshot,type:jsonb" json:"to"`
	ProcessID     int64          `bun:"process_id,nullzero" json:"process_id,string,omitempty"`
	EffectiveDate *time.Time     `bun:"effective_date,nullzero" json:"effective_date,omitempty"`
	Remark        string         `bun:"remark" json:"remark"`
	CreatedAt     time.Time      `bun:"created_at,nullzero,default:now()" json:"created_at"`
}

// 以下方法均接收 bun.IDB（*bun.DB 或 bun.Tx 都满足），
// 以便人事流程在同一事务内驱动员工主数据 + 履历 + 动态的一致变更。

// GetTx 在给定 DB/Tx 上读取在职员工。
func (s *Service) GetTx(ctx context.Context, idb bun.IDB, id int64) (*Employee, error) {
	e := new(Employee)
	if err := idb.NewSelect().Model(e).Where("id = ? AND deleted_at IS NULL", id).Limit(1).Scan(ctx); err != nil {
		return nil, err
	}
	return e, nil
}

// CreateTx 插入员工（缺 ID 时分配 Snowflake）。
func (s *Service) CreateTx(ctx context.Context, idb bun.IDB, e *Employee) error {
	if e.ID == 0 {
		e.ID = idgen.NextID()
	}
	_, err := idb.NewInsert().Model(e).Exec(ctx)
	return err
}

// SaveTx 全字段更新员工（按主键）。
func (s *Service) SaveTx(ctx context.Context, idb bun.IDB, e *Employee) error {
	e.UpdatedAt = time.Now()
	_, err := idb.NewUpdate().Model(e).WherePK().Exec(ctx)
	return err
}

// AddJobHistory 追加任职履历。
func (s *Service) AddJobHistory(ctx context.Context, idb bun.IDB, h *JobHistory) error {
	if h.ID == 0 {
		h.ID = idgen.NextID()
	}
	_, err := idb.NewInsert().Model(h).Exec(ctx)
	return err
}

// AddEvent 追加人事动态事件。
func (s *Service) AddEvent(ctx context.Context, idb bun.IDB, ev *Event) error {
	if ev.ID == 0 {
		ev.ID = idgen.NextID()
	}
	if ev.OccurredAt.IsZero() {
		ev.OccurredAt = time.Now()
	}
	_, err := idb.NewInsert().Model(ev).Exec(ctx)
	return err
}

// Snapshot 抽取员工任职关键字段，用于履历前后对比。
func Snapshot(e *Employee) map[string]any {
	return map[string]any{
		"dept_id":           e.DeptID,
		"dept_name":         e.DeptName,
		"position_id":       e.PositionID,
		"position_name":     e.PositionName,
		"job_level":         e.JobLevel,
		"manager_id":        e.ManagerID,
		"org_path":          e.OrgPath,
		"employment_status": e.EmploymentStatus,
	}
}
