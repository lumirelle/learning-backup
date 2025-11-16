package com.oddy.aop;

import com.oddy.aop.entity.student.Student;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainAop {

  public static void main(String[] args) {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("aop.xml");
    Student student = context.getBean(Student.class);
    student.study("!!!!!");
  }
}
