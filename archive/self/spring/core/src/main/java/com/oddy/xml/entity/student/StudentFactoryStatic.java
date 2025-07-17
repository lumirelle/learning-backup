package com.oddy.xml.entity.student;

public class StudentFactoryStatic {

  public static Student create() {
    System.out.println("调用工厂方法创建对象");
    Student student = new Student();
    student.setName("Oddy from static factory method");
    return student;
  }
}
