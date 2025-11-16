package com.example.classroomschool.service;

import com.example.classroomschool.entity.homework.StudentHomework;
import com.example.classroomschool.mapper.StudentHomeWorkMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StudentHomeWorkService {

  @Resource
  StudentHomeWorkMapper studentHomeWorkMapper;

  public List<StudentHomework> getHomeWork(int homeworkId) {
    return studentHomeWorkMapper.selectHomework(homeworkId);
  }
}
