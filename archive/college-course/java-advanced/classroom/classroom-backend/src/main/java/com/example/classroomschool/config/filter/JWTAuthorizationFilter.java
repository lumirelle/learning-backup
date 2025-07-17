package com.example.classroomschool.config.filter;


import com.example.classroomschool.util.token.TokenUtil;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * 请求验证的过滤器，进行token验证和权限添加
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

  public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
    super(authenticationManager);
  }

  @Override
  public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    String token = request.getHeader("Authorization");
    //如果请求头中没有Authorization信息则放行
    if (token == null) {
      chain.doFilter(request, response);
      return;
    }
    // 如果请求头中有token，则进行解析，并且设置认证信息
    SecurityContextHolder.getContext().setAuthentication(getAuthentication(token));
    super.doFilterInternal(request, response, chain);
  }

  private UsernamePasswordAuthenticationToken getAuthentication(String tokenHeader) {
    String token = tokenHeader.replace("Bearer ", "");

    String username = TokenUtil.getAccount(token);
    String role = TokenUtil.getRole(token);
    if (username != null) {
      return new UsernamePasswordAuthenticationToken(username, null,
          Collections.singleton(new SimpleGrantedAuthority(role)));
    }
    return null;
  }
}
