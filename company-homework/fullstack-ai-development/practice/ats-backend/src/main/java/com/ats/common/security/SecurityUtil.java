package com.ats.common.security;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 Spring Security 上下文中读取当前登录用户。
 * <p>
 * 配合 {@code JwtAuthenticationFilter} 注入的 principal=userId(String) + authorities=ROLE_xxx。
 */
public final class SecurityUtil {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_HR = "ROLE_HR";
    public static final String ROLE_CANDIDATE = "ROLE_CANDIDATE";

    private SecurityUtil() {}

    /** 当前用户 id；未登录返回 {@code null}（匿名访问 GET /jobs 等公开接口时用得到） */
    public static Long currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof String s && !s.isBlank() && !"anonymousUser".equals(s)) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) { /* fall through */ }
        }
        return null;
    }

    /** 强制要求登录；未登录抛 401 */
    public static Long requireUserId() {
        Long id = currentUserIdOrNull();
        if (id == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        return id;
    }

    /** "ADMIN" / "HR" / "CANDIDATE" / null */
    public static String currentRoleOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return auth.getAuthorities().stream()
                .map(g -> g.getAuthority())
                .filter(s -> s.startsWith("ROLE_"))
                .findFirst()
                .map(s -> s.substring(5))
                .orElse(null);
    }

    public static boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }

    public static boolean isHr() {
        return hasRole(ROLE_HR);
    }

    public static boolean isCandidate() {
        return hasRole(ROLE_CANDIDATE);
    }

    private static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(g -> role.equals(g.getAuthority()));
    }
}
