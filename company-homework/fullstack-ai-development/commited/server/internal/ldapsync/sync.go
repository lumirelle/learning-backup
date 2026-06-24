package ldapsync

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"time"

	"github.com/uptrace/bun"

	"orghr/internal/infra/idgen"
)

// extSource 标记由本同步维护的行，与自建/业务数据隔离。
const extSource = "ldap"

// rootExtID 是 LDAP 同步出的根组织（代表项目/公司，LDAP 已去首级）的稳定键。
const rootExtID = "root"

var errDryRun = errors.New("dry-run rollback")

// Counts 是一类实体的同步计数。
type Counts struct {
	Created     int `json:"created"`
	Updated     int `json:"updated"`
	Deactivated int `json:"deactivated"`
}

// SyncReport 是一次同步（或 dry-run）的结果摘要。
type SyncReport struct {
	DryRun    bool     `json:"dry_run"`
	Orgs      Counts   `json:"orgs"`
	Depts     Counts   `json:"depts"`
	Employees Counts   `json:"employees"`
	Users     Counts   `json:"users"`
	Warnings  []string `json:"warnings,omitempty"`
}

// ---- 本地模型（映射既有表，仅声明同步需读写的列）----

type orgRow struct {
	bun.BaseModel `bun:"table:organizations"`
	ID            int64  `bun:"id,pk"`
	Code          string `bun:"code"`
	Name          string `bun:"name"`
	Type          string `bun:"type"`
	Path          string `bun:"path"`
	Status        string `bun:"status"`
	ExtSource     string `bun:"ext_source"`
	ExtID         string `bun:"ext_id"`
}

type deptRow struct {
	bun.BaseModel `bun:"table:departments"`
	ID            int64  `bun:"id,pk"`
	OrgID         int64  `bun:"org_id"`
	ParentID      int64  `bun:"parent_id,nullzero"`
	Code          string `bun:"code"`
	Name          string `bun:"name"`
	Path          string `bun:"path"`
	Status        string `bun:"status"`
	ExtSource     string `bun:"ext_source"`
	ExtID         string `bun:"ext_id"`
}

type empRow struct {
	bun.BaseModel `bun:"table:employees"`
	ID            int64  `bun:"id,pk"`
	EmployeeNo    string `bun:"employee_no"`
	UserID        int64  `bun:"user_id,nullzero"`
	Name          string `bun:"name"`
	WorkEmail     string `bun:"work_email"`
	OrgID         int64  `bun:"org_id"`
	OrgPath       string `bun:"org_path"`
	DeptID        int64  `bun:"dept_id,nullzero"`
	DeptName      string `bun:"dept_name"`
	Status        string `bun:"employment_status"`
	ExtSource     string `bun:"ext_source"`
	ExtID         string `bun:"ext_id"`
}

type userRow struct {
	bun.BaseModel `bun:"table:users"`
	ID            int64  `bun:"id,pk"`
	Username      string `bun:"username"`
	Name          string `bun:"name"`
	PasswordHash  string `bun:"password_hash"`
	EmployeeID    int64  `bun:"employee_id,nullzero"`
	Email         string `bun:"email"`
	Avatar        string `bun:"avatar"`
	WxUID         string `bun:"wx_uid"`
	OrgID         int64  `bun:"org_id"`
	OrgPath       string `bun:"org_path"`
	IsActive      bool   `bun:"is_active"`
	ExtSource     string `bun:"ext_source"`
	ExtID         string `bun:"ext_id"`
}

// Apply 把 LDAP 快照同步进库（幂等）。dryRun=true 时在事务内计算后回滚，不落库。
// 整个过程在单事务内：LDAP 来源的部门/人员被 upsert，快照中已消失的 LDAP 行被软删除收敛；
// 非 LDAP 来源（自建/业务）的行完全不受影响。
func Apply(ctx context.Context, db *bun.DB, snap *Snapshot, dryRun bool) (*SyncReport, error) {
	pv := BuildPreview(snap)
	rep := &SyncReport{DryRun: dryRun, Warnings: pv.Warnings}

	err := db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		s := &syncer{tx: tx, rep: rep, deptByPath: map[string]*deptRow{}}
		if err := s.run(ctx, pv); err != nil {
			return err
		}
		if dryRun {
			return errDryRun // 回滚，但 rep 已填好
		}
		return nil
	})
	if err != nil && !errors.Is(err, errDryRun) {
		return nil, err
	}
	return rep, nil
}

type syncer struct {
	tx         bun.Tx
	rep        *SyncReport
	org        *orgRow
	deptByPath map[string]*deptRow
	seenDept   []string // 本次出现的部门 ext_id
	seenEmp    []string // 本次出现的人员 ext_id
	seenUser   []string
}

func (s *syncer) run(ctx context.Context, pv *Preview) error {
	if err := s.ensureRootOrg(ctx); err != nil {
		return err
	}
	// 部门：parent-first 遍历，保证父节点先建、可取父 path/id。
	for _, root := range pv.Tree {
		if err := s.upsertDeptTree(ctx, root, s.org.Path, 0); err != nil {
			return err
		}
	}
	if err := s.upsertUsers(ctx, pv); err != nil {
		return err
	}
	return s.deactivateStale(ctx)
}

func (s *syncer) ensureRootOrg(ctx context.Context) error {
	o, err := scanOne[orgRow](ctx, s.tx, "ext_source = ? AND ext_id = ? AND deleted_at IS NULL", extSource, rootExtID)
	if err != nil {
		return err
	}
	if o == nil {
		o = &orgRow{
			ID: idgen.NextID(), Code: "ldap", Name: "组织架构（LDAP）", Type: "group",
			Status: "active", ExtSource: extSource, ExtID: rootExtID,
		}
		o.Path = "/o" + itoa(o.ID)
		if _, err := s.tx.NewInsert().Model(o).Exec(ctx); err != nil {
			return fmt.Errorf("建根组织失败: %w", err)
		}
		s.rep.Orgs.Created++
	}
	s.org = o
	return nil
}

func (s *syncer) upsertDeptTree(ctx context.Context, node *DeptNode, parentPath string, parentID int64) error {
	extID := node.EntryUUID
	if extID == "" { // 路径中间补全的节点无 entryUUID，用 path 作稳定键
		extID = "path:" + node.Path
	}
	s.seenDept = append(s.seenDept, extID)

	d, err := scanOne[deptRow](ctx, s.tx, "ext_source = ? AND ext_id = ? AND deleted_at IS NULL", extSource, extID)
	if err != nil {
		return err
	}
	if d == nil {
		d = &deptRow{
			ID: idgen.NextID(), OrgID: s.org.ID, ParentID: parentID,
			Code: extID, Name: node.Name, Status: "active",
			ExtSource: extSource, ExtID: extID,
		}
		d.Path = parentPath + "/d" + itoa(d.ID)
		if _, err := s.tx.NewInsert().Model(d).Exec(ctx); err != nil {
			return fmt.Errorf("建部门 %s 失败: %w", node.Path, err)
		}
		s.rep.Depts.Created++
	} else {
		newPath := parentPath + "/d" + itoa(d.ID)
		if d.Name != node.Name || d.ParentID != parentID || d.Path != newPath || d.Status != "active" {
			d.Name, d.ParentID, d.Path, d.Status = node.Name, parentID, newPath, "active"
			if _, err := s.tx.NewUpdate().Model(d).
				Column("name", "parent_id", "path", "status").
				Set("updated_at = now()").WherePK().Exec(ctx); err != nil {
				return fmt.Errorf("更新部门 %s 失败: %w", node.Path, err)
			}
			s.rep.Depts.Updated++
		}
	}
	s.deptByPath[node.Path] = d
	for _, c := range node.Children {
		if err := s.upsertDeptTree(ctx, c, d.Path, d.ID); err != nil {
			return err
		}
	}
	return nil
}

func (s *syncer) upsertUsers(ctx context.Context, pv *Preview) error {
	for _, pu := range pv.Users {
		extID := pu.WxUID
		if extID == "" {
			extID = pu.UID
		}
		if extID == "" {
			s.rep.Warnings = append(s.rep.Warnings, "用户缺少 wxUid/uid，已跳过")
			continue
		}
		// 定位部门（取第一个可解析的部门路径）。
		var dept *deptRow
		if len(pu.DeptPaths) > 0 {
			dept = s.deptByPath[pu.DeptPaths[0]]
		}
		orgPath, deptID, deptName := s.org.Path, int64(0), ""
		if dept != nil {
			orgPath, deptID, deptName = dept.Path, dept.ID, dept.Name
		}

		if err := s.upsertEmpUser(ctx, pu, extID, orgPath, deptID, deptName); err != nil {
			return err
		}
	}
	return nil
}

// upsertEmpUser upsert 一名人员对应的 employee 与 login user，并互相挂接。
//
// 关键：employee_no / username / wx_uid 都有全局唯一索引。先按 LDAP 键找，找不到时
// 再按自然键「认领」已有行（典型如 SSO 自动开通的同一人 ext_source=”），把它改造成
// LDAP 托管，避免与全局唯一索引冲突而 INSERT 失败。
func (s *syncer) upsertEmpUser(ctx context.Context, pu PreviewUser, extID, orgPath string, deptID int64, deptName string) error {
	s.seenEmp = append(s.seenEmp, extID)
	s.seenUser = append(s.seenUser, extID)

	// ---- employee：LDAP 键 → employee_no 认领 → 新建 ----
	e, err := scanOne[empRow](ctx, s.tx, "ext_source = ? AND ext_id = ? AND deleted_at IS NULL", extSource, extID)
	if err != nil {
		return err
	}
	if e == nil {
		if e, err = scanOne[empRow](ctx, s.tx, "employee_no = ? AND deleted_at IS NULL", pu.UID); err != nil {
			return err
		}
	}
	empNew := e == nil
	if empNew {
		e = &empRow{
			ID: idgen.NextID(), EmployeeNo: pu.UID, Name: pu.DisplayName,
			WorkEmail: pu.Mail, OrgID: s.org.ID, OrgPath: orgPath, DeptID: deptID, DeptName: deptName,
			Status: "active", ExtSource: extSource, ExtID: extID,
		}
		if _, err := s.tx.NewInsert().Model(e).Exec(ctx); err != nil {
			return fmt.Errorf("建员工 %s 失败: %w", pu.UID, err)
		}
		s.rep.Employees.Created++
	} else {
		e.Name, e.WorkEmail, e.OrgID, e.OrgPath, e.DeptID, e.DeptName, e.Status = pu.DisplayName, pu.Mail, s.org.ID, orgPath, deptID, deptName, "active"
		e.ExtSource, e.ExtID = extSource, extID
		if _, err := s.tx.NewUpdate().Model(e).
			Column("name", "work_email", "org_id", "org_path", "dept_id", "dept_name", "employment_status", "ext_source", "ext_id").
			Set("updated_at = now()").WherePK().Exec(ctx); err != nil {
			return fmt.Errorf("更新员工 %s 失败: %w", pu.UID, err)
		}
		s.rep.Employees.Updated++
	}

	// ---- login user：LDAP 键 → wx_uid 认领 → username 认领 → 新建 ----
	u, err := scanOne[userRow](ctx, s.tx, "ext_source = ? AND ext_id = ? AND deleted_at IS NULL", extSource, extID)
	if err != nil {
		return err
	}
	if u == nil && pu.WxUID != "" {
		if u, err = scanOne[userRow](ctx, s.tx, "wx_uid = ? AND deleted_at IS NULL", pu.WxUID); err != nil {
			return err
		}
	}
	if u == nil {
		if u, err = scanOne[userRow](ctx, s.tx, "username = ? AND deleted_at IS NULL", pu.UID); err != nil {
			return err
		}
	}
	if u == nil {
		u = &userRow{
			ID: idgen.NextID(), Username: pu.UID, Name: pu.DisplayName, PasswordHash: "",
			EmployeeID: e.ID, Email: pu.Mail, WxUID: pu.WxUID, OrgID: s.org.ID, OrgPath: orgPath,
			IsActive: true, ExtSource: extSource, ExtID: extID,
		}
		if _, err := s.tx.NewInsert().Model(u).Exec(ctx); err != nil {
			return fmt.Errorf("建账号 %s 失败: %w", pu.UID, err)
		}
		s.rep.Users.Created++
	} else {
		// 认领/更新：只改 LDAP 维护的字段，保留 password_hash / is_admin / roles 等既有权限。
		u.Username, u.Name, u.Email, u.WxUID, u.OrgID, u.OrgPath, u.EmployeeID, u.IsActive = pu.UID, pu.DisplayName, pu.Mail, pu.WxUID, s.org.ID, orgPath, e.ID, true
		u.ExtSource, u.ExtID = extSource, extID
		if _, err := s.tx.NewUpdate().Model(u).
			Column("username", "name", "email", "wx_uid", "org_id", "org_path", "employee_id", "is_active", "ext_source", "ext_id").
			Set("updated_at = now()").WherePK().Exec(ctx); err != nil {
			return fmt.Errorf("更新账号 %s 失败: %w", pu.UID, err)
		}
		s.rep.Users.Updated++
	}

	// 回挂 employee.user_id
	if e.UserID != u.ID {
		e.UserID = u.ID
		if _, err := s.tx.NewUpdate().Model(e).Column("user_id").WherePK().Exec(ctx); err != nil {
			return fmt.Errorf("挂接员工账号 %s 失败: %w", pu.UID, err)
		}
	}
	return nil
}

// scanOne 按条件查一行；无命中返回 (nil, nil)，真实错误才返回 error。
func scanOne[T any](ctx context.Context, tx bun.Tx, where string, args ...any) (*T, error) {
	row := new(T)
	err := tx.NewSelect().Model(row).Where(where, args...).Limit(1).Scan(ctx)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}
	return row, nil
}

// deactivateStale 把 LDAP 来源、但本次快照中已消失的部门/人员/账号软删除（LDAP 为准）。
func (s *syncer) deactivateStale(ctx context.Context) error {
	n, err := softDeleteMissing(ctx, s.tx, "departments", s.seenDept)
	if err != nil {
		return err
	}
	s.rep.Depts.Deactivated += n

	if n, err = softDeleteMissing(ctx, s.tx, "employees", s.seenEmp); err != nil {
		return err
	}
	s.rep.Employees.Deactivated += n

	if n, err = softDeleteMissing(ctx, s.tx, "users", s.seenUser); err != nil {
		return err
	}
	s.rep.Users.Deactivated += n
	return nil
}

// softDeleteMissing 软删除某表中 ext_source='ldap' 且 ext_id 不在 keep 内的行。
func softDeleteMissing(ctx context.Context, tx bun.Tx, table string, keep []string) (int, error) {
	q := tx.NewUpdate().Table(table).
		Set("deleted_at = ?", time.Now()).
		Where("ext_source = ? AND deleted_at IS NULL", extSource)
	if len(keep) > 0 {
		q = q.Where("ext_id NOT IN (?)", bun.In(keep))
	}
	res, err := q.Exec(ctx)
	if err != nil {
		return 0, fmt.Errorf("收敛 %s 失败: %w", table, err)
	}
	n, _ := res.RowsAffected()
	return int(n), nil
}

func itoa(n int64) string { return fmt.Sprintf("%d", n) }
