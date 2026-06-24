package api

import (
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog"
	"github.com/uptrace/bun"

	"orghr/internal/analytics"
	"orghr/internal/approval"
	"orghr/internal/archive"
	"orghr/internal/auth"
	"orghr/internal/care"
	"orghr/internal/contract"
	"orghr/internal/employee"
	"orghr/internal/infra/config"
	"orghr/internal/ldapsync"
	"orghr/internal/middleware"
	"orghr/internal/org"
	"orghr/internal/performance"
	"orghr/internal/process"
	"orghr/internal/report"
	"orghr/internal/timeline"
)

// Deps 是装配路由所需依赖。
type Deps struct {
	DB             *bun.DB
	Log            zerolog.Logger
	JWTSecret      string
	JWTExpireHours int

	// SSO 登录（可选）：SSO 为 nil 时相关端点自降级为「未启用」。
	SSO              *auth.SSOClient
	SSOAutoProvision bool

	// LDAP 组织/人员同步（可选）：未启用时相关端点返回「未启用」。
	LDAP config.LDAPConfig
}

// NewRouter 构建 gin 引擎并注册全部路由与中间件。
func NewRouter(d Deps) *gin.Engine {
	gin.SetMode(gin.ReleaseMode)
	r := gin.New()
	r.Use(middleware.RequestID(), middleware.Recover(d.Log), middleware.CORS())

	r.GET("/healthz", func(c *gin.Context) {
		c.JSON(200, gin.H{"code": 0, "message": "ok", "data": gin.H{"status": "healthy"}})
	})

	v1 := r.Group("/api/v1")
	v1.Use(middleware.Audit(d.DB))

	pub := v1.Group("")    // 公开（登录）
	authed := v1.Group("") // 需鉴权
	authed.Use(middleware.Auth(d.JWTSecret))

	// auth（含可选 SSO 登录）
	authSvc := auth.NewService(d.DB, d.JWTSecret, d.JWTExpireHours).WithSSO(d.SSO, d.SSOAutoProvision)
	auth.Register(pub, authed, auth.NewHandler(authSvc))
	// org
	orgH := org.NewHandler(org.NewService(org.NewRepo(d.DB)))
	org.Register(authed, orgH)
	org.RegisterMove(authed, orgH) // 节点移动（/dept-move /org-move）
	// employee（service 复用给 process）
	empSvc := employee.NewService(employee.NewRepo(d.DB))
	empH := employee.NewHandler(empSvc)
	employee.Register(authed, empH)
	employee.RegisterIO(authed, empH) // 花名册导入/导出（/roster-export|template|import）
	// 通用审批引擎（process 与 archive 借阅共用）
	appr := approval.New()
	// process（入离调转/转正 + 通用审批引擎，生效落地员工/履历/动态）
	process.Register(authed, process.NewHandler(process.NewService(d.DB, appr, empSvc)))
	// timeline（人事动态 + 操作日志读取）
	timeline.Register(authed, timeline.NewHandler(timeline.NewService(d.DB)))
	// analytics（多维统计看板）
	analytics.Register(authed, analytics.NewHandler(analytics.NewService(d.DB)))
	// contract（劳动合同 + 到期提醒 + 模板）
	contract.Register(authed, contract.NewHandler(contract.NewService(d.DB)))
	// performance（任职奖惩，录入即进员工动态时间线）
	performance.Register(authed, performance.NewHandler(performance.NewService(d.DB, empSvc)))
	// care（员工关怀：本月生日/周年名单）
	care.Register(authed, care.NewHandler(care.NewService(d.DB)))
	// archive（档案库 + 借阅审批，复用审批引擎）
	archive.Register(authed, archive.NewHandler(archive.NewService(d.DB, appr)))
	// report（人事报表：内置模板 + 导出 Excel）
	report.Register(authed, report.NewHandler(report.NewService(d.DB)))
	// ldap（组织/人员同步：preview / sync，仅管理员；未配置时自降级）
	ldapsync.Register(authed, ldapsync.NewHandler(d.LDAP, d.DB))

	return r
}
