package auth

import (
	"context"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/uptrace/bun"
	"golang.org/x/crypto/bcrypt"

	"orghr/internal/common"
	"orghr/internal/middleware"
)

// User 映射 users（登录账号）表。
type User struct {
	bun.BaseModel `bun:"table:users,alias:u"`

	ID           int64      `bun:"id,pk" json:"id,string"`
	Username     string     `bun:"username" json:"username"`
	Name         string     `bun:"name" json:"name"`
	PasswordHash string     `bun:"password_hash" json:"-"`
	EmployeeID   int64      `bun:"employee_id,nullzero" json:"employee_id,string,omitempty"`
	Email        string     `bun:"email" json:"email"`
	Phone        string     `bun:"phone" json:"phone"`
	Avatar       string     `bun:"avatar" json:"avatar"`
	OrgID        int64      `bun:"org_id" json:"org_id,string"`
	OrgPath      string     `bun:"org_path" json:"org_path"`
	Roles        []string   `bun:"roles,type:jsonb" json:"roles"`
	WxUID        string     `bun:"wx_uid" json:"wx_uid,omitempty"`
	IsAdmin      bool       `bun:"is_admin" json:"is_admin"`
	IsActive     bool       `bun:"is_active" json:"is_active"`
	LastLoginAt  *time.Time `bun:"last_login_at,nullzero" json:"last_login_at,omitempty"`
	CreatedAt    time.Time  `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt    time.Time  `bun:"updated_at,nullzero,default:now()" json:"updated_at"`
}

// Service 处理登录与当前用户。
type Service struct {
	db      *bun.DB
	secret  string
	expireH int

	sso              *SSOClient // nil = SSO 未启用
	ssoAutoProvision bool
}

// NewService 构造 auth 服务。
func NewService(db *bun.DB, secret string, expireH int) *Service {
	return &Service{db: db, secret: secret, expireH: expireH}
}

// WithSSO 启用 SSO 登录（client 为 nil 时保持禁用）。
func (s *Service) WithSSO(client *SSOClient, autoProvision bool) *Service {
	s.sso = client
	s.ssoAutoProvision = autoProvision
	return s
}

// Login 校验账号密码并签发 JWT。
func (s *Service) Login(ctx context.Context, username, password string) (string, *User, error) {
	u := new(User)
	err := s.db.NewSelect().Model(u).
		Where("username = ? AND deleted_at IS NULL", username).Limit(1).Scan(ctx)
	if err != nil {
		return "", nil, common.NewError(401, 1101, "账号或密码错误")
	}
	if !u.IsActive {
		return "", nil, common.NewError(403, 1103, "账号已停用")
	}
	if bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(password)) != nil {
		return "", nil, common.NewError(401, 1101, "账号或密码错误")
	}
	now := time.Now()
	u.LastLoginAt = &now
	_, _ = s.db.NewUpdate().Model(u).Column("last_login_at").WherePK().Exec(ctx)

	tok, err := s.sign(u)
	if err != nil {
		return "", nil, common.ErrInternal
	}
	return tok, u, nil
}

func (s *Service) sign(u *User) (string, error) {
	claims := jwt.MapClaims{
		// uid 以字符串存储：Snowflake int64 超出 JSON number(float64) 安全范围，
		// 直接用数字会在 JWT 往返时丢精度，导致不同用户 id collapse。
		"uid":      strconv.FormatInt(u.ID, 10),
		"username": u.Username,
		"org_path": u.OrgPath,
		"is_admin": u.IsAdmin,
		"roles":    u.Roles,
		"iat":      time.Now().Unix(),
		"exp":      time.Now().Add(time.Duration(s.expireH) * time.Hour).Unix(),
	}
	return jwt.NewWithClaims(jwt.SigningMethodHS256, claims).SignedString([]byte(s.secret))
}

// Me 返回当前用户详情。
func (s *Service) Me(ctx context.Context, uid int64) (*User, error) {
	u := new(User)
	if err := s.db.NewSelect().Model(u).Where("id = ?", uid).Limit(1).Scan(ctx); err != nil {
		return nil, common.ErrNotFound
	}
	return u, nil
}

// ListUsers 列出账号（用于审批人选择 / 账号管理）。password_hash 不会被序列化。
func (s *Service) ListUsers(ctx context.Context, keyword, scope string) ([]*User, error) {
	var list []*User
	q := s.db.NewSelect().Model(&list).Where("deleted_at IS NULL")
	if keyword != "" {
		kw := "%" + keyword + "%"
		q = q.Where("(name ILIKE ? OR username ILIKE ?)", kw, kw)
	}
	q = common.Subtree(q, "org_path", scope)
	err := q.Order("id").Limit(200).Scan(ctx)
	return list, err
}

// Handler 暴露 auth HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

type loginReq struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

// Login POST /auth/login
func (h *Handler) Login(c *gin.Context) {
	var req loginReq
	if err := c.ShouldBindJSON(&req); err != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	tok, u, err := h.svc.Login(c.Request.Context(), req.Username, req.Password)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, gin.H{"token": tok, "user": u})
}

// Me GET /auth/me
func (h *Handler) Me(c *gin.Context) {
	p := middleware.CurrentPrincipal(c)
	if p == nil {
		common.FailErr(c, common.ErrUnauthorized)
		return
	}
	u, err := h.svc.Me(c.Request.Context(), p.UserID)
	if err != nil {
		common.FailErr(c, common.ErrNotFound)
		return
	}
	common.OK(c, u)
}

// Users GET /users —— 列出账号（审批人选择 / 账号管理），非管理员收敛到本组织子树。
func (h *Handler) Users(c *gin.Context) {
	scope := ""
	if p := middleware.CurrentPrincipal(c); p != nil && !p.IsAdmin {
		scope = p.OrgPath
	}
	list, err := h.svc.ListUsers(c.Request.Context(), c.Query("keyword"), scope)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, list)
}

// Register 注册 auth 路由：公开组挂登录，鉴权组挂 me/logout。
// SSO 三个端点均为纯静态路径（radix 安全），handler 对「未启用」自降级。
func Register(pub, authed *gin.RouterGroup, h *Handler) {
	pub.POST("/auth/login", h.Login)
	pub.GET("/auth/sso/config", h.SSOConfig)
	pub.POST("/auth/sso/login", h.SSOLogin)
	pub.POST("/auth/sso/logout", h.SSOLogout) // 公开：浏览器登出时本地会话可能已失效
	authed.GET("/auth/me", h.Me)
	authed.GET("/users", h.Users)
	authed.POST("/auth/logout", func(c *gin.Context) {
		common.OK(c, gin.H{"status": "logged_out"}) // 无状态：客户端丢弃 token
	})
}

// HashPassword 生成 bcrypt 哈希（供 seed 使用）。
func HashPassword(plain string) (string, error) {
	b, err := bcrypt.GenerateFromPassword([]byte(plain), bcrypt.DefaultCost)
	return string(b), err
}
