package com.ats.controller;

import com.ats.auth.AdminUserService;
import com.ats.auth.dto.BatchCreateUsersReq;
import com.ats.auth.dto.BatchCreateUsersVO;
import com.ats.auth.dto.AdminUserListItemVO;
import com.ats.auth.dto.CreateUserReq;
import com.ats.auth.dto.MeVO;
import com.ats.auth.dto.UpdateUserReq;
import com.ats.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仅 ADMIN 角色可访问的用户管理接口。
 * <p>
 * ADMIN 账号本身不能通过 API 创建（防权限提升），仅支持通过 DB seed 或直接 SQL 生成。
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AdminUserListItemVO>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return ApiResponse.ok(adminUserService.listUsers(role, activeOnly));
    }

    @PatchMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminUserListItemVO> updateUser(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateUserReq req) {
        return ApiResponse.ok(adminUserService.updateUser(id, req));
    }

    /** 创建单个 HR / CANDIDATE 账号。 */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MeVO> createUser(@Valid @RequestBody CreateUserReq req) {
        return ApiResponse.ok(adminUserService.createUser(req));
    }

    /**
     * 批量创建 HR / CANDIDATE 账号（最多 100 条 / 批）。
     * 单行失败不会回滚整批，前端可逐行高亮成功 / 失败结果。
     */
    @PostMapping("/users/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BatchCreateUsersVO> batchCreate(@Valid @RequestBody BatchCreateUsersReq req) {
        return ApiResponse.ok(adminUserService.batchCreate(req));
    }
}
