package com.dorm.config.security;

import com.dorm.filter.security.DefaultSessionAttributesFilter;
import com.dorm.service.user.UserService;
import com.dorm.service.user.teacher.TeacherService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类，注意，在使用了它之后，POST 请求需要传递 _csrf 参数
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        UserService userService,
        TeacherService teacherService
    ) throws Exception {
        // 在认证过滤器之后添加自定义过滤器，在 session 中添加默认属性（baseUrl 和用户信息）
        http.addFilterAfter(
            new DefaultSessionAttributesFilter(userService, teacherService),
            UsernamePasswordAuthenticationFilter.class
        );

        // 请求的（登录）认证配置，控制哪些访问地址可以不登陆，哪些要登陆
        http.authorizeHttpRequests(customizer -> customizer
            // permitAll -> 允许不登陆就访问
            .requestMatchers("/login", "/register", "/api/register", "/api/all-in-one-card/consume-self")
            .permitAll()
            // 这个是图片等等的前端的东西，也允许
            .requestMatchers("/css/**", "/icons/**", "/images/**", "/js/**", "/vendor/**")
            .permitAll()
            // authenticated -> 需要登录的。其他请求需要登录！
            .anyRequest()
            .authenticated()
        );

        // 登录由 spring security 实现，不需要自己实现接口 /api/login
        http.formLogin(customizer -> customizer
            // 登陆页面的地址
            .loginPage("/login")
            // 实现登录的地址，主要是设置了之后，前端要访问这个地址来登录
            .loginProcessingUrl("/api/login")
            // 登录成功之后，去往的地址
            .defaultSuccessUrl("/home")
            // 登录失败的处理
            .failureHandler((request, response, exception) -> {
                String errorMsg;
                if (exception instanceof BadCredentialsException) {
                    errorMsg = "用户名或密码错误";
                } else if (exception instanceof DisabledException) {
                    errorMsg = "账户已禁用";
                } else {
                    errorMsg = "登录失败";
                }

                // 将错误信息存储在session中
                // 设置一个标志表示这是重定向后的消息
                request.getSession().setAttribute("msg", errorMsg);
                request.getSession()
                    .setAttribute("isRedirectMessage", true);

                // 将错误信息传递到登录页面
                response.sendRedirect("/login");
            }));

        // 退出登录也是一样
        http.logout(customizer -> customizer
            // 设置了之后，前端要访问这个地址来退出登录
            .logoutUrl("/api/logout")
            // 退出登录成功之后，去往的地址
            .logoutSuccessUrl("/login"));

        return http.build();
    }

}
