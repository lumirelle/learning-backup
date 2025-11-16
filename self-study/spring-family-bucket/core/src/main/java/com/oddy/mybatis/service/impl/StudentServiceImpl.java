package com.oddy.mybatis.service.impl;

import com.oddy.mybatis.entity.Student;
import com.oddy.mybatis.mapper.StudentMapper;
import com.oddy.mybatis.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StudentServiceImpl implements StudentService {

  @Resource
  private StudentMapper studentMapper;

  // 业务方法
  // 开启事务
  @Transactional
  @Override
  public void doTest() {
    Student test = new Student();
    test.setSName("Test");
    studentMapper.insertStudent(test);
    // 抛出异常，回滚已经做的事情
    if (true) {
      throw new RuntimeException("Test Exception");
    }
    studentMapper.insertStudent(test);
  }

}
