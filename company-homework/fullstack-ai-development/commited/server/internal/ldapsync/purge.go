package ldapsync

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/uptrace/bun"
)

// PurgeReport 是清理演示数据的结果计数。
type PurgeReport struct {
	DryRun        bool `json:"dry_run"`
	Organizations int  `json:"organizations"`
	Departments   int  `json:"departments"`
	Employees     int  `json:"employees"`
	Positions     int  `json:"positions"`
	Contracts     int  `json:"contracts"`
	Performance   int  `json:"performance"`
	ArchiveItems  int  `json:"archive_items"`
	Events        int  `json:"events"`
	JobHistory    int  `json:"job_history"`
	Processes     int  `json:"processes"`
	Approvals     int  `json:"approvals"`
}

// PurgeDemo 清理「演示/假数据」：软删 ext_source=” 的组织/部门/员工/岗位，并清掉这些
// 演示员工关联的业务数据（合同/奖惩/档案借阅/动态/履历/流程/审批），让组织页与花名册只剩
// LDAP 同步来的真实数据。**保留登录账号（users）**——否则会把 super_admin 等后台入口删掉。
//
// 说明：LDAP 同步只产出 org/dept/employee/user，不产出任何业务数据；故业务数据按「演示员工」
// 范围清理（员工关联表按 employee_id 圈定，审批/档案分类等无员工外键的表本期全部为演示数据，
// 一次性清空）。dryRun=true 时事务内计算后回滚，不落库。
func PurgeDemo(ctx context.Context, db *bun.DB, dryRun bool) (*PurgeReport, error) {
	rep := &PurgeReport{DryRun: dryRun}

	err := db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		// 演示员工 id（ext_source 为空、未删）
		var demoIDs []int64
		if err := tx.NewSelect().Table("employees").Column("id").
			Where("ext_source = '' AND deleted_at IS NULL").Scan(ctx, &demoIDs); err != nil {
			return fmt.Errorf("取演示员工失败: %w", err)
		}

		now := time.Now()
		soft := func(table, where string, args ...any) (int, error) {
			res, err := tx.NewUpdate().Table(table).Set("deleted_at = ?", now).
				Where("deleted_at IS NULL").Where(where, args...).Exec(ctx)
			if err != nil {
				return 0, fmt.Errorf("软删 %s 失败: %w", table, err)
			}
			n, _ := res.RowsAffected()
			return int(n), nil
		}
		hard := func(table, where string, args ...any) (int, error) {
			q := tx.NewDelete().Table(table)
			if where != "" {
				q = q.Where(where, args...)
			} else {
				q = q.Where("1 = 1")
			}
			res, err := q.Exec(ctx)
			if err != nil {
				return 0, fmt.Errorf("清理 %s 失败: %w", table, err)
			}
			n, _ := res.RowsAffected()
			return int(n), nil
		}

		var err error
		if len(demoIDs) > 0 {
			// 演示员工关联的业务数据
			if rep.JobHistory, err = hard("employee_job_history", "employee_id IN (?)", bun.In(demoIDs)); err != nil {
				return err
			}
			if rep.Events, err = hard("hr_events", "employee_id IN (?)", bun.In(demoIDs)); err != nil {
				return err
			}
			if rep.Processes, err = hard("hr_processes", "employee_id IN (?)", bun.In(demoIDs)); err != nil {
				return err
			}
			if rep.Contracts, err = soft("contracts", "employee_id IN (?)", bun.In(demoIDs)); err != nil {
				return err
			}
			if rep.Performance, err = soft("rewards_punishments", "employee_id IN (?)", bun.In(demoIDs)); err != nil {
				return err
			}
			if rep.ArchiveItems, err = soft("archive_items", "employee_id IN (?) OR employee_id IS NULL", bun.In(demoIDs)); err != nil {
				return err
			}
		}
		// 无员工外键、本期全为演示的表：一次性清空
		if rep.Approvals, err = hard("approval_steps", ""); err != nil {
			return err
		}
		if _, err = hard("approval_instances", ""); err != nil {
			return err
		}
		if _, err = hard("archive_borrows", ""); err != nil {
			return err
		}
		if _, err = hard("archive_categories", ""); err != nil {
			return err
		}

		// 演示组织结构（保留 LDAP 来源）
		if rep.Positions, err = soft("positions", "1 = 1"); err != nil {
			return err
		}
		if rep.Employees, err = soft("employees", "ext_source = ''"); err != nil {
			return err
		}
		if rep.Departments, err = soft("departments", "ext_source = ''"); err != nil {
			return err
		}
		if rep.Organizations, err = soft("organizations", "ext_source = ''"); err != nil {
			return err
		}

		if dryRun {
			return errDryRun
		}
		return nil
	})
	if err != nil && !errors.Is(err, errDryRun) {
		return nil, err
	}
	return rep, nil
}
