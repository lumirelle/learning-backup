// Package care 实现员工关怀：本月生日 / 入职周年名单（由 employees 计算，无需额外表）。
package care

import (
	"context"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/middleware"
)

// Item 关怀名单项。
type Item struct {
	EmployeeID int64  `bun:"id" json:"employee_id,string"`
	Name       string `bun:"name" json:"name"`
	DeptName   string `bun:"dept_name" json:"dept_name"`
	Date       string `bun:"date" json:"date"`             // MM-DD
	Years      int    `bun:"years" json:"years,omitempty"` // 周年才有
}

// Upcoming 某月关怀汇总。
type Upcoming struct {
	Month         int    `json:"month"`
	Birthdays     []Item `json:"birthdays"`
	Anniversaries []Item `json:"anniversaries"`
}

// Service 关怀业务（只读聚合）。
type Service struct{ db *bun.DB }

// NewService 构造 service。
func NewService(db *bun.DB) *Service { return &Service{db: db} }

func (s *Service) base(scope string) *bun.SelectQuery {
	q := s.db.NewSelect().Table("employees").
		Where("deleted_at IS NULL").
		Where("employment_status IN ('active','probation')")
	return common.Subtree(q, "org_path", scope)
}

// Compute 计算某月生日与入职周年名单。
func (s *Service) Compute(ctx context.Context, month int, scope string) (*Upcoming, error) {
	out := &Upcoming{Month: month, Birthdays: []Item{}, Anniversaries: []Item{}}

	if err := s.base(scope).
		ColumnExpr("id").ColumnExpr("name").ColumnExpr("dept_name").
		ColumnExpr("to_char(birthday,'MM-DD') AS date").
		Where("birthday IS NOT NULL AND extract(month from birthday) = ?", month).
		OrderExpr("extract(day from birthday)").
		Scan(ctx, &out.Birthdays); err != nil {
		return nil, err
	}

	if err := s.base(scope).
		ColumnExpr("id").ColumnExpr("name").ColumnExpr("dept_name").
		ColumnExpr("to_char(hired_at,'MM-DD') AS date").
		ColumnExpr("(extract(year from age(hired_at)))::int AS years").
		Where("hired_at IS NOT NULL AND extract(month from hired_at) = ?", month).
		Where("extract(year from age(hired_at)) >= 1").
		OrderExpr("extract(day from hired_at)").
		Scan(ctx, &out.Anniversaries); err != nil {
		return nil, err
	}
	return out, nil
}

// Handler 暴露关怀 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

// Upcoming GET /care/upcoming?month=
func (h *Handler) Upcoming(c *gin.Context) {
	month := int(time.Now().Month())
	if v := c.Query("month"); v != "" {
		if m, err := strconv.Atoi(v); err == nil && m >= 1 && m <= 12 {
			month = m
		}
	}
	scope := ""
	if p := middleware.CurrentPrincipal(c); p != nil && !p.IsAdmin {
		scope = p.OrgPath
	}
	out, err := h.svc.Compute(c.Request.Context(), month, scope)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, out)
}

// Register 注册关怀路由（需鉴权）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/care/upcoming", h.Upcoming)
}
