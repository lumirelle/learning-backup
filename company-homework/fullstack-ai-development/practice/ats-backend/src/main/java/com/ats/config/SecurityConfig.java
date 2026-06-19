package com.ats.config;

import com.ats.auth.JwtAuthEntryPoint;
import com.ats.auth.JwtAuthenticationFilter;
import com.ats.auth.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 主配置（M1 接入 JWT）。
 *
 * <h3>策略</h3>
 * <ul>
 *   <li><strong>无状态</strong>：SessionCreationPolicy.STATELESS</li>
 *   <li><strong>CSRF disable</strong>：JWT + SameSite=Lax 防御已足够，关闭可简化前端</li>
 *   <li><strong>CORS</strong>：由 {@link CorsConfig#corsFilter} 提供，{@code allowCredentials=true}，支持 localhost 与局域网私网 Origin
 *       让前端 axios {@code withCredentials} 能携带 refresh cookie</li>
 *   <li><strong>放行清单</strong>：/health、/auth/(register|login|refresh)、GET /jobs(/*)
 *       —— 其余全部 .authenticated()，由 method-level {@code @PreAuthorize} 做细粒度</li>
 *   <li><strong>认证入口</strong>：未认证访问受保护资源由 {@link JwtAuthEntryPoint}
 *       统一输出 ApiResponse 401</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthEntryPoint authEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(authEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        // /auth/register · /auth/login 公开；/auth/refresh · /auth/logout 只凭 cookie 操作
                        .requestMatchers("/auth/register", "/auth/login", "/auth/refresh", "/auth/logout").permitAll()
                        // 候选人可未登录浏览岗位 + 标签 + 部门字典（M6 增加 /sub-departments 给岗位创建下拉）
                        .requestMatchers(HttpMethod.GET, "/jobs", "/jobs/*", "/tags", "/departments", "/sub-departments").permitAll()
                        // 登录 / 注册页"水位"展示用的聚合统计（仅 GET，仅返回不可识别个体的聚合数字）
                        .requestMatchers(HttpMethod.GET, "/stats/public").permitAll()
                        // 其他全部需要登录
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
