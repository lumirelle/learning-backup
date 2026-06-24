// Package analytics 提供组织人事的多维统计聚合（看板/工作台用）。
// 全部为只读聚合查询，按 org_path 子树做数据权限范围限制。
package analytics

import (
	"context"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/middleware"
)

// 在职口径：正式 + 试用。
var activeStatuses = []string{"active", "probation"}

// Overview 工作台/看板汇总指标。
type Overview struct {
	Headcount        int `json:"headcount"`         // 在职（active+probation）
	Probation        int `json:"probation"`         // 试用
	ThisMonthHires   int `json:"this_month_hires"`  // 本月入职
	ThisMonthLeaves  int `json:"this_month_leaves"` // 本月离职
	PendingProcesses int `json:"pending_processes"` // 待审批流程
	TotalEmployees   int `json:"total_employees"`   // 全量（含离职）
}

// Bucket 是「标签-计数」聚合项。
type Bucket struct {
	Label string `bun:"label" json:"label"`
	Count int    `bun:"count" json:"count"`
}

// MonthStat 入离职月度趋势项。
type MonthStat struct {
	Month  string `json:"month"`
	Hires  int    `json:"hires"`
	Leaves int    `json:"leaves"`
	Net    int    `json:"net"`
}

// HeadcountDept 编制 vs 实有（按部门）。
type HeadcountDept struct {
	Dept    string `json:"dept"`
	Planned int    `json:"planned"`
	Actual  int    `json:"actual"`
}

// HeadcountResp 编制对比结果。
type HeadcountResp struct {
	PlannedTotal int             `json:"planned_total"`
	ActualTotal  int             `json:"actual_total"`
	ByDept       []HeadcountDept `json:"by_dept"`
}

// Service 统计聚合。
type Service struct{ db *bun.DB }

// NewService 构造 service。
func NewService(db *bun.DB) *Service { return &Service{db: db} }

// emp 返回带软删过滤与数据范围的 employees 查询。
func (s *Service) emp(scope string) *bun.SelectQuery {
	q := s.db.NewSelect().Table("employees").Where("deleted_at IS NULL")
	return common.Subtree(q, "org_path", scope)
}

// Overview 计算汇总指标。
func (s *Service) Overview(ctx context.Context, scope string) (*Overview, error) {
	now := time.Now()
	mStart := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, time.UTC)
	mEnd := mStart.AddDate(0, 1, 0)

	ov := &Overview{}
	ov.Headcount, _ = s.emp(scope).Where("employment_status IN (?)", bun.In(activeStatuses)).Count(ctx)
	ov.Probation, _ = s.emp(scope).Where("employment_status = ?", "probation").Count(ctx)
	ov.ThisMonthHires, _ = s.emp(scope).Where("hired_at >= ? AND hired_at < ?", mStart, mEnd).Count(ctx)
	ov.ThisMonthLeaves, _ = s.emp(scope).Where("left_at >= ? AND left_at < ?", mStart, mEnd).Count(ctx)
	ov.TotalEmployees, _ = s.emp(scope).Count(ctx)

	pq := common.Subtree(s.db.NewSelect().Table("hr_processes").Where("status = ?", "pending"), "org_path", scope)
	ov.PendingProcesses, _ = pq.Count(ctx)
	return ov, nil
}

// dimExpr 返回某维度的 SQL 标签表达式。
func dimExpr(dim string) string {
	switch dim {
	case "level":
		return "coalesce(nullif(job_level,''),'未定级')"
	case "gender":
		return "case gender when 'male' then '男' when 'female' then '女' else '其他' end"
	case "education":
		return "coalesce(nullif(education,''),'未知')"
	case "position":
		return "coalesce(nullif(position_name,''),'未分配')"
	case "age":
		return `case
            when birthday is null then '未知'
            when date_part('year', age(birthday)) < 25 then '<25'
            when date_part('year', age(birthday)) < 30 then '25-29'
            when date_part('year', age(birthday)) < 35 then '30-34'
            when date_part('year', age(birthday)) < 40 then '35-39'
            else '40+' end`
	default: // dept
		return "coalesce(nullif(dept_name,''),'未分配')"
	}
}

// Structure 人员结构分布（在职口径）。
func (s *Service) Structure(ctx context.Context, dim, scope string) ([]Bucket, error) {
	expr := dimExpr(dim)
	var out []Bucket
	err := s.emp(scope).
		Where("employment_status IN (?)", bun.In(activeStatuses)).
		ColumnExpr(expr+" AS label").
		ColumnExpr("count(*) AS count").
		GroupExpr(expr).
		OrderExpr("count DESC, label ASC").
		Scan(ctx, &out)
	return out, err
}

// Tenure 司龄分布（在职口径）。
func (s *Service) Tenure(ctx context.Context, scope string) ([]Bucket, error) {
	expr := `case
        when hired_at is null then '未知'
        when date_part('year', age(hired_at)) < 1 then '<1年'
        when date_part('year', age(hired_at)) < 3 then '1-3年'
        when date_part('year', age(hired_at)) < 5 then '3-5年'
        when date_part('year', age(hired_at)) < 10 then '5-10年'
        else '10年+' end`
	var out []Bucket
	err := s.emp(scope).
		Where("employment_status IN (?)", bun.In(activeStatuses)).
		ColumnExpr(expr+" AS label").
		ColumnExpr("count(*) AS count").
		GroupExpr(expr).
		OrderExpr("count DESC").
		Scan(ctx, &out)
	return out, err
}

// Turnover 入离职月度趋势（[from, to] 闭区间，按月）。
func (s *Service) Turnover(ctx context.Context, from, to time.Time, scope string) ([]MonthStat, error) {
	toExclusive := to.AddDate(0, 1, 0)
	type mc struct {
		M string `bun:"m"`
		C int    `bun:"c"`
	}
	scan := func(col string) (map[string]int, error) {
		var rows []mc
		err := s.emp(scope).
			ColumnExpr("to_char("+col+",'YYYY-MM') AS m").
			ColumnExpr("count(*) AS c").
			Where(col+" IS NOT NULL AND "+col+" >= ? AND "+col+" < ?", from, toExclusive).
			GroupExpr("m").
			Scan(ctx, &rows)
		m := make(map[string]int, len(rows))
		for _, r := range rows {
			m[r.M] = r.C
		}
		return m, err
	}
	hires, err := scan("hired_at")
	if err != nil {
		return nil, err
	}
	leaves, err := scan("left_at")
	if err != nil {
		return nil, err
	}
	var out []MonthStat
	for cur := from; !cur.After(to); cur = cur.AddDate(0, 1, 0) {
		key := cur.Format("2006-01")
		h, l := hires[key], leaves[key]
		out = append(out, MonthStat{Month: key, Hires: h, Leaves: l, Net: h - l})
	}
	return out, nil
}

// Headcount 编制 vs 实有（总量 + 按部门）。
func (s *Service) Headcount(ctx context.Context, scope string) (*HeadcountResp, error) {
	deptExpr := "coalesce(nullif(dept_name,''),'未分配')"
	// 实有（在职）按部门
	actual := []HeadcountDept{}
	if err := s.emp(scope).
		Where("employment_status IN (?)", bun.In(activeStatuses)).
		ColumnExpr(deptExpr+" AS dept").
		ColumnExpr("count(*) AS actual").
		GroupExpr(deptExpr).
		Scan(ctx, &actual); err != nil {
		return nil, err
	}
	// 编制按部门（positions.headcount 汇总，join departments 取名 + 范围过滤）
	type pc struct {
		Dept    string `bun:"dept"`
		Planned int    `bun:"planned"`
	}
	planned := []pc{}
	pq := s.db.NewSelect().TableExpr("positions AS p").
		Join("JOIN departments AS d ON d.id = p.dept_id").
		ColumnExpr("d.name AS dept").
		ColumnExpr("coalesce(sum(p.headcount),0) AS planned").
		Where("p.deleted_at IS NULL AND d.deleted_at IS NULL").
		GroupExpr("d.name")
	pq = common.Subtree(pq, "d.path", scope)
	if err := pq.Scan(ctx, &planned); err != nil {
		return nil, err
	}

	merged := map[string]*HeadcountDept{}
	get := func(dept string) *HeadcountDept {
		if merged[dept] == nil {
			merged[dept] = &HeadcountDept{Dept: dept}
		}
		return merged[dept]
	}
	resp := &HeadcountResp{}
	for _, a := range actual {
		get(a.Dept).Actual = a.Actual
		resp.ActualTotal += a.Actual
	}
	for _, p := range planned {
		get(p.Dept).Planned = p.Planned
		resp.PlannedTotal += p.Planned
	}
	for _, v := range merged {
		resp.ByDept = append(resp.ByDept, *v)
	}
	return resp, nil
}

// ---- Handler ----

// Handler 暴露统计 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

// scopeOf 取数据范围：非管理员一律收敛到本人组织子树。
func scopeOf(c *gin.Context) string {
	scope := c.Query("scope")
	if p := middleware.CurrentPrincipal(c); p != nil && !p.IsAdmin {
		if scope == "" || !strings.HasPrefix(scope, p.OrgPath) {
			scope = p.OrgPath
		}
	}
	return scope
}

// Overview GET /analytics/overview
func (h *Handler) Overview(c *gin.Context) {
	ov, err := h.svc.Overview(c.Request.Context(), scopeOf(c))
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, ov)
}

// Structure GET /analytics/structure?dim=
func (h *Handler) Structure(c *gin.Context) {
	dim := c.DefaultQuery("dim", "dept")
	out, err := h.svc.Structure(c.Request.Context(), dim, scopeOf(c))
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, gin.H{"dim": dim, "buckets": out})
}

// Turnover GET /analytics/turnover?from=YYYY-MM&to=YYYY-MM
func (h *Handler) Turnover(c *gin.Context) {
	now := time.Now()
	to := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, time.UTC)
	from := to.AddDate(0, -11, 0)
	if v := c.Query("from"); v != "" {
		if t, err := time.Parse("2006-01", v); err == nil {
			from = t
		}
	}
	if v := c.Query("to"); v != "" {
		if t, err := time.Parse("2006-01", v); err == nil {
			to = t
		}
	}
	out, err := h.svc.Turnover(c.Request.Context(), from, to, scopeOf(c))
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, out)
}

// Tenure GET /analytics/tenure
func (h *Handler) Tenure(c *gin.Context) {
	out, err := h.svc.Tenure(c.Request.Context(), scopeOf(c))
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, out)
}

// Headcount GET /analytics/headcount
func (h *Handler) Headcount(c *gin.Context) {
	out, err := h.svc.Headcount(c.Request.Context(), scopeOf(c))
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, out)
}

// Register 注册统计路由（均需鉴权）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/analytics/overview", h.Overview)
	authed.GET("/analytics/structure", h.Structure)
	authed.GET("/analytics/turnover", h.Turnover)
	authed.GET("/analytics/tenure", h.Tenure)
	authed.GET("/analytics/headcount", h.Headcount)
}
