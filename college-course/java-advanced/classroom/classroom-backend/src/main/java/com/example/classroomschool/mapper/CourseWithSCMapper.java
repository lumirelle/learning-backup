package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.course.CourseWithSC;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseWithSCMapper {

  List<CourseWithSC> selectCourse(int StudentId);
}
