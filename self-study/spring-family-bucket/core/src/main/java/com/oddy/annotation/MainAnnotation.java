package com.oddy.annotation;

import com.oddy.annotation.config.ApplicationConfig;
import com.oddy.annotation.entity.student.Student;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainAnnotation {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        ApplicationConfig.class);
    Student student = context.getBean("student", Student.class);
    System.out.println(student);
    // 3. 触发同步、异步任务
//    try {
//      student.asyncTask();
//      student.syncTask();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
    // 6.5 发布事件
    student.publishEvent();
//    context.close();
  }
}
