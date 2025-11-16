package com.oddy.demossm.controller;

import com.alibaba.fastjson2.JSONObject;
import com.oddy.demossm.entity.User;
import jakarta.servlet.http.HttpSession;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@Controller
public class HelloController {

  @Autowired
  private User userRequest;

  @Autowired
  private User userSession;

  @ResponseBody
  @GetMapping("/hello")
  public String hello() {
    return "Hello World!";
  }

  //  1. ModelAndView 基本用法
  //  复杂写法
  //  @GetMapping("/")
  //  public ModelAndView index() {
  //    ModelAndView modelAndView = new ModelAndView("index");
  //    modelAndView.getModel().put("username", "Oddy");
  //    modelAndView.getModel().put("password", "Oddy");
  //    return modelAndView;
  //  }
  //  简单写法，直接返回 view 名称字符串
  //  使用自动装配的 Model 实现数据传递
  @GetMapping("/")
  public String index(Model model, HttpSession session) {
    User user = new User();
    user.setUsername("Oddy Simple");
    user.setPassword("Oddy Simple Password");
    model.addAttribute("username", user.getUsername());
    model.addAttribute("password", user.getPassword());
    session.setAttribute("user", user);
    return "index";
  }

  // 2. @RequestParam、@RequestHeader
  // params 属性限制请求参数
  // params="xxx" 表示必须携带 xxx 参数
  // params!="xxx" 表示不能携带 xxx 参数
  // params="xxx=yyy" 表示必须携带 xxx 参数，且值为 yyy
  // params="xxx!=yyy" 表示必须携带 xxx 参数，且值不能为 yyy
  // @RequestParam 注解可以获取请求参数，
  // 如果请求参数名和方法参数名一致，可以省略 @RequestParam 注解
  @PostMapping(value = "/login")
  public String login(Model model, @RequestParam String username, @RequestParam String password) {
    log.info("username: " + username);
    log.info("password: " + password);
    model.addAttribute("username", username);
    model.addAttribute("password", password);
    return "index";
  }

  // 此外，还可以使用对象作为方法形参，接收请求参数，
  // 请求参数名和对象属性名一致，Spring 会为我们自动组装为对象
  @PostMapping("/login-obj")
  public String loginObj(Model model, User user) {
    log.info(user.toString());
    model.addAttribute("username", user.getUsername());
    model.addAttribute("password", user.getPassword());
    return "index";
  }
  // @RequestHeader 注解可以获取请求头信息，用法和 @RequestParam 类似，不再赘述

  // 3. @CookieValue、@SessionAttribute
  @GetMapping("/cookie-session")
  public String cookieSession(Model model, @CookieValue("JSESSIONID") String sessionId,
      @SessionAttribute("user") User user) {
    log.info("sessionId: " + sessionId);
    log.info("user: " + user.toString());
    model.addAttribute("sessionId", sessionId);
    model.addAttribute("username", user.getUsername());
    model.addAttribute("password", user.getPassword());
    return "cookie-session";
  }

  // 4. Redirect、Forward
  // 需要注意的是，现在流行的前后端分离模式下，
  // 后端只起到提供接口/发送数据的作用，
  // 本项目是前后端不分离的模式
  @GetMapping("/redirect")
  public String redirect() {
    return "redirect:/hello";
  }

  @GetMapping("/forward")
  public String forward() {
    return "forward:/hello";
  }

  // 5. Bean 的 Web 作用域
  @GetMapping("/scope")
  // 表示返回的是字符串而不是页面的名称
  @ResponseBody
  public String scope() {
    return "userRequest: " + userRequest.toSuperString() + "<br />" + "userSession: "
        + userSession.toSuperString();
  }

  // Restful 是一种风格，如下所示
  // 即将路径的一部分作为参数使用
  // 并且为一个 url，按请求方法不同分为不同功能
  @PostMapping("/restful/{str}")
  @ResponseBody
  public String restfulPost(HttpSession session, @PathVariable("str") String sth) {
    session.setAttribute("restful", "restful: " + sth);
    return "restful post: " + sth;
  }

  @GetMapping("/restful")
  @ResponseBody
  public String restfulGet(HttpSession session) {
    Object restful = session.getAttribute("restful");
    if (restful == null) {
      return "restful get-null";
    }
    return "restful get: " + restful;
  }

  @DeleteMapping("/restful")
  @ResponseBody
  public String restfulDelete(HttpSession session) {
    session.removeAttribute("restful");
    return "restful delete";
  }

  // 6. 异常处理
  @GetMapping("/error/{str}")
  public String error(@PathVariable String str) {
    throw new RuntimeException(str);
  }

  // 7. JSON 数据交互
  // 本项目使用的是 fastjson
  // 手动转换格式
  @GetMapping(value = "/json", produces = "application/json;charset=utf-8")
  @ResponseBody
  public String json() {
    // 创建一个 User 对象
    User user = new User();
    user.setUsername("Oddy JSON + 乱码测试");
    user.setPassword("Oddy JSON");
    // 将对象转换为 JSON 字符串
    return JSONObject.toJSONString(user);
  }

  // SpringMVC 支持自动转换格式，需要 fastjson2-extension-spring6 依赖，
  // 以及在配置文件中配置消息转换器，
  // 此后，方法直接返回对象即可
  @GetMapping(value = "/json-auto", produces = "application/json;charset=utf-8")
  @ResponseBody
  public User jsonAuto() {
    // 创建一个 User 对象
    User user = new User();
    user.setUsername("Oddy JSON + 乱码测试");
    user.setPassword("Oddy JSON");
    return user;
  }

  @PostMapping(value = "/json-receive")
  @ResponseBody
  // SpringMVC 在接受 JSON 数据时，需要使用对象作为参数接受数据，并且使用 @RequestBody 注解做标记
  public String jsonReceive(@RequestBody User user) {
    if (user == null) {
      return "json-receive-null";
    }
    if (!Objects.equals(user.getUsername(), "abc") || !Objects.equals(user.getPassword(), "123")) {
      return "json-receive-fail";
    }
    log.info(user.toString());
    return "json-receive-success";
  }

  // 8. axios
  // 前后端分离时，前端页面是动态渲染的，需要使用 axios 向后端发送请求获取数据
  @GetMapping("/axios")
  public String axios() {
    return "axios";
  }

}
