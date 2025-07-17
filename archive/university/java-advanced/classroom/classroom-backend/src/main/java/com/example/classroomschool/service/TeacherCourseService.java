package com.example.classroomschool.service;

import com.example.classroomschool.entity.course.TeacherCourse;
import com.example.classroomschool.mapper.TeacherCourseMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TeacherCourseService {

  @Resource
  TeacherCourseMapper teacherCourseMapper;

  public int joinCourse(TeacherCourse teacherCourse) {
    return teacherCourseMapper.insert(teacherCourse);
  }


  public int deleteTC(int teacher, int course) {
    return teacherCourseMapper.courseDelete(teacher, course);
  }

  public int deleteByCourseId(int course) {
    return teacherCourseMapper.allCourseDelete(course);
  }

  public TeacherCourse findTC(int teacher, int course) {
    return teacherCourseMapper.selectTc(teacher, course);
  }

  public int topTheCourse(int id, int CourseId, String top) {
    return teacherCourseMapper.changeTop(id, CourseId, top);
  }

  public int pigeonholeCourse(int teacher, int course, String pigeonhole) {
    return teacherCourseMapper.pigeonholeC(teacher, course, pigeonhole);
  }

  public int sortNum(int course, int teacher, int displayNum) {
    return teacherCourseMapper.sortNumber(course, teacher, displayNum);
  }

  public List<TeacherCourse> findByTeacherId(int teacher) {
    return teacherCourseMapper.listTeacher(teacher);
  }

  public List<TeacherCourse> findByCourseId(int course) {
    return teacherCourseMapper.selectTcByCourse(course);
  }

  public int update(TeacherCourse teacherCourse) {
    return teacherCourseMapper.updateByPrimaryKey(teacherCourse);
  }

  public List<TeacherCourse> getNumber(Integer course) {
    return teacherCourseMapper.selectStudentTc(course);
  }

}
