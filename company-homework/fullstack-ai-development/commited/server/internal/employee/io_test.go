package employee_test

import (
	"bytes"
	"context"
	"testing"

	"github.com/xuri/excelize/v2"

	"orghr/internal/employee"
	"orghr/internal/testutil"
)

// readSeekCloser 让 *bytes.Reader 满足 multipart.File（补一个 no-op Close）。
type readSeekCloser struct{ *bytes.Reader }

func (readSeekCloser) Close() error { return nil }

// buildXLSX 按导入模板列顺序生成一个含表头 + 给定数据行的 xlsx。
func buildXLSX(t *testing.T, rows [][]string) *readSeekCloser {
	t.Helper()
	xl := excelize.NewFile()
	defer func() { _ = xl.Close() }()
	sheet := xl.GetSheetName(0)
	header := []string{"工号*", "姓名*", "性别", "部门", "职级", "手机", "邮箱", "学历", "入职日期"}
	for i, h := range header {
		cell, _ := excelize.CoordinatesToCellName(i+1, 1)
		_ = xl.SetCellValue(sheet, cell, h)
	}
	for ri, row := range rows {
		for ci, v := range row {
			cell, _ := excelize.CoordinatesToCellName(ci+1, ri+2)
			_ = xl.SetCellValue(sheet, cell, v)
		}
	}
	buf, err := xl.WriteToBuffer()
	if err != nil {
		t.Fatalf("build xlsx: %v", err)
	}
	return &readSeekCloser{bytes.NewReader(buf.Bytes())}
}

// TestImportPartialSuccess 验证批量导入：合法行入库、非法行返回行级错误、批内工号去重。
func TestImportPartialSuccess(t *testing.T) {
	db := testutil.DB(t)
	testutil.Reset(t, db)
	ctx := context.Background()
	svc := employee.NewService(employee.NewRepo(db))

	// 预置一个已存在工号，用于校验“与库内重复”。
	if _, err := db.NewInsert().Model(&employee.Employee{
		ID: 1, EmployeeNo: "EXIST", Name: "已存在", OrgID: 1, OrgPath: "/hq", EmploymentStatus: "active",
	}).Exec(ctx); err != nil {
		t.Fatalf("seed: %v", err)
	}

	rows := [][]string{
		{"E100", "合法甲", "男", "研发部", "P5", "", "", "bachelor", "2024-03-01"}, // ✓
		{"E101", "合法乙", "女", "产品部", "P4", "", "", "master", ""},             // ✓（无入职日期）
		{"", "缺工号", "男", "", "", "", "", "", ""},                            // ✗ 必填
		{"E102", "坏日期", "男", "", "", "", "", "", "2024/03/01"},              // ✗ 日期格式
		{"EXIST", "库内重复", "男", "", "", "", "", "", ""},                      // ✗ 与库内重复
		{"E100", "批内重复", "男", "", "", "", "", "", ""},                       // ✗ 与本批 E100 重复
		{"", "", "", "", "", "", "", "", ""},                                // 空行：跳过（不计入失败）
	}
	file := buildXLSX(t, rows)
	res, err := svc.Import(ctx, file)
	if err != nil {
		t.Fatalf("import: %v", err)
	}
	if res.Success != 2 {
		t.Fatalf("应成功 2 行，实际 %d", res.Success)
	}
	if res.Failed != 4 {
		t.Fatalf("应失败 4 行，实际 %d（errors=%v）", res.Failed, res.Errors)
	}
	if len(res.Errors) != 4 {
		t.Fatalf("应有 4 条行级错误，实际 %d", len(res.Errors))
	}
	// 库内确实多了 2 名（EXIST + E100 + E101 = 3）。
	cnt, _ := db.NewSelect().Table("employees").Count(ctx)
	if cnt != 3 {
		t.Fatalf("库内应有 3 名员工，实际 %d", cnt)
	}
	// 合法行字段映射正确（性别中文→英文枚举、入职日期解析）。
	var e employee.Employee
	_ = db.NewSelect().Model(&e).Where("employee_no = ?", "E100").Scan(ctx)
	if e.Gender != "male" || e.HiredAt == nil || e.HiredAt.Format("2006-01-02") != "2024-03-01" {
		t.Fatalf("E100 字段映射异常: gender=%s hired=%v", e.Gender, e.HiredAt)
	}
}
