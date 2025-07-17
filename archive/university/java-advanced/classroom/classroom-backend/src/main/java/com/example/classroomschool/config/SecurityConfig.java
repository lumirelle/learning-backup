package com.example.classroomschool.config;


import com.example.classroomschool.config.filter.JWTAuthorizationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 权限分配验证、跨域配置
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        // 跨域
        .cors(cors -> cors.configurationSource(request -> {
          // 1. 添加cors配置信息
          CorsConfiguration corsConfiguration = new CorsConfiguration();
          corsConfiguration.addAllowedOrigin("http://localhost:8100"); // 生产环境中请使用确定的域名
          corsConfiguration.addAllowedHeader("*");
          corsConfiguration.addAllowedMethod("*");
          corsConfiguration.setAllowCredentials(true); // 允许携带cookie
          corsConfiguration.setMaxAge(3600L);
          // 2. 添加映射路径
          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/**", corsConfiguration);
          return corsConfiguration;
        }))
        // 关闭csrf
        .csrf(AbstractHttpConfigurer::disable)
        // 权限配置
        .authorizeRequests(
            requests -> requests.antMatchers("/api/student/homework", "/api/student/homeworkShow")
                .hasAnyRole("teacher", "student")
                .antMatchers("/api/student/**")
                .hasRole("student")
                .antMatchers("/api/v1/**")
                .authenticated()
                .antMatchers("/api/teacher/**")
                .hasRole("teacher")
                // 需要验证了的用户才能访问
                .antMatchers("/api/start/**")
                .authenticated()
                // 其他都放行
                .anyRequest()
                .permitAll())
        // 添加 jwt 过滤器
        .addFilter(new JWTAuthorizationFilter(authenticationManager()))
        // 不需要session
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }
}
