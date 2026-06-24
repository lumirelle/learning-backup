package employee

import (
	"context"
	"mime/multipart"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/xuri/excelize/v2"

	"orghr/internal/common"
	"orghr/internal/infra/idgen"
)

const xlsxMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

// 导入模板列（顺序即列序）。带 * 为必填。
var importHeaders = []string{
	"工号*", "姓名*", "性别(男/女)", "部门", "职级",
	"手机", "工作邮箱", "学历(college/bachelor/master/phd)", "入职日期(YYYY-MM-DD)",
}

// 导出列（在职花名册）。
var exportHeaders = []string{"工号", "姓名", "性别", "部门", "职级", "状态", "手机", "工作邮箱", "入职日期"}

var genderToCN = map[string]string{"male": "男", "female": "女"}
var genderFromCN = map[string]string{"男": "male", "女": "female", "male": "male", "female": "female"}
var statusToCN = map[string]string{"probation": "试用", "active": "正式", "leaving": "离职中", "left": "已离职"}

// ExportXLSX 导出（筛选后的）花名册为 xlsx 字节。
func (s *Service) ExportXLSX(ctx context.Context, f ListFilter) ([]byte, error) {
	f.Page, f.Size = 1, 10000
	list, _, err := s.repo.List(ctx, f)
	if err != nil {
		return nil, err
	}
	xl := excelize.NewFile()
	defer func() { _ = xl.Close() }()
	sheet := xl.GetSheetName(0)
	for i, h := range exportHeaders {
		cell, _ := excelize.CoordinatesToCellName(i+1, 1)
		_ = xl.SetCellValue(sheet, cell, h)
	}
	for ri, e := range list {
		hired := ""
		if e.HiredAt != nil {
			hired = e.HiredAt.Format("2006-01-02")
		}
		row := []string{e.EmployeeNo, e.Name, genderToCN[e.Gender], e.DeptName, e.JobLevel, statusToCN[e.EmploymentStatus], e.Phone, e.WorkEmail, hired}
		for ci, v := range row {
			cell, _ := excelize.CoordinatesToCellName(ci+1, ri+2)
			_ = xl.SetCellValue(sheet, cell, v)
		}
	}
	buf, err := xl.WriteToBuffer()
	if err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

// TemplateXLSX 生成导入模板（仅表头 + 一行示例）。
func (s *Service) TemplateXLSX() ([]byte, error) {
	xl := excelize.NewFile()
	defer func() { _ = xl.Close() }()
	sheet := xl.GetSheetName(0)
	for i, h := range importHeaders {
		cell, _ := excelize.CoordinatesToCellName(i+1, 1)
		_ = xl.SetCellValue(sheet, cell, h)
	}
	sample := []string{"EMP9001", "示例员工", "男", "研发部", "P5", "13800000000", "demo@example.com", "bachelor", "2024-01-01"}
	for ci, v := range sample {
		cell, _ := excelize.CoordinatesToCellName(ci+1, 2)
		_ = xl.SetCellValue(sheet, cell, v)
	}
	buf, err := xl.WriteToBuffer()
	if err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

// ImportError 行级错误。
type ImportError struct {
	Row int    `json:"row"`
	Msg string `json:"msg"`
}

// ImportResult 导入结果（部分成功）。
type ImportResult struct {
	Success int           `json:"success"`
	Failed  int           `json:"failed"`
	Errors  []ImportError `json:"errors"`
}

// Import 解析上传的 xlsx，逐行校验后入库（部分成功，返回行级错误）。
func (s *Service) Import(ctx context.Context, file multipart.File) (*ImportResult, error) {
	xl, err := excelize.OpenReader(file)
	if err != nil {
		return nil, err
	}
	defer func() { _ = xl.Close() }()
	rows, err := xl.GetRows(xl.GetSheetName(0))
	if err != nil {
		return nil, err
	}
	res := &ImportResult{Errors: []ImportError{}}
	if len(rows) <= 1 {
		return res, nil
	}

	// 已存在工号集合 + 批内去重
	existing := map[string]bool{}
	var nos []string
	_ = s.repo.db.NewSelect().Table("employees").Column("employee_no").Where("deleted_at IS NULL").Scan(ctx, &nos)
	for _, n := range nos {
		existing[n] = true
	}

	cell := func(row []string, i int) string {
		if i < len(row) {
			return strings.TrimSpace(row[i])
		}
		return ""
	}

	for idx := 1; idx < len(rows); idx++ {
		rowNo := idx + 1 // 1-based，含表头
		row := rows[idx]
		no, name := cell(row, 0), cell(row, 1)
		if no == "" && name == "" {
			continue // 跳过空行
		}
		if no == "" || name == "" {
			res.Failed++
			res.Errors = append(res.Errors, ImportError{Row: rowNo, Msg: "工号与姓名必填"})
			continue
		}
		if existing[no] {
			res.Failed++
			res.Errors = append(res.Errors, ImportError{Row: rowNo, Msg: "工号 " + no + " 重复"})
			continue
		}
		emp := &Employee{
			ID:               idgen.NextID(),
			EmployeeNo:       no,
			Name:             name,
			Gender:           genderFromCN[cell(row, 2)],
			DeptName:         cell(row, 3),
			JobLevel:         cell(row, 4),
			Phone:            cell(row, 5),
			WorkEmail:        cell(row, 6),
			Education:        cell(row, 7),
			EmploymentType:   "full_time",
			EmploymentStatus: "active",
		}
		if d := cell(row, 8); d != "" {
			t, e := time.Parse("2006-01-02", d)
			if e != nil {
				res.Failed++
				res.Errors = append(res.Errors, ImportError{Row: rowNo, Msg: "入职日期格式应为 YYYY-MM-DD"})
				continue
			}
			emp.HiredAt = &t
		}
		if _, e := s.repo.db.NewInsert().Model(emp).Exec(ctx); e != nil {
			res.Failed++
			res.Errors = append(res.Errors, ImportError{Row: rowNo, Msg: "入库失败"})
			continue
		}
		existing[no] = true
		res.Success++
	}
	return res, nil
}

// 限制错误明细数量，避免响应过大。
func (r *ImportResult) capErrors(n int) {
	if len(r.Errors) > n {
		r.Errors = r.Errors[:n]
	}
}

// ---- Handlers（纯静态路由，避开 /employees/:id 子树，radix 安全）----

// Export GET /roster-export —— 导出（筛选后的）花名册 xlsx。
func (h *Handler) Export(c *gin.Context) {
	f := ParseFilter(c)
	data, err := h.svc.ExportXLSX(c.Request.Context(), f)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	c.Header("Content-Disposition", "attachment; filename=roster-"+time.Now().Format("20060102")+".xlsx")
	c.Data(200, xlsxMime, data)
}

// Template GET /roster-template —— 下载导入模板。
func (h *Handler) Template(c *gin.Context) {
	data, err := h.svc.TemplateXLSX()
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	c.Header("Content-Disposition", "attachment; filename=roster-template.xlsx")
	c.Data(200, xlsxMime, data)
}

// Import POST /roster-import —— 上传 xlsx 批量导入（部分成功 + 行级错误）。
func (h *Handler) Import(c *gin.Context) {
	fh, err := c.FormFile("file")
	if err != nil {
		common.Fail(c, 400, 1401, "请上传 file 字段（xlsx）")
		return
	}
	f, err := fh.Open()
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	defer func() { _ = f.Close() }()
	res, err := h.svc.Import(c.Request.Context(), f)
	if err != nil {
		common.Fail(c, 400, 1401, "解析失败，请确认为有效 xlsx")
		return
	}
	res.capErrors(50)
	common.OK(c, res)
}

// RegisterIO 注册导入导出（纯静态路径）。
func RegisterIO(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/roster-export", h.Export)
	authed.GET("/roster-template", h.Template)
	authed.POST("/roster-import", h.Import)
}
