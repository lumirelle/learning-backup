package controller;

import entity.UserBean;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.impl.SimpleBeanService;

@WebServlet("/servlet/login")
public class LoginServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      // 设置编码
      request.setCharacterEncoding("utf-8");
      response.setContentType("text/html;charset=utf-8");

      // 请求参数对应 input 标签的 name 属性
      String username = request.getParameter("username");
      String password = request.getParameter("password");

      // 从数据库中查找用户
      SimpleBeanService<UserBean> simpleUserService = new SimpleBeanService<>(UserBean.class);
      UserBean user = simpleUserService.findBeanByPrimaryKey(username); // 用户主码为 username

      // 验证成功
      if (user != null && password.equals(user.getPassword())) {
        request.getSession().setAttribute("username", username); // 保存用户名，相当于登录
        response.sendRedirect(request.getContextPath() + "/index.jsp");
      }
      // 验证失败，调度回登录页面
      else {
        request.setAttribute("inputUsername", username);
        request.setAttribute("inputPassword", password);
        request.setAttribute("invalidate", "yes");
        request.getRequestDispatcher("/login.jsp").forward(request, response);
      }
    } catch (IOException | ServletException e) {
      e.printStackTrace();
    }
  }
}
