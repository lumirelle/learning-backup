package com.ats.controller;

import com.ats.common.response.ApiResponse;
import com.ats.organization.OrganizationService;
import com.ats.organization.dto.DepartmentCreateReq;
import com.ats.organization.dto.DepartmentUpdateReq;
import com.ats.organization.dto.OrgTreeNodeVO;
import com.ats.organization.dto.SubDepartmentCreateReq;
import com.ats.organization.dto.SubDepartmentUpdateReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin 组织树管理（M6）：部门 / 子部门 CRUD + 整树查询。
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminOrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/departments/tree")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrgTreeNodeVO> tree() {
        return ApiResponse.ok(organizationService.getTree());
    }

    @PostMapping("/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrgTreeNodeVO> createDepartment(@Valid @RequestBody DepartmentCreateReq req) {
        return ApiResponse.ok(organizationService.createDepartment(req));
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrgTreeNodeVO> updateDepartment(@PathVariable Long id,
                                                       @Valid @RequestBody DepartmentUpdateReq req) {
        return ApiResponse.ok(organizationService.updateDepartment(id, req));
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteDepartment(@PathVariable Long id) {
        organizationService.deleteDepartment(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/sub-departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrgTreeNodeVO> createSubDepartment(@Valid @RequestBody SubDepartmentCreateReq req) {
        return ApiResponse.ok(organizationService.createSubDepartment(req));
    }

    @PutMapping("/sub-departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrgTreeNodeVO> updateSubDepartment(@PathVariable Long id,
                                                          @Valid @RequestBody SubDepartmentUpdateReq req) {
        return ApiResponse.ok(organizationService.updateSubDepartment(id, req));
    }

    @DeleteMapping("/sub-departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSubDepartment(@PathVariable Long id) {
        organizationService.deleteSubDepartment(id);
        return ApiResponse.ok(null);
    }
}
