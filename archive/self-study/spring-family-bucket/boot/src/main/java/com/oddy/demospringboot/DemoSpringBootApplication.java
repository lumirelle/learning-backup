package com.oddy.demospringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 相当于 @SpringBootConfiguration、@EnableAutoConfiguration、@ComponentScan
// @SpringBootConfiguration：标注当前类是一个 Spring Boot 配置类
// @EnableAutoConfiguration：开启自动配置功能，以前需要配置的东西，Spring Boot 帮我们自动配置
// @ComponentScan：开启当前路径下组件扫描，相当于 <context:component-scan base-package="com.oddy.demospringboot"/>
@SpringBootApplication
// !!! 部署外部 Tomcat，继承 SpringBootServletInitializer
// public class DemoSpringBootApplication extends SpringBootServletInitializer {
public class DemoSpringBootApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoSpringBootApplication.class, args);
  }

// !!! 部署外部 Tomcat，重写 configure 方法
//  @Override
//  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//    return builder.sources(DemoSpringBootApplication.class);
//  }

}
