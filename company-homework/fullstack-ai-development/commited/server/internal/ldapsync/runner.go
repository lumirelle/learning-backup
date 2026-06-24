package ldapsync

import (
	"context"
	"encoding/json"
	"time"

	"github.com/uptrace/bun"

	"orghr/internal/infra/config"
	"orghr/internal/infra/idgen"
)

// SyncLog 映射 ldap_sync_logs：每次实际同步（非 dry-run）落一行。
type SyncLog struct {
	bun.BaseModel `bun:"table:ldap_sync_logs,alias:lsl"`

	ID          int64           `bun:"id,pk" json:"id,string"`
	Trigger     string          `bun:"trigger" json:"trigger"`
	OperatorID  int64           `bun:"operator_id" json:"operator_id,string"`
	Operator    string          `bun:"operator" json:"operator"`
	Status      string          `bun:"status" json:"status"`
	DryRun      bool            `bun:"dry_run" json:"dry_run"`
	Orgs        int             `bun:"orgs" json:"orgs"`
	Depts       int             `bun:"depts" json:"depts"`
	Employees   int             `bun:"employees" json:"employees"`
	Users       int             `bun:"users" json:"users"`
	Deactivated int             `bun:"deactivated" json:"deactivated"`
	Detail      json.RawMessage `bun:"detail,type:jsonb" json:"detail,omitempty"`
	Message     string          `bun:"message" json:"message"`
	DurationMs  int64           `bun:"duration_ms" json:"duration_ms"`
	CreatedAt   time.Time       `bun:"created_at,nullzero,default:now()" json:"created_at"`
}

func sum(c Counts) int { return c.Created + c.Updated }

// RunSync 跑一次完整同步（连接→读取→落库），并把结果写入 ldap_sync_logs（dry-run 不记录）。
// trigger: manual / scheduled / cli；operator 为触发人（定时/CLI 可为空）。
func RunSync(ctx context.Context, db *bun.DB, cfg config.LDAPConfig, trigger string, operatorID int64, operator string, dryRun bool) (*SyncReport, error) {
	start := time.Now()

	rep, err := dialReadApply(ctx, db, cfg, dryRun)
	if dryRun {
		return rep, err // dry-run 仅预览，不记录
	}

	log := &SyncLog{
		ID: idgen.NextID(), Trigger: trigger, OperatorID: operatorID, Operator: operator,
		DryRun: false, DurationMs: time.Since(start).Milliseconds(),
	}
	if err != nil {
		log.Status, log.Message = "failed", err.Error()
	} else {
		log.Status = "success"
		log.Orgs, log.Depts, log.Employees, log.Users = sum(rep.Orgs), sum(rep.Depts), sum(rep.Employees), sum(rep.Users)
		log.Deactivated = rep.Depts.Deactivated + rep.Employees.Deactivated + rep.Users.Deactivated
		if b, e := json.Marshal(rep); e == nil {
			log.Detail = b
		}
	}
	// 记录失败也要落库，故用独立 context，避免请求取消把日志也丢了。
	logCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	_, _ = db.NewInsert().Model(log).Exec(logCtx)

	return rep, err
}

func dialReadApply(ctx context.Context, db *bun.DB, cfg config.LDAPConfig, dryRun bool) (*SyncReport, error) {
	cli, err := Dial(cfg)
	if err != nil {
		return nil, err
	}
	defer cli.Close()

	snap, err := cli.Read()
	if err != nil {
		return nil, err
	}
	return Apply(ctx, db, snap, dryRun)
}

// ListSyncLogs 返回最近的同步记录（倒序）。
func ListSyncLogs(ctx context.Context, db *bun.DB, limit int) ([]*SyncLog, error) {
	if limit <= 0 || limit > 100 {
		limit = 20
	}
	var list []*SyncLog
	err := db.NewSelect().Model(&list).Order("created_at DESC").Limit(limit).Scan(ctx)
	return list, err
}
