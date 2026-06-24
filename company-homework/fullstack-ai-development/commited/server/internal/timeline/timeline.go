// Package timeline 提供人事动态（hr_events）与操作日志（audit_logs）的检索。
package timeline

import (
	"context"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/employee"
)

// AuditLog 映射 audit_logs（只读）。
type AuditLog struct {
	bun.BaseModel `bun:"table:audit_logs,alias:al"`

	ID        int64     `bun:"id,pk" json:"id,string"`
	UserID    int64     `bun:"user_id" json:"user_id,string"`
	Username  string    `bun:"username" json:"username"`
	Method    string    `bun:"method" json:"method"`
	Path      string    `bun:"path" json:"path"`
	Status    int       `bun:"status" json:"status"`
	IP        string    `bun:"ip" json:"ip"`
	RequestID string    `bun:"request_id" json:"request_id"`
	CreatedAt time.Time `bun:"created_at" json:"created_at"`
}

// Service 动态/日志读取。
type Service struct{ db *bun.DB }

// NewService 构造 service。
func NewService(db *bun.DB) *Service { return &Service{db: db} }

// Events 检索人事动态时间线。
func (s *Service) Events(ctx context.Context, employeeID int64, eventType string, page, size int) ([]*employee.Event, int, error) {
	var list []*employee.Event
	q := s.db.NewSelect().Model(&list)
	if employeeID != 0 {
		q = q.Where("employee_id = ?", employeeID)
	}
	if eventType != "" {
		q = q.Where("event_type = ?", eventType)
	}
	total, err := q.Order("occurred_at DESC").Limit(size).Offset((page - 1) * size).ScanAndCount(ctx)
	return list, total, err
}

// AuditLogs 检索操作日志。
func (s *Service) AuditLogs(ctx context.Context, username, path string, page, size int) ([]*AuditLog, int, error) {
	var list []*AuditLog
	q := s.db.NewSelect().Model(&list)
	if username != "" {
		q = q.Where("username = ?", username)
	}
	if path != "" {
		q = q.Where("path ILIKE ?", "%"+path+"%")
	}
	total, err := q.Order("id DESC").Limit(size).Offset((page - 1) * size).ScanAndCount(ctx)
	return list, total, err
}

// Handler 暴露动态/日志 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

// Timeline GET /timeline
func (h *Handler) Timeline(c *gin.Context) {
	page, size := common.ParsePage(c)
	var empID int64
	if v := c.Query("employee_id"); v != "" {
		empID, _ = strconv.ParseInt(v, 10, 64)
	}
	list, total, err := h.svc.Events(c.Request.Context(), empID, c.Query("type"), page, size)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.Page(c, list, total, page, size)
}

// AuditLogs GET /audit-logs
func (h *Handler) AuditLogs(c *gin.Context) {
	page, size := common.ParsePage(c)
	list, total, err := h.svc.AuditLogs(c.Request.Context(), c.Query("username"), c.Query("path"), page, size)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.Page(c, list, total, page, size)
}

// Register 注册动态/日志路由。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/timeline", h.Timeline)
	authed.GET("/audit-logs", h.AuditLogs)
}
