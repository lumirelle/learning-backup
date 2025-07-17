package com.example.classroomschool.service;

import com.example.classroomschool.entity.course.CourseWithTC;
import com.example.classroomschool.mapper.CourseWithTCMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class CourseWithTCService {

  @Resource
  CourseWithTCMapper courseWithTCMapper;

  public List<CourseWithTC> findByTeacherId(int teacherId) {
    return courseWithTCMapper.selectCourse(teacherId);
  }
}
