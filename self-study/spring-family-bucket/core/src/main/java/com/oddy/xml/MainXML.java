package com.oddy.xml;

import com.oddy.xml.entity.student.Student;
import com.oddy.xml.entity.student.StudentFactoryBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Oddy
 */
public class MainXML {

  public static void main(String[] args) {
    // 创建应用程序上下文
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application.xml");

    // 从上下文中获取 bean
    Student student = context.getBean("student", Student.class);
    Student student1 = context.getBean("student1", Student.class);
    Student student2 = context.getBean("student2", Student.class);
    Student student3 = context.getBean("student3", Student.class);
    Student student4 = context.getBean("student4", Student.class);
    // 直接用名称获取 FactoryBean 得到的是工厂方法创建的 bean
    // 用 & + 名称 获取 FactoryBean 得到的是 FactoryBean 本身
    Student student5 = context.getBean("student5", Student.class);
    StudentFactoryBean factoryBean = context.getBean("&student5", StudentFactoryBean.class);

    // 测试
    System.out.println(student);
    student.getTeacher().teach();
    System.out.println(student1);
    System.out.println(student2);
    System.out.println(student3);
    System.out.println(student4);
    System.out.println(student5);
    System.out.println(factoryBean);
  }
}
