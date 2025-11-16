package com.oddy.demossm.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

// 5. 拦截器
// 拦截器不同于 Filter：Filter 先过滤请求，再进入 Servlet，而拦截器是先进入 Servlet，再开始拦截
// 请求的方法中发生异常时，拦截器不会执行 postHandle 方法，但仍旧执行 afterCompletion 方法
@Slf4j
public class MainInterceptor implements HandlerInterceptor {

  // 一般用于权限控制
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    log.info("MainInterceptor preHandle: " + request.getRequestURI());
    return true;
  }

  // 一般用于日志记录
  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) {
    log.info("MainInterceptor postHandle: " + request.getRequestURI());
  }

  // 一般用于资源释放
  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    log.info("MainInterceptor afterCompletion: " + request.getRequestURI());
  }

}
