package main

import (
	"context"
	"os"
	"path/filepath"
	"sort"
	"strings"

	"orghr/internal/infra/config"
	"orghr/internal/infra/db"
	"orghr/internal/infra/logger"
)

// 顺序执行 migrations/*.sql，已执行的记录在 schema_migrations，跳过重复。
func main() {
	cfg := config.Load()
	log := logger.New(cfg.Env)
	database := db.Open(cfg.DatabaseURL)
	defer func() { _ = database.Close() }()
	ctx := context.Background()

	if _, err := database.ExecContext(ctx,
		`CREATE TABLE IF NOT EXISTS schema_migrations(
			version VARCHAR(128) PRIMARY KEY,
			applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW())`); err != nil {
		log.Fatal().Err(err).Msg("init schema_migrations")
	}

	dir := migrationsDir()
	files, _ := filepath.Glob(filepath.Join(dir, "*.sql"))
	sort.Strings(files)
	if len(files) == 0 {
		log.Warn().Str("dir", dir).Msg("no migration files found")
	}

	for _, f := range files {
		version := filepath.Base(f)
		var applied int
		if err := database.NewRaw("SELECT COUNT(1) FROM schema_migrations WHERE version = ?", version).
			Scan(ctx, &applied); err != nil {
			log.Fatal().Err(err).Msg("check applied")
		}
		if applied > 0 {
			log.Info().Str("v", version).Msg("skip (already applied)")
			continue
		}
		b, err := os.ReadFile(f)
		if err != nil {
			log.Fatal().Err(err).Str("f", f).Msg("read file")
		}
		for _, stmt := range splitStatements(string(b)) {
			if _, err := database.ExecContext(ctx, stmt); err != nil {
				log.Fatal().Err(err).Str("v", version).Str("stmt", preview(stmt)).Msg("apply failed")
			}
		}
		if _, err := database.ExecContext(ctx,
			"INSERT INTO schema_migrations(version) VALUES (?)", version); err != nil {
			log.Fatal().Err(err).Msg("record version")
		}
		log.Info().Str("v", version).Msg("applied")
	}
	log.Info().Msg("migrations done")
}

func migrationsDir() string {
	if d := os.Getenv("MIGRATIONS_DIR"); d != "" {
		return d
	}
	return "migrations"
}

// splitStatements 按分号拆分语句并剔除行注释（本项目 DDL 不含语句内分号）。
func splitStatements(s string) []string {
	var out []string
	for _, raw := range strings.Split(s, ";") {
		var b strings.Builder
		for _, line := range strings.Split(raw, "\n") {
			if strings.HasPrefix(strings.TrimSpace(line), "--") {
				continue
			}
			b.WriteString(line)
			b.WriteByte('\n')
		}
		if stmt := strings.TrimSpace(b.String()); stmt != "" {
			out = append(out, stmt)
		}
	}
	return out
}

func preview(s string) string {
	s = strings.TrimSpace(s)
	if len(s) > 80 {
		return s[:80]
	}
	return s
}
