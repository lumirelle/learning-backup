package com.example.classroomschool.service;


import com.example.classroomschool.entity.course.CourseWithSC;
import com.example.classroomschool.mapper.CourseWithSCMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class CourseWithSCService {

  @Resource
  private CourseWithSCMapper courseWithscMapper;

  public List<CourseWithSC> findByStudentId(int studentId) {
    return courseWithscMapper.selectCourse(studentId);
  }
}
