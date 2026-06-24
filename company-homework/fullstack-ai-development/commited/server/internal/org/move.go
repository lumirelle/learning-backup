package org

import (
	"context"
	"fmt"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/uptrace/bun"

	"orghr/internal/common"
)

// rebase 把某张表里以 oldPath 为前缀（或等于 oldPath）的路径列整体迁移到 newPath。
// 仅用于内部计算的路径（ASCII code 拼接），故按字节长度切片安全。
func rebase(ctx context.Context, tx bun.Tx, table, col, oldPath, newPath string) error {
	q := fmt.Sprintf(
		"UPDATE %s SET %s = ? || substr(%s, ?), updated_at = now() WHERE %s = ? OR %s LIKE ?",
		table, col, col, col, col,
	)
	_, err := tx.ExecContext(ctx, q, newPath, len(oldPath)+1, oldPath, oldPath+"/%")
	return err
}

// MoveDept 移动部门：更新自身 path + parent，并级联子部门与员工 org_path。
func (s *Service) MoveDept(ctx context.Context, id, newParentID int64) error {
	return s.repo.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		d := new(Department)
		if err := tx.NewSelect().Model(d).Where("id = ? AND deleted_at IS NULL", id).Limit(1).Scan(ctx); err != nil {
			return common.NewError(404, 1003, "部门不存在")
		}
		if d.ExtSource == "ldap" {
			return common.NewError(409, 1109, "该部门由 LDAP 同步维护，不可手动移动")
		}
		oldPath := d.Path
		var newPath string
		if newParentID == 0 {
			o := new(Organization)
			if err := tx.NewSelect().Model(o).Where("id = ?", d.OrgID).Limit(1).Scan(ctx); err != nil {
				return common.NewError(400, 1003, "所属组织不存在")
			}
			newPath = o.Path + "/" + d.Code
		} else {
			p := new(Department)
			if err := tx.NewSelect().Model(p).Where("id = ? AND deleted_at IS NULL", newParentID).Limit(1).Scan(ctx); err != nil {
				return common.NewError(400, 1003, "上级部门不存在")
			}
			if p.OrgID != d.OrgID {
				return common.NewError(400, 1002, "暂不支持跨组织移动")
			}
			if p.Path == oldPath || strings.HasPrefix(p.Path, oldPath+"/") {
				return common.NewError(400, 1002, "不能移动到自身或其子部门")
			}
			newPath = p.Path + "/" + d.Code
		}
		if newPath == oldPath && d.ParentID == newParentID {
			return nil // 无变化
		}

		uq := tx.NewUpdate().Model((*Department)(nil)).Set("path = ?", newPath).Set("updated_at = now()").Where("id = ?", id)
		if newParentID == 0 {
			uq = uq.Set("parent_id = NULL")
		} else {
			uq = uq.Set("parent_id = ?", newParentID)
		}
		if _, err := uq.Exec(ctx); err != nil {
			return err
		}
		// 节点已更新为 newPath；rebase 仅命中子孙（path LIKE oldPath/%）与直属员工（org_path = oldPath）。
		if err := rebase(ctx, tx, "departments", "path", oldPath, newPath); err != nil {
			return err
		}
		return rebase(ctx, tx, "employees", "org_path", oldPath, newPath)
	})
}

// MoveOrg 移动组织：更新自身 path + parent，并级联子组织、部门、员工 org_path。
func (s *Service) MoveOrg(ctx context.Context, id, newParentID int64) error {
	return s.repo.db.RunInTx(ctx, nil, func(ctx context.Context, tx bun.Tx) error {
		o := new(Organization)
		if err := tx.NewSelect().Model(o).Where("id = ? AND deleted_at IS NULL", id).Limit(1).Scan(ctx); err != nil {
			return common.NewError(404, 1003, "组织不存在")
		}
		if o.ExtSource == "ldap" {
			return common.NewError(409, 1109, "该组织由 LDAP 同步维护，不可手动移动")
		}
		oldPath := o.Path
		var newPath string
		if newParentID == 0 {
			newPath = "/" + o.Code
		} else {
			p := new(Organization)
			if err := tx.NewSelect().Model(p).Where("id = ? AND deleted_at IS NULL", newParentID).Limit(1).Scan(ctx); err != nil {
				return common.NewError(400, 1003, "上级组织不存在")
			}
			if p.Path == oldPath || strings.HasPrefix(p.Path, oldPath+"/") {
				return common.NewError(400, 1002, "不能移动到自身或其子组织")
			}
			newPath = p.Path + "/" + o.Code
		}
		if newPath == oldPath && o.ParentID == newParentID {
			return nil
		}

		uq := tx.NewUpdate().Model((*Organization)(nil)).Set("path = ?", newPath).Set("updated_at = now()").Where("id = ?", id)
		if newParentID == 0 {
			uq = uq.Set("parent_id = NULL")
		} else {
			uq = uq.Set("parent_id = ?", newParentID)
		}
		if _, err := uq.Exec(ctx); err != nil {
			return err
		}
		if err := rebase(ctx, tx, "organizations", "path", oldPath, newPath); err != nil {
			return err
		}
		if err := rebase(ctx, tx, "departments", "path", oldPath, newPath); err != nil {
			return err
		}
		return rebase(ctx, tx, "employees", "org_path", oldPath, newPath)
	})
}

// ---- Handlers（纯静态路由，id 放 body，radix 安全）----

type moveReq struct {
	ID          string `json:"id" binding:"required"`
	NewParentID string `json:"new_parent_id"`
}

func (r moveReq) ids() (id, parent int64) {
	fmt.Sscan(r.ID, &id)
	if r.NewParentID != "" {
		fmt.Sscan(r.NewParentID, &parent)
	}
	return id, parent
}

// MoveDept POST /dept-move
func (h *Handler) MoveDept(c *gin.Context) {
	var req moveReq
	if c.ShouldBindJSON(&req) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	id, parent := req.ids()
	if err := h.svc.MoveDept(c.Request.Context(), id, parent); err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, gin.H{"moved": true})
}

// MoveOrg POST /org-move
func (h *Handler) MoveOrg(c *gin.Context) {
	var req moveReq
	if c.ShouldBindJSON(&req) != nil {
		common.FailErr(c, common.ErrValidation)
		return
	}
	id, parent := req.ids()
	if err := h.svc.MoveOrg(c.Request.Context(), id, parent); err != nil {
		common.FailAny(c, err)
		return
	}
	common.OK(c, gin.H{"moved": true})
}

// RegisterMove 注册节点移动路由。
func RegisterMove(authed *gin.RouterGroup, h *Handler) {
	authed.POST("/dept-move", h.MoveDept)
	authed.POST("/org-move", h.MoveOrg)
}
