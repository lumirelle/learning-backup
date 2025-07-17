package com.oddy.demospringboot.controller;

import com.oddy.demospringboot.entity.Account;
import com.oddy.demospringboot.mapper.AccountMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 使用 @Tag 在 Swagger 生成的 API 页面中，为该 Controller 添加自定义名称、描述等
@Tag(name = "账户注册相关", description = "包括获取验证码、注册账户请求等操作。")
// 开启接口校验
@Validated
@Slf4j
@RestController
public class RegisterController {

  private final Random random = new Random();

  @Resource
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String mailFrom;

  // 使用 @ApiResponse 为接口添加返回结果样例
  @ApiResponse(responseCode = "200", description = "获取成功")
  @ApiResponse(responseCode = "500", description = "获取失败")
  // 使用 @Operation 在 API 界面中为接口添加总结、描述等
  @Operation(summary = "获取邮箱验证码", description = "获取邮箱验证码")
  @PostMapping("/verify-code")
  // 使用 @Parameter 为接口参数添加描述、样例等
  public String verifyCode(HttpSession session,
      @Parameter(description = "接收验证码的邮件地址", example = "test@test.com") @Email @RequestParam String email) {
    // 生成 6 位随机验证码
    int code = 100000 + random.nextInt(900000);
    session.setAttribute("code", code);
    session.setAttribute("email", email);
    log.info("用户发起注册，验证码为：" + code + "，邮箱地址为：" + email);

    // 发送验证码到用户邮箱
    String subject = "注册验证码";
    String text = "您的验证码为：" + code + "，有效时间为 3 分钟，请妥善保存。";
    SimpleMailMessage message = new SimpleMailMessage();
    message.setSubject(subject);
    message.setText(text);
    message.setFrom(mailFrom);
    message.setTo(email);
    try {
      mailSender.send(message);
    } catch (MailSendException e) {
      log.error("Send Verify Code Error", e);
      return "fail!";
    }
    return "success!";
  }

  @Resource
  private AccountMapper accountMapper;

  @Resource
  private PasswordEncoder passwordEncoder;

  // 使用邮箱验证码注册
  @PostMapping("/do-register")
  public String doRegister(@NotBlank @RequestParam String name, @Email @RequestParam String email,
      @NotBlank @RequestParam String code, @NotBlank @RequestParam String password,
      HttpSession session) {
    // 验证邮箱和验证码一致性
    if (email.equals(session.getAttribute("email").toString()) &&
        code.equals(session.getAttribute("code").toString())) {
      Account account = new Account();
      account.setName(name);
      account.setEmail(email);
      // 密码加密
      account.setPassword(passwordEncoder.encode(password));
      accountMapper.insert(account);
      return "success!";
    }

    return "fail!";
  }

}
