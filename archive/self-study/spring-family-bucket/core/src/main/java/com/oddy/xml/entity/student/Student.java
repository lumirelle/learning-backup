package com.oddy.xml.entity.student;

import com.oddy.xml.entity.teacher.Teacher;
import lombok.Data;

@Data
public class Student {

  private String name;

  private Teacher teacher;

  // 通过构造方法注入属性，需要显示提供参数名称，否则使用 javac -parameters 编译，旧的 -debug 参数已经弃用
//  @ConstructorProperties({"name", "teacher"})
//  public Student(String name, Teacher teacher) {
//    this.name = name;
//    this.teacher = teacher;
//  }
}
