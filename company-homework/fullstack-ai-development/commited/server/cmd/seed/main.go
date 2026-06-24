package main

import (
	"context"
	"fmt"
	"time"

	"github.com/rs/zerolog"
	"github.com/uptrace/bun"

	"orghr/internal/auth"
	"orghr/internal/contract"
	"orghr/internal/employee"
	"orghr/internal/infra/config"
	"orghr/internal/infra/db"
	"orghr/internal/infra/idgen"
	"orghr/internal/infra/logger"
	"orghr/internal/org"
)

// Role 角色目录（seed 专用最小映射）。
type Role struct {
	bun.BaseModel `bun:"table:roles,alias:r"`
	ID            int64    `bun:"id,pk"`
	Code          string   `bun:"code"`
	Name          string   `bun:"name"`
	Description   string   `bun:"description"`
	IsBuiltin     bool     `bun:"is_builtin"`
	Perms         []string `bun:"perms,type:jsonb"`
}

// DataDict 数据字典（seed 专用最小映射）。
type DataDict struct {
	bun.BaseModel `bun:"table:data_dicts,alias:dd"`
	ID            int64  `bun:"id,pk"`
	Category      string `bun:"category"`
	Code          string `bun:"code"`
	Label         string `bun:"label"`
	SortOrder     int    `bun:"sort_order"`
	Enabled       bool   `bun:"enabled"`
}

func main() {
	cfg := config.Load()
	log := logger.New(cfg.Env)
	database := db.Open(cfg.DatabaseURL)
	defer func() { _ = database.Close() }()
	ctx := context.Background()

	n, _ := database.NewSelect().Table("organizations").Count(ctx)
	if n > 0 {
		log.Info().Int("organizations", n).Msg("core already seeded, skip")
		ensureContracts(ctx, database, log) // 合同独立幂等，补到既有数据上
		return
	}

	pw, err := auth.HashPassword("ChangeMe@123")
	if err != nil {
		log.Fatal().Err(err).Msg("hash password")
	}

	// ---- 组织 ----
	hq := &org.Organization{ID: idgen.NextID(), Code: "hq", Name: "示例集团", ShortName: "集团", Type: "group", Path: "/hq", Status: "active", SortOrder: 1}
	tech := &org.Organization{ID: idgen.NextID(), ParentID: hq.ID, Code: "tech", Name: "科技子公司", ShortName: "科技", Type: "subsidiary", Path: "/hq/tech", Status: "active", SortOrder: 1}
	biz := &org.Organization{ID: idgen.NextID(), ParentID: hq.ID, Code: "biz", Name: "商业子公司", ShortName: "商业", Type: "subsidiary", Path: "/hq/biz", Status: "active", SortOrder: 2}
	mustInsert(ctx, database, log, &[]*org.Organization{hq, tech, biz})

	// ---- 部门 ----
	type deptDef struct {
		code, name, path string
		org              *org.Organization
	}
	defs := []deptDef{
		{"hr", "人力资源部", "/hq/hr", hq},
		{"fin", "财务部", "/hq/fin", hq},
		{"rd", "研发部", "/hq/tech/rd", tech},
		{"pd", "产品部", "/hq/tech/pd", tech},
		{"sales", "销售部", "/hq/biz/sales", biz},
		{"mkt", "市场部", "/hq/biz/mkt", biz},
	}
	depts := make([]*org.Department, 0, len(defs))
	for i, d := range defs {
		depts = append(depts, &org.Department{
			ID: idgen.NextID(), OrgID: d.org.ID, Code: d.code, Name: d.name,
			Path: d.path, Status: "active", SortOrder: i + 1,
		})
	}
	mustInsert(ctx, database, log, &depts)

	// ---- 岗位 ----
	positions := make([]*org.Position, 0)
	for _, d := range depts {
		positions = append(positions,
			&org.Position{ID: idgen.NextID(), OrgID: d.OrgID, DeptID: d.ID, Code: d.Code + "_mgr", Name: d.Name + "经理", JobFamily: "管理", LevelSeq: "M", Headcount: 1, Status: "active"},
			&org.Position{ID: idgen.NextID(), OrgID: d.OrgID, DeptID: d.ID, Code: d.Code + "_spec", Name: d.Name + "专员", JobFamily: "专业", LevelSeq: "P", Headcount: 8, Status: "active"},
		)
	}
	mustInsert(ctx, database, log, &positions)

	// ---- 职级 ----
	levels := make([]*org.JobLevel, 0)
	for i := 3; i <= 8; i++ {
		levels = append(levels, &org.JobLevel{ID: idgen.NextID(), SeqCode: "P", Name: "专业序列", LevelCode: fmt.Sprintf("P%d", i), LevelOrder: i})
	}
	for i := 1; i <= 4; i++ {
		levels = append(levels, &org.JobLevel{ID: idgen.NextID(), SeqCode: "M", Name: "管理序列", LevelCode: fmt.Sprintf("M%d", i), LevelOrder: 100 + i})
	}
	mustInsert(ctx, database, log, &levels)

	// ---- 角色 ----
	roles := []*Role{
		{ID: idgen.NextID(), Code: "super_admin", Name: "超级管理员", IsBuiltin: true, Perms: []string{"*"}},
		{ID: idgen.NextID(), Code: "hr_admin", Name: "人事管理员", IsBuiltin: true, Perms: []string{"org:read", "employee:read", "employee:write", "employee:export", "employee:import", "transfer:apply", "transfer:approve", "contract:read", "contract:write", "stat:read", "report:read"}},
		{ID: idgen.NextID(), Code: "manager", Name: "部门经理", IsBuiltin: true, Perms: []string{"org:read", "employee:read", "transfer:apply", "transfer:approve", "stat:read"}},
		{ID: idgen.NextID(), Code: "employee", Name: "普通员工", IsBuiltin: true, Perms: []string{"employee:read"}},
		{ID: idgen.NextID(), Code: "auditor", Name: "审计员", IsBuiltin: true, Perms: []string{"audit:read", "employee:read", "stat:read"}},
	}
	mustInsert(ctx, database, log, &roles)

	// ---- 数据字典 ----
	dicts := []*DataDict{}
	addDict := func(cat string, kv [][2]string) {
		for i, p := range kv {
			dicts = append(dicts, &DataDict{ID: idgen.NextID(), Category: cat, Code: p[0], Label: p[1], SortOrder: i + 1, Enabled: true})
		}
	}
	addDict("education", [][2]string{{"college", "大专"}, {"bachelor", "本科"}, {"master", "硕士"}, {"phd", "博士"}})
	addDict("employment_status", [][2]string{{"probation", "试用"}, {"active", "正式"}, {"leaving", "离职中"}, {"left", "已离职"}})
	addDict("leave_reason", [][2]string{{"personal", "个人原因"}, {"better_offer", "职业发展"}, {"relocation", "搬迁"}, {"layoff", "裁员"}})
	addDict("contract_type", [][2]string{{"fixed_term", "固定期限"}, {"open_ended", "无固定期限"}, {"intern", "实习"}, {"labor_dispatch", "劳务派遣"}})
	mustInsert(ctx, database, log, &dicts)

	// ---- 员工（约 30 名）----
	surnames := []string{"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴", "徐", "孙", "马", "朱", "胡"}
	given := []string{"伟", "芳", "娜", "敏", "静", "强", "磊", "洋", "勇", "艳", "杰", "涛", "明", "霞", "丽"}
	eduCodes := []string{"college", "bachelor", "master", "phd"}
	statuses := []string{"active", "active", "active", "active", "probation", "left"}
	levelCodes := []string{"P3", "P4", "P5", "P6", "P7", "M1", "M2"}

	employees := make([]*employee.Employee, 0, 30)
	events := make([]*employee.Event, 0)
	for i := 0; i < 30; i++ {
		d := depts[i%len(depts)]
		gender := "male"
		if i%2 == 1 {
			gender = "female"
		}
		status := statuses[i%len(statuses)]
		hiredYear := 2018 + i%8
		hired := time.Date(hiredYear, time.Month(1+i%12), 1+i%27, 0, 0, 0, 0, time.UTC)
		birth := time.Date(1985+i%15, time.Month(1+(i*3)%12), 1+(i*5)%27, 0, 0, 0, 0, time.UTC)
		emp := &employee.Employee{
			ID:               idgen.NextID(),
			EmployeeNo:       fmt.Sprintf("EMP%04d", i+1),
			Name:             surnames[i%len(surnames)] + given[(i*7)%len(given)],
			Gender:           gender,
			Birthday:         &birth,
			Phone:            fmt.Sprintf("13%09d", 100000000+i*137),
			WorkEmail:        fmt.Sprintf("emp%04d@example.com", i+1),
			Education:        eduCodes[i%len(eduCodes)],
			OrgID:            d.OrgID,
			OrgPath:          d.Path,
			DeptID:           d.ID,
			DeptName:         d.Name,
			JobLevel:         levelCodes[i%len(levelCodes)],
			EmploymentType:   "full_time",
			EmploymentStatus: status,
			HiredAt:          &hired,
		}
		if status == "active" {
			reg := hired.AddDate(0, 3, 0)
			emp.RegularAt = &reg
		}
		if status == "left" {
			left := hired.AddDate(2, 0, 0)
			emp.LeftAt = &left
		}
		employees = append(employees, emp)

		events = append(events, &employee.Event{
			ID: idgen.NextID(), EmployeeID: emp.ID, EventType: "onboard",
			Title: fmt.Sprintf("%s 入职 %s", emp.Name, d.Name), OrgPath: d.Path, OccurredAt: hired,
		})
		if status == "left" {
			events = append(events, &employee.Event{
				ID: idgen.NextID(), EmployeeID: emp.ID, EventType: "leave",
				Title: fmt.Sprintf("%s 离职", emp.Name), OrgPath: d.Path, OccurredAt: *emp.LeftAt,
			})
		}
	}
	mustInsert(ctx, database, log, &employees)
	mustInsert(ctx, database, log, &events)

	// ---- 演示账号 ----
	users := []*auth.User{
		{ID: idgen.NextID(), Username: "super_admin", Name: "超级管理员", PasswordHash: pw, Email: "admin@example.com", OrgID: hq.ID, OrgPath: "/hq", Roles: []string{"super_admin"}, IsAdmin: true, IsActive: true},
		{ID: idgen.NextID(), Username: "hr01", Name: "人事小李", PasswordHash: pw, OrgID: hq.ID, OrgPath: "/hq", Roles: []string{"hr_admin"}, IsActive: true},
		{ID: idgen.NextID(), Username: "mgr01", Name: "研发经理", PasswordHash: pw, OrgID: tech.ID, OrgPath: "/hq/tech/rd", Roles: []string{"manager"}, IsActive: true},
		{ID: idgen.NextID(), Username: "emp01", Name: "研发小王", PasswordHash: pw, OrgID: tech.ID, OrgPath: "/hq/tech/rd", Roles: []string{"employee"}, IsActive: true},
	}
	mustInsert(ctx, database, log, &users)

	ensureContracts(ctx, database, log)

	log.Info().
		Int("orgs", 3).Int("depts", len(depts)).Int("positions", len(positions)).
		Int("employees", len(employees)).Int("users", len(users)).
		Msg("seed completed (default password: ChangeMe@123)")
}

// ensureContracts 幂等地为在职员工生成合同（含若干即将到期，供提醒演示）。
func ensureContracts(ctx context.Context, database *bun.DB, log zerolog.Logger) {
	cnt, _ := database.NewSelect().Table("contracts").Count(ctx)
	if cnt > 0 {
		return
	}
	tpl := &contract.Template{
		ID: idgen.NextID(), Name: "标准固定期限劳动合同", Type: "fixed_term",
		Content: "甲方（用人单位）与乙方（员工）订立本固定期限劳动合同……", Enabled: true,
	}
	mustInsert(ctx, database, log, tpl)

	var emps []struct {
		ID      int64      `bun:"id"`
		HiredAt *time.Time `bun:"hired_at"`
	}
	_ = database.NewSelect().Table("employees").Column("id", "hired_at").
		Where("deleted_at IS NULL AND employment_status IN ('active','probation')").
		Order("id").Limit(40).Scan(ctx, &emps)

	now := time.Now()
	contracts := make([]*contract.Contract, 0, len(emps))
	for i, e := range emps {
		start := now.AddDate(-1, 0, 0)
		if e.HiredAt != nil {
			start = *e.HiredAt
		}
		// 错开到期日：部分 10/25 天内到期（命中 30 天提醒），其余 6 月 / 1 年 / 2 年
		var end time.Time
		switch i % 5 {
		case 0:
			end = now.AddDate(0, 0, 10)
		case 1:
			end = now.AddDate(0, 0, 25)
		case 2:
			end = now.AddDate(0, 6, 0)
		case 3:
			end = now.AddDate(1, 0, 0)
		default:
			end = now.AddDate(2, 0, 0)
		}
		id := idgen.NextID()
		contracts = append(contracts, &contract.Contract{
			ID: id, ContractNo: fmt.Sprintf("HT-%d", id), EmployeeID: e.ID, TemplateID: tpl.ID,
			Type: "fixed_term", Status: "active", SignDate: &start, StartDate: &start, EndDate: &end,
			SalaryBand: "10k-20k",
		})
	}
	if len(contracts) > 0 {
		mustInsert(ctx, database, log, &contracts)
	}
	log.Info().Int("contracts", len(contracts)).Msg("contracts seeded")
}

func mustInsert(ctx context.Context, database *bun.DB, log zerolog.Logger, model any) {
	if _, err := database.NewInsert().Model(model).Exec(ctx); err != nil {
		log.Fatal().Err(err).Msg("seed insert failed")
	}
}
