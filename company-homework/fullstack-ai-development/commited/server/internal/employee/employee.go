package employee

import (
	"context"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/common"
	"orghr/internal/middleware"
)

// Employee 人事档案主表（花名册）。
type Employee struct {
	bun.BaseModel `bun:"table:employees,alias:e"`

	ID               int64      `bun:"id,pk" json:"id,string"`
	EmployeeNo       string     `bun:"employee_no" json:"employee_no"`
	UserID           int64      `bun:"user_id,nullzero" json:"user_id,string,omitempty"`
	Name             string     `bun:"name" json:"name"`
	EnName           string     `bun:"en_name" json:"en_name"`
	Gender           string     `bun:"gender" json:"gender"`
	Birthday         *time.Time `bun:"birthday,nullzero" json:"birthday,omitempty"`
	Avatar           string     `bun:"avatar" json:"avatar"`
	Phone            string     `bun:"phone" json:"phone"`
	WorkEmail        string     `bun:"work_email" json:"work_email"`
	Education        string     `bun:"education" json:"education"`
	OrgID            int64      `bun:"org_id" json:"org_id,string"`
	OrgPath          string     `bun:"org_path" json:"org_path"`
	DeptID           int64      `bun:"dept_id,nullzero" json:"dept_id,string,omitempty"`
	DeptName         string     `bun:"dept_name" json:"dept_name"`
	PositionID       int64      `bun:"position_id,nullzero" json:"position_id,string,omitempty"`
	PositionName     string     `bun:"position_name" json:"position_name"`
	JobLevel         string     `bun:"job_level" json:"job_level"`
	ManagerID        int64      `bun:"manager_id,nullzero" json:"manager_id,string,omitempty"`
	EmploymentType   string     `bun:"employment_type" json:"employment_type"`
	EmploymentStatus string     `bun:"employment_status" json:"employment_status"`
	HiredAt          *time.Time `bun:"hired_at,nullzero" json:"hired_at,omitempty"`
	RegularAt        *time.Time `bun:"regular_at,nullzero" json:"regular_at,omitempty"`
	LeftAt           *time.Time `bun:"left_at,nullzero" json:"left_at,omitempty"`
	CreatedAt        time.Time  `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt        time.Time  `bun:"updated_at,nullzero,default:now()" json:"updated_at"`
}

// Event 映射 hr_events（人事动态时间线）。
type Event struct {
	bun.BaseModel `bun:"table:hr_events,alias:ev"`

	ID         int64     `bun:"id,pk" json:"id,string"`
	EmployeeID int64     `bun:"employee_id,nullzero" json:"employee_id,string,omitempty"`
	EventType  string    `bun:"event_type" json:"event_type"`
	Title      string    `bun:"title" json:"title"`
	OrgPath    string    `bun:"org_path" json:"org_path"`
	ActorID    int64     `bun:"actor_id,nullzero" json:"actor_id,string,omitempty"`
	OccurredAt time.Time `bun:"occurred_at,nullzero,default:now()" json:"occurred_at"`
}

// ListFilter 花名册检索条件。
type ListFilter struct {
	Keyword  string
	Status   string
	JobLevel string
	OrgPath  string
	DeptID   int64
	Page     int
	Size     int
}

// Repo 员工域数据访问。
type Repo struct{ db *bun.DB }

// NewRepo 构造 repo。
func NewRepo(db *bun.DB) *Repo { return &Repo{db: db} }

// List 分页检索员工。
func (r *Repo) List(ctx context.Context, f ListFilter) ([]*Employee, int, error) {
	var list []*Employee
	q := r.db.NewSelect().Model(&list).Where("deleted_at IS NULL")
	if f.Keyword != "" {
		kw := "%" + f.Keyword + "%"
		q = q.Where("(name ILIKE ? OR employee_no ILIKE ? OR phone ILIKE ?)", kw, kw, kw)
	}
	if f.Status != "" {
		q = q.Where("employment_status = ?", f.Status)
	}
	if f.JobLevel != "" {
		q = q.Where("job_level = ?", f.JobLevel)
	}
	if f.DeptID != 0 {
		q = q.Where("dept_id = ?", f.DeptID)
	}
	q = common.Subtree(q, "org_path", f.OrgPath)
	total, err := q.Order("id DESC").Limit(f.Size).Offset((f.Page - 1) * f.Size).ScanAndCount(ctx)
	return list, total, err
}

// Get 取单个员工。
func (r *Repo) Get(ctx context.Context, id int64) (*Employee, error) {
	e := new(Employee)
	err := r.db.NewSelect().Model(e).Where("id = ? AND deleted_at IS NULL", id).Limit(1).Scan(ctx)
	return e, err
}

// Timeline 取员工动态时间线。
func (r *Repo) Timeline(ctx context.Context, empID int64) ([]*Event, error) {
	var l []*Event
	err := r.db.NewSelect().Model(&l).Where("employee_id = ?", empID).Order("occurred_at DESC").Limit(100).Scan(ctx)
	return l, err
}

// Service 员工域业务。
type Service struct{ repo *Repo }

// NewService 构造 service。
func NewService(repo *Repo) *Service { return &Service{repo: repo} }

// Handler 暴露员工域 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

// ParseFilter 从查询参数解析花名册筛选条件（含数据权限范围），List 与 Export 共用。
func ParseFilter(c *gin.Context) ListFilter {
	f := ListFilter{
		Keyword:  c.Query("keyword"),
		Status:   c.Query("status"),
		JobLevel: c.Query("job_level"),
	}
	if v := c.Query("dept_id"); v != "" {
		f.DeptID, _ = strconv.ParseInt(v, 10, 64)
	}
	// 数据权限：非管理员仅可见本组织子树
	if p := middleware.CurrentPrincipal(c); p != nil && !p.IsAdmin && p.OrgPath != "" {
		f.OrgPath = p.OrgPath
	}
	return f
}

// List GET /employees
func (h *Handler) List(c *gin.Context) {
	page, size := common.ParsePage(c)
	f := ParseFilter(c)
	f.Page, f.Size = page, size
	list, total, err := h.svc.repo.List(c.Request.Context(), f)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.Page(c, list, total, page, size)
}

// Get GET /employees/:id
func (h *Handler) Get(c *gin.Context) {
	id, _ := strconv.ParseInt(c.Param("id"), 10, 64)
	e, err := h.svc.repo.Get(c.Request.Context(), id)
	if err != nil {
		common.FailErr(c, common.ErrNotFound)
		return
	}
	common.OK(c, e)
}

// Timeline GET /employees/:id/timeline
func (h *Handler) Timeline(c *gin.Context) {
	id, _ := strconv.ParseInt(c.Param("id"), 10, 64)
	l, err := h.svc.repo.Timeline(c.Request.Context(), id)
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, l)
}

// Register 注册员工域路由（均需鉴权）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/employees", h.List)
	authed.GET("/employees/:id", h.Get)
	authed.GET("/employees/:id/timeline", h.Timeline)
}
