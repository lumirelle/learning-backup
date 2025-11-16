package com.oddy.demospringboot.controller;

import com.oddy.demospringboot.entity.Account;
import com.oddy.demospringboot.mapper.AccountMapper;
import com.oddy.demospringboot.repo.AccountRepository;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class TestController {

  // 读取配置文件中的值
  @Value("${test.message}")
  private String message;

  @Value("${test.id}")
  private Integer id;

  @Value("${test.name}")
  private String name;

  @Value("${test.age}")
  private Integer age;

  @Value("${test.sex}")
  private String sex;

  @Value("${test.email}")
  private String email;

  @GetMapping("/test")
  @ResponseBody
  public String test() {
    return message;
  }

  // 无需自己实现 HttpMessageConverter
  @GetMapping("/test-object-yaml")
  @ResponseBody
  public Account testObject() {
    Account account = new Account();
    account.setId(id);
    account.setName(name);
    account.setAge(age);
    account.setSex(sex);
    account.setEmail(email);
    return account;
  }

  @Resource
  private AccountMapper accountMapper;

  @GetMapping("/test-object-query")
  @ResponseBody
  public Account queryTestObject(@RequestParam Integer id) {
    return accountMapper.selectById(id);
  }

  @Resource
  private AccountRepository accountRepository;

  @GetMapping("/test-object-query-jpa")
  @ResponseBody
  public Account queryTestObjectJPA(@RequestParam Integer id) {
    return accountRepository.findById(id).get();
  }

  @GetMapping("/test-logback-mdc")
  @ResponseBody
  public String logbackMDC(HttpSession session) {
    // 通过 MDC 机制传入了一对键值对值
    MDC.put("sessionId", session.getId());
    log.info("用户访问了一次测试接口！");
    return message;
  }

  @Resource
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String mailFrom;

  @GetMapping("/test-email")
  @ResponseBody
  public String email(@RequestParam String to) {
    log.info("邮件发送中...");
    log.info("邮件发送至：" + to);
    String subject = "【火星皇家理工大学教务处】关于近期学校对您的处分决定";
    log.info("邮件主题：" + subject);
    String text = "同学你好，经监控和教务处巡查发现，您近期存在太漂亮、太可爱、迷住您男朋友的行为，现已通知您的男朋友，请发送五个抱抱，在 "
        + LocalDateTime.now().plusHours(7) + " 前交到您男朋友的微信，祝您七夕节快乐。";
    log.info("邮件内容：" + text);

    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setSubject(subject);
    mailMessage.setText(text);
    mailMessage.setFrom(mailFrom);
    mailMessage.setTo(to);
    mailSender.send(mailMessage);
    return "Finish!";
  }

}
