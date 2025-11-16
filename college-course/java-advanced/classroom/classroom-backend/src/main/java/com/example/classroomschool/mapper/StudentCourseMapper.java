package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.course.StudentCourse;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentCourseMapper {

  int deleteByPrimaryKey(Integer id);

  int insert(StudentCourse record);

  int changeTop(int id, int courseId, String top);

  int insertSelective(StudentCourse record);

  StudentCourse selectByPrimaryKey(Integer id);

  int deleteByCourse(Integer course);

  StudentCourse selectSc(int student, int course);

  int sortNumber(int course, int student, int displayNum);

  int courseDelete(int student, int course);

  int pigeonholeC(int student, int course, String pigeonhole);

  int updateByPrimaryKeySelective(StudentCourse record);

  int updateByPrimaryKey(StudentCourse record);

  List<StudentCourse> selectAllSc(int course);

  List<StudentCourse> listStudent(int student);

  int pigeonholeTheCourse(int course, String pigeonhole);

  List<StudentCourse> selectByStudentTop(int student);

  List<StudentCourse> selectByStudentPigeonhole(int student);
}
