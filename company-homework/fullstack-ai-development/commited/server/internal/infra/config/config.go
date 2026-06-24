package config

import (
	"os"
	"strconv"
	"time"

	"github.com/joho/godotenv"
)

// Config 持有运行期配置，全部来自环境变量（dev 下可由 .env 注入）。
type Config struct {
	Env            string
	Port           string
	DatabaseURL    string
	JWTSecret      string
	JWTExpireHours int
	RedisURL       string

	// SSO：ProjectID 与 APISecret 均非空才视为启用。
	// AutoProvision 控制「SSO 验证通过但本地无账号」时是否自动开通最小权限账号，
	// 默认关闭（HR 数据敏感，默认仅放行已有账号）。
	SSOBaseURL       string
	SSOProjectID     string
	SSOAPISecret     string
	SSOAutoProvision bool

	// LDAP：URL 与 BindDN/BindPW 均非空才视为启用。
	// 用于把组织架构同步进本系统（部门树 + 人员），LDAP 为唯一事实源。
	LDAP LDAPConfig
}

// LDAPConfig LDAP 目录连接与同步参数。
type LDAPConfig struct {
	URL      string
	BaseDN   string
	UserDN   string
	GroupDN  string
	BindDN   string
	BindPW   string
	StartTLS bool

	// SyncInterval 后台定时同步间隔（Go duration，如 "6h"）。空 = 关闭，仅超管手动同步。
	SyncInterval time.Duration
}

// Enabled 表示 LDAP 同步是否配置完整。
func (c LDAPConfig) Enabled() bool {
	return c.URL != "" && c.BindDN != "" && c.BindPW != ""
}

// Load 读取环境变量并返回配置；缺省值仅用于本地开发。
// APP_PORT 缺省 8090 与前端 dev 代理（web nitro.devProxy → :8090）保持一致，
// 这样 `mise run //server:api` 无需额外前缀即可被前端直连；生产由 docker-compose
// 显式设为 8080（nginx 统一入口反代）。
func Load() *Config {
	_ = godotenv.Load()
	return &Config{
		Env:            getenv("APP_ENV", "dev"),
		Port:           getenv("APP_PORT", "8090"),
		DatabaseURL:    getenv("DATABASE_URL", "postgres://orghr:orghr@localhost:5433/orghr?sslmode=disable"),
		JWTSecret:      getenv("JWT_SECRET", "dev-secret-change-me"),
		JWTExpireHours: atoi(getenv("JWT_EXPIRE_HOURS", "24"), 24),
		RedisURL:       getenv("REDIS_URL", ""),

		SSOBaseURL:       getenv("SSO_BASE_URL", "https://sso.example.com"),
		SSOProjectID:     getenv("SSO_PROJECT_ID", ""),
		SSOAPISecret:     getenv("SSO_API_SECRET", ""),
		SSOAutoProvision: getenv("SSO_AUTO_PROVISION", "") == "true",

		LDAP: LDAPConfig{
			URL:          getenv("LDAP_URL", ""),
			BaseDN:       getenv("LDAP_BASE_DN", ""),
			UserDN:       getenv("LDAP_USER_DN", ""),
			GroupDN:      getenv("LDAP_GROUP_DN", ""),
			BindDN:       getenv("LDAP_BIND_DN", ""),
			BindPW:       getenv("LDAP_BIND_PW", ""),
			StartTLS:     getenv("LDAP_START_TLS", "") == "true",
			SyncInterval: parseDuration(getenv("LDAP_SYNC_INTERVAL", "")),
		},
	}
}

// parseDuration 解析 Go duration（如 "6h"）；空或非法返回 0（关闭）。
func parseDuration(s string) time.Duration {
	if s == "" {
		return 0
	}
	d, err := time.ParseDuration(s)
	if err != nil {
		return 0
	}
	return d
}

func getenv(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}

func atoi(s string, def int) int {
	if n, err := strconv.Atoi(s); err == nil {
		return n
	}
	return def
}
