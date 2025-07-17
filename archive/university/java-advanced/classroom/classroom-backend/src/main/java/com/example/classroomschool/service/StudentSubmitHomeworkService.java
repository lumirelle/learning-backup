package com.example.classroomschool.service;

import com.example.classroomschool.entity.homework.StudentSubmitHomework;
import com.example.classroomschool.mapper.StudentSubmitHomeworkMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StudentSubmitHomeworkService {

  @Resource
  StudentSubmitHomeworkMapper studentSubmitHomeworkMapper;

  public int readHomework(String student, int homeworkId, String review) {
    return studentSubmitHomeworkMapper.updateRead(student, homeworkId, review);
  }

  public int scoringHomework(String student, int homeworkId, String review, int score) {
    return studentSubmitHomeworkMapper.updateScore(student, homeworkId, review, score);
  }

  public int handIn(StudentSubmitHomework studentSubmitHomework) {
    return studentSubmitHomeworkMapper.insert(studentSubmitHomework);
  }

  public StudentSubmitHomework getHomework(String student, int homework) {
    return studentSubmitHomeworkMapper.selectHomework(student, homework);
  }

  public int update(StudentSubmitHomework studentSubmitHomework) {
    return studentSubmitHomeworkMapper.updateByPrimaryKey(studentSubmitHomework);
  }

  public int deleteByHomeworkId(int homeworkId) {
    return studentSubmitHomeworkMapper.deleteByHomeworkId(homeworkId);
  }

  public int deleteUserHomework(String student, Integer homework) {
    return studentSubmitHomeworkMapper.deleteByMap(student, homework);
  }

}
