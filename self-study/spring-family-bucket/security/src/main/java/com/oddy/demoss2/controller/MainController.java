package com.oddy.demoss2.controller;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class MainController {

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

//  @PostMapping("/pay")
//  @ResponseBody
//  public String pay(@RequestParam("target") String target) {
//    JSONObject json = new JSONObject();
//    log.info("支付成功，支付目标：{}", target);
//    json.put("code", 0);
//    json.put("msg", "支付成功");
//    json.put("data", target);
//    return json.toJSONString();
//  }

//  @Resource
//  private UserDetailsManager userDetailsManager;

//  @Resource
//  private PasswordEncoder passwordEncoder;

  // 使用 UserDetailsManager，可以快速执行用户相关的管理操作，如修改密码
  // UserDetailsService 则只能获取用户信息
//  @PostMapping("/change-password")
//  @ResponseBody
//  public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
//    userDetailsManager.changePassword(oldPassword, passwordEncoder.encode(newPassword));
//    JSONObject json = new JSONObject();
//    log.info("修改密码，旧密码：{}，新密码：{}", oldPassword, newPassword);
//    json.put("code", 0);
//    json.put("msg", "修改密码成功");
//    json.put("data", newPassword);
//    return json.toJSONString();
//  }

  // （权限过滤）使用 @PreAuthorize 注解 配合 SpringEL 表达式，实现方法级别的权限控制
  // 还可以使用 @PostAuthorize 注解，在方法执行后再进行权限校验
  // 与之类似的还有 @Secured 注解，但是 @Secured 只能使用常量，不够灵活
  // （参数过滤）此外还有 @PreFilter 和 @PostFilter 注解，可以对集合类型的参数或返回值进行过滤
  // 注意，除了 Controller 外，所有被 Spring 管理的 bean 都可以使用注解控制权限
  // 我们可以在任意 bean 的方法上使用注解，只要不满足权限，就会返回 403 错误
  @PreAuthorize("hasRole('ADMIN')")
  @RequestMapping("/debug")
  @ResponseBody
  public String debug() {
    JSONObject json = new JSONObject();
    json.put("code", 0);
    json.put("msg", "Debug success! Now there is no bugs in this garden!");
    return json.toJSONString();
  }

}
