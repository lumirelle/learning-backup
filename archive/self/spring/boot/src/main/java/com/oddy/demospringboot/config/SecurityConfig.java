package com.oddy.demospringboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity.authorizeHttpRequests(auth -> auth.requestMatchers("/static/**")
            .permitAll()
            .requestMatchers("/register", "/verify-code", "/do-register")
            .permitAll()
            .anyRequest()
            .authenticated())
        .formLogin(login -> login.loginPage("/login")
            .loginProcessingUrl("/do-login")
            .defaultSuccessUrl("/")
            .permitAll())
        .logout(logout -> logout.logoutUrl("/do-logout")
            .logoutSuccessUrl("/login")
            .permitAll())
        // csrf 记得禁用
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }

}
