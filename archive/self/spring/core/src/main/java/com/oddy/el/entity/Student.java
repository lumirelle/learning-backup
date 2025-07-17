package com.oddy.el.entity;

import java.util.List;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class Student {

  // 使用外部文件注入属性值，除了在成员变量上使用 @Value 注解，还可以在 setter/构造方法的参数上使用 @Value 注解
  // 使用 ${} 包裹
  // @Value("${student.name}")
  // 也可以直接注入字符串，不过这样没有意义
  // @Value("张三")
  // 也可以使用 SpEL 表达式，使用 #{} 包裹
  @Value("#{'abc'.toUpperCase().concat('!')}")
  private String name;

  private final String nameNull = null;

  private List<String> hobbies = List.of("篮球", "足球", "乒乓球");

  public String sayHello() {
    return "Hello Student!";
  }
}
