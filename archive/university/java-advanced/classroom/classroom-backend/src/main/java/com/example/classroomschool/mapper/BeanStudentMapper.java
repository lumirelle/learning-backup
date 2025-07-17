package com.example.classroomschool.mapper;

import com.example.classroomschool.entity.student.BeanStudent;
import org.springframework.stereotype.Repository;

@Repository
public interface BeanStudentMapper {

  BeanStudent selectStudentPlus(Integer student);
}
