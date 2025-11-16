package com.oddy.demospringboot.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
// Runner 用于在 Spring Boot 启动后执行一些特定的任务
public class TestRunner implements ApplicationRunner {

  @Override
  public void run(ApplicationArguments args) {
    log.info("自定义的测试运行器启动！");
  }

}
