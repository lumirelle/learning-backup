package com.oddy.junit;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.oddy.mybatis.config.ApplicationConfig;
import com.oddy.mybatis.entity.Student;
import com.oddy.mybatis.mapper.StudentMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

// @ExtendWith 的作用是在测试开始时初始化 Spring 容器
@ExtendWith(SpringExtension.class)
// @ContextConfiguration 的作用是指定 Spring 容器的配置信息
@ContextConfiguration(classes = ApplicationConfig.class)
class MainTest {

  @Resource
  private StudentMapper studentMapper;

  @Test
  void test() {
    System.out.println("test start");
    Student student = studentMapper.selectStudentById(1);
    System.out.println(student);
    assertEquals("鸡哥", student.getSName());
  }

}
