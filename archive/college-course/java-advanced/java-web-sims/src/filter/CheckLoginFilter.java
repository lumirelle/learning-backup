package filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import util.Logger;

public class CheckLoginFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) {
    Logger.info("LoginFilter: 初始化成功！");
  }

  /**
   * 检查用户是否已经登录，如果没有登录，则重定向到登录页面
   *
   * @param servletRequest  Servlet 请求
   * @param servletResponse Servlet 响应
   * @param filterChain     过滤器链
   * @throws ServletException Servlet 异常
   * @throws IOException      IO 异常
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws ServletException, IOException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    String uri = request.getRequestURI();

    // 如果已经登录（用户名不为空）或者访问的是登录页面，则不需要重定向
    boolean noNeedRedirect = request.getSession().getAttribute("username") != null || uri.equals(
        request.getContextPath() + "/login.jsp");

    if (noNeedRedirect) {
      Logger.info("LoginFilter: 不需要登录：" + request.getRequestURI());
      filterChain.doFilter(request, response);
    } else {
      Logger.info("LoginFilter: 需要登录：" + request.getRequestURI());
      response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
  }


  @Override
  public void destroy() {
    Logger.info("LoginFilter: 销毁成功！");
  }
}
