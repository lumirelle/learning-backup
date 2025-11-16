package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.homework.StudentSubmitHomework;

public interface StudentSubmitHomeworkMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(StudentSubmitHomework record);

  int insertSelective(StudentSubmitHomework record);

  StudentSubmitHomework selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(StudentSubmitHomework record);

  int updateByPrimaryKey(StudentSubmitHomework record);

  StudentSubmitHomework selectHomework(String student, int homework);

  int deleteByHomeworkId(int homeworkId);

  int updateRead(String student, int homeworkId, String review);

  int updateScore(String student, int homeworkId, String review, int score);

  int deleteByMap(String student, Integer homework);

}
