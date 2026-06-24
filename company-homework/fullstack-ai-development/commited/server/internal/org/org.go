package org

import (
	"context"
	"fmt"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/infra/idgen"
)

// ---- Models ----

// Organization 公司主体（集团/子公司/BU）树。
type Organization struct {
	bun.BaseModel `bun:"table:organizations,alias:o"`

	ID          int64           `bun:"id,pk" json:"id,string"`
	ParentID    int64           `bun:"parent_id,nullzero" json:"parent_id,string,omitempty"`
	Code        string          `bun:"code" json:"code"`
	Name        string          `bun:"name" json:"name"`
	ShortName   string          `bun:"short_name" json:"short_name"`
	Type        string          `bun:"type" json:"type"`
	Path        string          `bun:"path" json:"path"`
	SortOrder   int             `bun:"sort_order" json:"sort_order"`
	Status      string          `bun:"status" json:"status"`
	Description string          `bun:"description" json:"description"`
	ExtSource   string          `bun:"ext_source" json:"ext_source,omitempty"`
	CreatedAt   time.Time       `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt   time.Time       `bun:"updated_at,nullzero,default:now()" json:"updated_at"`
	Children    []*Organization `bun:"-" json:"children,omitempty"`
}

// Department 部门树（隶属某组织）。
type Department struct {
	bun.BaseModel `bun:"table:departments,alias:d"`

	ID          int64         `bun:"id,pk" json:"id,string"`
	OrgID       int64         `bun:"org_id" json:"org_id,string"`
	ParentID    int64         `bun:"parent_id,nullzero" json:"parent_id,string,omitempty"`
	Code        string        `bun:"code" json:"code"`
	Name        string        `bun:"name" json:"name"`
	ShortName   string        `bun:"short_name" json:"short_name"`
	Path        string        `bun:"path" json:"path"`
	SortOrder   int           `bun:"sort_order" json:"sort_order"`
	HeadUserID  int64         `bun:"head_user_id,nullzero" json:"head_user_id,string,omitempty"`
	Status      string        `bun:"status" json:"status"`
	Description string        `bun:"description" json:"description"`
	ExtSource   string        `bun:"ext_source" json:"ext_source,omitempty"`
	CreatedAt   time.Time     `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt   time.Time     `bun:"updated_at,nullzero,default:now()" json:"updated_at"`
	Children    []*Department `bun:"-" json:"children,omitempty"`
}

// Position 岗位。
type Position struct {
	bun.BaseModel `bun:"table:positions,alias:p"`

	ID        int64     `bun:"id,pk" json:"id,string"`
	OrgID     int64     `bun:"org_id" json:"org_id,string"`
	DeptID    int64     `bun:"dept_id,nullzero" json:"dept_id,string,omitempty"`
	Code      string    `bun:"code" json:"code"`
	Name      string    `bun:"name" json:"name"`
	JobFamily string    `bun:"job_family" json:"job_family"`
	LevelSeq  string    `bun:"level_seq" json:"level_seq"`
	Headcount int       `bun:"headcount" json:"headcount"`
	Status    string    `bun:"status" json:"status"`
	CreatedAt time.Time `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt time.Time `bun:"updated_at,nullzero,default:now()" json:"updated_at"`
}

// JobLevel 职级序列条目。
type JobLevel struct {
	bun.BaseModel `bun:"table:job_levels,alias:jl"`

	ID          int64  `bun:"id,pk" json:"id,string"`
	SeqCode     string `bun:"seq_code" json:"seq_code"`
	Name        string `bun:"name" json:"name"`
	LevelCode   string `bun:"level_code" json:"level_code"`
	LevelOrder  int    `bun:"level_order" json:"level_order"`
	Description string `bun:"description" json:"description"`
}

// ---- Repo ----

// Repo 组织域数据访问。
type Repo struct{ db *bun.DB }

// NewRepo 构造 repo。
func NewRepo(db *bun.DB) *Repo { return &Repo{db: db} }

func (r *Repo) allOrgs(ctx context.Context) ([]*Organization, error) {
	var list []*Organization
	err := r.db.NewSelect().Model(&list).Where("deleted_at IS NULL").Order("sort_order", "id").Scan(ctx)
	return list, err
}

func (r *Repo) deptsByOrg(ctx context.Context, orgID int64) ([]*Department, error) {
	var list []*Department
	q := r.db.NewSelect().Model(&list).Where("deleted_at IS NULL")
	if orgID != 0 {
		q = q.Where("org_id = ?", orgID)
	}
	err := q.Order("sort_order", "id").Scan(ctx)
	return list, err
}

func (r *Repo) getOrg(ctx context.Context, id int64) (*Organization, error) {
	o := new(Organization)
	err := r.db.NewSelect().Model(o).Where("id = ?", id).Limit(1).Scan(ctx)
	return o, err
}

func (r *Repo) getDept(ctx context.Context, id int64) (*Department, error) {
	d := new(Department)
	err := r.db.NewSelect().Model(d).Where("id = ?", id).Limit(1).Scan(ctx)
	return d, err
}

// ---- Service ----

// Service 组织域业务。
type Service struct{ repo *Repo }

// NewService 构造 service。
func NewService(repo *Repo) *Service { return &Service{repo: repo} }

// OrgTree 返回组织主体树。
func (s *Service) OrgTree(ctx context.Context) ([]*Organization, error) {
	list, err := s.repo.allOrgs(ctx)
	if err != nil {
		return nil, err
	}
	m := make(map[int64]*Organization, len(list))
	for _, o := range list {
		m[o.ID] = o
	}
	var roots []*Organization
	for _, o := range list {
		if o.ParentID != 0 {
			if p, ok := m[o.ParentID]; ok {
				p.Children = append(p.Children, o)
				continue
			}
		}
		roots = append(roots, o)
	}
	return roots, nil
}

// TreeNode 是组织 + 部门合并后的统一树节点（供前端单棵可折叠树渲染）。
type TreeNode struct {
	ID        int64       `json:"id,string"`
	Name      string      `json:"name"`
	Type      string      `json:"type"` // org | dept
	Path      string      `json:"path"`
	ExtSource string      `json:"ext_source,omitempty"`
	Children  []*TreeNode `json:"children,omitempty"`
}

// Structure 返回「组织主体 + 部门」合并后的统一树：组织作上层节点，其部门树挂在该组织下。
func (s *Service) Structure(ctx context.Context) ([]*TreeNode, error) {
	orgRoots, err := s.OrgTree(ctx)
	if err != nil {
		return nil, err
	}
	var convertOrg func(o *Organization) *TreeNode
	convertOrg = func(o *Organization) *TreeNode {
		n := &TreeNode{ID: o.ID, Name: o.Name, Type: "org", Path: o.Path, ExtSource: o.ExtSource}
		for _, c := range o.Children {
			n.Children = append(n.Children, convertOrg(c))
		}
		// 挂上本组织的部门树
		depts, derr := s.DeptTree(ctx, o.ID)
		if derr == nil {
			for _, d := range depts {
				n.Children = append(n.Children, convertDept(d))
			}
		}
		return n
	}
	var roots []*TreeNode
	for _, o := range orgRoots {
		roots = append(roots, convertOrg(o))
	}
	return roots, nil
}

func convertDept(d *Department) *TreeNode {
	n := &TreeNode{ID: d.ID, Name: d.Name, Type: "dept", Path: d.Path, ExtSource: d.ExtSource}
	for _, c := range d.Children {
		n.Children = append(n.Children, convertDept(c))
	}
	return n
}

// DeptTree 返回（某组织的）部门树。
func (s *Service) DeptTree(ctx context.Context, orgID int64) ([]*Department, error) {
	list, err := s.repo.deptsByOrg(ctx, orgID)
	if err != nil {
		return nil, err
	}
	m := make(map[int64]*Department, len(list))
	for _, d := range list {
		m[d.ID] = d
	}
	var roots []*Department
	for _, d := range list {
		if d.ParentID != 0 {
			if p, ok := m[d.ParentID]; ok {
				p.Children = append(p.Children, d)
				continue
			}
		}
		roots = append(roots, d)
	}
	return roots, nil
}

// CreateOrg 新增组织并计算 materialized path。
func (s *Service) CreateOrg(ctx context.Context, in *Organization) (*Organization, error) {
	in.ID = idgen.NextID()
	if in.Status == "" {
		in.Status = "active"
	}
	if in.Type == "" {
		in.Type = "subsidiary"
	}
	if in.ParentID != 0 {
		p, err := s.repo.getOrg(ctx, in.ParentID)
		if err != nil {
			return nil, common.NewError(400, 1003, "上级组织不存在")
		}
		in.Path = fmt.Sprintf("%s/%s", p.Path, in.Code)
	} else {
		in.Path = "/" + in.Code
	}
	if _, err := s.repo.db.NewInsert().Model(in).Exec(ctx); err != nil {
		return nil, common.NewError(409, 1004, "组织编码冲突")
	}
	return in, nil
}

// CreateDept 新增部门并计算 path（继承组织/上级部门）。
func (s *Service) CreateDept(ctx context.Context, in *Department) (*Department, error) {
	in.ID = idgen.NextID()
	if in.Status == "" {
		in.Status = "active"
	}
	if in.ParentID != 0 {
		p, err := s.repo.getDept(ctx, in.ParentID)
		if err != nil {
			return nil, common.NewError(400, 1003, "上级部门不存在")
		}
		in.OrgID = p.OrgID
		in.Path = fmt.Sprintf("%s/%s", p.Path, in.Code)
	} else {
		o, err := s.repo.getOrg(ctx, in.OrgID)
		if err != nil {
			return nil, common.NewError(400, 1003, "所属组织不存在")
		}
		in.Path = fmt.Sprintf("%s/%s", o.Path, in.Code)
	}
	if _, err := s.repo.db.NewInsert().Model(in).Exec(ctx); err != nil {
		return nil, common.NewError(409, 1004, "部门编码冲突")
	}
	return in, nil
}

// Positions 返回岗位列表。
func (s *Service) Positions(ctx context.Context) ([]*Position, error) {
	var list []*Position
	err := s.repo.db.NewSelect().Model(&list).Where("deleted_at IS NULL").Order("id").Scan(ctx)
	return list, err
}

// JobLevels 返回职级序列。
func (s *Service) JobLevels(ctx context.Context) ([]*JobLevel, error) {
	var list []*JobLevel
	err := s.repo.db.NewSelect().Model(&list).Order("seq_code", "level_order").Scan(ctx)
	return list, err
}

// ---- Handler ----

// Handler 暴露组织域 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

// Structure GET /org-structure —— 组织 + 部门合并的统一树。
func (h *Handler) Structure(c *gin.Context) {
	t, err := h.svc.Structure(c.Request.Context())
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, t)
}

// OrgTree GET /orgs/tree
func (h *Handler) OrgTree(c *gin.Context) {
	t, err := h.svc.OrgTree(c.Request.Context())
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, t)
}

// CreateOrg POST /orgs
func (h *Handler) CreateOrg(c *gin.Context) {
	var in Organization
	if c.ShouldBindJSON(&in) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	o, err := h.svc.CreateOrg(c.Request.Context(), &in)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, o)
}

// DeptTree GET /departments/tree?org_id=
func (h *Handler) DeptTree(c *gin.Context) {
	var orgID int64
	fmt.Sscan(c.Query("org_id"), &orgID)
	t, err := h.svc.DeptTree(c.Request.Context(), orgID)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, t)
}

// CreateDept POST /departments
func (h *Handler) CreateDept(c *gin.Context) {
	var in Department
	if c.ShouldBindJSON(&in) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	d, err := h.svc.CreateDept(c.Request.Context(), &in)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, d)
}

// Positions GET /positions
func (h *Handler) Positions(c *gin.Context) {
	l, err := h.svc.Positions(c.Request.Context())
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, l)
}

// JobLevels GET /job-levels
func (h *Handler) JobLevels(c *gin.Context) {
	l, err := h.svc.JobLevels(c.Request.Context())
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, l)
}

// Register 注册组织域路由（均需鉴权）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/org-structure", h.Structure)
	authed.GET("/orgs/tree", h.OrgTree)
	authed.POST("/orgs", h.CreateOrg)
	authed.GET("/departments/tree", h.DeptTree)
	authed.POST("/departments", h.CreateDept)
	authed.GET("/positions", h.Positions)
	authed.GET("/job-levels", h.JobLevels)
}
