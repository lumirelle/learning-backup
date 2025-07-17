package com.oddy.demoss2.init;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

// 0. 初始化 SpringSecurity
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
  // 不需要重写/实现任何方法
  // 这里实际上会自动注册一个 Filter: DelegatingFilterProxy
  // SpringSecurity 底层就是通过 N 个 Filter 来实现的
}
