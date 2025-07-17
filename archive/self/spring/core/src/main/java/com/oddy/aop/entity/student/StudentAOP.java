package com.oddy.aop.entity.student;

import java.lang.reflect.Method;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

public class StudentAOP implements MethodBeforeAdvice, AfterReturningAdvice { // advisor 实现
  // 还可以实现 MethodInterceptor 接口，实现环绕方法，与 aop:around 一致

  // 使用 JoinPoint 获取被代理的方法的信息
//  public void afterStudy(JoinPoint joinPoint) {
//    System.out.println("fuck you man!" + joinPoint.getArgs()[0]);
//  }
//
  // 环绕方法，需要手动执行原方法，可以获得方法返回值等更多的属性
//  public void aroundStudy(ProceedingJoinPoint joinPoint) throws Throwable {
//    System.out.println("fuck me man!" + joinPoint.getArgs()[0]);
//    joinPoint.proceed();
//    System.out.println("fuck you man!" + joinPoint.getArgs()[0]);
//  }

  @Override
  public void before(Method method, Object[] args, Object target) throws Throwable {
    System.out.println("fuck me man!" + args[0]);
  }

  @Override
  public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
    System.out.println("fuck you man!" + args[0]);
  }
}
