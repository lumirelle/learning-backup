package com.ats.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 解析 {@code Authorization: Bearer xxx}，把当前用户写入 {@code SecurityContext}。
 *
 * <h3>设计取舍</h3>
 * <ul>
 *   <li>解析失败 / token 无效 → <strong>不抛异常、不写 context</strong>。后续若访问的是
 *       受保护路径，由 {@link JwtAuthEntryPoint} 统一输出 401；若是 permitAll 路径
 *       （如 {@code /health}）即便携带了过期 token 也能放行。</li>
 *   <li>{@code principal = String(userId)} — Controller 可用
 *       {@code @AuthenticationPrincipal String userId} 直接拿。</li>
 *   <li>{@code authorities = ROLE_<role>} — 自动适配
 *       {@code @PreAuthorize("hasRole('HR')")}。</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null
                && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = header.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtService.verifyAccessToken(token);
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            catch (RuntimeException e) {
                // 包含 BizException / JwtException / IllegalArgumentException 等；
                // 保持未认证状态，受保护路径会被 EntryPoint 接管输出 401
                log.debug("[JWT-FILTER] token verify failed: {}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
