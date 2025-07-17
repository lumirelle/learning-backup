package com.example.classroomschool.service;

import com.example.classroomschool.entity.homework.Homework;
import com.example.classroomschool.mapper.HomeworkMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class HomeworkService {

  @Resource
  HomeworkMapper homeworkMapper;

  public int addHomework(Homework homework) {
    return homeworkMapper.insert(homework);
  }

  public List<Homework> findByCourseId(int courseId) {
    return homeworkMapper.selectHomework(courseId);
  }

  public int updateHomework(Homework homework) {
    return homeworkMapper.updateByPrimaryKey(homework);
  }

  public Homework findById(int homeworkId) {
    return homeworkMapper.selectByPrimaryKey(homeworkId);
  }

  public int deleteByHomeworkId(int homeworkId) {
    return homeworkMapper.deleteByPrimaryKey(homeworkId);
  }

}
