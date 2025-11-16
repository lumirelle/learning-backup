package com.oddy.xml.entity.student;

import org.springframework.beans.factory.FactoryBean;

public class StudentFactoryBean implements FactoryBean<Student> {

  public Student create() {
    System.out.println("调用工厂方法创建对象");
    Student student = new Student();
    student.setName("Oddy from factory bean");
    return student;
  }

  @Override
  public Student getObject() throws Exception {
    return create();
  }

  @Override
  public Class<?> getObjectType() {
    return Student.class;
  }
}
