package com.example.classroomschool.service;

import com.example.classroomschool.entity.course.Course;
import com.example.classroomschool.mapper.CourseMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

  @Resource
  CourseMapper courseMapper;

  public Course findCourseByKey(String courseKey) {
    return courseMapper.selectCourse(courseKey);
  }

  public int addCourse(Course course) {
    return courseMapper.insert(course);
  }

  public int stopTheKey(int id, String stopKey) {
    return courseMapper.updateDeleting(id, stopKey);
  }

  public int resetTheKey(int id, String newKey) {
    return courseMapper.updateKey(id, newKey);
  }

  public int deleteById(int id) {
    return courseMapper.deleteByPrimaryKey(id);
  }

  public int alterCourse(int id, int master, String name, String classname, String year,
      String term) {
    return courseMapper.updateCourse(id, master, name, classname, year, term);
  }

  public Course findById(int courseId) {
    return courseMapper.selectByPrimaryKey(courseId);
  }

  public int update(int courseId, int teacherId, int id) {
    return courseMapper.updateTeacher(courseId, teacherId, id);
  }

}
