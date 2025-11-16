package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.course.Course;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(Course record);

  int insertSelective(Course record);

  Course selectByPrimaryKey(Integer id);

  Course selectCourse(String key);

  int updateByPrimaryKeySelective(Course record);

  int updateDeleting(int id, String stopKey);

  int updateKey(int id, String newKey);

  int updateByPrimaryKey(Course record);

  int updateCourse(int id, int master, String name, String classname, String year, String term);

  int updateTeacher(int courseId, int teacherId, int id);
}
