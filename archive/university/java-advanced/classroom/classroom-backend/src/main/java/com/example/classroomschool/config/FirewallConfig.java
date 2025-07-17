package com.example.classroomschool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

@Configuration
public class FirewallConfig {

  // 允许多请求地址多斜杠  比如 /a//b
  @Bean
  public HttpFirewall httpFirewall() {
    return new DefaultHttpFirewall();
  }
}
