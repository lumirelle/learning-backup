package com.oddy.aop.entity.student;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

// 注解：2. 配置切面
@Aspect
@Component
public class StudentAOP2 {

  // 注解：3. 切面实现
//  @After("execution(public void com.oddy.aop.entity.student.Student2.study(String))")
//  public void afterStudy(JoinPoint joinPoint) {
//    System.out.println("fuck you man!" + joinPoint.getArgs()[0]);
//  }
  // 参数命名绑定
  @After(value = "execution(public void com.oddy.aop.entity.student.Student2.study(..)) && args(str)", argNames = "str")
  public void afterStudy(String str) {
    System.out.println("fuck you man!" + str);
  }
  // before、around 等类似
}
