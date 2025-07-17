package com.oddy.mybatis;

import com.oddy.mybatis.config.ApplicationConfig;
import com.oddy.mybatis.mapper.StudentMapper;
import lombok.extern.java.Log;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Log
public class MainMyBatis {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        ApplicationConfig.class);

    // 指定 mapper 方式 1
//    SqlSessionTemplate sqlSessionTemplate = context.getBean(SqlSessionTemplate.class);
//    StudentMapper studentMapper = sqlSessionTemplate.getMapper(StudentMapper.class);
    // 指定 mapper 方式 2，直接从容器中取出
    StudentMapper studentMapper = context.getBean(StudentMapper.class);
    log.info(studentMapper.selectStudentById(1).toString());

    // 事务
//    StudentService studentService = context.getBean(StudentService.class);
//    studentService.doTest();
  }

}
