package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.course.CourseWithTC;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseWithTCMapper {
    List<CourseWithTC> selectCourse(int teacherId);
}
