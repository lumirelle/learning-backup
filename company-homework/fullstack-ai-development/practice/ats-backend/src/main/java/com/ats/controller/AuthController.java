package com.ats.controller;

import com.ats.auth.AuthService;
import com.ats.auth.dto.ChangePasswordReq;
import com.ats.auth.dto.ProfileUpdateReq;
import com.ats.auth.dto.LoginReq;
import com.ats.auth.dto.MeVO;
import com.ats.auth.dto.RegisterReq;
import com.ats.auth.dto.TokenVO;
import com.ats.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 候选人自助注册（role 固定为 CANDIDATE）。
     * HR 账号由 Admin 通过 POST /admin/users 创建。
     */
    @PostMapping("/register")
    public ApiResponse<MeVO> register(@Valid @RequestBody RegisterReq req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<TokenVO> login(@Valid @RequestBody LoginReq req,
                                      HttpServletResponse response) {
        return ApiResponse.ok(authService.login(req, response));
    }

    /**
     * 用 HttpOnly Cookie 中的 refresh token 换新的 access token（rotation）。
     * 前端无需显式传 refresh token，浏览器自动携带 cookie（withCredentials=true）。
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenVO> refresh(HttpServletRequest request,
                                        HttpServletResponse response) {
        return ApiResponse.ok(authService.refresh(request, response));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request,
                                    HttpServletResponse response) {
        authService.logout(request, response);
        return ApiResponse.ok();
    }

    /** 当前登录用户信息；principal 为 JwtAuthenticationFilter 写入的 userId 字符串 */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MeVO> me(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(authService.me(Long.parseLong(userId)));
    }

    @PatchMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MeVO> updateProfile(@AuthenticationPrincipal String userId,
                                             @Valid @RequestBody ProfileUpdateReq req) {
        return ApiResponse.ok(authService.updateProfile(Long.parseLong(userId), req));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal String userId,
                                            @Valid @RequestBody ChangePasswordReq req) {
        authService.changePassword(Long.parseLong(userId), req);
        return ApiResponse.ok();
    }
}
