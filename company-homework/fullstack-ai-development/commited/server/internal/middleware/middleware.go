package middleware

import (
	"bytes"
	"context"
	"io"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/rs/zerolog"
	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/infra/idgen"
)

// ---- Principal（当前登录主体）----

// Principal 是从 JWT 解析出的当前用户上下文。
type Principal struct {
	UserID   int64
	Username string
	OrgPath  string
	Roles    []string
	IsAdmin  bool
}

const principalKey = "principal"

// CurrentPrincipal 取出当前请求的登录主体，未登录返回 nil。
func CurrentPrincipal(c *gin.Context) *Principal {
	if v, ok := c.Get(principalKey); ok {
		if p, ok := v.(*Principal); ok {
			return p
		}
	}
	return nil
}

// ---- RequestID ----

// RequestID 生成/透传 X-Request-Id，贯穿日志与审计。
func RequestID() gin.HandlerFunc {
	return func(c *gin.Context) {
		rid := c.GetHeader("X-Request-Id")
		if rid == "" {
			rid = strconv.FormatInt(idgen.NextID(), 10)
		}
		c.Set("request_id", rid)
		c.Header("X-Request-Id", rid)
		c.Next()
	}
}

// ---- Recover ----

// Recover 统一兜底 panic，返回 500。
func Recover(log zerolog.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if r := recover(); r != nil {
				log.Error().Interface("panic", r).Str("path", c.FullPath()).Msg("panic recovered")
				common.FailErr(c, common.ErrInternal)
				c.Abort()
			}
		}()
		c.Next()
	}
}

// ---- CORS ----

// CORS 允许跨域（内网 demo 放开）。
func CORS() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Header("Access-Control-Allow-Origin", "*")
		c.Header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
		c.Header("Access-Control-Allow-Headers", "Authorization,Content-Type,X-Request-Id")
		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	}
}

// ---- Auth（JWT）----

// Auth 解析 Bearer JWT 为 Principal 并注入上下文。
func Auth(secret string) gin.HandlerFunc {
	return func(c *gin.Context) {
		h := c.GetHeader("Authorization")
		if !strings.HasPrefix(h, "Bearer ") {
			common.FailErr(c, common.ErrUnauthorized)
			c.Abort()
			return
		}
		tok := strings.TrimPrefix(h, "Bearer ")
		claims := jwt.MapClaims{}
		t, err := jwt.ParseWithClaims(tok, claims, func(*jwt.Token) (any, error) {
			return []byte(secret), nil
		})
		if err != nil || !t.Valid {
			common.FailErr(c, common.ErrTokenInvalid)
			c.Abort()
			return
		}
		c.Set(principalKey, principalFromClaims(claims))
		c.Next()
	}
}

func principalFromClaims(claims jwt.MapClaims) *Principal {
	p := &Principal{}
	// uid 以字符串编码（见 auth.sign），避免 Snowflake int64 经 JSON number 丢精度。
	if v, ok := claims["uid"].(string); ok {
		p.UserID, _ = strconv.ParseInt(v, 10, 64)
	}
	if v, ok := claims["username"].(string); ok {
		p.Username = v
	}
	if v, ok := claims["org_path"].(string); ok {
		p.OrgPath = v
	}
	if v, ok := claims["is_admin"].(bool); ok {
		p.IsAdmin = v
	}
	if arr, ok := claims["roles"].([]any); ok {
		for _, r := range arr {
			if s, ok := r.(string); ok {
				p.Roles = append(p.Roles, s)
			}
		}
	}
	return p
}

// ---- Audit（操作日志）----

// AuditLog 映射 audit_logs 表（中间件写入）。
type AuditLog struct {
	bun.BaseModel `bun:"table:audit_logs,alias:al"`

	ID        int64     `bun:"id,pk,autoincrement"`
	UserID    int64     `bun:"user_id"`
	Username  string    `bun:"username"`
	Method    string    `bun:"method"`
	Path      string    `bun:"path"`
	Status    int       `bun:"status"`
	IP        string    `bun:"ip"`
	RequestID string    `bun:"request_id"`
	Body      string    `bun:"body"`
	CreatedAt time.Time `bun:"created_at,nullzero,default:now()"`
}

// Audit 对写请求落操作日志（GET/OPTIONS 跳过）。
func Audit(database *bun.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		if c.Request.Method == "GET" || c.Request.Method == "OPTIONS" {
			c.Next()
			return
		}
		var body string
		if c.Request.Body != nil {
			b, _ := io.ReadAll(c.Request.Body)
			c.Request.Body = io.NopCloser(bytes.NewReader(b)) // 还原供后续 handler 读取
			if len(b) > 2000 {
				b = b[:2000]
			}
			body = string(b)
		}
		// /auth/* 请求体含凭证（密码 / SSO token），脱敏后再落库
		if strings.Contains(c.Request.URL.Path, "/auth/") {
			body = "[redacted]"
		}

		c.Next()

		al := &AuditLog{
			Method: c.Request.Method,
			Path:   c.FullPath(),
			Status: c.Writer.Status(),
			IP:     c.ClientIP(),
			Body:   body,
		}
		if v, ok := c.Get("request_id"); ok {
			al.RequestID, _ = v.(string)
		}
		if p := CurrentPrincipal(c); p != nil {
			al.UserID = p.UserID
			al.Username = p.Username
		}
		go func() {
			ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
			defer cancel()
			_, _ = database.NewInsert().Model(al).Exec(ctx)
		}()
	}
}
