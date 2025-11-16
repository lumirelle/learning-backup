package com.oddy.xml.entity.student;

public class StudentFactoryDynamic {

  public Student create() {
    System.out.println("调用工厂方法创建对象");
    Student student = new Student();
    student.setName("Oddy from dynamic factory method");
    return student;
  }
}
