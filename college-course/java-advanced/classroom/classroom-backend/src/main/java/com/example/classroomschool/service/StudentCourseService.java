package com.example.classroomschool.service;

import com.example.classroomschool.entity.course.StudentCourse;
import com.example.classroomschool.mapper.StudentCourseMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StudentCourseService {

  @Resource
  StudentCourseMapper studentCourseMapper;

  public int topTheCourse(int id, int courseId, String top) {
    return studentCourseMapper.changeTop(id, courseId, top);
  }

  public int joinCourse(StudentCourse studentCourse) {
    return studentCourseMapper.insert(studentCourse);
  }

  public int deleteCourse(int student, int course) {
    return studentCourseMapper.courseDelete(student, course);
  }

  public int deleteByCourseId(int course) {
    return studentCourseMapper.deleteByCourse(course);
  }

  public int pigeonholeCourse(int student, int course, String pigeonhole) {
    return studentCourseMapper.pigeonholeC(student, course, pigeonhole);
  }

  public StudentCourse findStudentCourse(int student, int course) {
    return studentCourseMapper.selectSc(student, course);
  }

  public List<StudentCourse> findByCourseId(int course) {
    return studentCourseMapper.selectAllSc(course);
  }

  public int sortNum(int course, int student, int displayNum) {
    return studentCourseMapper.sortNumber(course, student, displayNum);
  }

  public List<StudentCourse> findByStudent(int student) {
    return studentCourseMapper.listStudent(student);
  }

  public int pigeonholeByCourse(int course, String pigeonhole) {
    return studentCourseMapper.pigeonholeTheCourse(course, pigeonhole);
  }

  public List<StudentCourse> selectTop(int student) {
    return studentCourseMapper.selectByStudentTop(student);
  }

  public List<StudentCourse> selectPigeonhole(int student) {
    return studentCourseMapper.selectByStudentPigeonhole(student);
  }
}
