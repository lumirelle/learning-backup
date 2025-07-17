package com.oddy.el;

import com.oddy.el.config.ApplicationConfig;
import com.oddy.el.entity.Student;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class MainEL {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        ApplicationConfig.class);
    // 1. SpringEL 表达式
    ExpressionParser parser = new SpelExpressionParser();
    // 1.1 字面量
    Expression expression = parser.parseExpression("'Hello World!'");
    System.out.println("1: " + expression.getValue());
    // 1.2 字面量调用方法
    expression = parser.parseExpression("'Hello World!'.concat('!')");
    System.out.println("2: " + expression.getValue());
    // 1.3 字面量访问属性（调用对应 getter 方法也可以使用访问属性的方式实现）
    expression = parser.parseExpression("'Hello World!'.bytes");
    System.out.println("3: " + expression.getValue());
    // 1.4 给定对象调用方法
    Student student = context.getBean(Student.class);
    expression = parser.parseExpression("sayHello()");
    System.out.println("4: " + expression.getValue(student));
    // 1.5 给定对象访问属性
    expression = parser.parseExpression("name");
    System.out.println("5: " + expression.getValue(student));
    // 1.6 给定对象设置属性，注意：这里的属性必须有权限和 setter 方法
    expression = parser.parseExpression("name");
    expression.setValue(student, "李四");
    System.out.println("6: " + student.getName());
    // 1.7 构造方法
    expression = parser.parseExpression("new String('Hello World!').toUpperCase()");
    System.out.println("7: " + expression.getValue());
    // 1.8 算数运算符
    expression = parser.parseExpression("1 + 2 * 3");
    System.out.println("8: " + expression.getValue());
    // 1.9 关系运算符
    expression = parser.parseExpression("1 > 2");
    System.out.println("9: " + expression.getValue());
    // 1.10 逻辑运算符
    expression = parser.parseExpression("true and true");
    System.out.println("10: " + expression.getValue());
    // 1.11 赋值运算符
    expression = parser.parseExpression("name = '张四'");
    expression.getValue(student);
    System.out.println("11: " + student.getName());
    // 其余运算符省略
    // 1.12 需要导入包的情况，使用 T() 方法，参数是一个类的全限定名
    // 注意：在 new 对象的时候，不需要使用 T() 方法，直接使用全限定名即可
    expression = parser.parseExpression("T(java.lang.Math).random()");
    System.out.println("12: " + expression.getValue());
    // 1.13 集合基本用法 list/map ，集合的基本用法很多但是很常规，类似 python、javascript，因而这里只举例一种
    expression = parser.parseExpression("{'a', 'b', 'c'}");
    System.out.println("13: " + expression.getValue());
    // 1.14 集合条件获取，使用 .? 运算符，其中 #this 表示当前元素
    expression = parser.parseExpression("hobbies.?[#this != '篮球']");
    System.out.println("14: " + expression.getValue(student));
    // 1.15 集合投影，使用 .! 运算符，其中 #this 表示当前元素
    // 这里将 hobbies 集合中的每个元素的长度作为新集合返回
    expression = parser.parseExpression("hobbies.![#this.length()]");
    System.out.println("15: " + expression.getValue(student));
    // 1.16 安全导航运算符，使用 ?. 运算符，如果对象为 null，那么返回 null，否则返回对应的值
    expression = parser.parseExpression("nameNull?.length()");
    System.out.println("16: " + expression.getValue(student));
  }
}
