package com.fruit.interceptor;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, @Nonnull HttpServletResponse response,
      @Nonnull Object handler)
      throws Exception {
    String uri = request.getRequestURI();
    // 判断是否是登录/注册请求
    if (!(uri.contains("login") || uri.contains("Login") || uri.contains("register"))) {
      // 非登录/注册请求，判断是否登录
      if (request.getSession().getAttribute("user") != null) {
        // 已登录，放行
        return true;
      } else {
        // 未登录，判断是否是静态资源
        if (uri.contains("css") || uri.contains("js") || uri.contains("images")) {
          // 静态资源，放行
          return true;
        } else {
          // 非静态资源，重定向到登录页面
          response.sendRedirect(request.getContextPath() + "/user/toLogin.action");
        }
      }
    } else {
      // 登录/注册请求，放行
      return true;
    }
    return false;
  }

}
