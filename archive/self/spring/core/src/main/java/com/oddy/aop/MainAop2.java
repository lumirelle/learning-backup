package com.oddy.aop;

import com.oddy.aop.config.ApplicationConfig;
import com.oddy.aop.entity.student.Student2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainAop2 {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        ApplicationConfig.class);
    Student2 student2 = context.getBean(Student2.class);
    student2.study("!!!!!");
  }
}
