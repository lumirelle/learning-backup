package com.ats.auth;

import com.ats.common.exception.ErrorCode;
import com.ats.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security 过滤器链 *内部* 触发的认证失败（未携带 token 访问受保护资源、
 * SecurityContext 为空等）由这里统一响应 JSON，与 GlobalExceptionHandler 输出格式一致。
 *
 * <p>注意：Controller 层抛 {@code BizException(UNAUTHORIZED/INVALID_TOKEN)} 仍走
 * {@link com.ats.common.exception.GlobalExceptionHandler}，两个入口对前端表现一致。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.debug("[AUTH-ENTRY] uri={} reason={}", request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMsg())
        );
    }
}
