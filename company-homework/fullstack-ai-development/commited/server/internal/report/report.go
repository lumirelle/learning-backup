// Package report 实现人事报表：内置常用模板 + 预览(JSON) + 导出 Excel(.xlsx)。
package report

import (
	"context"
	"fmt"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"
	"github.com/xuri/excelize/v2"

	"orghr/internal/common"
	"orghr/internal/middleware"
)

// Template 报表模板（内置）。
type Template struct {
	Key  string `json:"key"`
	Name string `json:"name"`
	Desc string `json:"desc"`
}

var templates = []Template{
	{Key: "roster", Name: "在职花名册", Desc: "全部在职员工的基本信息"},
	{Key: "headcount", Name: "部门人数统计", Desc: "各部门在职人数"},
	{Key: "turnover", Name: "月度入离职", Desc: "近 12 个月入职/离职/净增"},
}

// Table 报表结果（列 + 行）。
type Table struct {
	Title   string     `json:"title"`
	Columns []string   `json:"columns"`
	Rows    [][]string `json:"rows"`
}

// Service 报表业务。
type Service struct{ db *bun.DB }

// NewService 构造 service。
func NewService(db *bun.DB) *Service { return &Service{db: db} }

func (s *Service) empWhere(scope string) string {
	w := "deleted_at IS NULL"
	if scope != "" {
		w += " AND org_path LIKE '" + sanitize(scope) + "%'"
	}
	return w
}

// 简单转义（scope 来自服务端 principal.OrgPath，非用户任意输入，仅防御）。
func sanitize(s string) string {
	out := make([]byte, 0, len(s))
	for i := 0; i < len(s); i++ {
		if s[i] == '\'' || s[i] == ';' {
			continue
		}
		out = append(out, s[i])
	}
	return string(out)
}

var genderCN = map[string]string{"male": "男", "female": "女"}
var statusCN = map[string]string{"probation": "试用", "active": "正式", "leaving": "离职中", "left": "已离职"}

// Build 生成某报表数据。
func (s *Service) Build(ctx context.Context, key, scope string) (*Table, error) {
	switch key {
	case "roster":
		var rows []struct {
			No     string     `bun:"employee_no"`
			Name   string     `bun:"name"`
			Gender string     `bun:"gender"`
			Dept   string     `bun:"dept_name"`
			Level  string     `bun:"job_level"`
			Status string     `bun:"employment_status"`
			Hired  *time.Time `bun:"hired_at"`
		}
		if err := s.db.NewSelect().Table("employees").
			Column("employee_no", "name", "gender", "dept_name", "job_level", "employment_status", "hired_at").
			Where(s.empWhere(scope)).
			Where("employment_status IN ('active','probation')").
			Order("employee_no").Scan(ctx, &rows); err != nil {
			return nil, err
		}
		t := &Table{Title: "在职花名册", Columns: []string{"工号", "姓名", "性别", "部门", "职级", "状态", "入职日期"}}
		for _, r := range rows {
			hired := ""
			if r.Hired != nil {
				hired = r.Hired.Format("2006-01-02")
			}
			t.Rows = append(t.Rows, []string{r.No, r.Name, genderCN[r.Gender], r.Dept, r.Level, statusCN[r.Status], hired})
		}
		return t, nil

	case "headcount":
		var rows []struct {
			Dept  string `bun:"dept"`
			Count int    `bun:"cnt"`
		}
		if err := s.db.NewSelect().Table("employees").
			ColumnExpr("coalesce(nullif(dept_name,''),'未分配') AS dept").
			ColumnExpr("count(*) AS cnt").
			Where(s.empWhere(scope)).
			Where("employment_status IN ('active','probation')").
			GroupExpr("dept").OrderExpr("cnt DESC").Scan(ctx, &rows); err != nil {
			return nil, err
		}
		t := &Table{Title: "部门人数统计", Columns: []string{"部门", "在职人数"}}
		for _, r := range rows {
			t.Rows = append(t.Rows, []string{r.Dept, fmt.Sprintf("%d", r.Count)})
		}
		return t, nil

	case "turnover":
		now := time.Now()
		to := time.Date(now.Year(), now.Month(), 1, 0, 0, 0, 0, time.UTC)
		from := to.AddDate(0, -11, 0)
		toExcl := to.AddDate(0, 1, 0)
		count := func(col string) map[string]int {
			var rows []struct {
				M string `bun:"m"`
				C int    `bun:"c"`
			}
			_ = s.db.NewSelect().Table("employees").
				ColumnExpr("to_char("+col+",'YYYY-MM') AS m").ColumnExpr("count(*) AS c").
				Where(s.empWhere(scope)).
				Where(col+" IS NOT NULL AND "+col+" >= ? AND "+col+" < ?", from, toExcl).
				GroupExpr("m").Scan(ctx, &rows)
			m := map[string]int{}
			for _, r := range rows {
				m[r.M] = r.C
			}
			return m
		}
		hires, leaves := count("hired_at"), count("left_at")
		t := &Table{Title: "月度入离职", Columns: []string{"月份", "入职", "离职", "净增"}}
		for cur := from; !cur.After(to); cur = cur.AddDate(0, 1, 0) {
			k := cur.Format("2006-01")
			h, l := hires[k], leaves[k]
			t.Rows = append(t.Rows, []string{k, fmt.Sprintf("%d", h), fmt.Sprintf("%d", l), fmt.Sprintf("%d", h-l)})
		}
		return t, nil
	}
	return nil, common.NewError(404, 1003, "报表不存在")
}

// Handler 暴露报表 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

func scopeOf(c *gin.Context) string {
	if p := middleware.CurrentPrincipal(c); p != nil && !p.IsAdmin {
		return p.OrgPath
	}
	return ""
}

// Templates GET /reports —— 列出内置报表模板。
func (h *Handler) Templates(c *gin.Context) {
	common.OK(c, templates)
}

// Report GET /reports/:key —— 默认 JSON 预览；?format=excel 导出 .xlsx。
func (h *Handler) Report(c *gin.Context) {
	key := c.Param("key")
	t, err := h.svc.Build(c.Request.Context(), key, scopeOf(c))
	if err != nil {
		common.FailAny(c, err)
		return
	}
	if c.Query("format") != "excel" {
		common.OK(c, t)
		return
	}

	f := excelize.NewFile()
	defer func() { _ = f.Close() }()
	sheet := "Sheet1"
	for i, col := range t.Columns {
		cell, _ := excelize.CoordinatesToCellName(i+1, 1)
		_ = f.SetCellValue(sheet, cell, col)
	}
	for ri, row := range t.Rows {
		for ci, val := range row {
			cell, _ := excelize.CoordinatesToCellName(ci+1, ri+2)
			_ = f.SetCellValue(sheet, cell, val)
		}
	}
	buf, err := f.WriteToBuffer()
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	filename := fmt.Sprintf("%s-%s.xlsx", key, time.Now().Format("20060102"))
	c.Header("Content-Disposition", "attachment; filename="+filename)
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", buf.Bytes())
}

// Register 注册报表路由（需鉴权）。GET /reports + GET /reports/:key（radix 安全：:key 下无静态子节点）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/reports", h.Templates)
	authed.GET("/reports/:key", h.Report)
}
