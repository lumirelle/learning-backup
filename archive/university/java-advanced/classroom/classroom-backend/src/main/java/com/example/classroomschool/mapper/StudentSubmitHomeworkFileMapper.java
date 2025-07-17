package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.homework.StudentSubmitHomeworkFile;
import java.util.List;

public interface StudentSubmitHomeworkFileMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(StudentSubmitHomeworkFile record);

  int insertSelective(StudentSubmitHomeworkFile record);

  StudentSubmitHomeworkFile selectByPrimaryKey(Integer id);

  int updateByPrimaryKeySelective(StudentSubmitHomeworkFile record);

  int updateByPrimaryKey(StudentSubmitHomeworkFile record);

  List<StudentSubmitHomeworkFile> selectHomework(String studentId, int homework);

  int deleteByMap(String account, Integer id);

  int deleteAllFile(Integer homework);

  List<StudentSubmitHomeworkFile> selectHomeworkByHomeworkId(Integer homework);
}
