// Package performance 实现任职奖惩：奖励/惩罚记录，录入即写入员工动态时间线。
package performance

import (
	"context"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/employee"
	"orghr/internal/infra/idgen"
	"orghr/internal/middleware"
)

// RewardPunishment 奖惩记录。
type RewardPunishment struct {
	bun.BaseModel `bun:"table:rewards_punishments,alias:rp"`

	ID            int64      `bun:"id,pk" json:"id,string"`
	EmployeeID    int64      `bun:"employee_id" json:"employee_id,string"`
	Kind          string     `bun:"kind" json:"kind"` // reward | punishment
	Category      string     `bun:"category" json:"category"`
	Title         string     `bun:"title" json:"title"`
	Reason        string     `bun:"reason" json:"reason"`
	Amount        *float64   `bun:"amount,nullzero" json:"amount,omitempty"`
	EffectiveDate *time.Time `bun:"effective_date,nullzero" json:"effective_date,omitempty"`
	RecordedBy    int64      `bun:"recorded_by" json:"recorded_by,string"`
	CreatedAt     time.Time  `bun:"created_at,nullzero,default:now()" json:"created_at"`

	EmployeeName string `bun:"-" json:"employee_name,omitempty"`
}

// Service 奖惩业务（录入后写 hr_events，复用 employee 域）。
type Service struct {
	db  *bun.DB
	emp *employee.Service
}

// NewService 构造 service。
func NewService(db *bun.DB, emp *employee.Service) *Service {
	return &Service{db: db, emp: emp}
}

// List 查询奖惩（可按员工/类型过滤）。
func (s *Service) List(ctx context.Context, employeeID int64, kind string) ([]*RewardPunishment, error) {
	var list []*RewardPunishment
	q := s.db.NewSelect().Model(&list).Where("rp.deleted_at IS NULL")
	if employeeID != 0 {
		q = q.Where("rp.employee_id = ?", employeeID)
	}
	if kind != "" {
		q = q.Where("rp.kind = ?", kind)
	}
	if err := q.Order("rp.id DESC").Limit(200).Scan(ctx); err != nil {
		return nil, err
	}
	// 回填员工姓名
	if len(list) > 0 {
		ids := make([]int64, 0, len(list))
		for _, r := range list {
			ids = append(ids, r.EmployeeID)
		}
		var rows []struct {
			ID   int64  `bun:"id"`
			Name string `bun:"name"`
		}
		_ = s.db.NewSelect().Table("employees").Column("id", "name").Where("id IN (?)", bun.In(ids)).Scan(ctx, &rows)
		nameOf := make(map[int64]string, len(rows))
		for _, r := range rows {
			nameOf[r.ID] = r.Name
		}
		for _, r := range list {
			r.EmployeeName = nameOf[r.EmployeeID]
		}
	}
	return list, nil
}

// Create 录入奖惩，并在事务内写入员工动态时间线。
func (s *Service) Create(ctx context.Context, in *RewardPunishment, actorID int64) (*RewardPunishment, error) {
	if in.EmployeeID == 0 || in.Title == "" {
		return nil, common.NewError(400, 1002, "缺少员工或标题")
	}
	if in.Kind != "reward" && in.Kind != "punishment" {
		return nil, common.NewError(400, 1002, "类型须为 reward/punishment")
	}
	in.ID = idgen.NextID()
	in.RecordedBy = actorID
	err := s.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		if _, e := tx.NewInsert().Model(in).Exec(ctx); e != nil {
			return e
		}
		emp, e := s.emp.GetTx(ctx, tx, in.EmployeeID)
		if e != nil {
			return common.NewError(400, 1003, "员工不存在")
		}
		when := time.Now()
		if in.EffectiveDate != nil {
			when = *in.EffectiveDate
		}
		verb := "奖励"
		if in.Kind == "punishment" {
			verb = "惩罚"
		}
		return s.emp.AddEvent(ctx, tx, &employee.Event{
			EmployeeID: emp.ID, EventType: "reward",
			Title:   emp.Name + " " + verb + "：" + in.Title,
			OrgPath: emp.OrgPath, ActorID: actorID, OccurredAt: when,
		})
	})
	if err != nil {
		return nil, err
	}
	return in, nil
}

// Handler 暴露奖惩 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

// List GET /rewards-punishments?employee_id=&kind=
func (h *Handler) List(c *gin.Context) {
	var empID int64
	if v := c.Query("employee_id"); v != "" {
		empID, _ = strconv.ParseInt(v, 10, 64)
	}
	list, err := h.svc.List(c.Request.Context(), empID, c.Query("kind"))
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, list)
}

type createReq struct {
	EmployeeID    string   `json:"employee_id" binding:"required"`
	Kind          string   `json:"kind" binding:"required"`
	Category      string   `json:"category"`
	Title         string   `json:"title" binding:"required"`
	Reason        string   `json:"reason"`
	Amount        *float64 `json:"amount"`
	EffectiveDate string   `json:"effective_date"`
}

// Create POST /rewards-punishments
func (h *Handler) Create(c *gin.Context) {
	var req createReq
	if c.ShouldBindJSON(&req) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	rp := &RewardPunishment{Kind: req.Kind, Category: req.Category, Title: req.Title, Reason: req.Reason, Amount: req.Amount}
	rp.EmployeeID, _ = strconv.ParseInt(req.EmployeeID, 10, 64)
	if req.EffectiveDate != "" {
		if t, err := time.Parse("2006-01-02", req.EffectiveDate); err == nil {
			rp.EffectiveDate = &t
		}
	}
	var actorID int64
	if p := middleware.CurrentPrincipal(c); p != nil {
		actorID = p.UserID
	}
	out, err := h.svc.Create(c.Request.Context(), rp, actorID)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, out)
}

// Register 注册奖惩路由（均需鉴权）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/rewards-punishments", h.List)
	authed.POST("/rewards-punishments", h.Create)
}
