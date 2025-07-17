package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.homework.Homework;
import java.util.List;

public interface HomeworkMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(Homework record);

  int insertSelective(Homework record);

  Homework selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(Homework record);

  int updateByPrimaryKey(Homework record);

  List<Homework> selectHomework(int courseId);

//    List<Homework> selectByCourse(int courseId);
}
