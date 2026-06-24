package process

import (
	"strconv"

	"github.com/gin-gonic/gin"

	"orghr/internal/common"
	"orghr/internal/middleware"
)

// Handler 暴露人事流程 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

type createReq struct {
	Type        string         `json:"type" binding:"required"`
	EmployeeID  string         `json:"employee_id"`
	Payload     map[string]any `json:"payload"`
	ApproverIDs []string       `json:"approver_ids" binding:"required"`
}

type actReq struct {
	Comment string `json:"comment"`
}

// Create POST /processes
func (h *Handler) Create(c *gin.Context) {
	var req createReq
	if c.ShouldBindJSON(&req) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	actor := middleware.CurrentPrincipal(c)
	if actor == nil {
		common.FailErr(c, common.ErrUnauthorized)
		return
	}
	in := CreateInput{Type: req.Type, Payload: req.Payload}
	in.EmployeeID, _ = strconv.ParseInt(req.EmployeeID, 10, 64)
	for _, s := range req.ApproverIDs {
		if id, err := strconv.ParseInt(s, 10, 64); err == nil && id != 0 {
			in.ApproverIDs = append(in.ApproverIDs, id)
		}
	}
	p, err := h.svc.Create(c.Request.Context(), in, actor)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, p)
}

// Approve POST /processes/:id/approve
func (h *Handler) Approve(c *gin.Context) {
	h.act(c, true)
}

// Reject POST /processes/:id/reject
func (h *Handler) Reject(c *gin.Context) {
	h.act(c, false)
}

func (h *Handler) act(c *gin.Context, approve bool) {
	actor := middleware.CurrentPrincipal(c)
	if actor == nil {
		common.FailErr(c, common.ErrUnauthorized)
		return
	}
	id, _ := strconv.ParseInt(c.Param("id"), 10, 64)
	var req actReq
	_ = c.ShouldBindJSON(&req)
	var (
		p   *HrProcess
		err error
	)
	if approve {
		p, err = h.svc.Approve(c.Request.Context(), id, actor, req.Comment)
	} else {
		p, err = h.svc.Reject(c.Request.Context(), id, actor, req.Comment)
	}
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, p)
}

// Get GET /processes/:id
func (h *Handler) Get(c *gin.Context) {
	id, _ := strconv.ParseInt(c.Param("id"), 10, 64)
	p, err := h.svc.Get(c.Request.Context(), id)
	if err != nil {
		common.FailErr(c, common.ErrNotFound)
		return
	}
	common.OK(c, p)
}

// List GET /processes
func (h *Handler) List(c *gin.Context) {
	page, size := common.ParsePage(c)
	f := ListFilter{Type: c.Query("type"), Status: c.Query("status"), Page: page, Size: size}
	// 数据权限：非管理员仅可见本组织子树的流程单。
	if p := middleware.CurrentPrincipal(c); p != nil && !p.IsAdmin {
		f.Scope = p.OrgPath
	}
	list, total, err := h.svc.List(c.Request.Context(), f)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.Page(c, list, total, page, size)
}

// Todo GET /approvals/todo
func (h *Handler) Todo(c *gin.Context) {
	actor := middleware.CurrentPrincipal(c)
	if actor == nil {
		common.FailErr(c, common.ErrUnauthorized)
		return
	}
	list, err := h.svc.Todo(c.Request.Context(), actor.UserID)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, list)
}

// Mine GET /approvals/mine
func (h *Handler) Mine(c *gin.Context) {
	actor := middleware.CurrentPrincipal(c)
	if actor == nil {
		common.FailErr(c, common.ErrUnauthorized)
		return
	}
	list, err := h.svc.Mine(c.Request.Context(), actor.UserID)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, list)
}

// Register 注册人事流程路由（均需鉴权）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/processes", h.List)
	authed.POST("/processes", h.Create)
	authed.GET("/processes/:id", h.Get)
	authed.POST("/processes/:id/approve", h.Approve)
	authed.POST("/processes/:id/reject", h.Reject)
	authed.GET("/approvals/todo", h.Todo)
	authed.GET("/approvals/mine", h.Mine)
}
