// Package archive 实现档案库：分类 / 档案条目 / 借阅（借阅走通用审批引擎，含归还状态机）。
package archive

import (
	"context"
	"fmt"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/approval"
	"orghr/internal/common"
	"orghr/internal/infra/idgen"
	"orghr/internal/middleware"
)

// ---- Models ----

// Category 档案分类（树）。
type Category struct {
	bun.BaseModel `bun:"table:archive_categories,alias:ac"`

	ID        int64       `bun:"id,pk" json:"id,string"`
	ParentID  int64       `bun:"parent_id,nullzero" json:"parent_id,string,omitempty"`
	Name      string      `bun:"name" json:"name"`
	Code      string      `bun:"code" json:"code"`
	Path      string      `bun:"path" json:"path"`
	SortOrder int         `bun:"sort_order" json:"sort_order"`
	CreatedAt time.Time   `bun:"created_at,nullzero,default:now()" json:"created_at"`
	Children  []*Category `bun:"-" json:"children,omitempty"`
}

// Item 档案条目。
type Item struct {
	bun.BaseModel `bun:"table:archive_items,alias:ai"`

	ID            int64          `bun:"id,pk" json:"id,string"`
	EmployeeID    int64          `bun:"employee_id,nullzero" json:"employee_id,string,omitempty"`
	CategoryID    int64          `bun:"category_id,nullzero" json:"category_id,string,omitempty"`
	Title         string         `bun:"title" json:"title"`
	FileMeta      map[string]any `bun:"file_meta,type:jsonb" json:"file_meta,omitempty"`
	StorageRef    string         `bun:"storage_ref" json:"storage_ref"`
	IsBorrowable  bool           `bun:"is_borrowable" json:"is_borrowable"`
	Status        string         `bun:"status" json:"status"`
	SecurityLevel string         `bun:"security_level" json:"security_level"`
	CreatedAt     time.Time      `bun:"created_at,nullzero,default:now()" json:"created_at"`
	UpdatedAt     time.Time      `bun:"updated_at,nullzero,default:now()" json:"updated_at"`

	EmployeeName string `bun:"-" json:"employee_name,omitempty"`
}

// Borrow 借阅单。
type Borrow struct {
	bun.BaseModel `bun:"table:archive_borrows,alias:ab"`

	ID         int64      `bun:"id,pk" json:"id,string"`
	ItemID     int64      `bun:"item_id" json:"item_id,string"`
	BorrowerID int64      `bun:"borrower_id" json:"borrower_id,string"`
	Reason     string     `bun:"reason" json:"reason"`
	Status     string     `bun:"status" json:"status"`
	ApprovalID int64      `bun:"approval_id,nullzero" json:"approval_id,string,omitempty"`
	DueDate    *time.Time `bun:"due_date,nullzero" json:"due_date,omitempty"`
	BorrowedAt *time.Time `bun:"borrowed_at,nullzero" json:"borrowed_at,omitempty"`
	ReturnedAt *time.Time `bun:"returned_at,nullzero" json:"returned_at,omitempty"`
	CreatedAt  time.Time  `bun:"created_at,nullzero,default:now()" json:"created_at"`

	ItemTitle    string             `bun:"-" json:"item_title,omitempty"`
	BorrowerName string             `bun:"-" json:"borrower_name,omitempty"`
	Approval     *approval.Instance `bun:"-" json:"approval,omitempty"`
}

// ---- Service ----

// Service 档案库业务。
type Service struct {
	db   *bun.DB
	appr *approval.Engine
}

// NewService 构造 service。
func NewService(db *bun.DB, appr *approval.Engine) *Service { return &Service{db: db, appr: appr} }

// CategoryTree 返回分类树。
func (s *Service) CategoryTree(ctx context.Context) ([]*Category, error) {
	var list []*Category
	if err := s.db.NewSelect().Model(&list).Order("sort_order", "id").Scan(ctx); err != nil {
		return nil, err
	}
	m := make(map[int64]*Category, len(list))
	for _, c := range list {
		m[c.ID] = c
	}
	var roots []*Category
	for _, c := range list {
		if c.ParentID != 0 {
			if p, ok := m[c.ParentID]; ok {
				p.Children = append(p.Children, c)
				continue
			}
		}
		roots = append(roots, c)
	}
	return roots, nil
}

// CreateCategory 新增分类。
func (s *Service) CreateCategory(ctx context.Context, in *Category) (*Category, error) {
	in.ID = idgen.NextID()
	if in.ParentID != 0 {
		p := new(Category)
		if err := s.db.NewSelect().Model(p).Where("id = ?", in.ParentID).Limit(1).Scan(ctx); err != nil {
			return nil, common.NewError(400, 1003, "上级分类不存在")
		}
		in.Path = fmt.Sprintf("%s/%s", p.Path, in.Code)
	} else {
		in.Path = "/" + in.Code
	}
	if _, err := s.db.NewInsert().Model(in).Exec(ctx); err != nil {
		return nil, common.ErrConflict
	}
	return in, nil
}

// Items 查询档案条目。
func (s *Service) Items(ctx context.Context, categoryID, employeeID int64) ([]*Item, error) {
	var list []*Item
	q := s.db.NewSelect().Model(&list).Where("ai.deleted_at IS NULL")
	if categoryID != 0 {
		q = q.Where("ai.category_id = ?", categoryID)
	}
	if employeeID != 0 {
		q = q.Where("ai.employee_id = ?", employeeID)
	}
	if err := q.Order("ai.id DESC").Limit(200).Scan(ctx); err != nil {
		return nil, err
	}
	s.enrichItems(ctx, list)
	return list, nil
}

// CreateItem 新增档案条目。
func (s *Service) CreateItem(ctx context.Context, in *Item) (*Item, error) {
	if in.Title == "" {
		return nil, common.NewError(400, 1002, "缺少标题")
	}
	in.ID = idgen.NextID()
	in.IsBorrowable = true
	in.Status = "in_stock"
	if in.SecurityLevel == "" {
		in.SecurityLevel = "normal"
	}
	if _, err := s.db.NewInsert().Model(in).Exec(ctx); err != nil {
		return nil, common.ErrConflict
	}
	return in, nil
}

// CreateBorrow 发起借阅：校验在库可借 → 建借阅单 + 审批实例（事务）。
func (s *Service) CreateBorrow(ctx context.Context, itemID, borrowerID int64, reason string, approverIDs []int64, due *time.Time) (*Borrow, error) {
	if len(approverIDs) == 0 {
		return nil, common.NewError(400, 1002, "请选择审批人")
	}
	b := &Borrow{ID: idgen.NextID(), ItemID: itemID, BorrowerID: borrowerID, Reason: reason, Status: "pending", DueDate: due}
	err := s.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		item := new(Item)
		if e := tx.NewSelect().Model(item).Where("id = ? AND deleted_at IS NULL", itemID).Limit(1).Scan(ctx); e != nil {
			return common.NewError(400, 1003, "档案不存在")
		}
		if !item.IsBorrowable || item.Status != "in_stock" {
			return common.NewError(409, 1201, "该档案当前不可借")
		}
		if _, e := tx.NewInsert().Model(b).Exec(ctx); e != nil {
			return e
		}
		inst, e := s.appr.Create(ctx, tx, "borrow", b.ID, borrowerID, approverIDs)
		if e != nil {
			return e
		}
		b.ApprovalID = inst.ID
		_, e = tx.NewUpdate().Model(b).Column("approval_id").WherePK().Exec(ctx)
		return e
	})
	if err != nil {
		return nil, err
	}
	return b, nil
}

// ApproveBorrow 审批通过：末步通过则借出（事务内置 item.status=borrowed）。
func (s *Service) ApproveBorrow(ctx context.Context, borrowID int64, actor *middleware.Principal, comment string) (*Borrow, error) {
	err := s.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		b := new(Borrow)
		if e := tx.NewSelect().Model(b).Where("id = ?", borrowID).Limit(1).Scan(ctx); e != nil {
			return common.ErrNotFound
		}
		if b.Status != "pending" {
			return common.NewError(409, 1201, "借阅单状态不允许此操作")
		}
		done, _, e := s.appr.Approve(ctx, tx, b.ApprovalID, actor.UserID, actor.IsAdmin, comment)
		if e != nil {
			return e
		}
		if done {
			now := time.Now()
			b.Status = "borrowed"
			b.BorrowedAt = &now
			if _, e := tx.NewUpdate().Model(b).Column("status", "borrowed_at").WherePK().Exec(ctx); e != nil {
				return e
			}
			if _, e := tx.NewUpdate().Model((*Item)(nil)).Set("status = ?", "borrowed").Set("updated_at = now()").Where("id = ?", b.ItemID).Exec(ctx); e != nil {
				return e
			}
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	return s.GetBorrow(ctx, borrowID)
}

// RejectBorrow 驳回借阅。
func (s *Service) RejectBorrow(ctx context.Context, borrowID int64, actor *middleware.Principal, comment string) (*Borrow, error) {
	err := s.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		b := new(Borrow)
		if e := tx.NewSelect().Model(b).Where("id = ?", borrowID).Limit(1).Scan(ctx); e != nil {
			return common.ErrNotFound
		}
		if b.Status != "pending" {
			return common.NewError(409, 1201, "借阅单状态不允许此操作")
		}
		if _, e := s.appr.Reject(ctx, tx, b.ApprovalID, actor.UserID, actor.IsAdmin, comment); e != nil {
			return e
		}
		b.Status = "rejected"
		_, e := tx.NewUpdate().Model(b).Column("status").WherePK().Exec(ctx)
		return e
	})
	if err != nil {
		return nil, err
	}
	return s.GetBorrow(ctx, borrowID)
}

// ReturnBorrow 归还：借阅单 returned + 档案回库。
func (s *Service) ReturnBorrow(ctx context.Context, borrowID int64) (*Borrow, error) {
	err := s.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		b := new(Borrow)
		if e := tx.NewSelect().Model(b).Where("id = ?", borrowID).Limit(1).Scan(ctx); e != nil {
			return common.ErrNotFound
		}
		if b.Status != "borrowed" {
			return common.NewError(409, 1201, "仅已借出可归还")
		}
		now := time.Now()
		b.Status = "returned"
		b.ReturnedAt = &now
		if _, e := tx.NewUpdate().Model(b).Column("status", "returned_at").WherePK().Exec(ctx); e != nil {
			return e
		}
		_, e := tx.NewUpdate().Model((*Item)(nil)).Set("status = ?", "in_stock").Set("updated_at = now()").Where("id = ?", b.ItemID).Exec(ctx)
		return e
	})
	if err != nil {
		return nil, err
	}
	return s.GetBorrow(ctx, borrowID)
}

// Borrows 列出借阅单（可按状态过滤）。
func (s *Service) Borrows(ctx context.Context, status string) ([]*Borrow, error) {
	var list []*Borrow
	q := s.db.NewSelect().Model(&list)
	if status != "" {
		q = q.Where("status = ?", status)
	}
	if err := q.Order("id DESC").Limit(200).Scan(ctx); err != nil {
		return nil, err
	}
	s.enrichBorrows(ctx, list)
	return list, nil
}

// GetBorrow 读取借阅单（含审批轨迹）。
func (s *Service) GetBorrow(ctx context.Context, id int64) (*Borrow, error) {
	b := new(Borrow)
	if err := s.db.NewSelect().Model(b).Where("id = ?", id).Limit(1).Scan(ctx); err != nil {
		return nil, common.ErrNotFound
	}
	s.enrichBorrows(ctx, []*Borrow{b})
	if b.ApprovalID != 0 {
		if inst, err := s.appr.Get(ctx, s.db, b.ApprovalID); err == nil {
			b.Approval = inst
		}
	}
	return b, nil
}

func (s *Service) enrichItems(ctx context.Context, list []*Item) {
	ids := make([]int64, 0)
	for _, it := range list {
		if it.EmployeeID != 0 {
			ids = append(ids, it.EmployeeID)
		}
	}
	if len(ids) == 0 {
		return
	}
	nameOf := s.namesOf(ctx, ids)
	for _, it := range list {
		it.EmployeeName = nameOf[it.EmployeeID]
	}
}

func (s *Service) enrichBorrows(ctx context.Context, list []*Borrow) {
	if len(list) == 0 {
		return
	}
	itemIDs := make([]int64, 0, len(list))
	userIDs := make([]int64, 0, len(list))
	for _, b := range list {
		itemIDs = append(itemIDs, b.ItemID)
		userIDs = append(userIDs, b.BorrowerID)
	}
	var items []struct {
		ID    int64  `bun:"id"`
		Title string `bun:"title"`
	}
	_ = s.db.NewSelect().Table("archive_items").Column("id", "title").Where("id IN (?)", bun.In(itemIDs)).Scan(ctx, &items)
	titleOf := make(map[int64]string, len(items))
	for _, it := range items {
		titleOf[it.ID] = it.Title
	}
	var users []struct {
		ID   int64  `bun:"id"`
		Name string `bun:"name"`
	}
	_ = s.db.NewSelect().Table("users").Column("id", "name").Where("id IN (?)", bun.In(userIDs)).Scan(ctx, &users)
	uname := make(map[int64]string, len(users))
	for _, u := range users {
		uname[u.ID] = u.Name
	}
	for _, b := range list {
		b.ItemTitle = titleOf[b.ItemID]
		b.BorrowerName = uname[b.BorrowerID]
	}
}

func (s *Service) namesOf(ctx context.Context, ids []int64) map[int64]string {
	var rows []struct {
		ID   int64  `bun:"id"`
		Name string `bun:"name"`
	}
	_ = s.db.NewSelect().Table("employees").Column("id", "name").Where("id IN (?)", bun.In(ids)).Scan(ctx, &rows)
	m := make(map[int64]string, len(rows))
	for _, r := range rows {
		m[r.ID] = r.Name
	}
	return m
}

// ---- Handler ----

// Handler 暴露档案库 HTTP 接口。
type Handler struct{ svc *Service }

// NewHandler 构造 handler。
func NewHandler(svc *Service) *Handler { return &Handler{svc: svc} }

func qInt(c *gin.Context, key string) int64 {
	if v := c.Query(key); v != "" {
		n, _ := strconv.ParseInt(v, 10, 64)
		return n
	}
	return 0
}

func pInt(c *gin.Context) int64 {
	id, _ := strconv.ParseInt(c.Param("id"), 10, 64)
	return id
}

func parseDate(s string) *time.Time {
	if t, err := time.Parse("2006-01-02", s); err == nil {
		return &t
	}
	return nil
}

// Categories GET /archive-categories
func (h *Handler) Categories(c *gin.Context) {
	t, err := h.svc.CategoryTree(c.Request.Context())
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, t)
}

// CreateCategory POST /archive-categories
func (h *Handler) CreateCategory(c *gin.Context) {
	var in Category
	if c.ShouldBindJSON(&in) != nil || in.Name == "" {
		common.FailErr(c, common.ErrValidation)
		return
	}
	out, err := h.svc.CreateCategory(c.Request.Context(), &in)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, out)
}

// Items GET /archives
func (h *Handler) Items(c *gin.Context) {
	list, err := h.svc.Items(c.Request.Context(), qInt(c, "category_id"), qInt(c, "employee_id"))
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, list)
}

type createItemReq struct {
	EmployeeID    string         `json:"employee_id"`
	CategoryID    string         `json:"category_id"`
	Title         string         `json:"title" binding:"required"`
	FileMeta      map[string]any `json:"file_meta"`
	SecurityLevel string         `json:"security_level"`
}

// CreateItem POST /archives
func (h *Handler) CreateItem(c *gin.Context) {
	var req createItemReq
	if c.ShouldBindJSON(&req) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	it := &Item{Title: req.Title, FileMeta: req.FileMeta, SecurityLevel: req.SecurityLevel}
	it.EmployeeID, _ = strconv.ParseInt(req.EmployeeID, 10, 64)
	it.CategoryID, _ = strconv.ParseInt(req.CategoryID, 10, 64)
	out, err := h.svc.CreateItem(c.Request.Context(), it)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, out)
}

type borrowReq struct {
	Reason     string `json:"reason"`
	ApproverID string `json:"approver_id" binding:"required"`
	DueDate    string `json:"due_date"`
}

// Borrow POST /archives/:id/borrow
func (h *Handler) Borrow(c *gin.Context) {
	var req borrowReq
	if c.ShouldBindJSON(&req) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	actor := middleware.CurrentPrincipal(c)
	approver, _ := strconv.ParseInt(req.ApproverID, 10, 64)
	var due *time.Time
	if req.DueDate != "" {
		due = parseDate(req.DueDate)
	}
	out, err := h.svc.CreateBorrow(c.Request.Context(), pInt(c), actor.UserID, req.Reason, []int64{approver}, due)
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, out)
}

type actReq struct {
	Action  string `json:"action" binding:"required"` // approve | reject | return
	Comment string `json:"comment"`
}

// BorrowAction POST /borrows/:id —— 单一动作分发（避免 :id 下多个静态子节点触发 gin radix 不确定性）。
func (h *Handler) BorrowAction(c *gin.Context) {
	var req actReq
	if c.ShouldBindJSON(&req) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	id := pInt(c)
	actor := middleware.CurrentPrincipal(c)
	var (
		out *Borrow
		err error
	)
	switch req.Action {
	case "approve":
		out, err = h.svc.ApproveBorrow(c.Request.Context(), id, actor, req.Comment)
	case "reject":
		out, err = h.svc.RejectBorrow(c.Request.Context(), id, actor, req.Comment)
	case "return":
		out, err = h.svc.ReturnBorrow(c.Request.Context(), id)
	default:
		common.FailErr(c, common.ErrValidation)
		return
	}
	if err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, out)
}

// Borrows GET /borrows
func (h *Handler) Borrows(c *gin.Context) {
	list, err := h.svc.Borrows(c.Request.Context(), c.Query("status"))
	if err != nil {
		common.FailErr(c, common.ErrInternal)
		return
	}
	common.OK(c, list)
}

// Register 注册档案库路由（均需鉴权）。
func Register(authed *gin.RouterGroup, h *Handler) {
	authed.GET("/archive-categories", h.Categories)
	authed.POST("/archive-categories", h.CreateCategory)
	authed.GET("/archives", h.Items)
	authed.POST("/archives", h.CreateItem)
	authed.POST("/archives/:id/borrow", h.Borrow)
	authed.GET("/borrows", h.Borrows)
	authed.POST("/borrows/:id", h.BorrowAction) // body: {action: approve|reject|return}
}
