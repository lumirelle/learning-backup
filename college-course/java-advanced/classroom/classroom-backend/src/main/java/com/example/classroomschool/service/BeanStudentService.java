package com.example.classroomschool.service;

import com.example.classroomschool.entity.student.BeanStudent;
import com.example.classroomschool.mapper.BeanStudentMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class BeanStudentService {

  @Resource
  BeanStudentMapper beanStudentMapper;

  public BeanStudent getStudent(Integer student) {
    return beanStudentMapper.selectStudentPlus(student);
  }
}
