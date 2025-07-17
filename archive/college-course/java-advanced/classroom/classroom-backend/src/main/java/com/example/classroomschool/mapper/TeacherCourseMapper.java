package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.course.TeacherCourse;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherCourseMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(TeacherCourse record);

  int insertSelective(TeacherCourse record);

  TeacherCourse selectByPrimaryKey(Integer id);

  int courseDelete(int teacher, int course);

  TeacherCourse selectTc(int teacher, int course);

  int changeTop(int id, int CourseId, String top);

  int pigeonholeC(int teacher, int course, String pigeonhole);

  int sortNumber(int course, int teacher, int displayNum);

  int updateByPrimaryKeySelective(TeacherCourse record);

  int updateByPrimaryKey(TeacherCourse record);

  List<TeacherCourse> listTeacher(int teacher);

  List<TeacherCourse> selectTcByCourse(int course);

  List<TeacherCourse> selectStudentTc(Integer course);

  int allCourseDelete(int course);

}
