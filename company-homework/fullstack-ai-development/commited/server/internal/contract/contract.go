// Package contract 实现劳动合同管理：签订/续签/终止/到期提醒 + 模板。
package contract

import (
	"context"
	"fmt"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/infra/idgen"
	"orghr/internal/middleware"
)

// Template 合同模板。
type Template struct {
	bun.BaseModel `bun:"table:contract_templates,alias:ct"`

	ID        int64     `bun:"id,pk" json:"id,string"`
	Name      string    `bun:"name" json:"name"`
	Type      string    `bun:"type" json:"type"`
	Content   string    `bun:"content" json:"content"`
	Enabled   bool      `bun:"enabled" json:"enabled"`
	CreatedAt time.Time `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt time.Time `bun:"updated_at,nullzero,default:now()" json:"updated_at"`
}

// Contract 劳动合同。
type Contract struct {
	bun.BaseModel `bun:"table:contracts,alias:c"`

	ID             int64          `bun:"id,pk" json:"id,string"`
	ContractNo     string         `bun:"contract_no" json:"contract_no"`
	EmployeeID     int64          `bun:"employee_id" json:"employee_id,string"`
	TemplateID     int64          `bun:"template_id,nullzero" json:"template_id,string,omitempty"`
	Type           string         `bun:"type" json:"type"`
	Status         string         `bun:"status" json:"status"`
	SignDate       *time.Time     `bun:"sign_date,nullzero" json:"sign_date,omitempty"`
	StartDate      *time.Time     `bun:"start_date,nullzero" json:"start_date,omitempty"`
	EndDate        *time.Time     `bun:"end_date,nullzero" json:"end_date,omitempty"`
	PrevContractID int64          `bun:"prev_contract_id,nullzero" json:"prev_contract_id,string,omitempty"`
	SalaryBand     string         `bun:"salary_band" json:"salary_band"`
	Terms          map[string]any `bun:"terms,type:jsonb" json:"terms,omitempty"`
	CreatedAt      time.Time      `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt      time.Time      `bun:"updated_at,nullzero,default:now()" json:"updated_at"`

	EmployeeName string `bun:"-" json:"employee_name,omitempty"`
	DaysLeft     *int   `bun:"-" json:"days_left,omitempty"`
}

// Service 合同业务。
type Service struct{ db *bun.DB }

// NewService 构造 service。
func NewService(db *bun.DB) *Service { return &Service{db: db} }

// scopeSubquery 对非管理员，按员工 org_path 子树限制可见合同。
func (s *Service) applyScope(q *bun.SelectQuery, scope string) *bun.SelectQuery {
	if scope != "" {
		// 按 `/` 边界限定到员工 org_path 子树，避免裸前缀误伤兄弟节点。
		expr, args := common.SubtreeExpr("org_path", scope)
		q = q.Where("employee_id IN (SELECT id FROM employees WHERE "+expr+")", args...)
	}
	return q
}

// ListInput 合同列表筛选。
type ListInput struct {
	Status     string
	EmployeeID int64
	ExpiringIn int // >0 时只看 N 天内到期的在用合同
	Scope      string
	Page, Size int
}

// List 分页查询合同。
func (s *Service) List(ctx context.Context, in ListInput) ([]*Contract, int, error) {
	var list []*Contract
	q := s.db.NewSelect().Model(&list).Where("c.deleted_at IS NULL")
	q = s.applyScope(q, in.Scope)
	if in.Status != "" {
		q = q.Where("c.status = ?", in.Status)
	}
	if in.EmployeeID != 0 {
		q = q.Where("c.employee_id = ?", in.EmployeeID)
	}
	if in.ExpiringIn > 0 {
		until := time.Now().AddDate(0, 0, in.ExpiringIn)
		q = q.Where("c.status = ?", "active").
			Where("c.end_date IS NOT NULL AND c.end_date >= ? AND c.end_date <= ?", time.Now(), until)
	}
	total, err := q.Order("c.end_date ASC NULLS LAST", "c.id DESC").
		Limit(in.Size).Offset((in.Page - 1) * in.Size).ScanAndCount(ctx)
	if err != nil {
		return nil, 0, err
	}
	s.enrich(ctx, list)
	return list, total, nil
}

// Reminders 返回 N 天内到期的在用合同（工作台提醒用，含剩余天数）。
func (s *Service) Reminders(ctx context.Context, days int, scope string) ([]*Contract, error) {
	if days <= 0 {
		days = 30
	}
	list, _, err := s.List(ctx, ListInput{ExpiringIn: days, Scope: scope, Page: 1, Size: 100})
	return list, err
}

// Get 读取合同。
func (s *Service) Get(ctx context.Context, id int64) (*Contract, error) {
	c := new(Contract)
	if err := s.db.NewSelect().Model(c).Where("c.id = ? AND c.deleted_at IS NULL", id).Limit(1).Scan(ctx); err != nil {
		return nil, common.ErrNotFound
	}
	s.enrich(ctx, []*Contract{c})
	return c, nil
}

// Create 签订合同。
func (s *Service) Create(ctx context.Context, c *Contract) (*Contract, error) {
	if c.EmployeeID == 0 {
		return nil, common.NewError(400, 1002, "缺少员工")
	}
	if c.EndDate != nil && c.StartDate != nil && c.EndDate.Before(*c.StartDate) {
		return nil, common.NewError(400, 1303, "合同期限非法")
	}
	c.ID = idgen.NextID()
	c.ContractNo = fmt.Sprintf("HT-%d", c.ID)
	if c.Type == "" {
		c.Type = "fixed_term"
	}
	if c.Status == "" {
		c.Status = "active"
	}
	if _, err := s.db.NewInsert().Model(c).Exec(ctx); err != nil {
		return nil, common.NewError(409, 1004, "合同创建失败")
	}
	return c, nil
}

// Renew 续签：原合同置 renewed，生成新合同关联上一份。
func (s *Service) Renew(ctx context.Context, id int64, newEnd *time.Time, salaryBand string) (*Contract, error) {
	var fresh *Contract
	err := s.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		old := new(Contract)
		if e := tx.NewSelect().Model(old).Where("id = ? AND deleted_at IS NULL", id).Limit(1).Scan(ctx); e != nil {
			return common.ErrNotFound
		}
		if old.Status != "active" {
			return common.NewError(409, 1201, "仅在用合同可续签")
		}
		old.Status = "renewed"
		old.UpdatedAt = time.Now()
		if _, e := tx.NewUpdate().Model(old).Column("status", "updated_at").WherePK().Exec(ctx); e != nil {
			return e
		}
		start := time.Now()
		if old.EndDate != nil {
			start = old.EndDate.AddDate(0, 0, 1)
		}
		if salaryBand == "" {
			salaryBand = old.SalaryBand
		}
		fresh = &Contract{
			ID: idgen.NextID(), EmployeeID: old.EmployeeID, TemplateID: old.TemplateID,
			Type: old.Type, Status: "active", SignDate: &start, StartDate: &start, EndDate: newEnd,
			PrevContractID: old.ID, SalaryBand: salaryBand,
		}
		fresh.ContractNo = fmt.Sprintf("HT-%d", fresh.ID)
		_, e := tx.NewInsert().Model(fresh).Exec(ctx)
		return e
	})
	if err != nil {
		return nil, err
	}
	return fresh, nil
}

// Terminate 终止合同。
func (s *Service) Terminate(ctx context.Context, id int64) (*Contract, error) {
	c := new(Contract)
	if err := s.db.NewSelect().Model(c).Where("id = ? AND deleted_at IS NULL", id).Limit(1).Scan(ctx); err != nil {
		return nil, common.ErrNotFound
	}
	if c.Status != "active" {
		return nil, common.NewError(409, 1201, "仅在用合同可终止")
	}
	c.Status = "terminated"
	c.UpdatedAt = time.Now()
	if _, err := s.db.NewUpdate().Model(c).Column("status", "updated_at").WherePK().Exec(ctx); err != nil {
		return nil, err
	}
	return c, nil
}

// Templates 列出模板。
func (s *Service) Templates(ctx context.Context) ([]*Template, error) {
	var list []*Template
	err := s.db.NewSelect().Model(&list).Order("id DESC").Scan(ctx)
	return list, err
}

// CreateTemplate 新增模板。
func (s *Service) CreateTemplate(ctx context.Context, t *Template) (*Template, error) {
	t.ID = idgen.NextID()
	if t.Type == "" {
		t.Type = "fixed_term"
	}
	t.Enabled = true
	if _, err := s.db.NewInsert().Model(t).Exec(ctx); err != nil {
		return nil, common.ErrConflict
	}
	return t, nil
}

// enrich 回填员工姓名与剩余天数。
func (s *Service) enrich(ctx context.Context, list []*Contract) {
	if len(list) == 0 {
		return
	}
	ids := make([]int64, 0, len(list))
	for _, c := range list {
		ids = append(ids, c.EmployeeID)
	}
	var rows []struct {
		ID   int64  `bun:"id"`
		Name string `bun:"name"`
	}
	_ = s.db.NewSelect().Table("employees").Column("id", "name").
		Where("id IN (?)", bun.In(ids)).Scan(ctx, &rows)
	nameOf := make(map[int64]string, len(rows))
	for _, r := range rows {
		nameOf[r.ID] = r.Name
	}
	today := time.Now()
	for _, c := range list {
		c.EmployeeName = nameOf[c.EmployeeID]
		if c.EndDate != nil {
			d := int(c.EndDate.Sub(today).Hours() / 24)
			c.DaysLeft = &d
		}
	}
}

// ---- Handler ----

// Handler 暴露合同 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

func scopeOf(c *gin.Context) string {
	if p := middleware.CurrentPrincipal(c); p != nil && !p.IsAdmin {
		return p.OrgPath
	}
	return ""
}

func parseDate(s string) *time.Time {
	if s == "" {
		return nil
	}
	if t, err := time.Parse("2006-01-02", s); err == nil {
		return &t
	}
	return nil
}

// List GET /contracts
func (h *Handler) List(c *gin.Context) {
	page, size := common.ParsePage(c)
	in := ListInput{Status: c.Query("status"), Scope: scopeOf(c), Page: page, Size: size}
	if v := c.Query("employee_id"); v != "" {
		fmt.Sscan(v, &in.EmployeeID)
	}
	if v := c.Query("expiring_in"); v != "" {
		fmt.Sscan(v, &in.ExpiringIn)
	}
	list, total, err := h.svc.List(c.Request.Context(), in)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.Page(c, list, total, page, size)
}

// Reminders GET /contracts/reminders?days=
func (h *Handler) Reminders(c *gin.Context) {
	days := 30
	if v := c.Query("days"); v != "" {
		fmt.Sscan(v, &days)
	}
	list, err := h.svc.Reminders(c.Request.Context(), days, scopeOf(c))
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, list)
}

type createReq struct {
	EmployeeID string `json:"employee_id" binding:"required"`
	TemplateID string `json:"template_id"`
	Type       string `json:"type"`
	SignDate   string `json:"sign_date"`
	StartDate  string `json:"start_date"`
	EndDate    string `json:"end_date"`
	SalaryBand string `json:"salary_band"`
}

// Create POST /contracts
func (h *Handler) Create(c *gin.Context) {
	var req createReq
	if c.ShouldBindJSON(&req) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	ct := &Contract{
		Type: req.Type, SalaryBand: req.SalaryBand,
		SignDate: parseDate(req.SignDate), StartDate: parseDate(req.StartDate), EndDate: parseDate(req.EndDate),
	}
	fmt.Sscan(req.EmployeeID, &ct.EmployeeID)
	if req.TemplateID != "" {
		fmt.Sscan(req.TemplateID, &ct.TemplateID)
	}
	out, err := h.svc.Create(c.Request.Context(), ct)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, out)
}

// Get GET /contracts/:id
func (h *Handler) Get(c *gin.Context) {
	var id int64
	fmt.Sscan(c.Param("id"), &id)
	out, err := h.svc.Get(c.Request.Context(), id)
	if err != nil {
		common.FailErr(c, common.ErrNotFound)
		return
	}
	common.OK(c, out)
}

type renewReq struct {
	EndDate    string `json:"end_date" binding:"required"`
	SalaryBand string `json:"salary_band"`
}

// Renew POST /contracts/:id/renew
func (h *Handler) Renew(c *gin.Context) {
	var id int64
	fmt.Sscan(c.Param("id"), &id)
	var req renewReq
	if c.ShouldBindJSON(&req) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	out, err := h.svc.Renew(c.Request.Context(), id, parseDate(req.EndDate), req.SalaryBand)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, out)
}

// Terminate DELETE /contracts/:id
func (h *Handler) Terminate(c *gin.Context) {
	var id int64
	fmt.Sscan(c.Param("id"), &id)
	out, err := h.svc.Terminate(c.Request.Context(), id)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, out)
}

// Templates GET /contract-templates
func (h *Handler) Templates(c *gin.Context) {
	list, err := h.svc.Templates(c.Request.Context())
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, list)
}

// CreateTemplate POST /contract-templates
func (h *Handler) CreateTemplate(c *gin.Context) {
	var t Template
	if c.ShouldBindJSON(&t) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	out, err := h.svc.CreateTemplate(c.Request.Context(), &t)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, out)
}

// Register 注册合同路由（均需鉴权）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/contracts", h.List)
	authed.GET("/contracts/reminders", h.Reminders)
	authed.POST("/contracts", h.Create)
	authed.GET("/contracts/:id", h.Get)
	authed.POST("/contracts/:id/renew", h.Renew)
	authed.DELETE("/contracts/:id", h.Terminate) // 终止=DELETE，避免 :id 下两个静态子节点触发 gin radix 不确定性
	authed.GET("/contract-templates", h.Templates)
	authed.POST("/contract-templates", h.CreateTemplate)
}
