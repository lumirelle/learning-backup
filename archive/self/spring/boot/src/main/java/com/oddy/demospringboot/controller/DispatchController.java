package com.oddy.demospringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DispatchController {

  // 无需配置首页（默认以 index.html 为首页）

  @GetMapping("/register")
  public String register() {
    return "register";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/debug")
  public String debug() {
    return "debug";
  }

}
