package auth

import (
	"bytes"
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"database/sql"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"

	"orghr/internal/common"
	"orghr/internal/infra/idgen"
)

// ---- SSO 客户端 ----
//
// 刻意只封装登录闭环必需的两个端点：verifyToken（校验回传 token）与 logout（注销通知）。
// 企微推送 / 审批 / 素材上传 / 通讯录刷新等接口均有外部副作用，不在本系统接入范围内。

// ssoUnassignedPath 是自动开通账号的 org_path 哨兵值：非空且不命中任何组织子树，
// 数据范围收敛为空集。绝不能留空串 —— 空 scope 在 common.Subtree 里语义是「不限范围」。
const ssoUnassignedPath = "/sso-unassigned"

// SSOClient 调用 SSO 开放接口，按「签名鉴权」文档做 HMAC-SHA256 请求签名。
type SSOClient struct {
	BaseURL string
	Project string
	secret  string
	hc      *http.Client
}

// NewSSOClient 构造客户端；project 或 secret 为空时返回 nil（SSO 禁用）。
func NewSSOClient(baseURL, project, secret string) *SSOClient {
	if project == "" || secret == "" {
		return nil
	}
	return &SSOClient{
		BaseURL: strings.TrimRight(baseURL, "/"),
		Project: project,
		secret:  secret,
		hc:      &http.Client{Timeout: 10 * time.Second},
	}
}

// SSOUser 是 verifyToken 返回的用户对象（只保留登录映射用到的字段）。
type SSOUser struct {
	WxUID  string `json:"wxUid"`
	Name   string `json:"name"`
	Email  string `json:"email"`
	Avatar string `json:"avatar"`
}

type ssoEnvelope struct {
	Code    int             `json:"code"`
	Message string          `json:"message"`
	Error   string          `json:"error"`
	Data    json.RawMessage `json:"data"`
}

// signSSO 按文档构造签名：canonical = lower(method\npath\nproject\ntimestamp)，
// HMAC-SHA256 后输出小写 hex。path 含 /openapi 前缀、不含域名与 query。
func signSSO(secret, method, path, project, timestamp string) string {
	canonical := strings.ToLower(method + "\n" + path + "\n" + project + "\n" + timestamp)
	mac := hmac.New(sha256.New, []byte(secret))
	mac.Write([]byte(canonical))
	return hex.EncodeToString(mac.Sum(nil))
}

// call 发起 POST+JSON 请求；signed 控制是否带签名头（logout 文档标注无需签名）。
// SSO 侧 HTTP 状态码恒为 200，成败由 body 里的 code 区分（1 成功 / 0 失败）。
func (c *SSOClient) call(ctx context.Context, path string, signed bool, body any, out any) error {
	buf, err := json.Marshal(body)
	if err != nil {
		return err
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, c.BaseURL+path, bytes.NewReader(buf))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	if signed {
		ts := strconv.FormatInt(time.Now().Unix(), 10)
		req.Header.Set("X-Timestamp", ts)
		req.Header.Set("X-Signature", signSSO(c.secret, http.MethodPost, path, c.Project, ts))
	}
	resp, err := c.hc.Do(req)
	if err != nil {
		return err
	}
	defer func() { _ = resp.Body.Close() }()

	var env ssoEnvelope
	if err := json.NewDecoder(resp.Body).Decode(&env); err != nil {
		return fmt.Errorf("sso 响应解析失败: %w", err)
	}
	if env.Code != 1 {
		msg := env.Message
		if msg == "" {
			msg = env.Error
		}
		return fmt.Errorf("sso: %s", msg)
	}
	if out != nil && len(env.Data) > 0 {
		return json.Unmarshal(env.Data, out)
	}
	return nil
}

// VerifyToken 校验 SSO 回传的登录 token，返回用户信息。
func (c *SSOClient) VerifyToken(ctx context.Context, token string) (*SSOUser, error) {
	u := new(SSOUser)
	err := c.call(ctx, "/openapi/sso/verifyToken", true,
		map[string]string{"project": c.Project, "token": token}, u)
	if err != nil {
		return nil, err
	}
	if u.WxUID == "" {
		return nil, errors.New("sso: 返回用户缺少 wxUid")
	}
	return u, nil
}

// Logout 通知 SSO 注销 token（文档标注无需签名）。仅失效该 token 本身，无其它副作用。
func (c *SSOClient) Logout(ctx context.Context, token string) error {
	return c.call(ctx, "/openapi/sso/logout", false,
		map[string]string{"project": c.Project, "token": token}, nil)
}

// ---- 登录服务 ----

// LoginURL 返回登录页跳转地址前缀（前端追加 &cb=回调地址）。
func (c *SSOClient) LoginURL() string {
	return c.BaseURL + "/login?project=" + c.Project
}

// LoginWithSSO 用 SSO 回传 token 登录：verifyToken 确认身份后映射本地账号并签发本系统 JWT。
//
// 映射策略（保守优先，避免误绑改坏数据）：
//  1. wx_uid 精确命中 → 直接登录；
//  2. 未绑定时按 email 精确匹配，且必须唯一命中 → 仅回写 wx_uid 完成绑定（不动其它字段）；
//  3. 仍无命中：autoProvision 关（默认）→ 拒绝；开 → 开通最小权限账号
//     （无密码、非管理员、org_path 哨兵值 → 数据范围为空，待管理员归位）。
//
// 本地账号已停用时按文档要求通知 SSO 注销该 token（防止停用账号借共享登录态静默续命）；
// 「本系统无账号」不注销 —— 用户在其它系统的 SSO 会话不应被本系统杀掉。
func (s *Service) LoginWithSSO(ctx context.Context, ssoToken string) (string, *User, error) {
	if s.sso == nil {
		return "", nil, common.NewError(404, 1105, "SSO 登录未启用")
	}
	su, err := s.sso.VerifyToken(ctx, ssoToken)
	if err != nil {
		return "", nil, common.NewError(401, 1101, "SSO 登录无效")
	}

	u, err := s.matchOrBindSSOUser(ctx, su)
	if err != nil {
		return "", nil, err
	}
	if u == nil {
		if !s.ssoAutoProvision {
			return "", nil, common.NewError(403, 1104, "账号未开通，请联系管理员")
		}
		if u, err = s.provisionSSOUser(ctx, su); err != nil {
			return "", nil, err
		}
	}
	if !u.IsActive {
		// 账号被停用：按 SSO 文档通知注销此 token，避免共享登录态下被反复静默登录。
		notifyCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_ = s.sso.Logout(notifyCtx, ssoToken)
		return "", nil, common.NewError(403, 1103, "账号已停用")
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

// matchOrBindSSOUser 按 wx_uid / email 匹配本地账号；email 命中时回写 wx_uid 绑定。
// 无命中返回 (nil, nil)。
func (s *Service) matchOrBindSSOUser(ctx context.Context, su *SSOUser) (*User, error) {
	u := new(User)
	err := s.db.NewSelect().Model(u).
		Where("wx_uid = ? AND deleted_at IS NULL", su.WxUID).Limit(1).Scan(ctx)
	if err == nil {
		return u, nil
	}
	if !errors.Is(err, sql.ErrNoRows) {
		return nil, common.ErrInternal
	}

	if su.Email == "" {
		return nil, nil
	}
	var byEmail []*User
	err = s.db.NewSelect().Model(&byEmail).
		Where("email = ? AND wx_uid = '' AND deleted_at IS NULL", su.Email).
		Limit(2).Scan(ctx)
	if err != nil {
		return nil, common.ErrInternal
	}
	switch len(byEmail) {
	case 0:
		return nil, nil
	case 1:
		u = byEmail[0]
		u.WxUID = su.WxUID // 只回写绑定列，不覆盖姓名/角色/范围等既有数据
		if _, err := s.db.NewUpdate().Model(u).Column("wx_uid").WherePK().Exec(ctx); err != nil {
			return nil, common.ErrInternal
		}
		return u, nil
	default:
		// 邮箱撞多个账号时拒绝自动绑定，交由管理员人工处理，避免绑错人。
		return nil, common.NewError(409, 1106, "邮箱匹配到多个账号，无法自动绑定，请联系管理员")
	}
}

// provisionSSOUser 自动开通最小权限账号：无密码（password_hash 为空，bcrypt 永不匹配，
// 不可走密码登录）、非管理员、org_path 用哨兵值（数据范围为空集），等待管理员分配。
func (s *Service) provisionSSOUser(ctx context.Context, su *SSOUser) (*User, error) {
	u := &User{
		ID:       idgen.NextID(),
		Username: "wx_" + su.WxUID,
		Name:     su.Name,
		Email:    su.Email,
		Avatar:   su.Avatar,
		WxUID:    su.WxUID,
		OrgPath:  ssoUnassignedPath,
		Roles:    []string{},
		IsActive: true,
	}
	if _, err := s.db.NewInsert().Model(u).Exec(ctx); err != nil {
		return nil, common.ErrInternal
	}
	return u, nil
}

// NotifySSOLogout 注销时通知 SSO 失效 token（尽力而为，失败不阻塞本地登出）。
func (s *Service) NotifySSOLogout(ctx context.Context, ssoToken string) {
	if s.sso == nil || ssoToken == "" {
		return
	}
	_ = s.sso.Logout(ctx, ssoToken)
}

// ---- HTTP handlers ----

// SSOConfig GET /auth/sso/config —— 前端据此渲染 SSO 入口（公开，无敏感信息）。
func (h *Handler) SSOConfig(c *gin.Context) {
	if h.svc.sso == nil {
		common.OK(c, gin.H{"enabled": false})
		return
	}
	common.OK(c, gin.H{"enabled": true, "login_url": h.svc.sso.LoginURL()})
}

type ssoTokenReq struct {
	Token string `json:"token" binding:"required"`
}

// SSOLogin POST /auth/sso/login —— 用 SSO 回传 token 换取本系统会话。
func (h *Handler) SSOLogin(c *gin.Context) {
	var req ssoTokenReq
	if err := c.ShouldBindJSON(&req); err != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	tok, u, err := h.svc.LoginWithSSO(c.Request.Context(), req.Token)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, gin.H{"token": tok, "user": u})
}

// SSOLogout POST /auth/sso/logout —— 登出时通知 SSO 失效 token；恒返回成功。
func (h *Handler) SSOLogout(c *gin.Context) {
	var req ssoTokenReq
	if err := c.ShouldBindJSON(&req); err == nil {
		h.svc.NotifySSOLogout(c.Request.Context(), req.Token)
	}
	common.OK(c, gin.H{"status": "logged_out"})
}
