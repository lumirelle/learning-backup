package com.oddy.aop.entity.student;

import org.springframework.stereotype.Component;

@Component
public class Student2 {

  public void study(String str) {
    System.out.println("Student is studying..." + str);
  }
}
