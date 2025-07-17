package com.oddy.annotation.config;

import com.oddy.annotation.entity.student.Student;
import com.oddy.annotation.entity.teacher.ArtTeacher;
import com.oddy.annotation.entity.teacher.Teacher;
import com.oddy.annotation.listener.TestListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 1. 导入其他配置类
// @Import({XXXConfig.class})
// 2.6 使用注解注册 bean
// @ComponentScan("com.oddy.annotation.entity")，启用组件扫描，扫描指定包下的所有组件
// 3. 异步任务支持
// @EnableAsync
// 具体见 Student.java
// 4. 定时任务支持
// @EnableScheduling
// 具体见 Student.java
@Configuration
public class ApplicationConfig {

  @Bean
  public Teacher teacher() {
    return new ArtTeacher();
  }

  // 2. 注册 bean
  // 2.1 懒加载
  // @Lazy
  // 2.2 作用域：原型模式/单例模式
  // @Scope("prototype")
  // 2.3 依赖：先实例化 teacher，再实例化 student
  // @DependsOn("teacher")
  @Bean
  public Student student(Teacher teacher) { // 2.4 使用方法参数的方式实现依赖注入
    // setter/构造方法依赖注入取决于你自己使用哪种方式
    Student student = new Student();
    // 设置默认值
    student.setName("Oddy");
    // setter 依赖注入
    student.setTeacher(teacher);
    return student;
    // 2.5 自动装配，见 Student.java 14:6
    // 2.6 使用注解注册 bean，见 Student.java 10:4
  }

  @Bean
  public TestListener testListener() {
    return new TestListener();
  }
}
