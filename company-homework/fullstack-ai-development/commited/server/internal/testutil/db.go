// Package testutil 提供 DB 集成测试的公共脚手架：连接独立测试库、不可用时优雅跳过、
// 用例间清表隔离。约定测试库为 orghr_test（与开发库 orghr 隔离），可用 TEST_DATABASE_URL 覆盖。
//
// 准备测试库（一次性）：
//
//	psql -h localhost -p 5433 -U orghr -d postgres -c 'CREATE DATABASE orghr_test'
//	DATABASE_URL=postgres://orghr:orghr@localhost:5433/orghr_test?sslmode=disable go run ./cmd/migrate
//
// 或直接 `mise run //server:test-db`（见 server/mise.toml）。
package testutil

import (
	"context"
	"os"
	"strings"
	"testing"
	"time"

	"github.com/uptrace/bun"

	"orghr/internal/infra/db"
)

const defaultDSN = "postgres://orghr:orghr@localhost:5433/orghr_test?sslmode=disable"

func dsn() string {
	if v := os.Getenv("TEST_DATABASE_URL"); v != "" {
		return v
	}
	return defaultDSN
}

// 全局建议锁：所有 DB 集成测试共享同一测试库，`go test ./...` 会并行运行各包的测试
// 进程；用一把 Postgres advisory lock 把「跨进程」的 DB 测试串行化，避免某个包的清表
// 把另一个包正在用的数据 TRUNCATE 掉（表现为死锁/查不到数据）。锁随持有它的连接保持，
// 在用例结束时释放。键为任意固定值，全仓一致即可；直接内联到 SQL（常量、无注入风险）。
const lockSQL = "SELECT pg_advisory_lock(6872747374)"
const unlockSQL = "SELECT pg_advisory_unlock(6872747374)"

// DB 打开测试库连接；若不可达（未起容器/未建库）则 t.Skip，避免在无 DB 环境下误判失败。
// 同时获取一把全局 advisory lock，把跨包的 DB 测试串行化；连接与锁在测试结束时自动释放。
func DB(t *testing.T) *bun.DB {
	t.Helper()
	d := db.Open(dsn())
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
	defer cancel()
	if err := d.PingContext(ctx); err != nil {
		_ = d.Close()
		t.Skipf("测试库不可达（%v）——请先 `mise run //server:test-db` 或设置 TEST_DATABASE_URL", err)
	}
	// 在一条专用连接上持锁（advisory lock 是会话级的，必须固定连接）。
	conn, err := d.Conn(context.Background())
	if err != nil {
		_ = d.Close()
		t.Fatalf("获取连接失败: %v", err)
	}
	if _, err := conn.ExecContext(context.Background(), lockSQL); err != nil {
		_ = conn.Close()
		_ = d.Close()
		t.Fatalf("获取测试锁失败: %v", err)
	}
	t.Cleanup(func() {
		_, _ = conn.ExecContext(context.Background(), unlockSQL)
		_ = conn.Close()
		_ = d.Close()
	})
	return d
}

// Reset 清空所有业务表（保留 schema_migrations），用例之间互不污染。
func Reset(t *testing.T, d *bun.DB) {
	t.Helper()
	ctx := context.Background()
	var tables []string
	err := d.NewSelect().
		ColumnExpr("tablename").
		TableExpr("pg_tables").
		Where("schemaname = 'public' AND tablename <> 'schema_migrations'").
		Scan(ctx, &tables)
	if err != nil {
		t.Fatalf("列出表失败: %v", err)
	}
	if len(tables) == 0 {
		return
	}
	if _, err := d.ExecContext(ctx, "TRUNCATE "+strings.Join(tables, ", ")+" RESTART IDENTITY CASCADE"); err != nil {
		t.Fatalf("清表失败: %v", err)
	}
}
