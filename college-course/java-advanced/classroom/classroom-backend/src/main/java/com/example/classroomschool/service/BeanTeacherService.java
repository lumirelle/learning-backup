package com.example.classroomschool.service;

import com.example.classroomschool.entity.teacher.BeanTeacher;
import com.example.classroomschool.mapper.BeanTeacherMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class BeanTeacherService {

  @Resource
  BeanTeacherMapper beanTeacherMapper;

  public BeanTeacher getTeacher(int teacher, int courseId) {
    return beanTeacherMapper.selectTeacherPlus(teacher, courseId);
  }

}
