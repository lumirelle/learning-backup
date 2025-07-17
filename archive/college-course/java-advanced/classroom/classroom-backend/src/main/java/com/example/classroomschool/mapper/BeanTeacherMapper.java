package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.teacher.BeanTeacher;
import org.springframework.stereotype.Repository;

@Repository
public interface BeanTeacherMapper {

  BeanTeacher selectTeacherPlus(int teacher, int courseId);
}
