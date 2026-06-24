package ldapsync

import (
	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/infra/config"
	"orghr/internal/middleware"
)

// Handler 暴露 LDAP 同步的管理接口（仅管理员）。LDAP 未配置时各端点返回「未启用」。
type Handler struct {
	cfg config.LDAPConfig
	db  *bun.DB
}

// NewHandler 构造 handler。
func NewHandler(cfg config.LDAPConfig, db *bun.DB) *Handler {
	return &Handler{cfg: cfg, db: db}
}

func (h *Handler) requireAdmin(c *gin.Context) bool {
	p := middleware.CurrentPrincipal(c)
	if p == nil || !p.IsAdmin {
		common.FailErr(c, common.ErrForbidden)
		return false
	}
	return true
}

func (h *Handler) ensureEnabled(c *gin.Context) bool {
	if !h.cfg.Enabled() {
		common.FailErr(c, common.NewError(404, 1107, "LDAP 未启用"))
		return false
	}
	return true
}

// Config GET /ldap/config —— 返回 LDAP 是否启用及连接概要（不含密码），供前端展示。
func (h *Handler) Config(c *gin.Context) {
	if !h.requireAdmin(c) {
		return
	}
	common.OK(c, gin.H{
		"enabled":  h.cfg.Enabled(),
		"url":      h.cfg.URL,
		"base_dn":  h.cfg.BaseDN,
		"user_dn":  h.cfg.UserDN,
		"group_dn": h.cfg.GroupDN,
		"bind_dn":  h.cfg.BindDN,
	})
}

// Preview GET /ldap/preview —— 只读：连接 LDAP，返回重建出的部门树 + 人员清单，不写库。
func (h *Handler) Preview(c *gin.Context) {
	if !h.requireAdmin(c) || !h.ensureEnabled(c) {
		return
	}
	cli, err := Dial(h.cfg)
	if err != nil {
		common.FailErr(c, common.NewError(502, 1108, err.Error()))
		return
	}
	defer cli.Close()

	snap, err := cli.Read()
	if err != nil {
		common.FailErr(c, common.NewError(502, 1108, err.Error()))
		return
	}
	common.OK(c, BuildPreview(snap))
}

// Sync POST /ldap/sync —— 连接 LDAP 并把组织/人员同步进库（幂等，事务），并记录同步操作。
// dry_run=true 时只返回将发生的变更计划，不落库、不记录。
func (h *Handler) Sync(c *gin.Context) {
	if !h.requireAdmin(c) || !h.ensureEnabled(c) {
		return
	}
	dryRun := c.Query("dry_run") == "true"

	var opID int64
	var op string
	if p := middleware.CurrentPrincipal(c); p != nil {
		opID, op = p.UserID, p.Username
	}

	report, err := RunSync(c.Request.Context(), h.db, h.cfg, "manual", opID, op, dryRun)
	if err != nil {
		common.FailErr(c, common.NewError(502, 1108, "LDAP 同步失败: "+err.Error()))
		return
	}
	common.OK(c, report)
}

// SyncLogs GET /ldap/sync-logs —— 同步操作历史（倒序）。
func (h *Handler) SyncLogs(c *gin.Context) {
	if !h.requireAdmin(c) {
		return
	}
	logs, err := ListSyncLogs(c.Request.Context(), h.db, 20)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, logs)
}

// Register 注册 LDAP 管理路由（均为静态路径，radix 安全；admin 级在 handler 内校验）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/ldap/config", h.Config)
	authed.GET("/ldap/preview", h.Preview)
	authed.POST("/ldap/sync", h.Sync)
	authed.GET("/ldap/sync-logs", h.SyncLogs)
}
