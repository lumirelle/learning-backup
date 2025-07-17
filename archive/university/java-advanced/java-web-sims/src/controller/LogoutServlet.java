package controller;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/servlet/logout")
public class LogoutServlet extends HttpServlet {

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

      request.getSession().invalidate();
      response.sendRedirect(request.getContextPath() + "/login.jsp");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
