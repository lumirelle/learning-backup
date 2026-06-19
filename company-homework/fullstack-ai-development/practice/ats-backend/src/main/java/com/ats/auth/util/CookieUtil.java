package com.ats.auth.util;

import com.ats.auth.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * HttpOnly Refresh Token Cookie 工具。
 *
 * <p>SameSite 属性必须通过 {@code Set-Cookie} header 直接写，
 * 因为 Java Servlet API {@link Cookie} 类不支持 SameSite 字段。
 */
@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final JwtProperties props;

    /** 写入 refresh cookie（登录 / 刷新后调用） */
    public void writeRefreshCookie(HttpServletResponse response, String rawRefreshToken, long maxAgeSeconds) {
        JwtProperties.RefreshCookie cfg = props.getRefreshCookie();

        StringBuilder header = new StringBuilder();
        header.append(cfg.getName()).append("=").append(rawRefreshToken);
        header.append("; Path=").append(cfg.getPath());
        header.append("; Max-Age=").append(maxAgeSeconds);
        header.append("; HttpOnly");
        if (cfg.isSecure()) header.append("; Secure");
        header.append("; SameSite=").append(cfg.getSameSite());
        if (cfg.getDomain() != null && !cfg.getDomain().isBlank()) {
            header.append("; Domain=").append(cfg.getDomain());
        }

        response.addHeader("Set-Cookie", header.toString());
    }

    /** 清除 refresh cookie（logout 后调用），设置 Max-Age=0 */
    public void clearRefreshCookie(HttpServletResponse response) {
        JwtProperties.RefreshCookie cfg = props.getRefreshCookie();

        StringBuilder header = new StringBuilder();
        header.append(cfg.getName()).append("=");
        header.append("; Path=").append(cfg.getPath());
        header.append("; Max-Age=0");
        header.append("; HttpOnly");
        if (cfg.isSecure()) header.append("; Secure");
        header.append("; SameSite=").append(cfg.getSameSite());

        response.addHeader("Set-Cookie", header.toString());
    }

    /** 从请求中读取 refresh cookie 原值；不存在返回 empty */
    public Optional<String> readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies)
                .filter(c -> props.getRefreshCookie().getName().equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
