package main

import (
	"context"
	"errors"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/rs/zerolog"
	"github.com/uptrace/bun"

	"orghr/internal/api"
	"orghr/internal/auth"
	"orghr/internal/infra/config"
	"orghr/internal/infra/db"
	"orghr/internal/infra/logger"
	"orghr/internal/ldapsync"
)

func main() {
	cfg := config.Load()
	log := logger.New(cfg.Env)

	database := db.Open(cfg.DatabaseURL)
	defer func() { _ = database.Close() }()

	sso := auth.NewSSOClient(cfg.SSOBaseURL, cfg.SSOProjectID, cfg.SSOAPISecret)
	if sso != nil {
		log.Info().Str("project", cfg.SSOProjectID).Bool("auto_provision", cfg.SSOAutoProvision).Msg("sso enabled")
	}

	r := api.NewRouter(api.Deps{
		DB:               database,
		Log:              log,
		JWTSecret:        cfg.JWTSecret,
		JWTExpireHours:   cfg.JWTExpireHours,
		SSO:              sso,
		SSOAutoProvision: cfg.SSOAutoProvision,
		LDAP:             cfg.LDAP,
	})

	srv := &http.Server{Addr: ":" + cfg.Port, Handler: r}
	go func() {
		log.Info().Str("port", cfg.Port).Msg("api listening")
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Fatal().Err(err).Msg("listen failed")
		}
	}()

	// LDAP 定时同步（默认关；LDAP_SYNC_INTERVAL 配置后启用）。
	stopSync := startLDAPScheduler(cfg, database, log)

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Info().Msg("shutting down")

	if stopSync != nil {
		stopSync()
	}
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	_ = srv.Shutdown(ctx)
}

// startLDAPScheduler 在配置了间隔且 LDAP 启用时，启动后台定时同步，返回停止函数。
func startLDAPScheduler(cfg *config.Config, database *bun.DB, log zerolog.Logger) func() {
	if cfg.LDAP.SyncInterval <= 0 || !cfg.LDAP.Enabled() {
		return nil
	}
	log.Info().Dur("interval", cfg.LDAP.SyncInterval).Msg("ldap scheduled sync enabled")
	ticker := time.NewTicker(cfg.LDAP.SyncInterval)
	done := make(chan struct{})
	go func() {
		for {
			select {
			case <-done:
				return
			case <-ticker.C:
				ctx, cancel := context.WithTimeout(context.Background(), 5*time.Minute)
				rep, err := ldapsync.RunSync(ctx, database, cfg.LDAP, "scheduled", 0, "system", false)
				cancel()
				if err != nil {
					log.Error().Err(err).Msg("ldap scheduled sync failed")
				} else {
					log.Info().Interface("report", rep).Msg("ldap scheduled sync done")
				}
			}
		}
	}()
	return func() { ticker.Stop(); close(done) }
}
