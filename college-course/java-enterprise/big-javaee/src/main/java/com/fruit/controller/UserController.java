package com.fruit.controller;

import com.fruit.entity.User;
import com.fruit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController extends BaseController {

  @Resource
  private UserService userService;

  @RequestMapping("/user/toLogin.action")
  public String toLogin() {
    return "/login.jsp";
  }

  @RequestMapping("/user/login.action")
  public String login(Model model, HttpServletRequest request, String username, String password) {
    Map<String, Object> map = new HashMap<>();
    map.put("username", username);
    map.put("password", password);
    List<User> userList = userService.find(map);
    if (userList != null && !userList.isEmpty()) {
      request.getSession().setAttribute("user", userList.get(0));
      return "/home.jsp";
    }
    model.addAttribute("errorMsg", "用户名或密码错误，请重新输入！");
    return "/login.jsp";
  }

  @RequestMapping("/user/registerPage.action")
  public String registerPage() {
    return "/register.jsp";
  }

  @RequestMapping("/user/register.action")
  public String register(User user, Model model, HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> map = new HashMap<>();
    map.put("username", user.getUsername());
    List<User> userList = userService.find(map);
    if (userList != null && !userList.isEmpty()) {
      model.addAttribute("errorMsg", "注册失败，用户名已被占用！");
      return "/register.jsp";
    }
    user.setUserId(UUID.randomUUID().toString());
    userService.insert(user);
    model.addAttribute("noticeMsg", "注册成功，请登录！");
    return "/login.jsp";
  }

}
